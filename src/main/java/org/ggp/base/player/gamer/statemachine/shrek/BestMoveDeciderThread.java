package org.ggp.base.player.gamer.statemachine.shrek;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

public class BestMoveDeciderThread implements Runnable {
	Role role;
	MachineState state;
	Instant startTime;
	StateMachine machine;
	List<Move> moves;
	Thread t;
	BestMove bestMove;
	long timeout;

	CountDownLatch latch;

	private static final int WINNING_SCORE = 100;
	private static final Duration TIME_TO_DECIDE = Duration.ofSeconds(17);

	@Override
	public void run() {
		int score = 0;
		int start_level = 0;

		for(Move legalMove : moves){
			Instant start = Instant.now();
			int result;
			try {
				result = minScore(state, role, legalMove, 0, startTime);
				Instant after = Instant.now();
				Duration minScoreTime = Duration.between(start, after);
				if (result > score) {
					score = result;
					bestMove.setMoveAndScore(legalMove, score);
				}
			} catch (MoveDefinitionException | TransitionDefinitionException | GoalDefinitionException e) { e.printStackTrace();}
		}
		latch.countDown();
	}
	public void start() {
	      if (t == null) {
	         t = new Thread (this);
	         t.start ();
	      }
	   }

	public BestMoveDeciderThread(Role role, MachineState state, Instant startTime, StateMachine machine,List<Move> moves,BestMove bestMove, CountDownLatch latch)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		this.role = role;
		this.state = state;
		this.startTime = startTime;
		this.machine = machine;
		this.moves = moves;
		this.bestMove = bestMove;
		this.latch = latch;
	}

	private int minScore(MachineState state, Role role, Move move, int level, Instant startTime)
			throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException
	{
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
		if(machine.isTerminal(state)) {
			return machine.getGoal(state, role);
		}
		if (Duration.between(startTime, Instant.now()).compareTo(TIME_TO_DECIDE) > 0) {
			long curTime = System.currentTimeMillis();
//			System.out.println(curTime);
//			System.out.println(System.currentTimeMillis()+ "\n");
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
			total+= machine.findReward(role, machine.performDepthCharge(state, depth));
		}
		return total/count;
	}




}

