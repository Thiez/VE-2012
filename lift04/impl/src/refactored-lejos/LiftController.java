public class LiftController {

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

	int nLevels;
	LiftMachine liftMachine;

	Requests requests;

        int top; // number of highest floor that can be reached
        Integer cur; //current floor
        Activity act;
        Activity lastDir;
        Doors doors;

	String prevState;

	public LiftController(int nLevels, LiftMachine lm) {
		this.nLevels = nLevels;
		this.liftMachine = lm;
		cur = new Integer(0);
		requests = new Requests(nLevels);
		top = nLevels - 1;
		reset();
	}

	public void stop() {
	}
	public void reset() {
		requests.reset();
 		act = Activity.STOPPED;
                lastDir = Activity.STOPPED;
                doors = Doors.CLOSED;
		prevState = "";
	}

	public void motor() {
		liftMachine.output("motor_"+act.toString());
		printState();
	}
	 public void report() {
                liftMachine.output("report!"+act.toString()+"("+cur.toString()+")");
		printState();
        }
	public void printState() {
                String newState = "State: ["+cur.toString()+
				    " "+doors.toString()+
				    " "+act.toString()+
				    " {"+requests.toString()+"}"+
				  "]";
                if (! newState.equals(prevState)) {
			liftMachine.message(newState);
                }
                prevState = newState;

	}
	public void move() {
		if (act == Activity.MOVINGUP && cur.intValue() < top) {
			liftMachine.up();
			cur = new Integer(cur.intValue() + 1);
                } else if (act == Activity.MOVINGDOWN && cur.intValue() > 0) {
			liftMachine.down();
                        cur = new Integer(cur.intValue() - 1);
                } else if (act != Activity.STOPPED) {
                        // ERROR!!!
                }
		printState();
	}

	public void waitDoors() {
		liftMachine.doSleep(2000);
	}
	public void openDoors() {
		liftMachine.openDoor();
		doors = Doors.OPEN;
		liftMachine.output("door_open");
		printState();
	}
	public void closeDoors() {
		liftMachine.closeDoor();
		doors = Doors.CLOSED;
		liftMachine.output("door_close");
		printState();
	}
	public void waitAfterDoors() {
		liftMachine.doSleep(1000);
	}
	public void waitMove() {
		printState();
	}
	public void waitStopped() {
		liftMachine.doSleep(1000);
		printState();
	}
	public void waitIdling() {
		//liftMachine.message("idling");
		liftMachine.doSleep(500);
	}
	private void updateRequestsFromQueue() {
		liftMachine.updateRequests(requests);
		printState();
	}


	public void go() {
		boolean hasToStop = false;
		boolean idled;

		updateRequestsFromQueue();

                while(!hasToStop) {

                        idled = true;
                        while (requests.isContainedIn(cur)) {

                                idled = false;
                                if (act != Activity.STOPPED) {
                                        lastDir = act;
                                        act = Activity.STOPPED;
                                        motor();
                                        report();
                                        waitStopped();
                                }
                                requests.remove(cur);
                                openDoors();
                                waitDoors();
                                closeDoors();
                                updateRequestsFromQueue();
                                waitAfterDoors();
                                updateRequestsFromQueue();
                        }
        
                        if (! requests.isEmpty()) {
                                idled = false;
                                act = decideDir();
                                lastDir = act;
                                motor();
                                report();
                                waitMove();
                                move();
                        }

                        if (idled) {
                                waitIdling();
                        }

                        updateRequestsFromQueue();
                }
	}
/*
	public void sleep(long n) {
		try {
			Thread.sleep(n);
		} catch(Exception e) {
			message("sleep interrupt");
		}
	}
 */

       public Activity decideDir() {
                int n = cur.intValue();
                Integer smallestHigher = requests.getSmallestHigher(n);
                Integer highestSmaller = requests.getHighestSmaller(n);;

                boolean hasHigher = smallestHigher != null;
                boolean hasLower = highestSmaller != null;


                if (lastDir == Activity.MOVINGUP && n < top && hasHigher) {
                        return lastDir;
                } else if (lastDir == Activity.MOVINGDOWN && n > 0 && hasLower) {
                        return lastDir;
                } else if (lastDir == Activity.MOVINGUP && n >= top) {
                        return Activity.MOVINGDOWN;
                } else if (lastDir == Activity.MOVINGDOWN && n <= 0) {
                        return Activity.MOVINGUP;
                } else if (hasHigher && !hasLower) {
                        return Activity.MOVINGUP;
                } else if (!hasHigher && hasLower) {
                        return Activity.MOVINGDOWN;
                } else if (hasHigher && hasLower && smallestHigher-cur < cur-highestSmaller) {
                        return Activity.MOVINGUP;
                } else if (hasHigher && hasLower && smallestHigher-cur > cur-highestSmaller) {
                        return Activity.MOVINGDOWN;
                } else {
                        // throw a coin?
                        return Activity.MOVINGUP;
                }
        }

}
