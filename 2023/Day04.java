
import java.io.*;
import java.util.*;

public class Day04 {

	private static class Game  {
		public List<Integer> given;
		public List<Integer> winners;

		private Game(List<Integer> g, List<Integer> w) {
			given = g;
			winners = w;
		}

		private static List<Integer> parseList(String line) {
			List<Integer> ret = new ArrayList<Integer>();

			System.out.format("\t\t\tparsing list %s\n", line);
			StringTokenizer st = new StringTokenizer(line, " ");
                        while (st.hasMoreTokens()) {
				ret.add(Integer.parseInt(st.nextToken()));
			}	
			return ret;
		}

		// Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
		public static Game parseGame(String line) {
			// skip over card & split into two string by |
			String dubList = line.substring(line.indexOf(':') + 1);
			System.out.format("\t\tparsing game %s\n", dubList);
			int splitPos = dubList.indexOf('|');

			// parse each list of numbers into a list.
			return new Game(parseList(dubList.substring(0, splitPos)),
				 parseList(dubList.substring(splitPos + 1)));
		}

                public int score() {
			List<Integer> good_times = new ArrayList<Integer>(given);
			good_times.retainAll(winners);

			int num_winners = good_times.size();
			int score = (num_winners == 0) ? 0 : (1 << (num_winners - 1));
			System.out.format("\t\t\tgame score:%s, count=%d, score=%d\n", good_times, num_winners, score);
			return score;
		}

                public int matches() {
			List<Integer> good_times = new ArrayList<Integer>(given);
			good_times.retainAll(winners);

			int num_winners = good_times.size();
			return num_winners;
		}
	}

	// Initial state.
	private static List<Game> games = new ArrayList<Game>();


    public static void main(String[] args) throws Exception { 
    	System.out.format("Day Four, Yo!\n\n");

        //String filename = "input/day04.sample";       	
        String filename = "input/day04.input";       	
       	File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
	int lineno = 0;
        for (String line ; (line = br.readLine()) != null ; lineno++) {
		line = line.trim();
		System.out.format("\timporting line (len=%d): \"%s\"\n", line.length(), line);
		games.add(Game.parseGame(line));
	}
	System.out.format("\nImport complete for %s:, lines=%d, games=%d\n\n", filename, lineno, games.size());

	int tot_score = 0;
	for (Game game : games) {
		int score = game.score();
		tot_score += score;
	}
	System.out.format("\nAnalysis-part1 complete: score=%d, gamecount=%d\n", tot_score, games.size());

	List<Integer> cardCounts = new ArrayList<Integer>();
	for (int i = 0 ; i < games.size() ; i++ ) {
		cardCounts.add(1);
	}
	System.out.format("\t\tcardcount-begin: %s\n", cardCounts);

	int gameno = 0;
        int tot_cards = 0;
	for (Game game : games) {
		int self_copies = cardCounts.get(gameno);
		int score = game.matches();
		System.out.format("\t\tplaying: game %d, copies=%d, matches=%d\n", gameno, self_copies, score);
		for (int i = 1 ; i <= score ; i++) {
			int currentPos = gameno + i;
			int currentCount = cardCounts.get(currentPos);
			cardCounts.set(currentPos, currentCount + self_copies);
		}

		System.out.format("\t\tcardcount-post-%d: %s\n", gameno, cardCounts);
		gameno++;
		tot_cards += self_copies;
	}

	System.out.format("\nAnalysis-part2 complete: cards=%d\n", tot_cards);
    }
}
