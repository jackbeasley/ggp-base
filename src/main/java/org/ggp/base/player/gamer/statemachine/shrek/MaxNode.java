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

	@Override
	Node select() throws MoveDefinitionException, TransitionDefinitionException {
		if (this.getRemainingMovesIndex() < this.getMoves().size()) {
			return this.expand();
		} else {
			double maxScore = 0;
			Node selectedMinNode = null;
			for(Node minNode : this.getChildren()){

				// Use negative value for selection b/c min nodes
				double selectionScore = selectMaxfn(minNode);
				if(selectionScore > maxScore){
					maxScore = selectionScore;
					selectedMinNode = minNode;
				}
			}
			return selectedMinNode;
		}
	}

	@Override
	Node expand() throws MoveDefinitionException, TransitionDefinitionException {
		// to create min child
		if (this.getChildren().size() < this.getMoves().size()
				&& this.getRemainingMovesIndex() == this.getChildren().size()) {

			MachineState simstate = this.getStateMachine().getNextState(getState(),
					this.getMoves().get(this.getRemainingMovesIndex()));

			Node newNode = new MinNode(this.getStateMachine(), simstate, getRole(), this);
			this.addChild(newNode);
		}

		//
		Node minNode = getChildren().get(getRemainingMovesIndex());
		Node maxNode = minNode.expand();
		return maxNode;
	}

	@Override
	public String toString() {
		return "MaxNode";
	}

}
