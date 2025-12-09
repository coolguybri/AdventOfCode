
import java.io.*;
import java.util.*;

public class Day07 {

    private static int DAY_NUM = 7;
    private static boolean SAMPLE_INPUT = false;
    
    private static char OOB_CHAR = '!';

	/*
	*/
    public static void main(String[] args) throws Exception { 
		long startTime = System.currentTimeMillis();
		
		boolean part1 = false;
		
		// Import the data; will throw if no file or data makes no sense.
		var fp = new FileParser.MatrixParser(DAY_NUM, SAMPLE_INPUT);
	    fp.parse();
	    
	    // Find starting point of tachyon beam.
	    FileParser.Pos startPos = null;
	    for (int x = 0 ; x < fp.getWidth(); x++) {
	    	for (int y = 0; y < fp.getHeight() ; y++) {
	    		char c = fp.getCell(x, y, OOB_CHAR);
	    		if (c == 'S') {
	    			if (startPos != null) {
	    				throw new IOException("multiple starting points detected");
	    			}
	    			
	    			startPos = new FileParser.Pos(x, y);
	    		}
	    	}
	    }
	    System.out.format("tachyon ray incoming at %s\n", startPos);
		
		if (part1) {
		
			Set<FileParser.Pos> beamFronts = new HashSet<FileParser.Pos>();
			beamFronts.add(startPos);
		
			int step = 0;
			int numSplit = 0;
			for ( ; !beamFronts.isEmpty() ; step++) {
				System.out.format(": step %d: starting with beamfronts: %s\n", step, beamFronts);
				for (var line : fp.getList()) {	
					System.out.format(": step %d: |  %s\n", step, line);
				}
				Set<FileParser.Pos> nextBeamFronts = new HashSet<FileParser.Pos>();
	
				for (var currPos : beamFronts) {
					var currChar = fp.getCell(currPos, OOB_CHAR);
					if (currChar == OOB_CHAR)
						continue;
						
					if ((currChar == '.') || (currChar == 'S')) {
						// this is normal space; tachyon beam goes straight down.
						nextBeamFronts.add(currPos.down());
						System.out.format(": step %d: space: %s\n", step, currPos);
						
						fp.setCell(currPos, '|');
	
					} else if (currChar == '^') {
						// this is a splitter, beams continues to the right and left.
						boolean place1 = nextBeamFronts.add(currPos.left());
						boolean place2 = nextBeamFronts.add(currPos.right());
						
						//if (place1 && place2)
						numSplit++;
						System.out.format(": step %d: splitter: %s, totSplit=%d\n", step, currPos, numSplit);
					} else if (currChar == '|') {
						// we already placed a beam here, nothing to do.
						System.out.format(": step %d: tachyon-already: %s\n", step, currPos);
					} else {
						throw new IOException("bad char");
					}
				}
				
				// Iterate the new wave-front.
				beamFronts = nextBeamFronts;
			}
			
			System.out.format(" final part 1 steps=%d, splits=%d\n", step, numSplit);
		} else {
			
			var beamFronts = new ArrayList<BeamFrontNode>();
			beamFronts.add(new BeamFrontNode(startPos, "", 1, false));
			
			var completeStepSequences = new HashMap<String, Long>();

			int step = 0;
			for ( ; !beamFronts.isEmpty() ; step++) {
				//System.out.format(": step %d: starting with beamfronts: %d\n", step, beamFronts.size());

				if ((step % 10) == 0) {
					System.out.format(": step %d: starting with beamfronts: %s\n", step, beamFronts);
					for (var line : fp.getList()) {	
						System.out.format(": step %d: |  %s\n", step, line);
					}
				}
				
				var nextBeamFronts = new ArrayList<BeamFrontNode>();
				
				var raySet = new HashSet<FileParser.Pos>();

				for (var currBeamFront : beamFronts) {
					var currChar = fp.getCell(currBeamFront.pos, OOB_CHAR);
					if (currChar == OOB_CHAR) {
					
						// We have left the frame, presumably at the bottom of the screen.
						//long numRegistered = completeStepSequences.get(currBeamFront.seq, null);
						completeStepSequences.put(currBeamFront.seq, currBeamFront.multiplier);
						System.out.format("final: adding seq=%s (%d), total %d unique sequences\n", currBeamFront.seq, currBeamFront.multiplier, completeStepSequences.size());

					} else if ((currChar == '.') || (currChar == 'S')) {
					
						raySet.add(currBeamFront.pos);
						
						if (currBeamFront.justSplit) {
							nextBeamFronts.add(new BeamFrontNode(currBeamFront.pos.down(), currBeamFront.seq + "|", currBeamFront.multiplier, false));
							//System.out.format(": step %d: space 1-postsplit, curr=%s, seq=%s\n", step, currBeamFront.pos, currBeamFront.seq);
						} else {
							// in order to descend at the same rate as splitters, we pause for one step at normal space, and then
							// evaluate it when we see it marked as a tachyon beam.
							nextBeamFronts.add(new BeamFrontNode(currBeamFront.pos, currBeamFront.seq + ".", currBeamFront.multiplier, false));
							//System.out.format(": step %d: space 1-rawdrop, curr=%s, seq=%s\n", step, currBeamFront.pos, currBeamFront.seq);
						}

					} else if (currChar == '|') {
					
						// this is normal space; tachyon beam goes straight down.
						nextBeamFronts.add(new BeamFrontNode(currBeamFront.pos.down(), currBeamFront.seq + "|", currBeamFront.multiplier, false));
						//System.out.format(": step %d: space 2, curr=%s, seq=%s\n", step, currBeamFront.pos, currBeamFront.seq);
						
					} else if (currChar == '^') {
						// this is a splitter, beams continues to the right and left.

						nextBeamFronts.add(new BeamFrontNode(currBeamFront.pos.left(), currBeamFront.seq + "<", currBeamFront.multiplier, true));
						nextBeamFronts.add(new BeamFrontNode(currBeamFront.pos.right(), currBeamFront.seq + ">", currBeamFront.multiplier, true));
						//System.out.format(": step %d: splitter: curr=%s, seq=%s\n", step, currBeamFront.pos, currBeamFront.seq);
						
					}  else {
						throw new IOException("bad char");
					}
				}
				
				for (var setPos : raySet) {
					fp.setCell(setPos, '|');
				}
				
				// condense the current beamFront set.
				var nextBeamFrontsCondensed = new ArrayList<BeamFrontNode>();
				//System.out.format(": step %d: condense start: %s\n", step, nextBeamFronts);

				for (int i = 0 ; i < nextBeamFronts.size() ; i++) {
					var bf = nextBeamFronts.get(i);
					
					for (int j = i + 1 ; j < nextBeamFronts.size() ; j++) {
						var bf2 = nextBeamFronts.get(j);
						if (bf2.pos.equals(bf.pos)) {
							long newMultiplier = bf.multiplier + bf2.multiplier;
							bf = new BeamFrontNode(bf.pos, bf.seq, newMultiplier, (bf.justSplit | bf2.justSplit));
							
							nextBeamFronts.remove(j);
							j--;
							System.out.format(": step %d: condense: i=%d, j=%d\n", step, i, j);
						}
					}
					
					nextBeamFrontsCondensed.add(bf);
				}
				//System.out.format(": step %d: condense end: %s\n", step, nextBeamFrontsCondensed);

				// Iterate the new wave-front.
				beamFronts = nextBeamFrontsCondensed;
			}
			
			System.out.format("sequences= (%d) %s\n", completeStepSequences.size(), completeStepSequences);
			long totalVariants = 0;
			for (var entry : completeStepSequences.entrySet()) {
           	 	String seq = entry.getKey();
            	long value = entry.getValue();
            	totalVariants += value;
            	System.out.format("calculating variants, %s=%d, %d\n", seq, value, totalVariants);
        	}
        	System.out.format("variants=%d\n", totalVariants);
		}
	
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.format("runtime: %d millis\n", duration);
	}
	
	static class BeamFrontNode {
		public final FileParser.Pos pos;
		public final	 String	seq;
		public final long multiplier;
		public boolean	justSplit;
		
		public BeamFrontNode(FileParser.Pos pos, String	seq, long multiplier, boolean justSplit) {
			this.pos = pos;
			this.seq = seq;
			this.multiplier = multiplier;
			this.justSplit = justSplit;
		}
		
		@Override
		public String toString() {
			return String.format("%s [%s] (* %d)", pos, seq, multiplier);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			BeamFrontNode that = (BeamFrontNode) o;
			return (this.pos == that.pos) && (this.seq == that.seq);
		}

		@Override
		public int hashCode() {
			return Objects.hash(pos, seq);
		}
	}
}
