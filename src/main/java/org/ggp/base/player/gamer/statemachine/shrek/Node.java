package org.ggp.base.player.gamer.statemachine.shrek;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public abstract class Node {

	private int numVisits;
	private int utility;

	private MachineState state;
	private StateMachine machine;
	private Role role;

	private int remainingMovesIndex;
	private List<List<Move>> moves;
	private List<Node> children;
	private Node parent;

	public Node(StateMachine machine, MachineState state, Role role) throws MoveDefinitionException {
		this.setStateMachine(machine);
		this.setNumVisits(1);
		this.setUtility(0);
		this.setState(state);
		this.setRole(role);
		this.setChildren(new ArrayList<Node>());
		this.setRemainingMovesIndex(0);
		if (!machine.isTerminal(state)) {
			this.setMoves(machine.getLegalJointMoves(state));
		} else {
			List<List<Move>> moves = new ArrayList<List<Move>>();
			this.setMoves(moves);
		}
		this.setParent(null);
	}

	public Node(StateMachine machine, MachineState state, Role role, Node parent) throws MoveDefinitionException {
		this(machine, state, role);

		this.setState(state);
		this.setParent(parent);
	}

	public int getNumVisits() {
		return numVisits;
	}

	public void setNumVisits(int numVisits) {
		this.numVisits = numVisits;
	}

	public void addVisit() {
		this.setNumVisits(this.getNumVisits() + 1);
	}

	public int getUtility() {
		return utility;
	}

	public void setUtility(int utility) {
		this.utility = utility;
	}

	public void addUtility(int score) {
		this.setUtility(this.getUtility() + score);
	}

	public MachineState getState() {
		return state;
	}

	public void setState(MachineState state) {
		this.state = state;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void addChild(Node child) {
		this.children.add(child);
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public List<List<Move>> getMoves() {
		return this.moves;
	}

	public void setMoves(List<List<Move>> remainingMoves) {
		this.moves = remainingMoves;
	}

	public void setStateMachine(StateMachine machine) {
		this.machine = machine;
	}

	public StateMachine getStateMachine() {
		return this.machine;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Role getRole() {
		return this.role;
	}

	public int getRemainingMovesIndex() {
		return remainingMovesIndex;
	}

	public void setRemainingMovesIndex(int remainingMovesIndex) {
		this.remainingMovesIndex = remainingMovesIndex;
	}

	public void incrementRemainingMovesIndex() {
		this.remainingMovesIndex++;
	}

	public void backPropagate(int score) {
		this.addVisit();
		this.addUtility(score);
		if (this.getParent() != null) {
			this.getParent().backPropagate(score);
		}
	}

	abstract Node select() throws MoveDefinitionException, TransitionDefinitionException;

	abstract Node expand() throws MoveDefinitionException, TransitionDefinitionException;

	// General selectfn function to calculate the utility of a node based on
	// utility and number of visits
	protected static double selectMinfn(Node node) {
		return (-1 * node.getUtility() / node.getNumVisits()) + 50 * Math.sqrt(Math.log(node.getParent().getNumVisits()) / node.getNumVisits());
	}

	// General selectfn function to calculate the utility of a node based on
	// utility and number of visits
	protected static double selectMaxfn(Node node) {
		return node.getUtility() / node.getNumVisits() + 50 * Math.sqrt(Math.log(node.getParent().getNumVisits()) / node.getNumVisits());
	}

	@Override
	public abstract String toString();
}
