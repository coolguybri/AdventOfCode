
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

public class Day17 {

	private static int DAY_NUM = 17;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = false;
	private static boolean part2 = !part1;

	private static int numThreads = 16;
	private static ExecutorService executor;
	private static long chunkSize = 1000000;
//	private static long chunkSize = 1000;
	private static AtomicLong nextChunk = new AtomicLong(0);
	private static AtomicBoolean resultFound = new AtomicBoolean(false);

	private static int REPORT_INTERVAL = 1000000;
	//private static int REPORT_INTERVAL = 1;

	public static long parseCombo(int combo, long a, long b, long c) throws Exception {
		switch (combo) {
			case 0:
			case 1:
			case 2:
			case 3:
				return combo;

			case 4:
				return a;
			case 5:
				return b;
			case 6:
				return c;
			default:
				throw new Exception(String.format("Yo bad input: %c", combo));
		}
	}


	private static class Machine {
		protected int inst_ptr;
		protected long register_a;
		protected long register_b;
		protected long register_c;
		protected List<String> output;
		protected List<Integer> instructions;

		public Machine(long seedA, long seedB, long seedC, List<Integer> instrList) {
			inst_ptr = 0;
			register_a = seedA;
			register_b = seedB;
			register_c = seedC;
			output = new ArrayList<String>();
			instructions = instrList;
		}

		public void process() throws Exception {
			//System.out.format("process [%d] start\n", Thread.currentThread().threadId());

			for (long numop = 0 ; ; numop++) {

				if ((inst_ptr < 0) || (inst_ptr > (instructions.size() - 2))) {
					// HALT.
					//System.out.format("process [%d] HALT due to instruction OOB (%d)\n", 
				//		Thread.currentThread().threadId(), inst_ptr);
					break;
				}

				int opcode = instructions.get(inst_ptr++);
				int operandRaw = instructions.get(inst_ptr++);
				//System.out.format("process [%d] op=%d, operand=%d\n",
				//	 Thread.currentThread().threadId(), opcode, operandRaw);

				switch(opcode) {
					case 0: {
						long operand = parseCombo(operandRaw, register_a, register_b, register_c);
						long numerator = register_a;
						long denominator = 1 << operand;
						long result = numerator / denominator;
						register_a = result;
						//System.out.format("[%d] op: ADV:%d (%d / %d = %d)\n", 
					//		Thread.currentThread().threadId(), operand, numerator, denominator, result);
						break;
					}

					case 1: {
						long operand = register_b;
						long operand2 = operandRaw;
						long result = operand ^ operand2;
						register_b = result;
						//System.out.format("[%d] op: BXL (%d ^ %d = %d)\n", 
						//	Thread.currentThread().threadId(), operand, operand2, result);
						break;
					}

					case 2: {
						long operand = parseCombo(operandRaw, register_a, register_b, register_c);
						long result = operand % 8;
						register_b = result;
						//System.out.format("[%d], op: BST (%d = %d)\n", 
						//	Thread.currentThread().threadId(), operand, result);
						break;
					}

					case 3: {
						if (register_a == 0) {
							//System.out.format("[%d] op: JNZ when A==0\n",
							//	 Thread.currentThread().threadId());
							break;
						}
						long operand = operandRaw;
						inst_ptr = (int) operand;
						//System.out.format("[%d] op: JNZ to %d\n", 
						//	Thread.currentThread().threadId(), inst_ptr);
						break;
					}

					case 4: {
						long operand = register_b;
						long operand2 = register_c;
						long result = operand ^ operand2;
						register_b = result;
						//System.out.format("[%d] op: BXC (%d ^ %d = %d)\n", 
						//	Thread.currentThread().threadId(), operand, operand2, result);
						break;
					}

					case 5: {
						long operand = parseCombo(operandRaw, register_a, register_b, register_c);
						long result = operand % 8;
						output.add(Integer.toString((int) result));
						//System.out.format("[%d]  op: OUT (%d), now=%s\n", 
						//	Thread.currentThread().threadId(), result, output);
						break;
					}

					case 6: {
						long operand = parseCombo(operandRaw, register_a, register_b, register_c);
						long numerator = register_a;
						long denominator = 1 << operand;
						long result = numerator / denominator;
						register_b = result;
						//System.out.format("[%d] op: BDV:%d (%d / %d = %d)\n", 
						//	Thread.currentThread().threadId(), operand, numerator, denominator, result);
						break;
					}

					case 7: {
						long operand = parseCombo(operandRaw, register_a, register_b, register_c);
						long numerator = register_a;
						long denominator = 1 << operand;
						long result = numerator / denominator;
						register_c = result;
						//System.out.format("[%d] op: CDV:%d (%d / %d = %d)\n", 
						//	Thread.currentThread().threadId(), operand, numerator, denominator, result);
						break; 	
					}
				}
			}
		}
	}

		
	/*
	*/
	public static boolean evaluate(Machine m, String desiredOutput) throws Exception {

		m.process();
		String szoutput = String.join(",", m.output);
		//System.out.format(" processing complete, output=%s\n", szoutput);
		
		if (!szoutput.equals(desiredOutput)) {
			return false;
		}

		System.out.format("anlysis [%d]: GOt IT=%s\n", Thread.currentThread().threadId(), szoutput);
		resultFound.set(true);
		return true;
	}


	/*
	*/
	public static boolean evaluateBatch(FileParser.InstructionParser fp, long chunkStart, long chunkSize) throws Exception {
		long chunkEnd = chunkStart + chunkSize;
		String desiredOutput = fp.instructions.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));
		System.out.format("analysis-chunk [%d] start: looking %d-%d for %s\n", 
			Thread.currentThread().threadId(), chunkStart, chunkEnd, desiredOutput);

		for (long run = chunkStart ; run < chunkEnd ; run++) { 
			if ((run % REPORT_INTERVAL) == 0) {
				System.out.format("analysis-chunk [%d]: run=%d\n", 
					Thread.currentThread().threadId(), run);
			}

			Machine m = new Machine(run, fp.register_b, fp.register_c, fp.instructions);
			if (evaluate(m, desiredOutput)) {
				System.out.format("analysis-chunk [%d] find: run=%d\n",
					 Thread.currentThread().threadId(), run);
				return true;
			}
		}

		// if we got here, we did not find it in this chunk.
		return false;
	}

	/*
	*/
	public static void queueBatch(FileParser.InstructionParser fp, long chunkStart, long chunkSize) {
		System.out.format("[%d] queue: queueing chunk %d (%d)\n",
			Thread.currentThread().threadId(), chunkStart, chunkSize);

		executor.submit(new Runnable() {
                 	public void run() {
				System.out.format("[%d] queue: executing chunk %d (%d)\n",
					Thread.currentThread().threadId(), chunkStart, chunkSize);

				try {
					if (!evaluateBatch(fp, chunkStart, chunkSize)) {
						System.out.format("[%d] queue: finished executing chunk %d with no results, getting next chunk\n",
							Thread.currentThread().threadId(), chunkStart);

						if (!resultFound.get()) {
							long chunk = nextChunk.getAndIncrement();
							queueBatch(fp, (chunk * chunkSize), chunkSize);
						}
						return;
					}
				} catch (Exception e) {
					System.out.format("[%d] queue: error executing chunk %d: %s\n",
						Thread.currentThread().threadId(), chunkStart, e);
					e.printStackTrace();
				}

				// We got it!
				System.out.format("[%d] process: FOUND on chunk=%d\n",
					 Thread.currentThread().threadId(), chunkStart);
				executor.shutdown();
			}
		});
	}


	/*
	*/
	public static void main(String[] args) throws Exception { 

 		// Import the data; will throw if no file or data makes no sense.
                FileParser.InstructionParser fp = new FileParser.InstructionParser(DAY_NUM, SAMPLE_INPUT);
                fp.parse();

		/* Init the threadpool and dispatch the inital chunks to it. */
		executor = Executors.newFixedThreadPool(numThreads);              
		for (int t = 0 ; t < numThreads ; t++) { 
			long chunk = nextChunk.getAndIncrement();
			queueBatch(fp, (chunk * chunkSize), chunkSize);
		}

		// WAIT for all threads to end
                try {
                        System.out.format("[%d] dispatch: waiting for unfinished tasks\n",
                                Thread.currentThread().threadId());
                        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.format("[%s] dispatch: All tasks have finished\n", Thread.currentThread().threadId());
	}
}
