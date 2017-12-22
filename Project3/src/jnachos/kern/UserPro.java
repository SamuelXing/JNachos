package jnachos.kern;

import jnachos.filesystem.OpenFile;
import jnachos.machine.Machine;

public class UserPro implements VoidFunctionPtr {
	
	public void call(Object pArg) {
		System.out.println("\n*** " + JNachos.getCurrentProcess().getName() + " ***");
		System.out.println("Run user program \""+ (String)pArg +"\"");
		
		String filename = (String)pArg;
		JNachos.startProcess(filename);
	}
	
	public UserPro(String strFiles) {
		Debug.print('t', "Entering User Program");
		
		String[] files = strFiles.split(",");
		for (int i = 0; i < files.length; i++) {
			int temIndex = files[i].lastIndexOf('\\') + 1;
			String fileName = files[i].substring(temIndex);
			//System.out.println("File Name: " + fileName);
			
			NachosProcess p = new NachosProcess(fileName);
			//System.out.println("Executable file: " + files[i]);
			p.fork(this, new String(files[i]));
		}
	}
}
