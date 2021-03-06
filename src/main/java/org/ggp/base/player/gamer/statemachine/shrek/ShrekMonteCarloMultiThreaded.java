package org.ggp.base.player.gamer.statemachine.shrek;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;


public class ShrekMonteCarloMultiThreaded extends StateMachineGamer {

	private static final Logger LOGGER = Logger.getLogger(ShrekMonteCarloMultiThreaded.class.getName());

	private ExecutorService es;

	@Override
	public StateMachine getInitialStateMachine() {
		ShrekPropNetMachine machine = new ShrekPropNetMachine();
		return machine;
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		 this.es = Executors.newFixedThreadPool(1);
	}

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		MachineState state = getCurrentState();
		Role role = getRole();

		// Get the start time to pass around so we can calculate elapsed time
		Instant startTime = Instant.now();
		Move bestMove = bestMove(role, state, startTime);
		LOGGER.info(bestMove.toString());
		return bestMove;

	}

	private Move bestMove(Role role, MachineState state, Instant startTime)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
		StateMachine machine = getStateMachine();

		List<Move> moves = machine.getLegalMoves(state,role);

		BestMoveCalculator bestMoveCalculator = new BestMoveCalculator(machine, state, startTime, role, moves,this.es);
		return bestMoveCalculator.call().getMove();
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
