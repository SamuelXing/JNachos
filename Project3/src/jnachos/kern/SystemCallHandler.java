/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.kern;

import java.util.Iterator;
import java.util.LinkedList;

import jnachos.filesystem.OpenFile;
import jnachos.machine.*;

/** The class handles System calls made from user programs. */
public class SystemCallHandler {
	/** The System call index for halting. */
	public static final int SC_Halt = 0;

	/** The System call index for exiting a program. */
	public static final int SC_Exit = 1;

	/** The System call index for executing program. */
	public static final int SC_Exec = 2;

	/** The System call index for joining with a process. */
	public static final int SC_Join = 3;

	/** The System call index for creating a file. */
	public static final int SC_Create = 4;

	/** The System call index for opening a file. */
	public static final int SC_Open = 5;

	/** The System call index for reading a file. */
	public static final int SC_Read = 6;

	/** The System call index for writting a file. */
	public static final int SC_Write = 7;

	/** The System call index for closing a file. */
	public static final int SC_Close = 8;

	/** The System call index for forking a forking a new process. */
	public static final int SC_Fork = 9;

	/** The System call index for yielding a program. */
	public static final int SC_Yield = 10;

	/** The System call index for sending a message. */
	public static final int SC_SendMsg = 13;

	/** The System call index for waiting a message. */
	public static final int SC_WaitMsg = 14;

	/** The System call index for sending a answer. */
	public static final int SC_SendAnswer = 15;

	/** The System call index for waiting a answer. */
	public static final int SC_WaitAnswer = 16;

	/**
	 * Entry point into the Nachos kernel. Called when a user program is executing,
	 * and either does a syscall, or generates an addressing or arithmetic
	 * exception.
	 * 
	 * For system calls, the following is the calling convention:
	 * 
	 * system call code -- r2 arg1 -- r4 arg2 -- r5 arg3 -- r6 arg4 -- r7
	 * 
	 * The result of the system call, if any, must be put back into r2.
	 * 
	 * And don't forget to increment the pc before returning. (Or else you'll loop
	 * making the same system call forever!
	 * 
	 * @pWhich is the kind of exception. The list of possible exceptions are in
	 *         Machine.java
	 **/
	public static void handleSystemCall(int pWhichSysCall) {

		Debug.print('a', "!!!!" + Machine.read1 + "," + Machine.read2 + "," + Machine.read4 + "," + Machine.write1 + ","
				+ Machine.write2 + "," + Machine.write4);

		switch (pWhichSysCall) {
		// If halt is received shut down
		case SC_Halt:
			Debug.print('a', "Shutdown, initiated by user program.");
			Interrupt.halt();
			break;

		case SC_Exit: {
			System.out.println("\nProcess" + JNachos.getCurrentProcess().getPid() + "_"
					+ JNachos.getCurrentProcess().getName() + " Enter System Call SC_Exit");
			// PCReg + 4
			Machine.writeRegister(Machine.PCReg, Machine.readRegister(Machine.PCReg) + 4);

			// Read in any arguments from the 4th register
			int arg = Machine.readRegister(4);

			System.out
					.println("Current Process " + JNachos.getCurrentProcess().getName() + " exiting with code " + arg);

			// check register for whether there is a waiting process
			int waited_epid = JNachos.getCurrentProcess().getPid();

			// whether the current process has waiting process
			if (JNachos.mWaitingList.containsKey(waited_epid)) {
				int waiting_epid = (int) JNachos.mWaitingList.get(waited_epid);

				// Remove the pair<waited_pid, waiting_pid> form waiting list
				JNachos.mWaitingList.remove(waited_epid);

				// save the input of the EXIT system call
				NachosProcess waiting_eprocess = JNachos.mProcesses.get(waiting_epid);
				waiting_eprocess.saveUserRegister(2, arg);

				// recover the waiting process
				System.out.println("Recover the waiting Process" + waiting_epid + " from waited Process" + waited_epid);
				Scheduler.readyToRun(waiting_eprocess);
			}

			// Remove the current process form running process table
			JNachos.mProcesses.remove(waited_epid);

			// check isEmpty for the buffer queue for the process
			LinkedList<BufferPool.Buffer> queueExit = JNachos.getCurrentProcess().getBufQueue();
			while (!queueExit.isEmpty()) {
				BufferPool.Buffer bufExit = queueExit.remove();
				if (bufExit.getType() == 0)
					JNachos.sendMsg(bufExit.getReceiver(), bufExit);
				else {
					// return buffer to buffer pool
					System.out.println("return buffer" + bufExit.getId());
					JNachos.mBufferPool.returnBufPool(bufExit.getId());
				}
			}

			// check whether finish handling the current processing buffer
			LinkedList<BufferPool.Buffer> temBufs = JNachos.getCurrentProcess().getCurBuf();
			while (!temBufs.isEmpty()) {
				BufferPool.Buffer temBuf = temBufs.remove();
				JNachos.sendMsg(temBuf.getReceiver(), temBuf);
			}

			// Finish the invoking process
			JNachos.getCurrentProcess().finish();
			break;
		}

		case SC_Fork: {
			System.out.println("\nProcess" + JNachos.getCurrentProcess().getPid() + "_"
					+ JNachos.getCurrentProcess().getName() + " Enter System Call SC_Fork");

			// Turn off interrupts
			boolean oldLevel_fork = Interrupt.setLevel(false);

			// PCReg + 4
			Machine.writeRegister(Machine.PCReg, Machine.readRegister(Machine.PCReg) + 4);

			// create a new child process and set and store the return value for parent
			// process
			NachosProcess child = new NachosProcess("c_" + JNachos.getCurrentProcess().getName());
			Machine.writeRegister(2, 0);

			// save current state of parent process
			AddrSpace space_fork = new AddrSpace(JNachos.getCurrentProcess().getSpace());
			child.setSpace(space_fork);
			child.saveUserState();

			// set and store the return value for child process
			Machine.writeRegister(2, child.getPid());

			// put the child process into the ready-list
			child.fork(new NewProcess(), child);
			System.out.println("Process" + JNachos.getCurrentProcess().getPid() + " forked Process" + child.getPid());

			// Return interrupts to their pre-call level
			Interrupt.setLevel(oldLevel_fork);
			break;
		}

		case SC_Join: {
			System.out.println("\nProcess" + JNachos.getCurrentProcess().getPid() + "_"
					+ JNachos.getCurrentProcess().getName() + " Enter System Call SC_Join");

			// Turn off interrupts
			boolean oldLevel_join = Interrupt.setLevel(false);

			// PCReg + 4
			Machine.writeRegister(Machine.PCReg, Machine.readRegister(Machine.PCReg) + 4);

			// get the pid called process
			int waited_jpid = Machine.readRegister(4);
			int waiting_jpid = JNachos.getCurrentProcess().getPid();

			// the called process should exist, not be the current process, run
			if (waited_jpid == 0 || waited_jpid == waiting_jpid || !JNachos.mProcesses.containsKey(waited_jpid))
				break;

			// put calling process's pid into the Waiting List
			System.out.println("Process" + JNachos.getCurrentProcess().getPid() + " waits for Process" + waited_jpid);
			JNachos.mWaitingList.put(waited_jpid, waiting_jpid);

			// sleep the calling process
			JNachos.getCurrentProcess().sleep();

			// Return interrupts to their pre-call level
			Interrupt.setLevel(oldLevel_join);
			break;
		}

		case SC_Exec: {
			System.out.println("\nProcess" + JNachos.getCurrentProcess().getPid() + "_"
					+ JNachos.getCurrentProcess().getName() + " Enter System Call SC_Exec");

			// PCReg + 4
			Machine.writeRegister(Machine.PCReg, Machine.readRegister(Machine.PCReg) + 4);

			// Turn off interrupts
			boolean oldLevel_exec = Interrupt.setLevel(false);

			// read address of string path of the executable file
			int addr = Machine.readRegister(4);
			// look for the string path from the simulated memory
			String filename = new String();
			int word = Machine.readMem(addr, 1);
			while ((char) word != '\0') {
				filename += (char) word;
				addr++;
				word = Machine.readMem(addr, 1);
			}
			System.out
					.println("Process" + JNachos.getCurrentProcess().getPid() + " executing file \"" + filename + "\"");

			// The executable file to run
			OpenFile executable = JNachos.mFileSystem.open(filename);

			// If the file does not exist
			if (executable == null) {
				Debug.print('t', "Unable to open file " + filename);
				System.out.println("Can not open file " + filename);
				break;
			}

			// Load the file into the memory space
			AddrSpace space = new AddrSpace(executable);
			JNachos.getCurrentProcess().setSpace(space);

			// set the initial register values
			space.initRegisters();

			// load page table register
			space.restoreState();

			// jump to the user progam
			// machine->Run never returns;
			Machine.run();

			// Return interrupts to their pre-call level
			Interrupt.setLevel(oldLevel_exec);
			break;
		}

		// send(receiver, message)
		case SC_SendMsg: {
			System.out.println("\nProcess" + JNachos.getCurrentProcess().getPid() + "_"
					+ JNachos.getCurrentProcess().getName() + " Enter System Call SC_SendMsg");

			// Turn off interrupts
			boolean oldLevel_SM = Interrupt.setLevel(false);

			// PCReg + 4
			Machine.writeRegister(Machine.PCReg, Machine.readRegister(Machine.PCReg) + 4);

			// get the receiver name
			int addrSM = Machine.readRegister(4);
			String rcvrName = new String();
			int wordSM = Machine.readMem(addrSM, 1);
			while ((char) wordSM != '\0') {
				rcvrName += (char) wordSM;
				addrSM++;
				wordSM = Machine.readMem(addrSM, 1);
			}
			System.out.println("Read first argument: " + rcvrName);

			// get the message
			addrSM = Machine.readRegister(5);
			String msg = new String();
			wordSM = Machine.readMem(addrSM, 1);
			while ((char) wordSM != '\0') {
				msg += (char) wordSM;
				addrSM++;
				wordSM = Machine.readMem(addrSM, 1);
			}
			System.out.println("Read second argument: " + msg);

			// get free buffer
			BufferPool.Buffer bufSM = JNachos.mBufferPool.getFreeBuffer();
			JNachos.getCurrentProcess().getCurBuf().add(bufSM);
			bufSM.setType(0);
			bufSM.setSender(JNachos.getCurrentProcess().getName());
			bufSM.setReceiver(rcvrName);
			bufSM.setData(msg);

			// send message buffer
			JNachos.sendMsg(rcvrName, bufSM);

			// return buffer id
			Machine.writeRegister(2, bufSM.getId());

			// Return interrupts to their pre-call level
			Interrupt.setLevel(oldLevel_SM);
			break;
		}

		// waitMsg(sender, message)
		case SC_WaitMsg: {
			System.out.println("\nProcess" + JNachos.getCurrentProcess().getPid() + "_"
					+ JNachos.getCurrentProcess().getName() + " Enter System Call SC_WaitMsg");

			// Turn off interrupts
			boolean oldLevel_WM = Interrupt.setLevel(false);

			// PCReg + 4
			Machine.writeRegister(Machine.PCReg, Machine.readRegister(Machine.PCReg) + 4);

			// check the limitation of the number of buffers
			if (JNachos.getCurrentProcess().getCurBuf().size() >= BufferPool.maxNumOfBuf) {
				while (!JNachos.getCurrentProcess().getBufQueue().isEmpty()) {
					BufferPool.Buffer temBuf = JNachos.getCurrentProcess().getBufQueue().remove();
					JNachos.mBufferPool.returnBufPool(temBuf.getId());
					System.out.println("Exceed maximum, return buffer" + temBuf.getId());
				}

				Machine.writeRegister(2, -1);
				break;
			}

			// get the valid sender
			int addrWM = Machine.readRegister(4);
			String sndrWM = new String();
			int wordWM = Machine.readMem(addrWM, 1);
			while ((char) wordWM != '\0') {
				sndrWM += (char) wordWM;
				addrWM++;
				wordWM = Machine.readMem(addrWM, 1);
			}
			System.out.println("Read first argument: " + sndrWM);

			// variable to ensure whether get the expected buffer
			int isPassed = 0;

			// check whether get the expected buffer
			NachosProcess curProcessWM = JNachos.getCurrentProcess();
			while (isPassed == 0) {

				// new buffer in queue?
				while (curProcessWM.getBufQueue().isEmpty()) {
					try {
						curProcessWM.yield();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				assert (!curProcessWM.getBufQueue().isEmpty());

				// get the buffer
				BufferPool.Buffer bufWM = curProcessWM.getBufQueue().remove();
				curProcessWM.getCurBuf().add(bufWM);
				System.out.println(curProcessWM.getName() + " receive message buffer" + bufWM.getId() + " from "
						+ bufWM.getSender() + " with data \"" + bufWM.getData() + "\"");

				// validate identity of sender
				if (!bufWM.getSender().equals(sndrWM)) {
					System.out.println("Message buffer from malicious Sender: " + bufWM.getSender());
					bufWM.setReceiver(bufWM.getSender());
					bufWM.setSender("Nachos_Kernel");
					bufWM.setData("Nachos_Kernel: Wrong Receiver");
					JNachos.sendAnswer(bufWM.getReceiver(), bufWM);
					continue;
				}
				System.out.println("Validation passed");

				// copy message to second argument
				addrWM = Machine.readRegister(5);
				String messageWM = bufWM.getData();
				for (int i = 0; i < messageWM.length(); i++) {
					Machine.writeMem(addrWM, 1, messageWM.charAt(i));
					addrWM++;
				}
				Machine.writeMem(addrWM, 1, '\0');

				// return buffer id
				Machine.writeRegister(2, bufWM.getId());

				// set the passed status
				isPassed = 1;
			}

			// Return interrupts to their pre-call level
			Interrupt.setLevel(oldLevel_WM);
			break;
		}

		// result = sendAnswer(answer, buffer)
		case SC_SendAnswer: {
			System.out.println("\nProcess" + JNachos.getCurrentProcess().getPid() + "_"
					+ JNachos.getCurrentProcess().getName() + " Enter System Call SC_SendAnswer");

			// Turn off interrupts
			boolean oldLevel_SA = Interrupt.setLevel(false);

			// PCReg + 4
			Machine.writeRegister(Machine.PCReg, Machine.readRegister(Machine.PCReg) + 4);

			// get buffer id from register
			int bid = Machine.readRegister(5);
			// check the validation of the buffer id
			if (bid == -1) {
				Machine.writeRegister(2, -1);
				break;
			}

			// get buffer referring to its id
			BufferPool.Buffer bufSA = JNachos.mBufferPool.getBuffer(bid);

			// get the answer
			int addrSA = Machine.readRegister(4);
			String answerSA = new String();
			int wordSA = Machine.readMem(addrSA, 1);
			while ((char) wordSA != '\0') {
				answerSA += (char) wordSA;
				addrSA++;
				wordSA = Machine.readMem(addrSA, 1);
			}
			System.out.println("Read first argument: " + answerSA);

			// reset the buffer
			bufSA.setType(1);
			bufSA.setReceiver(bufSA.getSender());
			bufSA.setSender(JNachos.getCurrentProcess().getName());
			bufSA.setData(answerSA);

			// sender answer to originer sender
			JNachos.sendAnswer(bufSA.getReceiver(), bufSA);

			// Return interrupts to their pre-call level
			Interrupt.setLevel(oldLevel_SA);
			break;
		}

		// waitAnswer(answer, buffer)
		case SC_WaitAnswer: {
			System.out.println("\nProcess" + JNachos.getCurrentProcess().getPid() + "_"
					+ JNachos.getCurrentProcess().getName() + " Enter System Call SC_WaitAnswer");

			// Turn off interrupts
			boolean oldLevel_WA = Interrupt.setLevel(false);

			// PCReg + 4
			Machine.writeRegister(Machine.PCReg, Machine.readRegister(Machine.PCReg) + 4);

			// get target buffer id
			int addrWA = Machine.readRegister(5);
			// check the buffer id
			if (JNachos.mBufferPool.hasBuffer(addrWA)) {
				System.out.println("Exceed maximum number of buffers");
				JNachos.getCurrentProcess().getCurBuf().remove(JNachos.mBufferPool.getBuffer(addrWA));
				Machine.writeRegister(2, 0);
				break;
			}
			
			// variable to ensure whether get the target buffer
			int isPassed = 0;

			// check the buffer queue
			NachosProcess curProcessWA = JNachos.getCurrentProcess();
			while (isPassed == 0) {
				while (curProcessWA.getBufQueue().isEmpty()) {
					try {
						curProcessWA.yield();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				assert (!curProcessWA.getBufQueue().isEmpty());

				// get the buffer
				BufferPool.Buffer bufWA = curProcessWA.getBufQueue().remove();
				System.out.println(curProcessWA.getName() + ": receive answer buffer" + bufWA.getId() + " from " + bufWA.getSender()
						+ " with data \"" + bufWA.getData() + "\"");

				// check the buffer
				if (bufWA.getId() != addrWA) {
					// return buffer to buffer pool
					System.out.println(curProcessWA.getName() + ": wrong bufer, return buffer" + bufWA.getId());
					JNachos.mBufferPool.returnBufPool(bufWA.getId());
					continue;
				}

				// copy message to second argument
				addrWA = Machine.readRegister(4);
				String answerWA = bufWA.getData();
				for (int i = 0; i < answerWA.length(); i++) {
					Machine.writeMem(addrWA, 1, answerWA.charAt(i));
					addrWA++;
				}
				Machine.writeMem(addrWA, 1, '\0');

				// whether the message comes from JNachos
				if (bufWA.getSender().equals("Nachos_Kernel"))
					Machine.writeRegister(2, 0);
				else
					Machine.writeRegister(2, 1);

				// dequeue handled buffer from buffer cache
				curProcessWA.getCurBuf().remove();
				
				// return buffer to Buffer Pool
				JNachos.mBufferPool.returnBufPool(bufWA.getId());
				isPassed = 1;
			}

			// Return interrupts to their pre-call level
			Interrupt.setLevel(oldLevel_WA);
			break;
		}

		default:
			Interrupt.halt();
			break;
		}
	}
}
