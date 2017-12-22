package jnachos.kern;

import jnachos.filesystem.OpenFile;
import jnachos.machine.Machine;

public class NewProcess implements VoidFunctionPtr {
	
	public void call(Object pArg) {
		System.out.println("*** " + JNachos.getCurrentProcess().getName() + " ***");
		
		JNachos.getCurrentProcess().restoreUserState();
		JNachos.getCurrentProcess().getSpace().restoreState();
		Machine.run();
		
		assert(false);
	}
	
	public NewProcess() {
	}
}
