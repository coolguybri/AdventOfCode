
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Day13 {

	private static int DAY_NUM = 13;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = false;
	private static boolean part2 = !part1;

	private static AtomicLong tokens = new AtomicLong(0);


	public static long gcd(long a, long b) {
	   if (b==0) return a;
	   return gcd(b,a%b);
	}

	
	/*
	*/
	public static void processMachine(FileParser.ClawParser.ClawMachine machine) {
		long best_score = 0;
		long best_score_a = 0;
		long best_score_b = 0;

		long prizeX = machine.prize.x;
		long prizeY = machine.prize.y;
		if (part2) {
			long crazynum = 10000000000000L;
			prizeX += crazynum;
			prizeY += crazynum;
		}
		long prizeSum = prizeX + prizeY;
		long aSum = machine.a.x + machine.a.y;
		long bSum = machine.b.x + machine.b.y;
		long start = System.currentTimeMillis();

		long loop_start = 0;
		long loop_iter = 1;
		boolean jump_detected = false;
		System.out.format("[%d] Analysis start:  machine=%s, asum=%d, bsum=%d, psum=%d, loopStart=%d, loopIter=%d\n",
			 Thread.currentThread().threadId(), machine, aSum, bSum, prizeSum, loop_start, loop_iter);

		// Iterate over every possibility of A, until the required B to sync up would be negative.
		for (long a = loop_start, loop_count = 0 ; ; a += loop_iter, loop_count++) {
			if ((loop_count % 1000000000L) == 0) {
				long updateTime = System.currentTimeMillis();
				float elapsed = ((float) (updateTime - start)) / 1000.0f;
				System.out.format("[%d] Analysis loop: machine=%s, a=%d, elaps=%.01f, iter=%d, bestScore=%d (%d, %d)\n",
					Thread.currentThread().threadId(), machine, a, elapsed, loop_iter, best_score, best_score_a, best_score_b);
			}

			long numer = (prizeSum - (aSum * a));
			long b = numer / bSum;
			if (b < 0) {
				System.out.format("[%d] Analysis breakout: machine=%s, a=%d, b=%d, time to break out\n",
					 Thread.currentThread().threadId(), machine, a, b);
				break;
			}
			boolean clean = ((numer % bSum) == 0);
			if (jump_detected) {
				if (!clean) {
					System.out.format("[%d] Analysis ERROR: a=%d, b=%d, NOT CLEAN multiple: numer=%d, bSum=%d, div=%d, remainder=%d\n", 
						Thread.currentThread().threadId(), machine, a, b, numer, bSum, (numer / bSum), (numer % bSum));
					continue;
				}
			} else {
				if (clean) {

					System.out.format("[%d] Analysis detect: machine=%s, a=%d, SWITCH ITER=%d, div=%d, remainder=%d\n",
						Thread.currentThread().threadId(), machine, a, loop_iter, (numer / bSum), (numer % bSum));
					
					long aval = aSum;
					long bval = bSum;
					for (int i = 0 ;  ; i++) {
						long factor = gcd(aval, bval);
						if (factor == 1) {
							break;
						}
						aval /= factor;
						bval /= factor;
						System.out.format("[%d] Analysis yo: machine=%s, a=%d, SWITCH ITER FACTOR, factor=%d, aval=%d, bval=%d\n",
							Thread.currentThread().threadId(), machine, a, factor, aval, bval);
					}
					loop_iter = bval;

					jump_detected = true;
					long updateTime = System.currentTimeMillis();
					float elapsed = ((float) (updateTime - start)) / 1000.0f;
					System.out.format("[%d] Analysis loop: machine=%s, a=%d, elaps=%.01f, bestScore=%d (%d, %d), SWITCH ITER=%d, div=%d, remainder=%d\n",
						Thread.currentThread().threadId(), machine, a, elapsed, best_score, best_score_a, best_score_b, loop_iter, (numer / bSum), (numer % bSum));
				}
			}

			// check.
			boolean xEquation = ((a * machine.a.x) + (b * machine.b.x)) == prizeX;
			boolean yEquation = ((a * machine.a.y) + (b * machine.b.y)) == prizeY;
			if (!xEquation || !yEquation) {
				//System.out.format("combo: a=%d, b=%d, did not pass double check, %b, %b\n", a, b, xEquation, yEquation);
				continue;
			}

			long cost = (3 * a) + b;
			if ((best_score == 0) || (cost < best_score)) {
				best_score = cost;
				best_score_a = a;
				best_score_b = b;
				System.out.format("[%d] Analysis score: machine=%s,a=%d, b=%d, updated best score to %d\n",
					Thread.currentThread().threadId(), machine, a, b, cost);
			} else {
				System.out.format("[%d] Analysis score: machine=%s, a=%d, b=%d, found another, but cost %d is higher than best %d\n",
					Thread.currentThread().threadId(), machine, a, b, cost, best_score);
			}
		}

		
		long new_tokens = (best_score_a * 3) + best_score_b;
		long tot_tokens = tokens.addAndGet(new_tokens);
		System.out.format("[%d] Analysis end: YO YO YO machine=%s, best=%d, a=%d, b=%d, tokens=%d, total_tokens=%d\n",
			 Thread.currentThread().threadId(), machine, best_score, best_score_a, best_score_b, new_tokens, tot_tokens);
	}


	/*
	*/
	public static void main(String[] args) throws Exception { 

		// Import the data; will throw if no file or data makes no sense.
		FileParser.ClawParser fp = new FileParser.ClawParser(DAY_NUM, SAMPLE_INPUT);
		fp.parse();

		List<FileParser.ClawParser.ClawMachine> machines = fp.getList();
		System.out.format("Analysis init: claw machines=%d, part1=%b\n", machines.size(), part1);

		ExecutorService executor = Executors.newFixedThreadPool(10);

		for (Iterator<FileParser.ClawParser.ClawMachine> machineIterator = machines.iterator() ; machineIterator.hasNext() ; ) {
			FileParser.ClawParser.ClawMachine machine = machineIterator.next();

			 executor.submit(new Runnable() {
                                    public void run(){
                                        System.out.format("[%d] dispatch: starting task for machine=%s\n",
                                                 Thread.currentThread().threadId(), machine);
                                        try {
                                                processMachine(machine);
                                        } catch (Exception e) {
                                                System.out.format("[%d] dispatch: exception in task for machine=%s: %s\n",
                                                         Thread.currentThread().threadId(), machine, e);
                                        }
                                        System.out.format("[%d] dispatch: ending task for machine=%s\n",
                                                 Thread.currentThread().threadId(), machine);
                                }});


		}

		// WAIT for all threads to end
		try {
			System.out.format("[%d] dispatch: waiting for unfinished tasks\n",
				Thread.currentThread().threadId());
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		System.out.format("[%s] dispatch: All tasks have finished\n", Thread.currentThread().threadId());

		System.out.format("Analysis end:  YO YO YO endscore=%d\n", tokens.get());
	}
}
