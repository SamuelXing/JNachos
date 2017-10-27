/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *
 *  Created by Patrick McSweeney on 12/13/08.
 *
 */
package jnachos.kern;

import jnachos.machine.*;
import java.util.Arrays;

/**
 * The ExceptionHanlder class handles all exceptions raised by the simulated
 * machine. This class is abstract and should not be instantiated.
 */
public abstract class ExceptionHandler {

	/**
	 * This class does all of the work for handling exceptions raised by the
	 * simulated machine. This is the only funciton in this class.
	 *
	 * @param pException
	 *            The type of exception that was raised.
	 */
	public static void handleException(ExceptionType pException) {
		switch (pException) {
		// If this type was a system call
		case SyscallException:

			// Get what type of system call was made
			int type = Machine.readRegister(2);

			// Invoke the System call handler
			SystemCallHandler.handleSystemCall(type);
			break;

// LRU implementation
		case PageFaultException:
			Statistics.numPageFaults++ ;
			int virtAddr = Machine.readRegister(Machine.BadVAddrReg);
			int vpn;
			// calculate the virtual page number, and offset within the page,
			// from the virtual address
			vpn = (int) virtAddr / Machine.PageSize;
			byte[] bytes = new byte[Machine.PageSize];
			JNachos.swap.readAt(bytes, Machine.PageSize, MMU.mPageTable[vpn].swapPage*Machine.PageSize);
			int freeBit = JNachos.getCurrentProcess().getSpace().mFreeMap.find();
			if(freeBit != -1 && JNachos.getCurrentProcess().getSpace().pointer < 16){
				MMU.mPageTable[vpn].physicalPage = freeBit;
				System.arraycopy(bytes, 0, Machine.mMainMemory, MMU.mPageTable[vpn].physicalPage * Machine.PageSize,
						Machine.PageSize);
				MMU.mPageTable[vpn].valid = true;
				MMU.mPageTable[vpn].use = true;
				JNachos.getCurrentProcess().getSpace().LRU_BUFFER[JNachos.getCurrentProcess().getSpace().pointer++] =
						MMU.mPageTable[vpn];
				Debug.print('a', "Pointer:" + JNachos.getCurrentProcess().getSpace().pointer);

				if (JNachos.getCurrentProcess().getSpace().pointer == Machine.NumPhysPages) {
					JNachos.getCurrentProcess().getSpace().pointer = 0;
				}
			}
			else{
				int min_val = Integer.MAX_VALUE;
				for(int j =0; j< Machine.NumPhysPages; j++){
					if(JNachos.getCurrentProcess().getSpace().LRU_Stack.contains(
							JNachos.getCurrentProcess().getSpace().LRU_BUFFER[j]))
					{
						int index = JNachos.getCurrentProcess().getSpace().LRU_Stack.indexOf(
								JNachos.getCurrentProcess().getSpace().LRU_BUFFER[j]);
						if(index < min_val){
							min_val = index;
							JNachos.getCurrentProcess().getSpace().pointer = j;
						}
					}
				}

				TranslationEntry OutPageInfo = JNachos.getCurrentProcess().getSpace().LRU_BUFFER[JNachos.getCurrentProcess().getSpace().pointer];
				int Position = OutPageInfo.physicalPage;
				OutPageInfo.physicalPage = -1;
				OutPageInfo.valid = false;
				OutPageInfo.use = false;
				// if page is dirty, then write back to swap space
				if (OutPageInfo.dirty) {
					byte[] copybytes = new byte[Machine.PageSize];
					System.arraycopy(Machine.mMainMemory, Position * Machine.PageSize, copybytes, 0, Machine.PageSize);
					JNachos.swap.writeAt(copybytes, Machine.PageSize, OutPageInfo.swapPage * Machine.PageSize);
				}
				// zero out needed space
				Arrays.fill(Machine.mMainMemory, Position * Machine.PageSize,
						(Position + 1) * Machine.PageSize, (byte) 0);
				// write page to victim's position
				MMU.mPageTable[vpn].physicalPage = JNachos.getCurrentProcess().getSpace().pointer;
				MMU.mPageTable[vpn].valid = true;
				MMU.mPageTable[vpn].use = true;
				// copy the swap content to main memory
				System.arraycopy(bytes, 0, Machine.mMainMemory, MMU.mPageTable[vpn].physicalPage * Machine.PageSize,
						Machine.PageSize);
				// changing the mapping relation
				JNachos.getCurrentProcess().getSpace().LRU_BUFFER[JNachos.getCurrentProcess().getSpace().pointer] = MMU.mPageTable[vpn];
			}
			break;

		// All other exceptions shut down for now
		default:
			System.out.println("HERE BREAK");
			System.out.println(pException);
			System.exit(0);
		}
	}
}
