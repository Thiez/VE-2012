package implementation;

/**
 * The main class for the robot factory implementation for Verification Engineering, a course given
 * at the university of Twente, 2011-2012. 
 * @author Roeland Kegel, Matthijs Hofstra
 */

public class Factory {

	private static FactoryController controller;
	private static FactoryModel model;	
	
	
	/**
	 * Currently, this program does accept arguments from the command line, but does not use them.
	 * Future implementations will possibly include a custom set of instructions given at startup.
	 * @param args a list of command line arguments.
	 */
	public static void main(String[] args) {
		model = new FactoryModel();
		controller = new FactoryController(model);
		controller.issueInstructions(0, "aa");
		controller.issueInstructions(1,"aa");
		controller.issueInstructions(2,"aa");
		while(!controller.checkIfDone()){
			try{
			Thread.sleep(5000);
			}catch(InterruptedException e){}
		}
		System.out.println("all done! Signaling product removal.");
		model.getPlatform().removeProduct();
		System.out.println("Done with product creation. Cleaning up & shutting down.");
		controller.quit();
	}

}
