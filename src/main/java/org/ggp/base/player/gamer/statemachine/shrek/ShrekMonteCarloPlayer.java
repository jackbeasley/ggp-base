package org.ggp.base.player.gamer.statemachine.shrek;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

public class ShrekMonteCarloPlayer extends StateMachineGamer {
	List<Role> roles;
	static long timeout;
	long startTime;

	private static final int WINNING_SCORE = 100;
	private static final Duration TIME_TO_DECIDE = Duration.ofSeconds(18);

	@Override
	public StateMachine getInitialStateMachine() {
		StateMachine machine = new CachedStateMachine(new ProverStateMachine());
		roles = machine.getRoles();

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
		this.timeout = timeout;
		this.startTime = System.currentTimeMillis();
		MachineState state = getCurrentState();
		Role role = getRole();
		System.out.println(TimeUnit.MILLISECONDS.toSeconds(timeout));
		// Get the start time to pass around so we can calculate elapsed time
		Instant startTime = Instant.now();
		return bestMove(role, state, startTime);

	}

	private Move bestMove(Role role, MachineState state, Instant startTime)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
		StateMachine machine = getStateMachine();

		List<Move> moves = machine.getLegalMoves(state,role);

		Move bestMove = null;
		int score = 0;
		int start_level = 0;

		for(Move legalMove : moves){
			Instant start = Instant.now();
			int result = minScore(state, role, legalMove, 0, startTime);
			Instant after = Instant.now();
			Duration minScoreTime = Duration.between(start, after);
			if (result > score) {
				score = result;
				bestMove = legalMove;
			}
		}
		if(score == 0){
			return machine.getRandomMove(state,role);
		}
		return bestMove;
	}

	private int minScore(MachineState state, Role role, Move move, int level, Instant startTime)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
		StateMachine machine = getStateMachine();

		int score = WINNING_SCORE;

		// Other players should have noop as legal move
		List<List<Move>> legalMoves = machine.getLegalJointMoves(state, role, move);

		// Loop though all sets of legal moves for each role
		for (List<Move> legalMoveSet : legalMoves) {
			MachineState simState = machine.getNextState(state, legalMoveSet);
			int highest = maxScore(simState, role, level+1, startTime);
			if (highest == 0) {
				return 0;
			} else if (highest < score) {
				score = highest;
			}
		}

		return score;
	}

	private int maxScore(MachineState state, Role role, int level, Instant startTime)
			throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
		int depth_limit = 2;
		StateMachine machine = getStateMachine();
		if(machine.isTerminal(state)) {
			return machine.getGoal(state, role);
		}
		if (Duration.between(startTime, Instant.now()).compareTo(TIME_TO_DECIDE) > 0) {
			long curTime = System.currentTimeMillis();

			System.out.println(curTime);
			System.out.println(System.currentTimeMillis()+ "\n");
			return monteCarlo(state,role,depth_limit)/7;
		}
		if (level>=depth_limit) {return monteCarlo(state,role,depth_limit+3);};
		List<Move> moves = machine.getLegalMoves(state,role);
		int score = 0;
		for(Move legalMove : moves){
			int result = minScore(state, role, legalMove, level, startTime);
			if(score == 100) return score;
			if(result > score) score = result;
		}
		return score;
	}

	private int monteCarlo(MachineState state, Role role, int count) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException{
		int total = 0;
		int[] depth = new int[1];
		for(int i = 0; i<count;i++){
			total+= getStateMachine().findReward(role, getStateMachine().performDepthCharge(state, depth));
		}
		return total/count;
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
		return "MonteCarlo Shrek";
	}

}
