package org.ggp.base.player.gamer.statemachine.shrek;

import java.util.List;

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

public class ShrekDepthLimitedPlayer extends StateMachineGamer {
	List<Role> roles;
	@Override
	public StateMachine getInitialStateMachine() {
		ShrekPropNetThreadSafeMachine machine = new ShrekPropNetThreadSafeMachine();
		machine.initialize(getMatch().getGame().getRules());
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
			int result = minScore(state, role, legalMove,0);
			if (result > score){
				score = result;
				bestMove = legalMove;
			}
		}
		if(score == 0){
			return machine.getRandomMove(state,role);
		}
		return bestMove;
	}

	private int minScore(MachineState state, Role role, Move move,int level)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
		StateMachine machine = getStateMachine();

		int score = 100;

		// Other players should have noop as legal move
		List<List<Move>> legalMoves = machine.getLegalJointMoves(state, role, move);

		// Loop though all sets of legal moves for each role
		for (List<Move> legalMoveSet : legalMoves) {
			MachineState simState = machine.getNextState(state, legalMoveSet);
			int highest = maxScore(simState, role,level+1);
			if (highest == 0) {
				return 0;
			} else if (highest < score) {
				score = highest;
			}
		}
		return score;
	}

	private int maxScore(MachineState state, Role role,int level)
			throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
		StateMachine machine = getStateMachine();
		int limit = 4;
		if(machine.isTerminal(state)) return machine.getGoal(state, role);
		if (level>=limit) {return evalfn(role,state);};
		List<Move> moves = machine.getLegalMoves(state,role);
		int score = 0;
		for(Move legalMove : moves){
			int result = minScore(state, role, legalMove,level);
			if(result > score) score = result;
		}
		return score;
	}

	//using mobility heuristic
	private int evalfn(Role role, MachineState state) throws MoveDefinitionException{
		List<Move> moves = getStateMachine().getLegalMoves(state,role);
		List<Move> feasibles = getStateMachine().findActions(role);
		return moves.size()/feasibles.size()*100;
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
		return "DepthLimited Shrek";
	}

}
