package org.ggp.base.player.gamer.statemachine.shrek;

import java.util.List;

import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.statemachine.verifier.StateMachineVerifier;

public class ShrekTestPlayer extends StateMachineGamer {

	private StateMachine machine;
	private StateMachine refMachine;

	private List<Role> roles;

	public ShrekTestPlayer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public StateMachine getInitialStateMachine() {
		machine = new CachedStateMachine(new ProverStateMachine());

		roles = machine.getRoles();

		return machine;
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		System.out.println("============================ START TESTING ===========================");
		StateMachine prover = new CachedStateMachine(new ProverStateMachine());
		prover.initialize(getMatch().getGame().getRules());
		StateMachine prover2 = new CachedStateMachine(new ProverStateMachine());
		prover2.initialize(getMatch().getGame().getRules());
		StateMachine propMachine = new ShrekPropNetMachine();
		propMachine.initialize(getMatch().getGame().getRules());
		StateMachineVerifier.checkMachineConsistency(prover, propMachine, 10000);
		System.out.println("============================ DONE TESTING ============================");
	}

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {

		return null;
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
		// TODO Auto-generated method stub
		return "TEST PLAYER, PLEASE IGNORE";
	}

}