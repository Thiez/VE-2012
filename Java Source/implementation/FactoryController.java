package implementation;

import factory.*;

public class FactoryController {
	private FactoryModel factory;
	
	public FactoryController(FactoryModel factory){
		this.factory = factory;
	}
	
	public void issueInstructions(int robot, String instructions){
		//adding new instructions, wakey wakey robots...
		synchronized(factory){
			factory.notifyAll();
		}
		factory.getRobot(robot).addInstructions(instructions);
		//commence the UGLY HACKING 8D
		String listInstructions = "[";
		for (int i = 0; i<instructions.length();i++){
			listInstructions = listInstructions + instructions.substring(i,i+1) + ",";
		}
		int lastChar = listInstructions.length()-1;
		listInstructions = listInstructions.substring(0,lastChar) + "]";
		System.out.println("issueInstructions!"+robot+"!"+listInstructions+")");
	}

	public void issueAllClear(){
		Platform p = factory.getPlatform();
		p.removeProduct();
		//after removing the product, wake up the platform so that a new product can enter... Should be changed to reflect a
		//chance at an incoming product, later.
		synchronized(p){
			p.notify();
		}
	}
	
	public boolean checkIfDone(){
		boolean result = true;
		for(int i = 0; i<FactoryModel.NR_OF_ROBOTS; i++) result = (factory.getRobot(i).allDone()) && result;
		return result;
	}
	
	public void quit(){
		factory.quit();
	}
	
}
