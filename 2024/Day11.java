
import java.io.*;
import java.util.*;

public class Day11 {

	private static int DAY_NUM = 11;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = true;
	private static boolean part2 = !part1;

	private static int MAX_BATCH = 100000000;

	public static int serialize(List<Long> currStones, int blink, int nextOutputPart) {
		int currPos = 0;
		int numParts = 0;
		for ( ; currPos < currStones.size() ; numParts++) {
			int partno = nextOutputPart + numParts;
			String outputFile = String.format("output/%02d-%04d.blink", blink, partno);
			System.out.format(" serialize: open file %s, already-written=%d\n", outputFile, currPos);

			try {
				FileOutputStream fos = new FileOutputStream(outputFile);
				ObjectOutputStream oos = new ObjectOutputStream(fos); 
				for (int i = 0 ; (i < MAX_BATCH) && (currPos < currStones.size()) ; i++) {
					Long val = currStones.get(currPos++);
					oos.writeObject(val);
					//System.out.format(" serialize: wrote %d, i=%d, currPos=%d\n", val, i, currPos);
				}
				fos.close();
			} catch (Exception e) {
				System.out.format("Error while writing data : %s\n", e);
				throw new RuntimeException(e);
			}
		}

		System.out.format(" serialize: complete numWritten=%d, numParts=%d\n", currPos, numParts);
		return nextOutputPart + numParts;
	}
		


	/*
	*/
	public static void main(String[] args) throws Exception { 
	
		long startTime = System.currentTimeMillis();

		int numBlinks = Integer.parseInt(args[0]);
		int startBlink = 1;
		if (args.length > 1) {
			startBlink = Integer.parseInt(args[1]);
		}

		if (startBlink == 1) {
			FileParser.ListLongListParser fp = new FileParser.ListLongListParser(DAY_NUM, SAMPLE_INPUT);
			fp.parse();
			List<Long> initStones = fp.getList().get(0);
			System.out.format("Analysis start: initial list: %d %s\n", initStones.size(), initStones);
			serialize(initStones, 1, 0);
		}

		int inputPartno = 0;
		int lastOutputPart = 0;
		long blinkStones = 0;
		for (int blink = startBlink ; blink < (numBlinks + startBlink) ; ) {
			List<Long> currStones;
			System.out.format("Process chunk loop: blink=%d, part=%d\n",
				 blink, inputPartno);
			{	
				// if the next part does not exist, go to the next blink.
				String inputFile = String.format("output/%02d-%04d.blink", blink, inputPartno);
				if (!(new File(inputFile).exists())) {
					blink++;
					blinkStones = 0;
					inputPartno = 0;
					lastOutputPart = 0;
					System.out.format("Process chunk end, moving to: blink=%d, inputPart=%d\n",
						 blink, inputPartno);
					continue;
				}

				currStones = new ArrayList<Long>();
				try {
					FileInputStream fos = new FileInputStream(inputFile);
					ObjectInputStream oos = new ObjectInputStream(fos); 
					while (true) {
						currStones.add((Long) oos.readObject());
					}
				} catch (EOFException f) {
					System.out.format("  eof after reading data : %d\n", currStones.size());
				} catch (Exception e) {
					System.out.format("Error while reading data : %s\n", e);
					throw new RuntimeException(e);
				}

				System.out.format("Process chunk read: blink=%d, part=%d, from %s, %d stones\n",
					 blink, inputPartno, inputFile, currStones.size());
			}

			System.out.format("Process chunk start: blink=%d, inputPart=%d, %d stones, [start=%d,num=%d]\n",
				 blink, inputPartno, currStones.size(), startBlink, numBlinks);

			long chunkStartTime = System.currentTimeMillis();
			List<Long> nextStones = new ArrayList<Long>(currStones.size() * 2);
			long pos = 0;
			for (Iterator<Long> posIter = currStones.iterator() ; posIter.hasNext() ; pos++) {
				long stone = posIter.next();
				if (stone == 0) {
					nextStones.add(1L);
					//System.out.format("  blink %d pos %d rule 1: %s -> %s\n", blink, pos, currStones, nextStones);
				} else {
					String str = String.format("%d", stone);
					int len = str.length();
					if ((len % 2) == 0) {
						Long stoneLeft = Long.parseLong(str.substring(0, len / 2));
						Long stoneRight = Long.parseLong(str.substring(len / 2));
						nextStones.add(stoneLeft);
						nextStones.add(stoneRight);
					//	System.out.format("  blink %d pos %d rule 2: %s -> %s\n", blink, pos, currStones, nextStones);
					} else {
						nextStones.add(stone * 2024);
					//	System.out.format("  blink %d pos %d rule default: %s -> %s\n", blink, pos, currStones, nextStones);
					}
				}
			}
				
			//System.out.format("blink %d/%d complete: %d %s\n", blink, inputPartno, nextStones.size(), nextStones);
			long chunkEndTime = System.currentTimeMillis();
			double chunkTime = (chunkEndTime - chunkStartTime) / 1000.0;
			double rate = nextStones.size() / chunkTime;
			blinkStones += nextStones.size();
			System.out.format("blink %d/%d complete: %d stones, %d for whole blink [%.02f](%.02f)\n",
				 blink, inputPartno, nextStones.size(), blinkStones, chunkTime, rate);

			// Serialize
			lastOutputPart = serialize(nextStones, blink+1, lastOutputPart);
			inputPartno++;
		}

		long endTime = System.currentTimeMillis();
		System.out.format("complete: time=%.02f\n", (endTime - startTime) / 1000.0);
	}
}
