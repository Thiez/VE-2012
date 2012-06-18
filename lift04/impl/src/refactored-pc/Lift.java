import java.io.*;
import java.util.HashSet;
import java.util.Collection;
import java.lang.Integer;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.text.SimpleDateFormat;

public class Lift extends Thread implements LiftMachine {

        private int nLevels = 4;
        private int top = nLevels-1;

        private LiftController liftController;

	ConcurrentLinkedQueue<Integer> queue;

	boolean debug;
	
  public static void main (String[] args) {
    int miliseconds = 1*750;
    if (args.length == 1) {
        try {
            miliseconds = Integer.parseInt(args[0]);
            if (miliseconds < 0) {
                miliseconds =  -miliseconds;
            }
        } catch(NumberFormatException nfe) {
            report("ignoring invalid timeout argument: \""+args[0]+"\""+
                "; using default timeout ("+miliseconds+")");
        }
    }
    Lift lift = new Lift(3, false);
    lift.start();
    new UI(lift).play();
  }

  public static void report(String s) {
    System.err.println("lift: "+s);
  }
	
	

	public Lift(int top, boolean debug) {
		this.queue = new ConcurrentLinkedQueue<Integer>();
		this.debug = debug;
		this.liftController = new LiftController(nLevels, this);
	}

	public ConcurrentLinkedQueue<Integer> getQ() {
		return queue;
	}

	public void run() {
		liftController.go();
	}

//  {------  Methods to implement LiftMchine interface

	public void reset() {
	}

	public void openDoor() {
	}
	public void closeDoor() {
	}

	public void up() {
		doSleep(1500);
	}
	public void down() {
		doSleep(1500);
	}

	public void doSleep(long n) {
                try {
                        Thread.sleep(n);
                } catch (InterruptedException ex) {
                        Logger.getLogger(Lift.class.getName()).log(Level.SEVERE, null, ex);
                }
	}

	public void output(String s) {
		System.out.println(s);
	}
	public void message(String s) {
		System.err.println(s);
	}

	public void updateRequests(Requests req) {
		Integer i;
		while((i = queue.poll()) != null) {
			if (i.intValue() >= 0 && i.intValue() <= top)
				req.add(i);
			else
				report("skipping invalid input: "+i);
		}
	}

//  }------  Methods to implement LiftMchine interface

}

class UI {
  Lift lift;
  SimpleDateFormat fmt;

  public UI(Lift lift) {
    this.lift = lift;
    fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  }

  public void play() {
    BufferedReader r = new BufferedReader( new InputStreamReader(System.in));
    String s;
    try { 
     while((s = r.readLine()) != null) {
       String[] words = s.split("!");
       if (words.length==2 &&
           (words[0].trim().equals("press_cabin") || words[0].trim().equals("press_corridor"))) {
	   String k = words[0].trim();
	   String w = words[1].trim();
           try {
               int i = java.lang.Integer.parseInt(w);
               lift.getQ().add(new Integer(i));
               String src = (k.equals("press_cabin") ? "cab" : "cor");
	       Lift.report("Input: ["+src+" "+i+"]");
           } catch(NumberFormatException nfe) {
                Lift.report("ignoring invalid line: \""+w+"\"");
           }
       } else {
	    Lift.report("skipping unexpected line: "+s);
       }
     }
    } catch(java.io.IOException e) {
            Lift.report("io exception: "+e.toString());
	    System.exit(1);
    }
  }
}
