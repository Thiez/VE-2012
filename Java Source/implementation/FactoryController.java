package implementation;

import factory.*;

public class FactoryController {
	private FactoryModel factory;
	
	public FactoryController(FactoryModel factory){
		this.factory = factory;
	}
	
	public void issueInstructions(int robot, String instructions){
		factory.getRobot(robot).addInstructions(instructions);
	}

	public void issueAllClear(){
		
		factory.getPlatform().removeProduct();
	}
	
	public boolean checkIfDone(){
		boolean result = true;
		for(int i = 0; (i<FactoryModel.NR_OF_ROBOTS) || result == false; i++){
			result = factory.getRobot(i).allDone();
		}
		return result;
	}
}
