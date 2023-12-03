
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.text.ParseException;
import java.math.*;

public class Day13 {


  static class PairNode {
  	String line1;
  	String line2;
  	
  	public PairNode(String l1, String l2) {
  		line1 = l1;
  		line2 = l2;
  		System.out.format("PairNode: left=\"%s\", right=\"%s\"\n", line1,line2);
  	}
  }
  
  
  static Object parseValue(String input) throws Exception	 {
  	if ((input == null) || input.isEmpty()) {
  		//System.out.format("parseValue: input is null, returning null\n");
  		return null;
  	}
  	if (input.charAt(0) == '[') {
  		return parseList(input);
  	} 
  	
  	return parseInt(input);
  }
  
  static Integer parseInt(String input) throws Exception {
  	//System.out.format("parseList: processing integer \"%s\"\n", input);
  	return Integer.parseInt(input);
  }
  
  static List<Object> parseList(String input) throws Exception {
  	//System.out.format("parseList: processing list \"%s\"\n", input);
  	
  	if ((input.charAt(0) != '[') || (input.charAt(input.length() - 1) != ']')) {
  		throw new Exception(String.format("bad input strig for parseList:\"%s\"\n", input));
  	}
  	input = input.substring(1, input.length() - 1);
  	//System.out.format("parseList: trimmed to \"%s\"\n", input);
  	
  	List<Object> retList = new ArrayList<Object>();
  	
  	
  	int currPos = 0;
  	int endPos = input.length();
  	while (currPos < endPos) {
  		if (input.charAt(currPos) == '[') {
  			// scan to end
  			int offset = 1;
  			for (int listpos = currPos+1 ; listpos < endPos ; listpos++) {
  				if (input.charAt(listpos) == '[')
  					offset++;
  				else if (input.charAt(listpos) == ']')
  					offset--;
  					
  				if (offset == 0) {
  					String szlist = input.substring(currPos, listpos + 1);
  					int sublen = (listpos + 1) - currPos;
  					List sublist = parseList(szlist);
  					retList.add(sublist);
  					
  					// go past the next comma
  					currPos += sublen + 1;
  					break;
  				}
  			}
  		} else {
  			// parsing an integer!
  			//System.out.format("parseList: parsing an int at pos=%d, input=%s\n", currPos, input);
  			if (!Character.isDigit(input.charAt(currPos))) {
  				throw new Exception(String.format("bad integer position: pos=%d, \"%s\"\n", currPos, input));
  			}
  			int commaPos = input.indexOf(',', currPos);
  			if (commaPos == -1) {
  				commaPos = input.length();
  			}
  			int sublen = commaPos - currPos;
  			//System.out.format("parseList: found int with pos=%d, cpos=%d, len=%d, input=\"%s\"\n", currPos, commaPos, sublen, input);
  			String sznum = input.substring(currPos, commaPos);
  			Integer num = parseInt(sznum);
  			retList.add(num);
  			
  			// move the curPtr to just past the comma; if we go out of the buffer, will be caught at top of next loop.
  			currPos += sublen + 1;
  		}
  	}
  	
  	//System.out.format("parseList: returning list of %d: %s\n", retList.size(), retList.toString());
  	return retList;
  } 
  
  
  static private List<PairNode> pairs = new ArrayList<PairNode>();


  	static int compareLists(List<Object> left, List<Object> right) {
  		//System.out.format("compareLists begin with \"%s\" : \"%s\"\n", left,right);
  		
  		int leftPos = 0;
  		int rightPos = 0;
  		for ( ; (leftPos < left.size()) && (rightPos < right.size()); leftPos++, rightPos++) {
  			int compare = compareObjects(left.get(leftPos), right.get(rightPos));
  			if (compare != 0) {
  				// we are either in order or out of order; deicison made.
  				
  				//System.out.format("compareLists end with \"%s\" : \"%s\": index diff at %d, determine=%d\n", left,right, leftPos, compare);
  				return compare;
  			}
  		}
  		
  		// Both ran out at the same time, they are equal.
  		if (leftPos == left.size() && rightPos == right.size()) {
  			//System.out.format("compareLists end with \"%s\" : \"%s\": ran out at same time, indetermined\n", left,right);
  			return 0;
  		}
  		
  		// if right size still has more, left ran out first, so correct order.
  		int compare = (rightPos < right.size()) ? 1 : -1;
  		//System.out.format("compareLists end with \"%s\" : \"%s\": ran out difftimes, determine=%d\n", left,right, compare);
  		return compare;
  	}
  
  
  	static int compareIntegers(Integer left, Integer right) {
   		return right - left; // - means wrong order, left is bigger
  	}


  	static int compareObjects(Object left, Object right) {
  		if (left instanceof List) {
  			if (right instanceof List) {
  				return compareLists((List<Object>) left, (List<Object>) right);
  			} else if (right instanceof Integer) {
  				return compareLists((List) left, Collections.singletonList((Integer) right));
  			}
  		} else if (left instanceof Integer) {
  			if (right instanceof Integer) {
  				return compareIntegers((Integer) left, (Integer) right);
  			} else if (right instanceof List) {
  				return compareLists(Collections.singletonList((Integer) left), (List) right);
  			}
  		}
  		
  		
  		// THROW?
  		return -1;
  	}

	
  public static void main(String[] args) throws Exception { 
  	System.out.format("Day 13 Mutha Fuckah:\n\n");
	
	// Open input file.
   	File file = new File("day13.input");
   	BufferedReader br = new BufferedReader(new FileReader(file));
   	boolean part1 = false;
   	
   	if (part1) {
		boolean hitEof = false;
		while (!hitEof) {
			String line1;
			if ((line1 = br.readLine()) == null) {
				throw new Exception("shit, bad first in input\n");
			}
		
			String line2;
			if ((line2 = br.readLine()) == null) {
				throw new Exception("shit, bad second in input\n");
			}
		
			String ender;
			if ((ender = br.readLine()) == null) {
				System.out.format("Input EOF\n");
				hitEof = true;
			}
			else if (ender.length() != 0) {
				throw new Exception("shit, bad ender in input\n");
			}
		
			pairs.add(new PairNode(line1, line2));
		}
		System.out.format("Import End: with pairs=%d\n\n", pairs.size());
	
	
		int pairset_count = 1;
		int num_good = 0;
		int index_sum = 0;
		for (Iterator<PairNode> iter = pairs.iterator() ; iter.hasNext() ; pairset_count++) {
			PairNode node = iter.next();
			System.out.format("Processing pair: with pair=%d/%d\n", pairset_count, pairs.size());
		
			List<Object> left = parseList(node.line1);
			List<Object> right = parseList(node.line2);
		
			// lets compare these fuckers.
			int correct = compareLists(left, right);
			if (correct > 0) {
				index_sum += pairset_count;
				num_good++;
				System.out.format(" pair %d CORRECT, good=%d, indexsum=%d total now\n", pairset_count, num_good, index_sum);
			}
		} 
	
		System.out.format("Analysis End: sum=%d, with pairs=%d\n\n", index_sum, pairs.size());
	  } else {
	  
	  	List<List<Object>> packets = new ArrayList<List<Object>>();
	  
		String line;
		while ((line = br.readLine()) != null) {
		
			if (line.isEmpty())
				continue;
				
			List<Object> currPacket = parseList(line);
			packets.add(currPacket);
			System.out.format("Import: packet=%s\n", currPacket);
		}
		System.out.format("Import End: with pairs=%d\n\n", packets.size());
		
		List<Object> div1 = parseList("[[2]]");
		List<Object> div2 = parseList("[[6]]");
		packets.add(div1);
		packets.add(div2);
		
		
		Collections.sort(packets, new PacketSorter());
		
		int packet_count = 1;
		int div1Pos = -1;
		int div2Pos = -1;
		for (Iterator<List<Object>> iter = packets.iterator() ; iter.hasNext() ; packet_count++) {
			List<Object> packet = iter.next();
			System.out.format("Ended packet: %s\n", packet);
			
			if ((div1Pos < 0) && compareLists(packet, div1) == 0) {
				div1Pos = packet_count;
			} else if ((div2Pos < 0) && compareLists(packet, div2) == 0) {
				div2Pos = packet_count;
			}
		} 
		
		System.out.format("Analysis End: div1=%d, div2=%d, product=%d\n\n", div1Pos, div2Pos, div1Pos * div2Pos);
		
	  }
   }
   
   static class PacketSorter implements Comparator<List<Object>>
   {
   		public int compare(List<Object> left, List<Object> right) {
   			return compareLists(left, right) * -1;
   		}
   }
}
