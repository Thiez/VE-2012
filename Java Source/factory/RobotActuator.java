package factory;

public class RobotActuator {

	private char currentZone;
	private RobotController controller;
	private boolean working;
	
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
		work();
		moveArm(Zones.IDLE);
		reportDone();
		System.out.println("instruction at "+ zone + " successfully completed.");
	}
	
	/**
	 * Dummy method to simulate "work" being done by the actuator. Currently immediately
	 * sets the working field to false.
	 */
	private void work(){
		working = false;
	};
	
	/**
	 * reports the completion of the instruction to the robot controller.
	 */
	private void reportDone(){
		controller.setWorking(false);
	}
	
}
