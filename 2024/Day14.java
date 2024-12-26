
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Day14 {

	private static int DAY_NUM = 14;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = false;
	private static boolean part2 = !part1;
	private static int numThreads = 1;

	private static int REPORT_INTERVAL = 1;

	private static int map_width = SAMPLE_INPUT ? 11 : 101;
	private static int map_height = SAMPLE_INPUT ? 7 : 103;


	/*
	*/
	public static void processRobot(FileParser.RobotParser.Robot robot, long runTime) {
	//	System.out.format("[%d] Process start: robot=%s, runtime=%d\n",
	//		 Thread.currentThread().threadId(), robot, runTime);

		// Iterate over every possibility of A, until the required B to sync up would be negative.
		long loop_start = 0;
		long loop_iter = 1;
		long startTime = System.currentTimeMillis();
		for (long a = loop_start, loop_count = 0 ; a < runTime ; a += loop_iter, loop_count++) {
			FileParser.Pos curr = robot.currPos;
			int nextX = curr.x + robot.velocity.x;
			while (nextX < 0) {
				nextX += map_width;
			}
			nextX %= map_width;
			int nextY = curr.y + robot.velocity.y;
			while (nextY < 0) {
				nextY += map_height;
			}
			nextY %= map_height;
			robot.currPos = new FileParser.Pos(nextX, nextY);

	/*		if ((loop_count % REPORT_INTERVAL) == 0) {
				long updateTime = System.currentTimeMillis();
				float elapsed = ((float) (updateTime - startTime)) / 1000.0f;
				System.out.format("[%d] Process loop: robot=%s, elaps=%.01f, iter=%d\n",
					Thread.currentThread().threadId(), robot, elapsed, a);
			} */
		}
		
		//System.out.format("[%d] Process end: YO YO YO robot=%s\n",
	//		 Thread.currentThread().threadId(), robot);
	}


	/*
	*/
	public static long computeSubRectangle(String name, int[][] snapshot, int xBegin, int xEnd, int yBegin, int yEnd) {
		long sum = 0;
		for (int x = xBegin ; x < xEnd ; x++) {
			for (int y = yBegin ; y < yEnd ; y++) {
				sum += snapshot[x][y];
			}
		}

		System.out.format("computeSubRectangle: %s: %d\n", name, sum);
		return sum;
	}


	/*
	*/
	public static int[][] snapshotCreate(List<FileParser.RobotParser.Robot> robots) {
		var snapshot = new int[map_width][map_height];
		for (Iterator<FileParser.RobotParser.Robot> robotIterator = robots.iterator() ; robotIterator.hasNext() ; ) {
			FileParser.RobotParser.Robot robot = robotIterator.next();
			int currVal = snapshot[robot.currPos.x][robot.currPos.y];
			int nextVal = currVal + 1;
			snapshot[robot.currPos.x][robot.currPos.y] = nextVal;
		}
		return snapshot;
	}

	/*
	*/
	public static void snapshotPrint(int[][] snapshot) {
		for (int h = 0 ; h < map_height ; h++) {
			for (int w = 0 ; w < map_width ; w++) {
				System.out.format("%d", snapshot[w][h]);
			}
			System.out.format("\n");
		}
	}


	/*
	*/
	public static void main(String[] args) throws Exception { 

		// Import the data; will throw if no file or data makes no sense.
		FileParser.RobotParser fp = new FileParser.RobotParser(DAY_NUM, SAMPLE_INPUT);
		fp.parse();

		List<FileParser.RobotParser.Robot> robots = fp.getList();
		System.out.format("Process init: robots=%d, part1=%b\n", robots.size(), part1);

		if (part1) {
			ExecutorService executor = Executors.newFixedThreadPool(numThreads);

			for (Iterator<FileParser.RobotParser.Robot> robotIterator = robots.iterator() ; robotIterator.hasNext() ; ) {
				FileParser.RobotParser.Robot robot = robotIterator.next();

				 executor.submit(new Runnable() {
					    public void run(){
						System.out.format("[%d] dispatch: starting task for robot=%s\n",
							 Thread.currentThread().threadId(), robot);
						try {
							processRobot(robot, 100);
						} catch (Exception e) {
							System.out.format("[%d] dispatch: exception in task for robot=%s: %s\n",
								 Thread.currentThread().threadId(), robot, e);
						}
						System.out.format("[%d] dispatch: ending task for robot=%s\n",
							 Thread.currentThread().threadId(), robot);
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

			// Convert robot list to a snapshot.
			var snapshot = snapshotCreate(robots);
			snapshotPrint(snapshot);

			long q1 = computeSubRectangle("q1",snapshot, 0, (map_width / 2), 0, (map_height / 2));
			long q2 = computeSubRectangle("q2", snapshot, (map_width / 2) + 1, map_width, 0, (map_height / 2));
			long q3 = computeSubRectangle("q3", snapshot, (map_width / 2) + 1, map_width, (map_height / 2) + 1, map_height);
			long q4 = computeSubRectangle("q4", snapshot, 0, (map_width / 2), (map_height / 2) + 1, map_height);
			long product = 1L;
			product *= q1;
			product *= q2;
			product *= q3;
			product *= q4;

			System.out.format("Process end:  YO YO YO safety-score=%d\n", product);
		} else {
			{
				var snapshot = snapshotCreate(robots);
				System.out.format("tree-search: init\n"); 
				snapshotPrint(snapshot);
			}


			int STEPS_MAX = Integer.MAX_VALUE;
			for (int steps = 0; steps < STEPS_MAX ; steps++) {
				for (Iterator<FileParser.RobotParser.Robot> robotIterator = robots.iterator() ; robotIterator.hasNext() ; ) {
					FileParser.RobotParser.Robot robot = robotIterator.next();
					processRobot(robot, 1L);
				}

				var snapshot = snapshotCreate(robots);
				System.out.format("tree-search: %d\n", steps); 
				snapshotPrint(snapshot);
				System.out.format("Press Enter to continue (snap %d)", steps);

				try{System.in.read();}
				catch(Exception e){}
			}
		}
	}
}
