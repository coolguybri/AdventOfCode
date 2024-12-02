
import java.io.*;
import java.util.*;

public class Day07 {

	static final boolean part2 = true;

	static enum HandType {
		FiveKind,
		FourKind,
		FullHouse,
		ThreeKind,
		TwoPair,
		OnePair,
		HighCard,
	}

	private static class Hand {
		public final String 		cards;
		public final long 			bet;
		public final HandType 		handType;

		private Hand(String c, long b, HandType ht) {
			cards = c;
			bet = b;
			handType = ht;
		}

		public String toString() {
			return String.format("[h: c=%s, b=%d, ht=%s]",
				 cards, bet, handType);
		}

		private static HandType determineHandTypeByMatchCount(int[] matchCounts) {
			
			System.out.format("\tdetermine: %s\n", Arrays.toString(matchCounts));
			int numPairs = 0;
			int numTriples = 0;
			for (int i = 0 ; i < 5 ; i++) {
				int count = matchCounts[i];
				switch (count) {
					case 5:
						return HandType.FiveKind;
					case 4:
						return HandType.FourKind;
					case 3:
						numTriples++;
						break;
					case 2:
						numPairs++;
						break;
					case 1:
					case 0:
						break;

					default:
					    System.out.format("\twobear: count=%d, matchCounts=%s", count, matchCounts);
						throw new RuntimeException("wtf");
						//break;
					}
			}
			//System.out.format("\tcard count2: pairs=%d, triples=%d\n", numPairs, numTriples);
			if (numTriples > 0) {
				if (numPairs > 0) {
					return HandType.FullHouse;
				}
				return HandType.ThreeKind;
			}
			switch (numPairs) {
				case 2:
					return HandType.TwoPair;
				case 1:
					return HandType.OnePair;
				case 0:
				default:
					return HandType.HighCard;
			}
		}

		private static HandType determineHandType(String c) {
			int[] matchCounts = new int[5];
			Arrays.fill(matchCounts, 0);
			boolean[] matchUsed = new boolean[5];
			Arrays.fill(matchUsed, false);
			int j_index = -1;

			for (int i = 0 ; i < 5 ; i++) {
				if (matchUsed[i])
					continue;
				matchUsed[i] = true;

				if (c.charAt(i) == 'J')
					j_index = i;

				int cardCount = 1;
				for (int j = i + 1 ; j < 5 ; j++) {
					if (c.charAt(i) == c.charAt(j)) {
						cardCount++;
						matchUsed[j] = true;
					}
				}

				matchCounts[i] = cardCount;
			}
			//System.out.format("\tcard count: %s  %s\n", c, Arrays.asList(matchCounts));
			HandType ht1 = determineHandTypeByMatchCount(matchCounts);

			if (part2 && j_index >= 0) {
				int jackCount = matchCounts[j_index];

				int best_index = 0;
				int best_value = 0;
				for (int i = 0 ; i < 5 ; i++) {
					if ((i != j_index) && (matchCounts[i] > best_value)) {
						best_index = i;
						best_value = matchCounts[i];
					}
				}

				System.out.format("\tWILD: adding %d wildcards to %d\n", jackCount , best_index);
				matchCounts[best_index] = best_value + jackCount;
			}

			HandType ht2 = determineHandTypeByMatchCount(matchCounts);
			System.out.format("\tFINAL: orig=%s, wild=%s\n", ht1, ht2);
			return ht2;
		}


		// 32T3K 765
		public static Hand parse(BufferedReader br) throws IOException {
			String line = br.readLine();
			if (line == null) {
				return null;
			}

			int splitPos = line.trim().indexOf(' ');
			String c = line.substring(0, splitPos);	
			long b = Long.parseLong(line.substring(splitPos + 1));
			HandType ht = determineHandType(c);

			Hand hand = new Hand(c, b, ht);
			System.out.format("imported hand: %s\n", hand);
			return hand;
		}
	}

	private static class Game {
		public final List<Hand> 	hands;

		private Game(List<Hand> h) {
			hands = h;
		}

		public String toString() {
			return String.format("[g: hands=%s]", hands);
		}

		public long play() {
			long total_winnings = 0;
			for (int i = 0 ; i < hands.size() ; i++) {
				Hand hand = hands.get(i);
				int rankno = i + 1;
				long winnings = hand.bet * rankno;
				total_winnings += winnings;
				System.out.format("playing rank=%d, hand=%s, winnings=%d, total=%d\n", 
					rankno, hand, winnings, total_winnings);
			}
			return total_winnings;
		}

		public static int getCardRank(char c) {
			switch (c) {
				case '9':
				case '8':
				case '7':
				case '6':
				case '5':
				case '4':
				case '3':
				case '2':
					return c - '0';
				case 'T':
					return 10;
				case 'J':
					return part2 ? 1 : 11;
				case 'Q':
					return 12;
				case 'K':
					return 13;
				case 'A':
					return 14;
				default:
					throw new RuntimeException("bad card");
			}
		
		}

		public static int compareCards(char c1, char c2) {
			if (c1 == c2)
				return 0;
			return getCardRank(c1) - getCardRank(c2);
			
		}

		public static Comparator<Hand> standardCompare = new Comparator<Hand>() {
			@Override
			public int compare(Hand h1, Hand h2) {
				int htcompare = h1.handType.compareTo(h2.handType);
				if (htcompare != 0) {
					//System.out.format("sorting %d for %s %s\n", htcompare, h1, h2);
					return htcompare * -1;
				}

				for (int i = 0 ; i < 5 ; i++) {
					int ccompare = Game.compareCards(h1.cards.charAt(i), h2.cards.charAt(i));
					if (ccompare != 0) {
						//System.out.format("sorting %d at pos %d for %s %s\n", ccompare, i, h1, h2);
						return ccompare;
					}
				}

				//System.out.format("sorting EQUAL for %s %s\n", h1, h2);
				return 0;
			}
		};

		public static Game parse(BufferedReader br) throws IOException {
			List<Hand> h = new ArrayList<Hand>();

			Hand hand;
			while ((hand = Hand.parse(br)) != null) {
				h.add(hand);
			}
			h.sort(standardCompare);

			Game game = new Game(h);
//			System.out.format("imported game: %s\n", game);
			return game;
		}
	}

	public static void main(String[] args) throws Exception { 
		long now = System.currentTimeMillis();
    	System.out.format("Day Seven, Yo!\n\n");

        //String filename = "input/day07.sample";
        String filename = "input/day07.input";       	
       	File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
		Game game = Game.parse(br);
		long answer = game.play();


		long done = System.currentTimeMillis();
		System.out.format("answer: %d\n", answer);
		System.out.format("execution: %d\n", (done - now));
	}
}
