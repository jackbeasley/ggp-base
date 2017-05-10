/**
 *
 */
package org.ggp.base.player.gamer.statemachine.shrek;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class MinNode extends Node {

	public MinNode(StateMachine machine, MachineState state, Role role, Node parent) throws MoveDefinitionException {
		super(machine, state, role, parent);
	}

	// Returns true if node fully built out
	public boolean processMove() throws TransitionDefinitionException, MoveDefinitionException {

		if (getMoves().size() == 0) {
			// Nothing to see here
			this.getParent().incrementRemainingMovesIndex();
			return true;
		}
		// Sim and add node to the tree for the current index
		MachineState simstate = getStateMachine().
				getNextState(getState(), getMoves().get(getRemainingMovesIndex()));

		this.addChild(new MaxNode(getStateMachine(), simstate, getRole(), this));

		// Don't search the move made this time again
		this.incrementRemainingMovesIndex();

		// If there are no more moves to make, let the parent know not to continue searching here
		if (this.getRemainingMovesIndex() >= getMoves().size() - 1) {
			return true;
		} else {
			return false;
		}


	}

}
