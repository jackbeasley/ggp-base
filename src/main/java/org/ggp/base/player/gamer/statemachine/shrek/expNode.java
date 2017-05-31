package org.ggp.base.player.gamer.statemachine.shrek;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class expNode {

	int visits = 0;
	double utility = 0;

	MachineState state;
	Role role;

	List<List<Move>> moves;
	List<expNode> children;
	expNode parent;
	boolean max;
	Move move;


	public expNode( MachineState state, Role role,expNode parent)
			throws MoveDefinitionException {
		this.state = state;
		this.role = role;
		this.parent = parent;
		this.max = true;
		this.children = new ArrayList<expNode>();
	}

	public expNode( MachineState state, Role role, expNode parent,Move move)
			throws MoveDefinitionException {
		this(state, role,parent);
		this.max = false;
		this.move = move;

	}

	public void backPropagate(int score){
		this.visits++;
		this.utility+=(score);
		if(parent != null){
			parent.backPropagate(score);
		}
	}

	public expNode select() throws TransitionDefinitionException, MoveDefinitionException{
		if(this.children.size()==0){
			return this;
		}
		if(this.visits==0){
			return this;
		}
		expNode branch = this.children.get(0);
		double score = Double.NEGATIVE_INFINITY;

		for(expNode child : this.children){
			if(child.visits==0){
				return child;
			}
			double newscore = selectfn(child);
			if(newscore > score){
				score = newscore;
				branch = child;
			}
		}
		return branch.select();
	}

	private double selectfn(expNode node){
		if(node.max){
			return selectMaxfn(node);
		} else {
			return selectMinfn(node);
		}
	}

	public static double selectMinfn(expNode node){
		return node.utility/node.visits+50*Math.sqrt(Math.log(node.parent.visits)/node.visits);
	}

	public static double selectMaxfn(expNode node){
		return (-1*node.utility/node.visits)+50*Math.sqrt(Math.log(node.parent.visits)/node.visits);
	}

}
