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

public class OCDShrek extends StateMachineGamer {

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
		List<Move> moves = machine.getLegalMoves(state, role);
		return moves.get(0);
	}

	// play function
	// bestMove function
	public Move bestMove (Role role, MachineState state)
		{
			StateMachine machine = getStateMachine();
			List <Move> moves = machine.getLegalMoves(state, role);
			Move bestMove = null;
			int highestScore = 0;

			//can return null
			for (int i = 0; i < moves.size(); i++)
			{
				int currentScore = maxScore (role, simulate(moves.get(i), state));
				if (currentScore == 100) {
					return moves.get(i);
				} else if (currentScore > highestScore) {
					highestScore = currentScore;
					bestMove = moves.get(i);
				}
			}
			return bestMove;
		}

	// maxscore is recursive exploration of the game tree
	public int maxScore(Role role, MachineState state) {
		StateMachine machine = getStateMachine();
		if (machine.isTerminal(state))
		{
			// The score for the role in the current state
			return score(state);
		} else {
			List<Move> moves = machine.getLegalMoves(state, role);
			int score = 0;
			for (Move move : moves)
			{
				int nextScore = maxScore(role, simulate(move, state));
				if (nextScore > score)
				{
					score = nextScore;
				}
			}
			return score;
		}
	}

	/*
	 * Should apply the move to the state and return the resulting sttae
	 */
	public MachineState simulate(Move move, MachineState state)
	{
		return state;
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
		return "Shrek has developed OCD";
	}

}
