
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

public class Day21 {

	private static int DAY_NUM = 21;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = false;
	private static boolean part2 = !part1;

	private static int numThreads = 1;
	private static long REPORT_INTERVAL = 100000;


	public static abstract class DirectionalPad {
		public HashMap<Character, FileParser.Pos>	posMap;

		public DirectionalPad() {
			posMap = new HashMap<Character, FileParser.Pos>();
		}

		public abstract String getName();
		public abstract boolean posIsIllegal(FileParser.Pos tracePos);

		private static void swap(char[] elements, int a, int b) {
		    char tmp = elements[a];
		    elements[a] = elements[b];
		    elements[b] = tmp;
		}
	
		// Heap’s algorithm for generating permutations	
		private static List<String> generatePermutations(String input) {

			var outputList = new ArrayList<String>();
			outputList.add(input);

			int inputLen = input.length();
			int[] indexes = new int[inputLen];
			for (int i = 0; i < inputLen; i++) {
			    indexes[i] = 0;
			}

			char[] elements = input.toCharArray();
			int i = 0;
			while (i < inputLen) {
			    if (indexes[i] < i) {
				swap(elements, i % 2 == 0 ?  0: indexes[i], i);
				outputList.add(new String(elements));
				indexes[i]++;
				i = 0;
			    }
			    else {
				indexes[i] = 0;
				i++;
			    }
			}

			return outputList;
		}

		/*
		*/
		public List<String> generateSequenceFor(List<String> inputList) throws Exception {

			List<String> outputList = new ArrayList<String>();

			for (var input : inputList) {
				FileParser.Pos currPos = posMap.get('A');

				// Generate all the permutations that would generate that sequence. This collector will have a list for every input
				// character, representing the permutations of stroked to transition from one key to another.
				var collector = new ArrayList<List<String>>();
				for (Character c : input.toCharArray()) {
					FileParser.Pos nextPos = posMap.get(c);
					// System.out.format("[%s] %s: processing %c; from %s to %s\n",
					// Thread.currentThread().threadId(), getName(), c, currPos, nextPos);

					String pressesForThisOne = "";
					int xDelta = nextPos.x - currPos.x;
					for (int i = xDelta ; i < 0 ; i++) {
						pressesForThisOne += '<';
					}
					for (int i = 0 ; i < xDelta ; i++) {
						pressesForThisOne += '>';
					}
					int yDelta = nextPos.y - currPos.y;
					for (int i = yDelta ; i < 0 ; i++) {
						pressesForThisOne += '^';
					}
					for (int i = 0 ; i < yDelta ; i++) {
						pressesForThisOne += 'v';
					}

					// Gernerate all the output sequences for this set.
					List<String> permutations = generatePermutations(pressesForThisOne);
					permutations = permutations.stream()
								.map(str -> (str + 'A'))
								.collect(Collectors.toList());
					Set<String> set = new HashSet<>(permutations);
					permutations.clear();
					permutations.addAll(set);

					// Verify that the psth does not hit the empty spot.
					/*var tracePos = currPos;
					List<String> safePermutations = new ArrayList<String>();
					for (var tracePerm : permutations) {
						boolean coolToAdd = true;
						for (var traceChar : tracePerm.toCharArray()) {
							switch (traceChar) {
							case '<':
								tracePos = tracePos.left();
								break;
							case '>':
								tracePos = tracePos.right();
								break;
							case '^':
								tracePos = tracePos.up();
								break;
							case 'v':
								tracePos = tracePos.down();
								break;
							case 'A':
								// cool, skip it.
								break;
							default:
								throw new Exception("crap, validation failed");
							}
							
							if (posIsIllegal(tracePos)) {
								coolToAdd = false;
								//System.out.format("[%s] %s: skipping due to bad keypad: (start=%s, bad=%s) %s\n",
								//  Thread.currentThread().threadId(), getName(), currPos, tracePos, tracePerm);
								break;
							}
						}
	
						if (coolToAdd) {
							safePermutations.add(tracePerm);
						}
					}
					permutations = safePermutations; */

					// System.out.format("[%s] %s: generated %s: permutations (%d) %s\n",
					// Thread.currentThread().threadId(), getName(), pressesForThisOne, permutations.size(), permutations);

					currPos = nextPos;
					collector.add(permutations);
				}

				// Convert the chunks recorded above to a list of actual permutations.
				List<String> prevList = new ArrayList<String>(collector.get(0));
				for (int c = 1 ; c < collector.size() ; c++) {
					List<String> currChunk = collector.get(c);
					List<String> nextList = new ArrayList<String>();
					for (String nextPermutation : currChunk) {
						for (String prevPermutation : prevList) {
							nextList.add(prevPermutation + nextPermutation);
						}
					}
					prevList = nextList;
				}

				outputList.addAll(prevList);
			}

			return outputList;
		}
	}

	public static class DirectionalPadToNumericPad extends DirectionalPad {

		@Override
		public String getName() { return "dpad->numpad"; }

		@Override
		public boolean posIsIllegal(FileParser.Pos tracePos) {
			return tracePos.equals(new FileParser.Pos(0, 3));
		}

		public DirectionalPadToNumericPad() {
			posMap.put('7', new FileParser.Pos(0, 0));
			posMap.put('8', new FileParser.Pos(1, 0));
			posMap.put('9', new FileParser.Pos(2, 0));
			posMap.put('4', new FileParser.Pos(0, 1));
			posMap.put('5', new FileParser.Pos(1, 1));
			posMap.put('6', new FileParser.Pos(2, 1));
			posMap.put('1', new FileParser.Pos(0, 2));
			posMap.put('2', new FileParser.Pos(1, 2));
			posMap.put('3', new FileParser.Pos(2, 2));
			posMap.put('0', new FileParser.Pos(1, 3));
			posMap.put('A', new FileParser.Pos(2, 3));
		}

	}

	public static class DirectionalPadToDirectionalPad extends DirectionalPad {

		private int dpadid;

		@Override
		public String getName() { return String.format("dpad->dpad (%d)", dpadid); }

		@Override
		public boolean posIsIllegal(FileParser.Pos tracePos) {
			return tracePos.equals(new FileParser.Pos(0, 0));
		}

		public DirectionalPadToDirectionalPad(int id) {
			dpadid = id;

			posMap.put('^', new FileParser.Pos(1, 0));
			posMap.put('A', new FileParser.Pos(2, 0));
			posMap.put('<', new FileParser.Pos(0, 1));
			posMap.put('v', new FileParser.Pos(1, 1));
			posMap.put('>', new FileParser.Pos(2, 1));
		}
	}

	public static class DirectionalPadChain {
		public List<DirectionalPad> list;

		public DirectionalPadChain() {
			list = new ArrayList<DirectionalPad>();
			list.add(new DirectionalPadToNumericPad());

			int additional = part1 ? 2 : 25;
			for (int i = 0 ; i < additional ; i++) {
				var dpad = new DirectionalPadToDirectionalPad(i+1);
				list.add(dpad);
				System.out.format("[%s] chain: adding %s\n",
					Thread.currentThread().threadId(), dpad.getName());
			}
		}

		public String generateBestSequenceFor(String sequenceForBackEnd) throws Exception {
			System.out.format("[%s] chain: processing %d keypads in the chain for %s\n",
				Thread.currentThread().threadId(), list.size(), sequenceForBackEnd);

			List<String> currList = new ArrayList<String>();
			currList.add(sequenceForBackEnd);
			for (var dpad : list) {
				List<String> newList = dpad.generateSequenceFor(currList);
				System.out.format("[%s] chain: updating %d (%s) -> %d (%s) (by %s)\n",
					Thread.currentThread().threadId(), currList.size(), currList.get(0),
					newList.size(), newList.get(0), dpad.getName());

				List<String> newListSafe = new ArrayList<String>();
				for (String contender : newList) {	
					boolean coolToAdd = true;

					// Verify that the psth does not hit the empty spot.
					var tracePos = dpad.posMap.get('A');
					for (var traceChar : contender.toCharArray()) {
						switch (traceChar) {
						case '<':
							tracePos = tracePos.left();
							break;
						case '>':
							tracePos = tracePos.right();
							break;
						case '^':
							tracePos = tracePos.up();
							break;
						case 'v':
							tracePos = tracePos.down();
							break;
						case 'A':
							// cool, skip it.
							break;
						default:
							throw new Exception("crap, validation failed");
						}
						
						if (dpad.posIsIllegal(tracePos)) {
							coolToAdd = false;
							//System.out.format("[%s] %s: skipping due to bad keypad: (bad=%s) %s\n",
							//  Thread.currentThread().threadId(), dpad.getName(), tracePos, contender);
							break;
						}
					}

					if (coolToAdd) {
						newListSafe.add(contender);
					}
				}

				currList = newListSafe;
			}

			// Find the ones witht he shortest keystrokes.
			int shortestTime = -1;
			String shortestSequence = null;
			for (String contender : currList) {

				if ((shortestTime < 0) || (contender.length() < shortestTime)) {
					shortestTime = contender.length();
					shortestSequence = contender;
					System.out.format("[%s] chain: found shortest time: %d:%s\n",
						Thread.currentThread().threadId(), contender.length(), contender);
				}
			}

			return shortestSequence;
		}
	}

	/*
	*/
	public static void main(String[] args) throws Exception { 

 		// Import the data; will throw if no file or data makes no sense.
                var fp = new FileParser.LineParser(DAY_NUM, SAMPLE_INPUT);
                fp.parse();
		List<String> inputCodes = fp.getList();
                System.out.format("[%s] Imported: %s\n", Thread.currentThread().threadId(), inputCodes);

		// Create the dpad chain.
		var chain = new DirectionalPadChain();

		long sum = 0;
		for (String doorCode : inputCodes) {
			// Just strip out the last letter; not part of official spec but matches all input.
			int numericPart = Integer.parseInt(doorCode.substring(0, doorCode.length() - 1));
			System.out.format("[%s] main: doorCode: %s, numeric=%d\n", Thread.currentThread().threadId(), doorCode, numericPart);

			String humanInput = chain.generateBestSequenceFor(doorCode);
			int buttonPresses = humanInput.length();
			long complexity = numericPart * buttonPresses;
			System.out.format("[%s] main: doorCode: %s, humanInput=%s, presses=%d, complexity=%d, sum=%d\n",
				 Thread.currentThread().threadId(), doorCode, humanInput, buttonPresses, complexity, sum);

			sum += complexity;
		}
                System.out.format("[%s] Total Sum of Complexities: %d\n", Thread.currentThread().threadId(), sum);
	}
}
