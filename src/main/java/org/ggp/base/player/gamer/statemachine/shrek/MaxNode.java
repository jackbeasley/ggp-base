package org.ggp.base.player.gamer.statemachine.shrek;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class MaxNode extends Node {

	public MaxNode(StateMachine machine, MachineState state, Role role) throws MoveDefinitionException {
		super(machine, state, role);
		// TODO Auto-generated constructor stub
	}

	public MaxNode(StateMachine machine, MachineState state, Role role, Node parent) throws MoveDefinitionException {
		super(machine, state, role, parent);
		// TODO Auto-generated constructor stub
	}

	// Returns true if node fully built out
	public boolean processNode() throws TransitionDefinitionException, MoveDefinitionException {
		if (getMoves().size() == 0) {
			// Nothing to see here
			if(getParent() != null){
				this.getParent().incrementRemainingMovesIndex();
			}

			return true;
		}


		if (getRemainingMovesIndex() > getChildren().size() - 1) {
			// Sim and add node to the tree for the current index
			MachineState simstate = getStateMachine().
					getNextState(getState(), getMoves().get(getRemainingMovesIndex()));

			this.addChild(new MinNode(getStateMachine(), simstate, getRole(), this));
			return false;
		} else {
			// Run process node on the child at the given index
			MinNode child = (MinNode) getChildren().get(getRemainingMovesIndex());
			if(child.processMove()) {
				// Child done, next time don't call it
				incrementRemainingMovesIndex();
			}
			return false;
		}

	}


}
