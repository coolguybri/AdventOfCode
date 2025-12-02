
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

public class Day21 {

	private static int DAY_NUM = 21;
	private static boolean SAMPLE_INPUT = true;
	private static boolean part1 = true;
	private static boolean part2 = !part1;

	private static int numThreads = 1;
	private static long REPORT_INTERVAL = 100000;

	private static AtomicInteger fileseq = new AtomicInteger(0);

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

		private static HashMap<String, List<String>> permutationCache = new HashMap<String, List<String>>();
	
		// Revised verion of Heap’s algorithm for generating permutations from chunks
		private static long generatePermutationsForChunks(List<List<String>> collector, BufferedWriter writer) {

			long writeCount = 0;

			// Build index for all chunks.
			int collectorLen = collector.size();
			int[] chunkIndexes = new int[collectorLen];
			for (int i = 0; i < collectorLen; i++) {
			    chunkIndexes[i] = 0;
			}

			// Determine the maximum length of the stringbuffer that we will need.
			int maxItemLen = 0;
			int maxItemCount = 1;
			StringBuilder tempSb = new StringBuilder();
			for (int c = 0 ; c < collectorLen ; c++) {
				List<String> chunk = collector.get(c);
				tempSb.append(String.format("[%d]", chunk.size()));
				int maxLenInChunk = 0;
				for (String item : chunk) {
					int slen = item.length();
					if (slen > maxLenInChunk) {
						maxLenInChunk = slen;
					}
				}
				maxItemLen += maxLenInChunk;
				maxItemCount *= chunk.size();
			}
			//System.out.format("[%s] perForChunk-start: numchunks=%d, maxItemLen=%d, maxItemCount=%d, chunkspec:%s\n",
			//	Thread.currentThread().threadId(), collector.size(), maxItemLen, maxItemCount, tempSb.toString());

			//var outputList = new ArrayList<String>(maxItemCount);
			StringBuilder sb = new StringBuilder(maxItemLen);
			//char[] sb = new char[maxItemLen];
			//int sbpos = 0;
		
			while (true) {
				// Add the current item.
				sb.setLength(0);
				//sbpos = 0;
				for (int c = 0 ; c < collectorLen ; c++) {
					List<String> chunk = collector.get(c);
					int thisIndex = chunkIndexes[c];
					String item = chunk.get(thisIndex);
					sb.append(item);
				}

				 //System.out.format("[%s] perForChunk-item: outputlen=%d\n",
				//	Thread.currentThread().threadId(), sb.length());
				String fullItem = sb.toString();
				//outputList.add(fullItem);	
				writer.write(fullItem);
				writer.newLine();
				writeCount++;

				// Incrment the counters.
				int chunkToIncrment = collectorLen - 1;
				for ( ; chunkToIncrment >= 0 ; chunkToIncrment--) {
					int lastIndex = chunkIndexes[chunkToIncrment];
					int lastMax = collector.get(chunkToIncrment).size();

					lastIndex++;
					if (lastIndex < lastMax) {
						chunkIndexes[chunkToIncrment] = lastIndex;
						// System.out.format("[%s] counter: incremented index chunkToIncrment=%d to %d, needshift=%b\n",
						//	Thread.currentThread().threadId(), chunkToIncrment, lastIndex, false);
						break;
					}

					lastIndex = 0;
					chunkIndexes[chunkToIncrment] = 0;
					//System.out.format("[%s] counter: incremented index chunkToIncrment=%d to %d, needshift=%b\n",
					 //       Thread.currentThread().threadId(), chunkToIncrment, lastIndex, true);
				}

				if (chunkToIncrment < 0) {
				//	System.out.format("[%s] counter: breaking out with chunkToIncrment=%d!\n",
				//		Thread.currentThread().threadId(), chunkToIncrment);
					break;
				}
			}

			//System.out.format("[%s] perForChunk-end with %d entries\n",
			//	Thread.currentThread().threadId(), outputList.size());
			return writeCount;
		}


		// Heap’s algorithm for generating permutations	
		private static List<String> generatePermutationsAndSuffix(String input) {

			List<String> cacheResult = permutationCache.get(input);
			if (cacheResult != null) {
				return cacheResult;
			}

			var outputSet = new HashSet<String>();
			outputSet.add(input + 'A');

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
				outputSet.add(new String(elements) + 'A');
				indexes[i]++;
				i = 0;
			    }
			    else {
				indexes[i] = 0;
				i++;
			    }
			}

			var outputList = new ArrayList<String>(outputSet);
			System.out.format("[%s] generated permutations for %s: %s\n",
				Thread.currentThread().threadId(), input, outputList);
			permutationCache.put(input, outputList);
			return outputList;
		}


		/*
		*/
		public String generateSequenceFor(String inputFile) throws Exception {

			var reader =  new BufferedReader(new FileReader(inputFile));
			long readCount = 0;

			String outputFile = String.format("tmp/yo-%000d.list", fileseq.getAndIncrement());
			var writer =  new BufferedWriter(new FileWriter(outputFile));
			long writeCount = 0;

			String input = null;
			while ((input = reader.readLine()) != null) {
				readCount += 1;
				FileParser.Pos currPos = posMap.get('A');

				// Generate all the permutations that would generate that sequence. This collector will have a list for every input
				// character, representing the permutations of stroked to transition from one key to another.
				var collector = new ArrayList<List<String>>();
				char[] scratchBuf = new char[20];
				for (Character c : input.toCharArray()) {
					FileParser.Pos nextPos = posMap.get(c);
					int scratchPos = 0;
					// System.out.format("[%s] %s: processing %c; from %s to %s\n",
					// Thread.currentThread().threadId(), getName(), c, currPos, nextPos);

					//String pressesForThisOne = "";
					int xDelta = nextPos.x - currPos.x;
					for (int i = xDelta ; i < 0 ; i++) {
						scratchBuf[scratchPos++] = '<';
					}
					for (int i = 0 ; i < xDelta ; i++) {
						scratchBuf[scratchPos++] = '>';
					}
					int yDelta = nextPos.y - currPos.y;
					for (int i = yDelta ; i < 0 ; i++) {
						scratchBuf[scratchPos++] = '^';
					}
					for (int i = 0 ; i < yDelta ; i++) {
						scratchBuf[scratchPos++] = 'v';
					}

					// Gernerate all the output sequences for this set.
					List<String> permutations = generatePermutationsAndSuffix(new String(scratchBuf, 0, scratchPos));

					// System.out.format("[%s] %s: generated %s: permutations (%d) %s\n",
					// Thread.currentThread().threadId(), getName(), pressesForThisOne, permutations.size(), permutations);

					currPos = nextPos;
					collector.add(permutations);
				}

				// Convert the chunks recorded above to a list of actual permutations.
				long genlen = generatePermutationsForChunks(collector, writer);
				writeCount += genlen;
				System.out.format("[%s] generateSequenceFor loop with %d entries (total=%d)\n",
					Thread.currentThread().threadId(), genlen, writeCount);
				
			}


			System.out.format("[%s] generateSequenceFor end with %d entries into file %s\n",
				Thread.currentThread().threadId(), writeCount, outputFile);
			reader.close();
			writer.close();
			return outputFile;
		}
	}

	public static class DirectionalPadToNumericPad extends DirectionalPad {

		private static FileParser.Pos DEAD_SPOT = new FileParser.Pos(0, 3);

		@Override
		public String getName() { return "dpad->numpad"; }

		@Override
		public boolean posIsIllegal(FileParser.Pos tracePos) {
			return tracePos.equals(DEAD_SPOT);
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

		private static FileParser.Pos DEAD_SPOT = new FileParser.Pos(0, 0);

		private int dpadid;

		@Override
		public String getName() { return String.format("dpad->dpad (%d)", dpadid); }

		@Override
		public boolean posIsIllegal(FileParser.Pos tracePos) {
			return tracePos.equals(DEAD_SPOT);
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

			//int additional = part1 ? 2 : 25;
			int additional = 3;
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

			// Seed the first inputsequence into an init file.
			String initReadFile = String.format("tmp/init-%000d.list", fileseq.getAndIncrement());
			{
				var writer =  new BufferedWriter(new FileWriter(initReadFile));
				writer.write(sequenceForBackEnd);
				writer.newLine();
				writer.close();
			}

			String currFile = initReadFile;
			for (var dpad : list) {
				String newFile = dpad.generateSequenceFor(currFile);

				int maxlen = 0;
				int minlen = Integer.MAX_VALUE;
				int fileLineCount = 0;
				String sample = null;
				{
					Reader reader =  new BufferedReader(new FileReader(currFile));
					String contender = null;
					while ((contender = reader.readLine()) != null) {
						if (sample == null) {
							sample = contender;
						}
						if (contender.length() > maxlen)
							maxlen = contender.length();
						if (contender.length() < minlen)
							minlen = contender.length();
						fileLineCount++;
					}
				}

				//System.out.format("[%s] chain: updating-a %d (%s) -> %d (%s) (by %s), max=%d, min=%d\n",
				//	Thread.currentThread().threadId(), fileLineCount, sample,
				//	newList.size(), newList.get(0), dpad.getName(), maxlen, minlen);
				System.out.format("[%s] chain: updating-a %d (%s) (by %s), max=%d, min=%d\n",
					Thread.currentThread().threadId(), fileLineCount, sample,
					dpad.getName(), maxlen, minlen);

				long dropped = 0;
				long notsafe = 0;
				String safeFile = String.format("tmp/safe-%000d.list", fileseq.getAndIncrement());
				{
					var writer =  new BufferedWriter(new FileWriter(safeFile));
					var reader =  new BufferedReader(new FileReader(currFile));
					String contender = null;
					while ((contender = reader.readLine()) != null) {

						int slen = contender.length();
						if ((slen - minlen) > 2) { // prev 6
							/*System.out.format("[%s] chain: dropping %s with len=%d, max=%d, min=%d\n",
								Thread.currentThread().threadId(), contender,
								slen, maxlen, minlen); */
							dropped++;
							continue;
						} 

						// Verify that the psth does not hit the empty spot.
						var tracePos = dpad.posMap.get('A');
						boolean coolToAdd = true;
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
								throw new Exception(String.format("crap, validation failed with %s", traceChar));
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
						} else {
							notsafe++;
						}
					}
				}

				System.out.format("[%s] chain: updating-b %d (%s) -> %d (%s) (by %s), max=%d, min=%d, dropped=%d, notsfae=%d\n",
					Thread.currentThread().threadId(), currList.size(), currList.get(0),
					newListSafe.size(), newListSafe.get(0), dpad.getName(), maxlen, minlen, dropped, notsafe);

				currList = newListSafe;
			}

			// Find the ones with the shortest keystrokes.
			int shortestTime = -1;
			String shortestSequence = null;
			for (String contender : currList) {
				if ((shortestTime < 0) || (contender.length() < shortestTime)) {
					shortestTime = contender.length();
					shortestSequence = contender;
				}
			}

			System.out.format("[%s] chain: found shortest time: %d:%s\n",
				Thread.currentThread().threadId(), shortestSequence.length(), shortestSequence);
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
