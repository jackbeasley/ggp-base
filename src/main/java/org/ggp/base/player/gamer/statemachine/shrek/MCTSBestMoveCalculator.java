package org.ggp.base.player.gamer.statemachine.shrek;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
	Node tree;
	int depthCharges;

	private static final int WINNING_SCORE = 100;
	private static final Duration TIME_TO_DECIDE = Duration.ofSeconds(15);
	private static final int DEPTH_LIMIT = 2;

	public MCTSBestMoveCalculator(StateMachine machine, MachineState state , Instant startTime, Role role,
			List<Move> moves,ExecutorService es) {
		this.role = role;
		this.state = state;
		this.startTime = startTime;
		this.machine = machine;
		this.moves = moves;
		this.es = es;
		try {
			this.tree = new MaxNode(machine, state, role);
		} catch (MoveDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		while(!((Duration.between(startTime, Instant.now()).compareTo(TIME_TO_DECIDE) > 0))){
			Node node = tree.select();
			int score;
			score = monteCarlo(node.getState(),role,4);
			node.backPropagate(score);
		}
		BestMove bestMove = new BestMove(null, 0);
		for(int i = 0; i < tree.getChildren().size(); i++){
			double score = tree.selectMinfn(tree.getChildren().get(i));
			if (score > bestMove.getScore()){
				bestMove.setScore((int)score);
				bestMove.setMove(tree.getMoves().get(i).get(machine.getRoles().indexOf(role)));
			}
		}

		System.out.println("depthCharges: " + depthCharges);
		return bestMove;
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
