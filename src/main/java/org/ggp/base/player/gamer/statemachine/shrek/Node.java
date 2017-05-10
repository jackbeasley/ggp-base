package org.ggp.base.player.gamer.statemachine.shrek;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;

public class Node {

	private int numVisits;
	private int utility;
	private MachineState state;
	private List<Node> children;
	private Node parent;

	public Node() {
		this.setNumVisits(0);
		this.setUtility(0);
		this.setState(null);
		this.setChildren(new ArrayList<Node>());
		this.setParent(null);
	}

	public Node(MachineState state, Node parent) {
		super();
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

}
