
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.text.ParseException;

public class Day2 {

	enum GameSignal {
		ROCK,
		PAPER,
		SCISSORS
	}
	
	static GameSignal getPredator(GameSignal signal) throws Exception {
		switch (signal) {
			case ROCK:
				return GameSignal.PAPER;
			case PAPER:
				return GameSignal.SCISSORS;
			case SCISSORS:
				return GameSignal.ROCK;
		}
		throw new Exception("impossible, yo");
	}
	
	static GameSignal getPrey(GameSignal signal) throws Exception {
		switch (signal) {
			case ROCK:
				return GameSignal.SCISSORS;
			case PAPER:
				return GameSignal.ROCK;
			case SCISSORS:
				return GameSignal.PAPER;
		}
		throw new Exception("impossible, yo");
	}
	
	static GameSignal parseMoveSelf(char input) throws ParseException {
		switch (input) {
			case 'X':
				return GameSignal.ROCK;
			case 'Y':
				return GameSignal.PAPER;
			case 'Z':
				return GameSignal.SCISSORS;
			default:
				throw new ParseException("get the hell out of here with this bullshit: " + input, 0);
		}
	}
	
	static GameSignal parseMoveOpponent(char input) throws ParseException {
		switch (input) {
			case 'A':
				return GameSignal.ROCK;
			case 'B':
				return GameSignal.PAPER;
			case 'C':
				return GameSignal.SCISSORS;
			default:
				throw new ParseException("get the hell out of here with this bullshit: " + input, 0);
		}
	}
	
	static GameSignal determineMoveSelf(char input, GameSignal oppoMove) throws Exception {
		switch (input) {
			case 'X': // time to throw this one and lose.
				return getPrey(oppoMove);
			case 'Y': // draw, need the same one.
				return oppoMove;
			case 'Z': // aww yeah time to kill.
				return getPredator(oppoMove);
			default:
				throw new ParseException("get the hell out of here with this bullshit: " + input, 0);
		}
	}
	
	static int scoreRound(GameSignal selfMove, GameSignal oppoMove) throws Exception {
		int score_move = 0;
		boolean gameWin = false;
		boolean gameDraw = false;
		boolean gameLose = false;
		
		switch (selfMove) {
			case ROCK:
				score_move = 1;
				gameWin = (oppoMove == GameSignal.SCISSORS);
				gameDraw = (oppoMove == GameSignal.ROCK);
				gameLose = (oppoMove == GameSignal.PAPER);
				break;
				
			case PAPER:
				score_move = 2;
				gameWin = (oppoMove == GameSignal.ROCK);
				gameDraw = (oppoMove == GameSignal.PAPER);
				gameLose = (oppoMove == GameSignal.SCISSORS);
				break;
				
			case SCISSORS:
				score_move = 3;
				gameWin = (oppoMove == GameSignal.PAPER);
				gameDraw = (oppoMove == GameSignal.SCISSORS);
				gameLose = (oppoMove == GameSignal.ROCK);
				break;
		}
		
		// Sanity check.
		if (Stream.of(gameWin, gameDraw, gameLose).filter(b -> b).count() != 1) {
       		throw new Exception("Shit, somebody is playing this game wrong!");
       	}
		
		int score_round = score_move;
		if (gameWin)
			score_round += 6;
		else if (gameDraw)
			score_round += 3;
		else if (gameLose)
			score_round += 0;
		else
			throw new Exception("Shit, somebody is realy playing this game wrong!");
			
		return score_round;
	}
	

  public static void main(String[] args) throws Exception { 
  	System.out.format("Day Two Mutha Fuckah! Question One, Yo:\n\n");

	// Initial state.
	int round_count = 0;
	int score_total = 0;

	// Open input file.
   	File file = new File("day2.input");
    BufferedReader br = new BufferedReader(new FileReader(file));
        
    // Loop through the input file until it hits EOF.
    String line;
    while ((line = br.readLine()) != null) {
    	// Track how many rounds we are playing.
	  	round_count++;

		// Skip blank lines.
		if (line.length() == 0)
			continue;

		// Freak if we see malformed line.
		if (line.length() < 3) {
			throw new Exception(String.format("round %d crap input: \"%s\"", round_count, line));
      	}
      	
      	// Read in this rounds's moves.
      	GameSignal oppoMove = parseMoveOpponent(line.charAt(0));
      	
      	boolean part1 = false;
      	
      	GameSignal selfMove;
      	if (part1) {
      		selfMove = parseMoveSelf(line.charAt(2));
      	} else {
      		selfMove = determineMoveSelf(line.charAt(2), oppoMove);
      	}
      	
      	int score_current = scoreRound(selfMove, oppoMove);
		score_total += score_current;
		System.out.format("round %d end (algorithm=%b), score=%d, total=%d\n", round_count, !part1, score_current, score_total);
      	
	}
		
	System.out.format("Analysis complete:\n rounds=%d, total=%d\n", round_count, score_total);
  }
}
