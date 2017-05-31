package org.ggp.base.player.gamer.statemachine.shrek;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class MCTSBestMoveCalculator implements Callable<BestMove> {
	Role role;
	MachineState state;
	Instant startTime;
	StateMachine machine;
	List<Move> moves;
	Thread thread;
	ExecutorService es;
	expNode tree;
	int depthCharges;

	private static final int WINNING_SCORE = 100;
	private static final Duration TIME_TO_DECIDE = Duration.ofSeconds(9);
	private static final int DEPTH_LIMIT = 2;

	public MCTSBestMoveCalculator(StateMachine machine, MachineState state , Instant startTime, Role role,
			List<Move> moves,ExecutorService es) {
		this.role = role;
		this.state = state;
		this.startTime = startTime;
		this.machine = machine;
		this.moves = moves;
		this.es = es;
	}

	@Override
	public BestMove call() {
		try {
			return findBestMove(this.moves, this.state, this.role);
		} catch (MoveDefinitionException | TransitionDefinitionException | GoalDefinitionException e) {
			e.printStackTrace();
			return null;
		}
	}

	private BestMove findBestMove(List<Move> moves, MachineState state, Role role)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		depthCharges = 0;
		this.tree = new expNode(state, role,null);
		this.tree.max = true;
		while(!((Duration.between(startTime, Instant.now()).compareTo(TIME_TO_DECIDE) > 0))){
			expNode node = tree.select();
			expNode leaf = expand(node);
			int score;
			if(!leaf.max){
				leaf = expand(leaf);
			}
			score = monteCarlo(leaf.state,role,4);
			leaf.backPropagate(score);
		}

		BestMove bestMove = new BestMove(null, Integer.MIN_VALUE);
		for(expNode child : tree.children){
			double score = (child.utility/child.visits);
			if (score > bestMove.getScore()){
				bestMove.setScore((int)score);
				bestMove.setMove(child.move);
			}
		}


		System.out.println("depthCharges: " + depthCharges);
		return bestMove;
	}

	private expNode expand(expNode node) throws MoveDefinitionException, TransitionDefinitionException{
		if (machine.isTerminal(node.state)){
			return node;
		}
		if (node.max) {
			// then player decides on a move
			List<Move> moves = machine.getLegalMoves(node.state,role);
			for(Move move : moves){
				expNode leaf = new expNode(node.state,role,node,move);
				node.children.add(leaf);
			}
		} else {
			for(List<Move> jointMove : machine.getLegalJointMoves(node.state,role,node.move)){
//				System.out.println("state: " + node.state);
				MachineState next = machine.getNextState(node.state, jointMove);
				expNode leaf = new expNode(next,role,node);
				node.children.add(leaf);
			}
		}
		//grab random child
		return node.children.get((new Random()).nextInt(node.children.size()));
	}

	private int monteCarlo(MachineState state, Role role, int count)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		int total = 0;

		List<Future<Integer>> futures = new ArrayList<Future<Integer>>();

		for (int i = 0; i < count; i++) {
			futures.add(es.submit(new DepthCharge(machine, role, state)));
		}
		for (Future<Integer> future : futures){
			try {
				total+=future.get();
				depthCharges++;
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		return total / (count+1);
	}






}
