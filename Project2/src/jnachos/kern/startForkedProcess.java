package jnachos.kern;

import jnachos.machine.Machine;

/**
 * Created by samuel on 9/23/17.
 */
public class startForkedProcess implements VoidFunctionPtr{

    public void call(Object arg) {
        // write the process's register info to the simulated machine's register
        JNachos.getCurrentProcess().restoreUserState();
        // restore the process's address space to the machine's address sapce
        JNachos.getCurrentProcess().getSpace().restoreState();

        // make the machine run this process.
        Machine.run();
    }
}
