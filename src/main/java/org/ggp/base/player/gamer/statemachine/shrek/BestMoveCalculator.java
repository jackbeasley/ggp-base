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

public class BestMoveCalculator implements Callable<BestMove> {
	Role role;
	MachineState state;
	Instant startTime;
	StateMachine machine;
	List<Move> moves;
	Thread thread;
	ExecutorService es;

	private int depthCharges = 0;

	private static final int WINNING_SCORE = 100;
	private static final Duration TIME_TO_DECIDE = Duration.ofSeconds(15);
	private static final int DEPTH_LIMIT = 2;

	public BestMoveCalculator(StateMachine machine, MachineState state , Instant startTime, Role role,
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
			BestMove move = findBestMove(this.moves, this.state, this.role);
			System.out.println("Total Depth Charges:" + depthCharges);
			return move;
		} catch (MoveDefinitionException | TransitionDefinitionException | GoalDefinitionException e) {
			e.printStackTrace();
			return null;
		}
	}

	private BestMove findBestMove(List<Move> moves, MachineState state, Role role)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		// Start with a null bestMove
		BestMove bestMove = new BestMove(null, 0);

		for (Move legalMove : moves) {
			// Run the recursive tree search
			int result = minScore(state, role, legalMove, 0, startTime);

			// If the new result is better than the last one, update the best move
			if (result > bestMove.getScore()) {
				// Update the best move
				bestMove = new BestMove(legalMove, result);
			}
		}
		return bestMove;
	}

	private int minScore(MachineState state, Role role, Move move, int level, Instant startTime)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
		int score = WINNING_SCORE;

		// Other players should have noop as legal move
		List<List<Move>> legalMoves = machine.getLegalJointMoves(state, role, move);

		// Loop though all sets of legal moves for each role
		for (List<Move> legalMoveSet : legalMoves) {
			// Simulate a move
			MachineState simState = machine.getNextState(state, legalMoveSet);

			// Make recursive maxScore call for the next level
			int highest = maxScore(simState, role, level + 1, startTime);
			if (highest == 0) {
				return 0;
			} else if (highest < score) {
				score = highest;
			}
		}

		return score;
	}

	private int maxScore(MachineState state, Role role, int level, Instant startTime)
			throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {

		// If it is a terminal state, just return the value of that state as
		// there are no moves to make
		if (machine.isTerminal(state)) {
			return machine.getGoal(state, role);
		}

		// If the time passed is greater than the time to decide, just return
		// the montecarlo result
		// before the time runs out
		if (Duration.between(startTime, Instant.now()).compareTo(TIME_TO_DECIDE) > 0) {
			// TODO: Why use depth limit as Monte Carlo sample count?
			return monteCarlo(state, role, DEPTH_LIMIT);
		}

		// If we are at the depth limit, use monte carlo to get estimated values
		if (level >= DEPTH_LIMIT) {
			// TODO: Why DEPTH_LIMIT + 3 for number of samples
			return monteCarlo(state, role, DEPTH_LIMIT + 3);
		}

		// We have time are are not below, run minmax
		List<Move> moves = machine.getLegalMoves(state, role);
		int score = 0;
		// Loop through moves and find the one with the smallest value
		for (Move legalMove : moves) {
			int result = minScore(state, role, legalMove, level, startTime);
			if (score == 100)
				// Winning state, just return as that is the max value
				return score;
			if (result > score)
				score = result;
		}
		// Is the highest score
		return score;
	}

	/*
	 * monteCarlo
	 * A function that performs a monte carlo heuristic on
	 * the given state for the given player.
	 *
	 * Runs count number of depth charges to calculate a probabilistic expected
	 * value
	 */
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
		return total / count;
	}


}
