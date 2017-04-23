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

public class ShrekOpponentHeuristicPlayer extends StateMachineGamer {
	List<Role> roles;
	List<MachineState> terminalStates = new ArrayList<MachineState>();

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
		int limit = 3;
		if(machine.isTerminal(state)) {
			return machine.getGoal(state, role);
		}
		if (level>=limit) {return evalfn(role,state);};
		List<Move> moves = machine.getLegalMoves(state,role);
		int score = 0;
		for(Move legalMove : moves){
			int result = minScore(state, role, legalMove,level);
			if(result > score) score = result;
		}
		return score;
	}

	private int evalfn(Role role, MachineState state) throws MoveDefinitionException{
		StateMachine machine = getStateMachine();
		List<Role> roles = machine.getRoles();
		int mobility = 0;
		for(Role curRole : roles){
			if(!curRole.equals(role)){
				mobility+=getMobility(curRole,state);
			}
		}
		return mobility/roles.size();
	}

	private int getMobility(Role role,MachineState state) throws MoveDefinitionException{
		List<Move> moves = getStateMachine().getLegalMoves(state,role);
		List<Move> feasibles = getStateMachine().findActions(role);
		double val = ((double)moves.size()/(double)feasibles.size())*100.0;
		return (int)val;
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
		return "Opponent Heuristic Shrek";
	}

}
