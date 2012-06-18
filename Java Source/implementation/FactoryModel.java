package implementation;

import factory.*;

public class FactoryModel {
	public static final int NR_OF_ROBOTS = 3;
	private RobotController[] robots;
	private Platform platform;
	
	public FactoryModel(){
		robots = new RobotController[]{	new RobotController(0,"abdi",this), 
										new RobotController(1,"bcdi", this), 
										new RobotController(2, "acdi", this) };
		Thread robot1 = new Thread(robots[0],"robot-1");
		Thread robot2 = new Thread(robots[1],"robot-2");
		Thread robot3 = new Thread(robots[2],"robot-3");
		platform = new Platform(this);
		Thread p = new Thread(platform, "platform");
		robots[0].setToken(true);
		System.err.println("[System] Robots and Platform successfully intialized...");
		robot1.start();
		robot2.start();
		robot3.start();
		p.start();
	}
	
	public Platform getPlatform(){
		return platform;
	}
	
	public void quit(){
		for(int i=0;i<robots.length;i++)robots[i].quit();
		synchronized(this){this.notifyAll();}
		platform.shutdown();
	}
	
	public RobotController getRobot(int robot){
		return robots[robot];
	}
	
	public void setError(int robotNr, int error){
		for (int i=((robotNr+1) % robots.length); i!=robotNr; i=((i+1)%robots.length)){
			robots[i].error(2);
		}
		System.err.println("[System] The following error has been detected in robot "+robotNr+": "+ Zones.errorTypes(error));
	}
}
