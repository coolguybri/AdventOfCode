
import java.io.*;
import java.util.*;

public class Day02 {

    protected static class Game {
			public final int id;
			public final int max_red;
			public final int max_blue;
			public final int max_green; 

        public Game(int i, int r, int b, int y) {
			id = i;
		 	max_red = r;
			max_blue = b;
			max_green = y;
        }

        public String toString() {
          return String.format("Game %d, red=%d, blu=%d, grn=%d", id, max_red, max_blue, max_green);
        }

        // Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
       	public static Game parseGame(String line) {
            // Parse the id.
 			int colPos = line.indexOf(':');
            String idString = line.substring(5, colPos);
			int idNum = Integer.parseInt(idString);
			
			int max_r = 0;
			int max_b = 0;
			int max_g = 0;
            StringTokenizer st1 = new StringTokenizer(line.substring(colPos + 1), ";");
			while (st1.hasMoreTokens()) {
				StringTokenizer st2 = new StringTokenizer(st1.nextToken(), ",");
				while (st2.hasMoreTokens()) {
					String reveal = st2.nextToken().trim();
//					System.out.format("reveal=%s\n", reveal);
				
					int quantityEndPos = reveal.indexOf(' ');
					int quantity = Integer.parseInt(reveal.substring(0, quantityEndPos));
					String color = reveal.substring(quantityEndPos).trim();
//					System.out.format(" quantity=%d, color=%s\n", quantity, color);

					if (color.equals("red")) {
						if (quantity > max_r) 
							max_r = quantity;
					} else if (color.equals("blue")) {
						if (quantity > max_b) 
							max_b = quantity;
					} else if (color.equals("green")) {
						if (quantity > max_g) 
							max_g = quantity;
					}
				}
			}

			return new Game(idNum, max_r, max_b, max_g);
		}
    }


	private static int EvaluateGames(List<Game> games, int test_red, int test_blue, int test_green) {
		int match_sum = 0;

		for (Game game : games) {
			if ((game.max_red > test_red) ||
				 (game.max_blue > test_blue) ||
				 (game.max_green > test_green)) {
				continue;
			}

		    System.out.format("game Match game=%s\n", game.toString());
			match_sum += game.id;
		}

		return match_sum;
    }


	private static int PowerScoreGames(List<Game> games) {
		int power_sum = 0;

		for (Game game : games) {
			int power = game.max_red * game.max_blue * game.max_green;
		    System.out.format("game=[%s], power=%d\n", game.toString(), power);
			power_sum += power;
		}

		return power_sum;
    }

 

    public static void main(String[] args) throws Exception { 
    	System.out.format("Day Two, Yo!\n\n");

        // Initial state.
        List<Game> games = new ArrayList<Game>();

        String filename = "day02.input";       	
       	File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
        
        // Loop through the input file. One game per line.
        String line;
        int lineNum = 0;
        for (; (line = br.readLine()) != null ; lineNum++) {
   			// Skip blank lines.
            line = line.trim();
			if (line.length() == 0) {
				continue;
			} 

			Game g = Game.parseGame(line);
            games.add(g);
		    System.out.format("line %d, game=%s\n", lineNum, g.toString());
		}
		System.out.format("\nImport complete:\n lines=%d\n\n", lineNum);

        // Test the games.
		int sum = EvaluateGames(games, 12, 14, 13);
		System.out.format("\nAnalysis complete:\n sum=%d\n\n", sum);
		int score = PowerScoreGames(games);
		System.out.format("\nScore complete:\n score=%d\n\n", score);
	}
}
