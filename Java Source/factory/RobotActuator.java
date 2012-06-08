package factory;

import java.util.Random;

public class RobotActuator implements Runnable{

	private char currentZone;
	private RobotController controller;
	private boolean working;
	private boolean shutdown;
	
	public RobotActuator(RobotController parent){
		controller = parent;
		currentZone = Zones.IDLE;
		working = false;
	}
	
	/**
	 * returns the zone in which the robot currently resides.
	 * @return char zone: a char which represents the current zone, where
	 */
	public char getZone(){
		return currentZone;
	}
	
	public void run(){
		while(!shutdown){
			//sleep for .5 seconds, then check if the process still needs work.
			try{Thread.sleep(500);}
			catch(InterruptedException e){System.out.println("oh noes, an interruptedException.");}
			if(working){
				//chance of 1 in 35 to stop working.
				working = (new Random().nextInt(35) != 0);
				if (!working) reportDone();
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
	
	private void moveArm(char zone){
		System.out.println("Moving to zone: "+zone);
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
		System.out.println("Instruction completed.");
		controller.doneWork();
	}
	
	public void quit(){shutdown = true;}
	
}
