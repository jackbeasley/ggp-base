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
	public MaxNode expandMinNode() throws TransitionDefinitionException, MoveDefinitionException {
		if (this.getRemainingMovesIndex() > this.getMoves().size()-1){
			this.getParent().incrementRemainingMovesIndex();
		}
		// Sim and add node to the tree for the current index
		MachineState simstate = getStateMachine().
				getNextState(getState(), getMoves().get(getRemainingMovesIndex()));
		MaxNode newNode = new MaxNode(getStateMachine(), simstate, getRole(), this);
		this.addChild(newNode);
		this.incrementRemainingMovesIndex();
		return newNode;

	}

}
