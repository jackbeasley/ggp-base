package org.ggp.base.player.gamer.statemachine.shrek;

import java.util.concurrent.Callable;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;

public class DepthCharge implements Callable<Integer> {

	StateMachine machine;
	Role role;
	MachineState state;

	public DepthCharge(StateMachine machine, Role role, MachineState state){
		this.machine = machine;
		this.role = role;
		this.state = state;
	}

	@Override
	public Integer call() throws Exception {

		// preformDepthCharge requires an empty integer array (depth) so it can
		// set the 0th element to
		// the number of state changes made to reach the terminal state

		int[] depth = new int[1];
		return machine.findReward(role, machine.performDepthCharge(state, depth));
	}

}
