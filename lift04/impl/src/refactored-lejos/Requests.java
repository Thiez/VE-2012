import java.util.ArrayList;

public class Requests {

	public void add(int i) {
		requests[i] = true;
	}
	public boolean isContainedIn(Integer i) {
		return requests[i.intValue()];
	}
	public boolean isEmpty() {
		for(int i=0; i<requests.length; i++) {
			if (requests[i]) {
				return false;
			}
		}
		return true;
	}
	public void remove(Integer i) {
		requests[i.intValue()] = false;
	}
	public void reset() {
		for(int i=0; i< requests.length; i++) {
			requests[i] = false;
		}
	}
	public Integer getSmallestHigher(int n) {
		for(int i=n+1; i < requests.length; i++) {
			if (requests[i]) {
                       		return new Integer(i);
			}
               	}
		return null;
	}
	public Integer getHighestSmaller(int n) {
		for(int i=n-1; i >= 0; i--) {
               		if (requests[i]) {
                       		return new Integer(i);
			}
		}
		return null;
        }

	public String toString() {
		String s = "";
		boolean first = true;
                for(int i=0; i< requests.length; i++) {
			if (requests[i]) {
                        	if(first)
					first = false;
				else
                                	s += ",";
                        	s += i;
			}
                }
		return s;
	}

	public Requests(int n) {
		requests = new boolean[n];
	}

	boolean[] requests;
}
