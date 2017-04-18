package org.ggp.base.player.gamer.statemachine.shrek;

import java.util.ArrayList;
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

public class ShrekMiniMaxPlayer extends StateMachineGamer {
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
		List<Move> moves = machine.getLegalMoves(state,role);
		Move move = moves.get(0);
		int score = 0;
		for (int i = 0; i<moves.size();i++){
			List<Move> simulatedMove = new ArrayList<Move>();
			simulatedMove.add(moves.get(i));
			int result = maxScore(machine.getNextState(state, simulatedMove), role);
			if (result == 100) return moves.get(i);
			if (result > score){
				score = result;
				move = moves.get(i);
			}
		}
		return move;
	}

	private Move bestMove(Role role, MachineState state) throws MoveDefinitionException
	{
		StateMachine machine = getStateMachine();
		List<Move> legalMoves = machine.getLegalMoves(state, role);

		int score = 0;
		for (Move move : legalMoves)
		{

		}
		return null;
	}

	private int minScore(MachineState state, Role role, Move move)
				throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
		StateMachine machine = getStateMachine();

		int score = 100;

		// Other players should have noop as legal move
		List<List<Move>> legalMoves = machine.getLegalJointMoves(state, role, move);

		// Loop though all sets of legal moves for each role
		for (List<Move> legalMoveSet : legalMoves) {
			MachineState simState = machine.getNextState(state, legalMoveSet);
			int highest = maxScore(simState, role);
			if (highest == 0) {
				return 0;
			} else if (highest < score) {
				score = highest;
			}
		}

		return score;
	}

	private int maxScore(MachineState state, Role role) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{

		StateMachine machine = getStateMachine();
		if(machine.isTerminal(state)){
			return machine.getGoal(state, role);
		}


		List<Move> moves = machine.getLegalMoves(state,role);
		int score = 0;
		for(Move legalMove : moves){

			List<List<Move>> legalMoves = machine.getLegalJointMoves(state, role, legalMove);
			for (List<Move> moveSet : legalMoves) {
				int result = minScore(machine.getNextState(state, moveSet), role, legalMove);

				if(result > score) score = result;
			}

		}
		return score;
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
		return "MiniMax Shrek";
	}

}
