package factory;

import implementation.*;

public class RobotController implements Runnable{
	
	private int robotNr;
	private FactoryModel factory;
	private String reachableZones;
	private String instructionSet;
	public RobotActuator actuator;
	private boolean token;
	private boolean online;
	private boolean shutdown;
	private int error;
	private Logger log = Logger.getInstance();
	
	/**
	 * creates a new instance of the RobotController class.
	 * @param int robotNr: the number of this robot
	 * @param char[] zones: the reachable zones of this robot
	 * @param FactoryModel factory: the factory to which this robot belongs
	 */
	public RobotController(int robotNr, String zones, FactoryModel factory){
		this.robotNr = robotNr;
		this.reachableZones = zones;
		this.factory = factory;
		error = -1;
		actuator = new RobotActuator(this);
		Thread actuatorThread = new Thread(actuator,"actuator-"+robotNr);
		actuatorThread.start();
		instructionSet = "";
		shutdown = false;
	}
	
	public void run(){
		System.err.println("[System] Starting up...");
		RobotController next_robot = factory.getRobot((robotNr + 1) % FactoryModel.NR_OF_ROBOTS);
		while(!shutdown){
			if(online && (error == -1)){
				if (!instructionSet.equals("")){
					//I'm not done but I dont have the token, wake up lazyheads...
					if(!token) synchronized(factory){factory.notifyAll();}

					char nextInstruction = instructionSet.charAt(0);
					//check if token & possible to go to zone. If so, execute instruction.
					if (token == true && validZone(nextInstruction)){
						boolean permission = askPermission(nextInstruction);
						if(permission){
							System.err.println("Robot "+robotNr+": permission was granted. Passing token.");
							instructionSet = instructionSet.substring(1);
							executeInstruction(nextInstruction);
							System.err.println("Robot " + robotNr + ": Executing instruction. Current set is: '" + instructionSet + "'");
							//TODO: this needs to be changed to a call to the controller eventually, nack'ing the instruction.
						}
					}else if(!validZone(nextInstruction)){
						//System.out.println("rejection!"+robotNr+")");
						log.inform( ModelActions.rejection, robotNr );
						System.err.println("Robot " + robotNr + ": " + nextInstruction + " is not a reachable zone, skipping.");
						instructionSet = instructionSet.substring(1);
					}
				}else{
					synchronized(factory){
						try {factory.wait();}
						catch(InterruptedException e){System.err.println("Interrupts are deprecated, wut is this.");}
					}
				}
				if(token){
					//System.out.println("forw_token!"+next_robot.getNr()+")");
					log.inform( ModelActions.forw_token, next_robot.getNr() );
					setToken(false);
					next_robot.setToken(true);
				}
			}
			try{
				Thread.sleep(1000);
			}catch(InterruptedException e){System.err.println("an interruptedException happened.");}
		}
	}

	/**
	 * Returns whether the tested zone is a valid zone (i.e., reachable) by checking the reachableZones String.
	 * @param char zone: zone to check
	 * @return boolean result: whether this is a reachable zone
	 */
	private boolean validZone(char zone){
		return (reachableZones.indexOf(zone) != -1);
	}
	
	/**
	 * method used to determine whether the robot is clear to go to the destination zone.
	 * @param zone
	 * @return
	 */
	private boolean askPermission(char zone){
		boolean result = true;
		int nextRobot = (robotNr + 1) % FactoryModel.NR_OF_ROBOTS;
		for (int i = nextRobot; !(i==robotNr); i = (i+1) % FactoryModel.NR_OF_ROBOTS){
			//System.out.println("requestClearance!"+robotNr+"!"+i+"!"+Zones.zoneType(zone)+")");
			log.inform( ModelActions.requestClearance, robotNr, i, Zones.zoneType(zone) );
			boolean iResult = factory.getRobot(i).grantPermission(zone);
			result = result && iResult;
			if (iResult) {
				//System.out.println("grantClearance!"+robotNr+")");
				log.inform( ModelActions.grantClearance, robotNr );
			} else {
				//System.out.println("denyClearance!"+robotNr+")");
				log.inform( ModelActions.denyClearance, robotNr );
			}
			
		}
		if(!result) System.err.println("Robot "+robotNr+": Permission to move to zone '"+zone+"' was denied. Passing token.");
		return result;
	}
	
	/**
	 * tells the robot actuator to perform an instruction.
	 * @param zone: the zone to move to
	 * @ensures this.actuator.getZone() == zone
	 */
	private void executeInstruction(char zone){
		actuator.executeInstruction(zone);
	}
	
	/**
	 * permission check for other robots, asking permission to go to a specified zone.
	 * Will return false if this is it's current zone, true otherwise.
	 * @param zone
	 * @return boolean permission: either granted or denied.
	 */
	public boolean grantPermission(char zone){
		boolean result = true;
		if (zone == actuator.getZone() && !(zone == Zones.IDLE)) result = false;
		return result;
	}
	
	/**
	 * sets the status of the controller to working/idle. Is called (set to true) when
	 * the arm is moved to a new zone by the actuator, and again (to false) when the actuator
	 * is done working in the current zone.
	 * @param working
	 */
	public boolean isWorking(){
		return actuator.working();
	}
	
	/**
	 * adds new instructions to the list of instructions of the robot. New instructions
	 * are added to the end of the current list and performed in sequence.
	 * @param instructions
	 */
	public void addInstructions(String instructions){
		instructionSet = instructionSet + instructions;
	}
	
	/**
	 * updates whether the robot currently has the token. Is called when it receives the
	 * token from another robot, then called again when the token is passed to the next robot.
	 * @param token
	 */
	public void setToken(boolean token){
		this.token = token;
	}
	
	/**
	 * updates the online status of this robot. Is called when a product has arrived to work
	 * on (to online), and again when all work is completed (to offline). These calls should
	 * be made by the platform.
	 * @param online
	 */
	public void setOnline(boolean online){
		this.online = online;
	}
	
	public void doneWork(){
		//System.out.println("confirmation!"+robotNr+")");
		log.inform( ModelActions.confirmation, robotNr );
		actuator.executeInstruction(Zones.IDLE);
		//todo: some sort of ack/nack needs to be sent to the controller. In the current system, however,
		//the controller is a human entity and as such, the "factorycontroller" is an unknown entity. Possibly do this with wait and notify?
	}
	
	/**
	 * checks whether the robot has performed all of its instructions. This method should
	 * be called by the supervising entity, which can respond to all robots being done by
	 * calling the platform to remove the product.
	 * @return done: is true when no instructions are queued or in progress.
	 */
	public boolean allDone(){
		return (instructionSet.equals(""));
	}
	
	/**
	 * Signals the thread to stop at the earliest convenience. Note that waking the threads takes place in the factoryModel,
	 * to centralize the entire affair and prevent unneccesary executions.
	 */
	public void quit(){
		actuator.quit();
		shutdown = true;
	}
	
	public int getNr(){return robotNr;}
	
	public void error(int error){
		this.error = error;
		//if this is not a co-error
		if (error != 2) factory.setError(robotNr, error);
		else System.err.println("Robot "+robotNr+": Going to co-error state...");
	}
}
