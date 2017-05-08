package org.ggp.base.player.gamer.statemachine.shrek;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;


public class ShrekMonteCarloMultiThreaded extends StateMachineGamer {

	private static final Logger LOGGER = Logger.getLogger(ShrekMonteCarloMultiThreaded.class.getName());

	static CountDownLatch latch;

	@Override
	public StateMachine getInitialStateMachine() {
		StateMachine machine = new CachedStateMachine(new ProverStateMachine());
		return machine;
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub

	}

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		MachineState state = getCurrentState();
		Role role = getRole();

		// Get the start time to pass around so we can calculate elapsed time
		Instant startTime = Instant.now();
		return bestMove(role, state, startTime);

	}

	private Move bestMove(Role role, MachineState state, Instant startTime)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
		StateMachine machine = getStateMachine();

		ExecutorService es = Executors.newFixedThreadPool(2);

		List<Move> moves = machine.getLegalMoves(state,role);
		List<Move> firstHalfMoves = new ArrayList<Move>(moves);
		List<Move> secondHalfMoves = split(firstHalfMoves,moves.size() / 2);

		Future<BestMove> firstHalfBest = es.submit(new BestMoveCalculator(machine, state, startTime, role, firstHalfMoves));
		Future<BestMove> secondHalfBest = es.submit(new BestMoveCalculator(machine, state, startTime, role, secondHalfMoves));

		try {
			BestMove firstBest = firstHalfBest.get();
			BestMove secondBest = secondHalfBest.get();
			if(firstBest.getScore()>secondBest.getScore()){
				return firstBest.getMove();
			} else {
				return secondBest.getMove();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		// Something wrong
		return null;

	}

	private List<Move> split(List<Move> list, int i) {
	    List<Move> x = new ArrayList<Move>(list.subList(i, list.size()));
	    // Remove items from end of original list
	    for (int j=list.size()-1; j>i; --j)
	        list.remove(j);
	    return x;
	}


	@Override
	public void stateMachineStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stateMachineAbort() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return "MonteCarlo MultiThreaded Shrek";
	}

}
