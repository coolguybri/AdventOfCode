
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Day11b {

	private static int DAY_NUM = 11;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = true;
	private static boolean part2 = !part1;

	private static int MAX_BATCH = 10000000;
	private static String OUTPUTDIR = "output-c";
	//private static int MAX_BATCH = 100000;

	private static AtomicLong blinkInCount = new AtomicLong(0);
	private static AtomicLong blinkOutCount = new AtomicLong(0);
//	private static AtomicLong stoneValueLargest = new AtomicLong(0);
	private static long stoneValueLargest = 0;

	public static int serialize(List<Long> currStones, int blink, String label) {
		long writeStartTime = System.currentTimeMillis();
		int currPos = 0;
		int numParts = 0;
		for ( ; currPos < currStones.size() ; numParts++) {
			String outputFile = String.format("%s/%02d%s-%04d.blink", OUTPUTDIR, blink, label, numParts);
			System.out.format("[%d] serialize: open file %s, already-written=%d\n", 
				Thread.currentThread().getId(), outputFile, currPos);

			try {
				FileOutputStream fos = new FileOutputStream(outputFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				GZIPOutputStream gos = new GZIPOutputStream(bos);
				ObjectOutputStream oos = new ObjectOutputStream(gos); 
				for (int i = 0 ; (i < MAX_BATCH) && (currPos < currStones.size()) ; i++) {
					Long val = currStones.get(currPos++);
					oos.writeObject(val);
				//	System.out.format("[%s] serialize: wrote %d, i=%d, currPos=%d\n",
				//		 Thread.currentThread().getId(), val, i, currPos);
				}
				bos.flush();
				gos.flush();
				gos.close();
				fos.close();
			} catch (Exception e) {
				System.out.format("[%d] Error while writing data : %s\n",	
					 Thread.currentThread().getId(), e);
				throw new RuntimeException(e);
			}
		}

		long writeEndTime = System.currentTimeMillis();
		float writeDuration = (writeEndTime - writeStartTime) / 1000.0f;
		System.out.format("[%d] serialize [%.02f]: complete numWritten=%d, numParts=%d\n", 
			Thread.currentThread().getId(), writeDuration, currPos, numParts);
		return numParts;
	}
		
	public static void processStones(int blinkNo, int partNo) {

		long readStartTime = System.currentTimeMillis();
		String inputFile = String.format("%s/%02d-%04d.blink", OUTPUTDIR, blinkNo, partNo);
		System.out.format("[%d] Process stone begin loop: blink=%d, part=%d, importfile=%s\n", 
			Thread.currentThread().getId(), blinkNo, partNo, inputFile);

		List<Long> currStones = new ArrayList<Long>();
		try {
			FileInputStream fis = new FileInputStream(inputFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			GZIPInputStream gis = new GZIPInputStream(bis);
			ObjectInputStream oos = new ObjectInputStream(gis); 
			while (true) {
				currStones.add((Long) oos.readObject());
			}
		} catch (EOFException f) {
			System.out.format("[%d] clean eof after reading data : %d\n", Thread.currentThread().getId(), currStones.size());
		} catch (Exception e) {
			System.out.format("Error while reading from %s : %s\n", inputFile, e);
			throw new RuntimeException(e);
		}

		long readEndTime = System.currentTimeMillis();
		float readDuration = (readEndTime - readStartTime) / 1000.0f;
		System.out.format("[%d] Process stone read-complete [%.02f]: blink=%d, part=%d, from %s, %d stones\n",
			 Thread.currentThread().getId(), readDuration, blinkNo, partNo, inputFile, currStones.size());
		blinkInCount.addAndGet(currStones.size());

		long chunkStartTime = System.currentTimeMillis();
		List<Long> nextStones = new ArrayList<Long>(currStones.size() * 2);
		long pos = 0;
		for (Iterator<Long> posIter = currStones.iterator() ; posIter.hasNext() ; pos++) {
			long stone = posIter.next();

			if (stone > stoneValueLargest)
				stoneValueLargest = stone;

			if (stone == 0) {
				nextStones.add(1L);
				//System.out.format("  blink %d pos %d rule 1: %s -> %s\n", blinkNo, pos, currStones, nextStones);
			} else {
				String str = String.format("%d", stone);
				int len = str.length();
				if ((len % 2) == 0) {
					Long stoneLeft = Long.parseLong(str.substring(0, len / 2));
					Long stoneRight = Long.parseLong(str.substring(len / 2));
					nextStones.add(stoneLeft);
					nextStones.add(stoneRight);
				//	System.out.format("  blink %d pos %d rule 2: %s -> %s\n", blinkNo, pos, currStones, nextStones);
				} else {
					nextStones.add(stone * 2024);
				//	System.out.format("  blink %d pos %d rule default: %s -> %s\n", blinkNo, pos, currStones, nextStones);
				}
			}
		}
			
		//System.out.format("[%d] blink %d/%d complete: %d %s\n", 
		//	Thread.currentThread().getId(), blinkNo, partNo, nextStones.size(), nextStones);
		long chunkEndTime = System.currentTimeMillis();
		float chunkDuration = (chunkEndTime - chunkStartTime) / 1000.0f;
		System.out.format("[%d] blink %d/%d complete [%.02f]: %d stones\n",
			 Thread.currentThread().getId(), blinkNo, partNo, chunkDuration, nextStones.size());
		blinkOutCount.addAndGet(nextStones.size());

		// Serialize
		serialize(nextStones, blinkNo, String.format("-%04d.tmp", partNo));
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
			System.out.format("[%d] Analysis start: initial list: %d %s\n",
				 Thread.currentThread().getId(), initStones.size(), initStones);
			serialize(initStones, 1, "");
		}


		int inputPartno = 0;
		for (int blink = startBlink ; blink < (numBlinks + startBlink) ; blink++) {

			ExecutorService executor = Executors.newFixedThreadPool(8);
			blinkInCount.set(0);
			blinkOutCount.set(0);
			List<File> inputFiles = new ArrayList<File>();

			// Look for input files for this blink.
			int partNo = 0;
			for ( ; ; partNo++) {
				String inputFile = String.format("%s/%02d-%04d.blink", OUTPUTDIR, blink, partNo);
				Integer blinkInt = Integer.valueOf(blink);
				Integer partInt = Integer.valueOf(partNo);
				File f = new File(inputFile);
				if (!f.exists()) {
					System.out.format("[%d] dispatch: blink %d part %d not found, finished dispatching workers\n", 
						Thread.currentThread().getId(), blink, partNo);
					break;
				}

				// save for cleanup.
				inputFiles.add(f);

				System.out.format("[%d] dispatch: queueing task for blink=%d, part=%d\n",
					 Thread.currentThread().getId(), blink, partNo);
				executor.submit(new Runnable() {
				    public void run(){ 
					System.out.format("[%d] dispatch: starting task for blink=%d, part=%d\n",
						 Thread.currentThread().getId(), blinkInt, partInt);
					try {
						processStones(blinkInt, partInt); 
					} catch (Exception e) {
						System.out.format("[%d] dispatch: excepotion in task for blink=%d, part=%d: %s\n",
							 Thread.currentThread().getId(), blinkInt, partInt, e);
					}
					System.out.format("[%d] dispatch: ending task for blink=%d, part=%d\n",
						 Thread.currentThread().getId(), blinkInt, partInt);
				}});
			}

			// WAIT for all threads to end
			try {
				System.out.format("[%d] dispatch: waiting for %d tasks\n", 
					Thread.currentThread().getId(), partNo);
				executor.shutdown();
			    	executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
		        System.out.format("[%s] dispatch: All %d tasks have finished\n", Thread.currentThread().getId(), partNo);

			// reorder all the files so the next blink reads them in the correct order.
			// Find all the part output files that may have been generated, and rename them so they can
			//  be impoted correctly by them next blink.`
			int nextOutputPart = 0;
			int nextBlink = blink+1;
			System.out.format("[%d] reorder: YO blink %d parts=%d in-stones=%d, out-stones=%d, largest=%d\n", 
				Thread.currentThread().getId(), blink, partNo, blinkInCount.get(), blinkOutCount.get(), stoneValueLargest);
			for (int p = 0 ; p < partNo ; p++) {
				System.out.format("[%d] reorder: blink %d part=%d starting\n", 
					Thread.currentThread().getId(), blink, p);
				for (int nextTempPart = 0; true ; nextTempPart++) {
					String seekFile = String.format("%s/%02d-%04d.tmp-%04d.blink", OUTPUTDIR, blink, p, nextTempPart);
					System.out.format("[%d] reorder: seeking %s\n", 
						Thread.currentThread().getId(), seekFile);
					if (!(new File(seekFile).exists())) {
						System.out.format("[%d] reorder: blink %d part %d tmp %d not found, finished dispatching workers\n", 
							Thread.currentThread().getId(), blink, partNo, nextTempPart);
						break;
					}

					// renane this to the next input file for the next blink.
					String newFile =  String.format("%s/%02d-%04d.blink", OUTPUTDIR, nextBlink, nextOutputPart++);
					if (!new File(seekFile).renameTo(new File(newFile))) {
						System.out.format("[%d] reorder: ERROR could not rename file %d %d %s %s\n", 
							Thread.currentThread().getId(), blink, partNo, seekFile, newFile);
					} else {
						System.out.format("[%d] reorder: success rename %s -> %s\n", 
							Thread.currentThread().getId(), seekFile, newFile);
					}
				}
			}

			// We made it! all the input was converted to output without an error or exception. Time to auto-delete the inpit.
			for (Iterator<File> remIter = inputFiles.iterator() ; remIter.hasNext() ; ) {
				File f = remIter.next();
				System.out.format("[%d] cleanup: starting delete for input %s\n", Thread.currentThread().getId(), f);
				if (f.delete()) {
					System.out.format("[%d] cleanup: success delete input %s\n", Thread.currentThread().getId(), f);
				} else {
					System.out.format("[%d] cleanup: FAIL delete input %s\n", Thread.currentThread().getId(), f);
				}
			}
				
		}

		long endTime = System.currentTimeMillis();
		System.out.format("complete: time=%.02f\n", (endTime - startTime) / 1000.0);
	}
}
