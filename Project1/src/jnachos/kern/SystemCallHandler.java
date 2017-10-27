/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.kern;

import jnachos.filesystem.OpenFile;
import jnachos.machine.*;

import java.util.ArrayList;
import java.util.List;

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

	/**
	 * Entry point into the Nachos kernel. Called when a user program is
	 * executing, and either does a syscall, or generates an addressing or
	 * arithmetic exception.
	 * 
	 * For system calls, the following is the calling convention:
	 * 
	 * system call code -- r2 arg1 -- r4 arg2 -- r5 arg3 -- r6 arg4 -- r7
	 * 
	 * The result of the system call, if any, must be put back into r2.
	 * 
	 * And don't forget to increment the pc before returning. (Or else you'll
	 * loop making the same system call forever!
	 * 
	 * @pWhich is the kind of exception. The list of possible exceptions are in
	 *         Machine.java
	 **/
	public static void handleSystemCall(int pWhichSysCall) {

		System.out.println("SysCall:" + pWhichSysCall);

		switch (pWhichSysCall) {
		// If halt is received shut down
		case SC_Halt:
			Debug.print('a', "Shutdown, initiated by user program.");
			Interrupt.halt();
			break;

		case SC_Exit:
			System.out.println("Entering Exit");
			boolean oldlevel = Interrupt.setLevel(false);
			// Read in any arguments from the 4th register
			int arg = Machine.readRegister(4);
			// increasing program counter
			Machine.NextProgram();

			System.out.println("Current Process " + JNachos.getCurrentProcess().getName() + " exiting with code " + arg);

			// check if any other Process waiting for current process to finish
			if(JNachos.getBlockTable().containsKey(JNachos.getCurrentProcess().getPID())){
				Integer blockPID = JNachos.getBlockTable().get(JNachos.getCurrentProcess().getPID());
				// save User register
				JNachos.getProcessTable().get(blockPID).saveUserRegister(2, arg);
				// make the blocked process ready to run
				Scheduler.readyToRun(JNachos.getProcessTable().get(blockPID));
				// remove blocked relationship
				JNachos.getBlockTable().remove(JNachos.getCurrentProcess().getPID());
				// make current process finish

			}
			JNachos.getCurrentProcess().finish();
			JNachos.getProcessTable().remove(JNachos.getCurrentProcess().getPID());
			Interrupt.setLevel(oldlevel);
			break;

		case SC_Fork:
			System.out.println("Entering Fork");
			// block Interrupt
			boolean oldLevel = Interrupt.setLevel(false);
			// increase program counter
			Machine.NextProgram();

			// return once,
			Machine.writeRegister(2, 0);
			System.out.println("Current Process PID: "+JNachos.getCurrentProcess().getPID());
			NachosProcess newProcess = new NachosProcess(JNachos.getCurrentProcess().getName()+"\'s child");
			newProcess.setSpace(new AddrSpace(JNachos.getCurrentProcess().getSpace()));

			newProcess.saveUserState();

			// return twice
			Machine.writeRegister(2, newProcess.getPID());
			newProcess.fork(new startForkedProcess(), newProcess);
			Interrupt.setLevel(oldLevel);
			break;

		case SC_Exec:
			// block interrupt
			System.out.println("Entering Exec");
			boolean old = Interrupt.setLevel(false);
			// increase program counter
			Machine.NextProgram();

			// Read in any arguments from the 4th register
			int argAddr = Machine.readRegister(4);

			// get filename
			int argValue = 1;
			List<Integer> name = new ArrayList<>();
			String executable_name = new String();
			while ((char)argValue != '\0'){
				argValue = Machine.readMem(argAddr, 1);
				if((char)argValue != '\0')
					executable_name += (char) argValue;
				argAddr++;

				name.add(argValue);
			}
			System.out.println("Program " + executable_name + " is running on Process " + JNachos.getCurrentProcess().getPID()+ ".....");
			JNachos.startProcess(executable_name);
			Interrupt.setLevel(old);
			break;

		case SC_Join:
			System.out.println("Enter Join");
			// block Interrupt
			boolean oldL = Interrupt.setLevel(false);
			// increasing program counter
			Machine.NextProgram();

			// Ensure that the specific process exists
			int param = Machine.readRegister(4);
			// check if waiting process ID equals current Process ID or not
			if(JNachos.getCurrentProcess().getPID() == param) {
				System.out.println("CANNOT WAITING ITSELF");
				break;
			}
			// check if process table have that process
			else if(!JNachos.getProcessTable().containsKey(param)){
				System.out.println("PROCESS DOES NOT EXIST");
				break;
			}
			else if(param == 0){
				System.out.println("SYS_ERROR");
				break;
			}

			else {
				System.out.println("Process " + JNachos.getCurrentProcess().getPID() +
						" is waiting for " + param +" to finish");
				JNachos.getBlockTable().put(param, JNachos.getCurrentProcess().getPID());
				JNachos.getCurrentProcess().sleep();
			}

			Interrupt.setLevel(oldL);
			break;


		default:
			Interrupt.halt();
			break;
		}
	}
}
