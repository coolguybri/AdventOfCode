
import java.io.*;
import java.util.*;

public class Day08 {

    private static int DAY_NUM = 8;
    private static boolean SAMPLE_INPUT = false;

	/*
	*/
    public static void main(String[] args) throws Exception { 
		long startTime = System.currentTimeMillis();
		
		boolean part1 = false;
		
		// Import the data; will throw if no file or data makes no sense.
		var fp = new FileParser.Pos3Parser(DAY_NUM, SAMPLE_INPUT);
	    fp.parse();
	    
	    var distanceList = new ArrayList<DistanceRecord>();
	    
	    List<FileParser.Pos3> pointList = fp.getList();
	    int pointListSize = pointList.size();
	    
	    for (int i = 0 ; i < (pointListSize - 1) ; i++) {
	    	FileParser.Pos3 p1 = pointList.get(i);
	    	for (int j = (i + 1) ; j < pointListSize ; j++) {
	    		FileParser.Pos3 p2 = pointList.get(j);
	    		
	    		var dr = new DistanceRecord(p1, p2);
	    		distanceList.add(dr);
	    		System.out.format("distances: computed: %s\n", dr);
	    	}
	    }
	    
		Collections.sort(distanceList, (d1, d2) -> Double.compare(d1.distance, d2.distance)); 
		System.out.format("\ndistances sorted: %s\n\n", distanceList);
		
		var circuitByJunction = new HashMap<FileParser.Pos3, Set<FileParser.Pos3>>();
		var circuitSet = new HashSet<Set<FileParser.Pos3>>();
		
		// lets go for the first 10.
		int connectGoal = SAMPLE_INPUT ? 10 : 1000;
		if (!part1) connectGoal = Integer.MAX_VALUE;
		int uncreatedCircuits = pointList.size();
		
		for (int c = 0 ; c < connectGoal ; c++) {
			DistanceRecord dr = distanceList.get(c);
			System.out.format("connecting (%d) start: %s\n", c, dr);

			var circuit1 = circuitByJunction.getOrDefault(dr.p1, null);
			if (circuit1 == null) {
				circuit1 = new HashSet<FileParser.Pos3>();
				circuit1.add(dr.p1);
				circuitSet.add(circuit1);
				uncreatedCircuits--;
				
				circuitByJunction.put(dr.p1, circuit1);
			}
			var circuit2 = circuitByJunction.getOrDefault(dr.p2, null);
			if (circuit2 == null) {
				circuit2 = new HashSet<FileParser.Pos3>();
				circuit2.add(dr.p2);
				circuitSet.add(circuit2);
				uncreatedCircuits--;
				
				circuitByJunction.put(dr.p2, circuit2);
			}
			if (circuit1 == circuit2) {
				System.out.format("pos %s and %s are already together in circuit: %s\n", dr.p1, dr.p2, circuit1);
				continue;
			}
			
			circuitSet.remove(circuit1);
			circuitSet.remove(circuit2);
			
			circuit1.addAll(circuit2);
			
			for (FileParser.Pos3 pos : circuit1) {
				circuitByJunction.put(pos, circuit1);
			}
			circuitSet.add(circuit1);
			
			System.out.format("connecting (%d) end:[%d] %s\n", c, circuitSet.size(), circuitSet);
			
			if (!part1 && (uncreatedCircuits == 0) && (circuitSet.size() == 1)) {
				long result = dr.p1.x * dr.p2.x;

				System.out.format("part2: collapsed all circuits to 1 with result=%d: %s\n", result, dr);
				break;
			}
		}
		
		if (part1) {
			var circuitList = new ArrayList<Set<FileParser.Pos3>>(circuitSet);
			Collections.sort(circuitList, (c1, c2) -> Long.compare(c2.size(), c1.size())); 
	
			System.out.format("\ncircuits post-sort: %s\n", circuitList);
			long accumulator = 1;
			for (int i = 0 ; i < 3 ; i++) {
				var circuit = circuitList.get(i);
				long count = circuit.size();
				accumulator *= count;
				System.out.format("circuits accum: %d (%d)\n", count, accumulator);
			}
			System.out.format("part1 result: %d\n", accumulator);
		}

	
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.format("runtime: %d millis\n", duration);
	}
	
	private static class DistanceRecord {
		public final FileParser.Pos3 p1;
		public final FileParser.Pos3 p2;
		public final double distance;
		
		public DistanceRecord(FileParser.Pos3 p1, FileParser.Pos3 p2) {
			this.p1 = p1;
			this.p2 = p2;
			this.distance = p1.getDistance(p2);
		}
		
		@Override
		public String toString() {
			return String.format("%s-%s: %.02f", p1, p2, distance);
		}
	}

}
