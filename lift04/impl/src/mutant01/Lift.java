import java.util.HashSet;
import java.util.Collection;
import java.lang.Integer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Random;

import java.util.logging.Level;
import java.util.logging.Logger;


public class Lift extends Thread {

	public enum Activity {
		STOPPED("stop"),
		MOVINGUP("up"),
		MOVINGDOWN("down");

		private Activity(String name) {
			this.name = name;
		}
		private final String name;
		public String toString() {
			return name;
		}
	}

	public enum Doors {
		OPEN("open"),
		CLOSED("closed");

		private Doors(String name) {
			this.name = name;
		}
		private final String name;
		public String toString() {
			return name;
		}
	}

	int stoppedWaitTime = 1000;
	int doorsWaitTime = 1500;
	int afterDoorsWaitTime = 1000;
	int moveWaitTime = 1500;
	int idlingWaitTime = 500;

	int fatigue = 0;
	int fatigue_bound = 100;
	Random generator;

	boolean debug;
	String prevState;

	int top; // number of highest floor that can be reached
	Integer cur; //current floor
	Activity act;
	Activity lastDir;
	Doors doors;
	ConcurrentLinkedQueue<Integer> queue;
	
	Collection<Integer> requests;

	public Lift(int top, boolean debug) {
		this.top = top;
		this.cur = new Integer(0);
		this.act = Activity.STOPPED;
		this.lastDir = Activity.STOPPED;
		this.doors = Doors.CLOSED;
		this.requests = new HashSet<Integer>();
		this.queue = new ConcurrentLinkedQueue<Integer>();
		this.debug = debug;
		this.prevState = "";
	
		this.fatigue = 0;
		this.generator = new Random();
	}

	public ConcurrentLinkedQueue<Integer> getQ() {
		return queue;
	}

	public void updateRequestsFromQueue() {
		Integer i;
		while((i = queue.poll()) != null) {
			if (i.intValue() >= 0 && i.intValue() <= top)
				requests.add(i);
			else
				MyMain.report("skipping invalid input: "+i);
		}
	}


	public void run() {
		boolean hasToStop = false;
		boolean idled;

		updateRequestsFromQueue();
		printState();

		while(!hasToStop) {

			idled = true;

			while (requests.contains(cur)) {
				idled = false;
		 		if (act != Activity.STOPPED) {
					lastDir = act;
					act = Activity.STOPPED;
					motor();
					printState();
					report();
					waitStopped();
					printState();
				}
				printState();
				requests.remove(cur);
				openDoors();
				printState();
				waitDoors();

				closeDoors();
				updateRequestsFromQueue();
				printState();
				waitAfterDoors();

				updateRequestsFromQueue();
			}
	
			if (! requests.isEmpty()) {
				idled = false;
				act = decideDir();
				lastDir = act;
				motor();
				printState();
				report();
				printState();
				waitMove();
				printState();
				move();
				printState();
			}

			if (idled) {
				waitIdling();
			}

			updateRequestsFromQueue();
			printState();
		}
	}

	public void wait(int t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException ex) {
			Logger.getLogger(Lift.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	public void waitStopped() {
		wait(stoppedWaitTime);
	}
	public void waitDoors() {
		wait(doorsWaitTime);
	}
	public void waitAfterDoors() {
		wait(afterDoorsWaitTime);
	}
	public void waitMove() {
		wait(moveWaitTime);
	}
	public void waitIdling() {
		wait(idlingWaitTime);
	}

	public void openDoors() {
		doors = Doors.OPEN;
		System.out.println("door_open");
	}
	public void closeDoors() {
		fatigue++;
		if (fatigue > 10) {
			int r = generator.nextInt(fatigue_bound);
			if (r > (fatigue_bound-fatigue)) {
				return;
			}
		}
		doors = Doors.CLOSED;
		System.out.println("door_close");
	}
	public void motor() {
		System.out.println("motor_"+act.toString());
	}
	public void report() {
		System.out.println("report!"+act.toString()+"("+cur.toString()+")");
	}
	public void printState() {
		int[] ints = constructIntArray(requests, null);
		String ss = "";
		boolean first =  true;
		for(Integer n: requests) {
			if (first) {
				first = false;
			} else {
				ss += ", ";
			}
			ss += n.intValue();
		}
		String s = "";
		for(int i=0; i< ints.length; i++) {
			if(i > 0)
				s += ",";
			s += ints[i];
		}
		//if (debug) MyMain.report("state: cur="+cur.toString()+" act="+act.toString()+" req={"+ss+"}"+" ireq={"+s+"}");
		String newState = "State: ["+cur.toString()+" "+doors.toString()+" "+act.toString()+" {"+ss+"}]";
		if (! newState.equals(prevState)) {
			MyMain.report(newState);
		}
		prevState = newState;
	}

	public void move() {
		if (act == Activity.MOVINGUP && cur.intValue() < top) {
			cur = new Integer(cur.intValue() + 1);
		} else if (act == Activity.MOVINGDOWN && cur.intValue() > 0) {
			cur = new Integer(cur.intValue() - 1);
		} else if (act != Activity.STOPPED) {
			// ERROR!!!
		}
	}

	private int[] constructIntArray(Collection<Integer> coll, Integer cur) {
		int[] a;
		int index = 0;
		if (cur != null && ! coll.contains(cur)) {
			a = new int[coll.size()+1];
			a[index++] = cur.intValue();
		} else {
			a = new int[coll.size()];
		}
		
		for(Integer i : coll){
    			a[index++] = i.intValue();
		}
		java.util.Arrays.sort(a);
		return a;
	}

	public Activity decideDir() {
		Integer smallestHigher = null;
		Integer highestSmaller = null;
		int n = cur.intValue();
		int[] ints = constructIntArray(requests, cur);
		int i = java.util.Arrays.binarySearch(ints, cur.intValue());
		if (i+1 < ints.length) {
			smallestHigher =  new Integer(ints[i+1]);
		}
		if (i-1 >= 0) {
			highestSmaller =  new Integer(ints[i-1]);
		}
		
		boolean hasHigher = smallestHigher != null;
		boolean hasLower = highestSmaller != null;

		
		String dbg = "decideDir: i="+i+" cur="+cur.intValue()+" lastDir="+lastDir.toString()+" sh="+smallestHigher+" hs="+highestSmaller;

		if (lastDir == Activity.MOVINGUP && n < top && hasHigher) {
			if (debug) MyMain.report(dbg+" "+1);
			return lastDir;
		} else if (lastDir == Activity.MOVINGDOWN && n > 0 && hasLower) {
			if (debug) MyMain.report(dbg+" "+2);
			return lastDir;
		} else if (lastDir == Activity.MOVINGUP && n >= top) {
			if (debug) MyMain.report(dbg+" "+3);
			return Activity.MOVINGDOWN;
		} else if (lastDir == Activity.MOVINGDOWN && n <= 0) {
			if (debug) MyMain.report(dbg+" "+4);
			return Activity.MOVINGUP;
		} else if (hasHigher && !hasLower) {
			if (debug) MyMain.report(dbg+" "+5);
			return Activity.MOVINGUP;
		} else if (!hasHigher && hasLower) {
			if (debug) MyMain.report(dbg+" "+6);
			return Activity.MOVINGDOWN;
		} else if (hasHigher && hasLower && smallestHigher-cur < cur-highestSmaller) {
			if (debug) MyMain.report(dbg+" "+7);
			return Activity.MOVINGUP;
		} else if (hasHigher && hasLower && smallestHigher-cur > cur-highestSmaller) {
			if (debug) MyMain.report(dbg+" "+8);
			return Activity.MOVINGDOWN;
		} else {
			if (debug) MyMain.report(dbg+" "+9);
			// throw a coin?
			return Activity.MOVINGUP;
		}
	}

}

