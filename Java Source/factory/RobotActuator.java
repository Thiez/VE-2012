package factory;

import java.util.Random;

public class RobotActuator implements Runnable{
	
	private final Logger log = Logger.getInstance();
	private volatile char currentZone;
	private final RobotController controller;
	private volatile boolean working;
	private volatile boolean shutdown;
	private int error;
	
	public RobotActuator(RobotController parent){
		controller = parent;
		currentZone = Zones.IDLE;
		working = false;
		error = -1;
	}
	
	/**
	 * returns the zone in which the robot currently resides.
	 * @return char zone: a char which represents the current zone, where
	 */
	public synchronized char getZone(){
		return currentZone;
	}
	
	public void run(){
		while(!shutdown){
			//sleep for .5 seconds, then check if the process still needs work.
			try{Thread.sleep(500);}
			catch(InterruptedException e){System.err.println("oh noes, an interruptedException.");}
			if (error == -1){
				error = detectError();
				if (error != -1) controller.error(error);
				if(working){
					//chance of 1 in 30 to stop working.
					working = (new Random().nextInt(30) != 0);
					if (!working) reportDone();
				}
			}
		}
	}
	
	/**
	 * returns whether the robot is still working on its current instruction, or
	 * idling.
	 * @return
	 */
	public boolean working(){
		return working;
	}
	
	/**
	 * method moving the arm to a new zone.
	 * @param zone
	 */
	
	private synchronized void moveArm(char zone){
		//System.out.println("moveToZone!"+controller.getNr()+"!"+Zones.zoneType(zone)+")");
		log.inform( ModelActions.moveToZone, controller.getNr(), Zones.zoneType(zone) );
		System.err.println("Robot "+controller.getNr()+": Moved to zone: "+zone);
		currentZone = zone;
	}
	
	/**
	 * method called by the controller to execute instruction. Moves the arm to a zone,
	 * executes work, returns the arm to the idle position and confirms the execution of the
	 * instruction.
	 * @param zone
	 */
	public void executeInstruction(char zone){
		moveArm(zone);
		if(zone != Zones.IDLE){
			work();
		}
	}
	
	/**
	 * Dummy method to simulate "work" being done by the actuator. Currently immediately
	 * sets the working field to false.
	 */
	private void work(){
		working = true;
	};
	
	/**
	 * reports the completion of the instruction to the robot controller.
	 */
	private void reportDone(){
		//System.out.println("doneWork!"+controller.getNr()+")");
		log.inform( ModelActions.doneWork, controller.getNr() );
		System.err.println("Robot "+controller.getNr()+": Instruction completed.");
		controller.doneWork();
	}
	
	private int detectError(){
		int result = -1;
		int errorSeed = new Random().nextInt(500);
		if (errorSeed == 0) result = 0;
		else if (errorSeed == 1) result = 1;
		//return result;
		return -1;	// No errors!
	}
	
	public void quit(){shutdown = true;}
	
}
