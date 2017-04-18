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
			int result = maxscore(machine,machine.getNextState(state, simulatedMove), role);
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

	private int minScore(Role role, Move move, MachineState state)
				throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
		StateMachine machine = getStateMachine();


		// Get opponents
		List<Role> roles = machine.getRoles();

		int score = 100;

		List<List<Move>> legalMoves = new ArrayList<List<Move>>();

		// Create a list of lists that contains the possible moves for each role
		for (int roleid = 0; roleid < roles.size(); roleid++)
		{
			if (roles.get(roleid) == role)
			{
				List<Move> possibleMove = new ArrayList<Move>();
				possibleMove.add(move);
				legalMoves.set(roleid, possibleMove);
			} else {
				legalMoves.set(roleid, machine.getLegalMoves(state, roles.get(roleid)));
			}
		}

		for (List<Move> list : legalMoves)
		{
			for
		}


		// Get the next state if legalMove is made
		List<Move> simMove = new ArrayList<Move>();
		simMove.add(move);
		simMove.add(legalMove);
		MachineState simState = machine.getNextState(state, simMove);

		int highest = maxScore(machine, simState, opponent);
		if (highest < score)
		{
			score = highest;
		}
		return score;
	}

	private int maxScore(StateMachine machine,MachineState state,Role role) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
		if(machine.isTerminal(state)){
			return machine.getGoal(state, role);
		}
		List<Move>moves = machine.getLegalMoves(state,role);
		int score = 0;
		for(int i = 0; i<moves.size();i++){
			List<Move> simulatedMove = new ArrayList<Move>();
			simulatedMove.add(moves.get(i));
			int result = maxscore(machine,machine.getNextState(state, simulatedMove),role);
			if(result > score) score = result;
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
