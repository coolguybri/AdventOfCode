
import java.io.*;
import java.util.*;

public class Day09 {

    private static int DAY_NUM = 9;
    private static boolean SAMPLE_INPUT = true;

	/*
	*/
    public static void main(String[] args) throws Exception { 
		long startTime = System.currentTimeMillis();
		
		boolean part1 = false;
		
		// Import the data; will throw if no file or data makes no sense.
		var fp = new FileParser.PosParser(DAY_NUM, SAMPLE_INPUT);
	    fp.parse();
	    
	    var measurementList = new ArrayList<MeasurementRecord>();
	    
	    List<FileParser.Pos> pointList = fp.getList();
	    int pointListSize = pointList.size();
	    
	    // Build line list.
	    var linesHorizontal = new ArrayList<LineRecord>();
	    var linesVertical = new ArrayList<LineRecord>();
	    
		var lastPos = pointList.get(pointList.size() - 1);
		FileParser.Pos currPos;
	    for (var iter = pointList.iterator() ; iter.hasNext() ; lastPos = currPos) {
	    	currPos = iter.next();
	    	
	    	LineRecord line;
	    	if (lastPos.x == currPos.x) {
	    		if (lastPos.y < currPos.y)
	    			line = new LineRecord(lastPos, currPos);
	    		else
	    			line = new LineRecord(currPos, lastPos);
	    		linesVertical.add(line);
	    	} else {
	    		if (lastPos.x < currPos.x)
	    			line = new LineRecord(lastPos, currPos);
	    		else
	    			line = new LineRecord(currPos, lastPos);
	    		linesHorizontal.add(line);
	    	}
	    }
		System.out.format("measurements: horizontals: %s\n", linesHorizontal);
		System.out.format("measurements: veriticals: %s\n\n", linesVertical);

	    
	    // Build rectangle list.
	    for (int i = 0 ; i < (pointListSize - 1) ; i++) {
	    	FileParser.Pos p1 = pointList.get(i);
	    	for (int j = (i + 1) ; j < pointListSize ; j++) {
	    		FileParser.Pos p2 = pointList.get(j);
	    		
	    		var mr = new MeasurementRecord(p1, p2);
	    		measurementList.add(mr);
	    		System.out.format("measurements: computed: %s\n", mr);
	    	}
	    }
	
		Collections.sort(measurementList, (m1, m2) -> Long.compare(m2.area, m1.area)); 
		System.out.format("\nmeasurements sorted: %s\n\n", measurementList);
		
		if (!part1) {
	    	for (var iter = measurementList.iterator() ; iter.hasNext() ; ) {
	    		var mr = iter.next();
				System.out.format("measurements: filtering: %s\n", mr);
			}
	    }
	    
		System.out.format("\nmeasurements biggest area: %s\n\n", measurementList.get(0));

	
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.format("runtime: %d millis\n", duration);
	}
	
	private static class LineRecord {
		public final FileParser.Pos p1;
		public final FileParser.Pos p2;
		
		public LineRecord(FileParser.Pos p1, FileParser.Pos p2) {
			this.p1 = p1;
			this.p2 = p2;
		}
		
		@Override
		public String toString() {
			return String.format("%s-%s", p1, p2);
		}
	}
	
	private static class MeasurementRecord {
		public final FileParser.Pos p1;
		public final FileParser.Pos p2;
		public final long area;
		
		public MeasurementRecord(FileParser.Pos p1, FileParser.Pos p2) {
			this.p1 = p1;
			this.p2 = p2;
			
			long width = Math.abs(p1.x - p2.x) + 1;
			long height = Math.abs(p1.y - p2.y) + 1;
			this.area = width * height;
		}
		
		@Override
		public String toString() {
			return String.format("%s-%s: %d", p1, p2, area);
		}
	}

}
