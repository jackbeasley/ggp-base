package org.ggp.base.player.gamer.statemachine.shrek;

import java.util.List;

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

public class ShrekAlphaBetaPlayer extends StateMachineGamer {

	private static int DEFAULT_ALPHA = 0;
	private static int DEFAULT_BETA = 100;

	List<Role> roles;
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
		StateMachine machine = getStateMachine();
		MachineState state = getCurrentState();
		Role role = getRole();
		return bestMove(role, state);

	}

	private Move bestMove(Role role, MachineState state)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
		StateMachine machine = getStateMachine();

		List<Move> moves = machine.getLegalMoves(state,role);

		Move bestMove = null;
		int score = 0;

		for(Move legalMove : moves){
			List<List<Move>> legalMoves = machine.getLegalJointMoves(state, role, legalMove);
			int result = minScore(state, role, legalMove, DEFAULT_ALPHA, DEFAULT_BETA);
			if (result > score){
				score = result;
				bestMove = legalMove;
			}
		}
		return bestMove;
	}

	private int minScore(MachineState state, Role role, Move move, int alpha, int beta)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
		StateMachine machine = getStateMachine();

		int score = 100;

		// Other players should have noop as legal move
		List<List<Move>> legalMoves = machine.getLegalJointMoves(state, role, move);

		// Loop though all sets of legal moves for each role
		for (List<Move> legalMoveSet : legalMoves) {
			MachineState simState = machine.getNextState(state, legalMoveSet);
			int highest = maxScore(simState, role, alpha, beta);
			beta = Math.min(highest, beta);
			if (beta <= alpha) {
				return alpha;
			}
		}

		return beta;
	}

	private int maxScore(MachineState state, Role role, int alpha, int beta)
			throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
		StateMachine machine = getStateMachine();

		if(machine.isTerminal(state)) return machine.getGoal(state, role);

		List<Move> moves = machine.getLegalMoves(state,role);
		int score = 0;
		for(Move legalMove : moves){

			int result = minScore(state, role, legalMove, alpha, beta);

			alpha = Math.max(alpha, result);

			if(alpha >= beta) {
				return beta;
			}
		}

		return alpha;
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
		return "Shrek is Alpha, Donkey is Beta";
	}

}
