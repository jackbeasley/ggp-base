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

public class ShrekCompulsiveDeliberationPlayer extends StateMachineGamer {

	@Override
	public StateMachine getInitialStateMachine() {
		return new CachedStateMachine(new ProverStateMachine());
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
		List<Move>moves = machine.getLegalMoves(state,role);
		Move move = moves.get(0);
		int score = 0;
		for (int i = 0; i<moves.size();i++){
			List<Move> simulatedMove = new ArrayList<Move>();
			// Single player so we will always be role 0
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


	private int maxscore(StateMachine machine,MachineState state,Role role) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException{
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
		return "Compulsive Shrek";
	}

}
