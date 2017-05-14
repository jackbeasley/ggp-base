package org.ggp.base.player.gamer.statemachine.shrek;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;

public class MaxNode extends Node {

	public MaxNode(StateMachine machine, MachineState state, Role role) throws MoveDefinitionException {
		super(machine, state, role);
		// TODO Auto-generated constructor stub
	}

	public MaxNode(StateMachine machine, MachineState state, Role role, Node parent) throws MoveDefinitionException {
		super(machine, state, role, parent);
		// TODO Auto-generated constructor stub
	}
//
//	@Override
//	public MaxNode select() throws TransitionDefinitionException, MoveDefinitionException{
//		if(this.getRemainingMovesIndex() <= this.getMoves().size()-1){
//			return this.expandMaxNode();
//		} else{
//
//			Node selectedMin = selectMinChild();
//			if(selectedMin == null){
//				return this;
//			}
//
//			MaxNode maxNode = (MaxNode) selectMaxChild(selectedMin);
//			if(maxNode == null){
//				return this;
//			}
//
//			return maxNode.select();
//		}
//	}
//
//	private Node selectMinChild(){
//		double maxScore = 0;
//		Node selectedMinNode = null;
//		for(Node minNode : this.getChildren()){
//
//			double selectionScore = selectMinfn(minNode);
//
//			if(selectionScore > maxScore){
//				maxScore = selectionScore;
//				selectedMinNode = (MaxNode) minNode;
//			}
//		}
//		return selectedMinNode;
//	}
//
//	private Node selectMaxChild(Node selectedMin){
//		double maxScore = 0;
//		MaxNode maxNode = null;
//		for(Node curMaxNode : selectedMin.getChildren()){
//			double selectionScore = selectMaxfn(curMaxNode);
//			if(selectionScore > maxScore){
//				maxScore = selectionScore;
//				maxNode = (MaxNode) curMaxNode;
//			}
//		}
//		return maxNode;
//	}
//
//
//	private double selectMinfn(Node node){
//		System.out.println(node.getNumVisits());
//		return node.getUtility()/node.getNumVisits() + 50*Math.sqrt(Math.log(node.getParent().getNumVisits())/node.getNumVisits());
//	}
//
//	private double selectMaxfn(Node node){
//		return (-1*node.getUtility()/node.getNumVisits()) + 50*Math.sqrt(Math.log(node.getParent().getNumVisits())/node.getNumVisits());
//	}
//

//	@Override
//	public MaxNode expandMaxNode() throws TransitionDefinitionException, MoveDefinitionException {
//		if(this.getChildren().size() < this.getMoves().size() && this.getRemainingMovesIndex() == this.getChildren().size()){
//			MachineState simstate = getStateMachine().
//					getNextState(getState(), getMoves().get(getRemainingMovesIndex()));
//			MaxNode newNode = new MaxNode(getStateMachine(), simstate, getRole(), this);
//			this.addChild(newNode);
//		}
//		MaxNode minNode = (MaxNode) getChildren().get(getRemainingMovesIndex());
//		MaxNode node = minNode.expandMinNode();
//		return node;
//	}
//
//	@Override
//	public MaxNode expandMinNode() throws TransitionDefinitionException, MoveDefinitionException {
//		// Sim and add node to the tree for the current index
//
//		if(getStateMachine().isTerminal(getState())){
//			this.getParent().incrementRemainingMovesIndex();
//			return (MaxNode) this.getParent();
//		}
//
//		MachineState simstate = getStateMachine().
//				getNextState(getState(), getMoves().get(getRemainingMovesIndex()));
//		MaxNode newNode = new MaxNode(getStateMachine(), simstate, getRole(), this);
//		this.addChild(newNode);
//		this.incrementRemainingMovesIndex();
//
//		if (this.getRemainingMovesIndex() > this.getMoves().size()-1){
//			this.getParent().incrementRemainingMovesIndex();
//		}
//
//		return newNode;
//
//	}

}
