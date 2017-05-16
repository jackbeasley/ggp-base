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

	@Override
	Node select() {
		// Maximize the
		double maxScore = 0;
		Node maxNode = null;
		for (Node curMaxNode : this.getChildren()) {
			double selectionScore = Node.selectMinfn(curMaxNode);
			if (selectionScore > maxScore) {
				maxScore = selectionScore;
				maxNode = curMaxNode;
			}
		}
		return maxNode;
	}

	@Override
	Node expand() throws TransitionDefinitionException, MoveDefinitionException {
		if(getStateMachine().isTerminal(getState())){
			this.getParent().incrementRemainingMovesIndex();
			return this.getParent();
		}

		MachineState simstate = getStateMachine().
				getNextState(getState(), getMoves().get(getRemainingMovesIndex()));
		Node newNode = new MaxNode(getStateMachine(), simstate, getRole(), this);
		this.addChild(newNode);
		this.incrementRemainingMovesIndex();

		if (this.getRemainingMovesIndex() > this.getMoves().size()-1){
			this.getParent().incrementRemainingMovesIndex();
		}

		return newNode;
	}

	@Override
	public String toString() {
		return "MinNode";
	}

}
