package org.ggp.base.player.gamer.statemachine.shrek;

import org.ggp.base.util.statemachine.Move;

public class BestMove {
	Move move;
	int score;

	public BestMove(){

	}

	public void setMoveAndScore(Move move,int score){
		this.move=move;
		this.score = score;
	}

	public int getScore(){
		return score;
	}

	public Move getMove(){
		return move;
	}
}
