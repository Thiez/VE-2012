package implementation;

/**
 * The main class for the robot factory implementation for Verification Engineering, a course given
 * at the university of Twente, 2011-2012. 
 * @author Roeland Kegel, Matthijs Hofstra
 */

public class Factory {

	private static FactoryController controller;
	private static FactoryModel model;	
	private static int iterations;
	private static int finishedProducts;


	/**
	 * Currently, this program does accept arguments from the command line, but does not use them.
	 * Future implementations will possibly include a custom set of instructions given at startup.
	 * @param args a list of command line arguments.
	 */
	public static void main(String[] args) {
		model = new FactoryModel();
		controller = new FactoryController(model);
		finishedProducts = 0;
		if (args.length == 0){
			controller.issueInstructions(0, "a");
			controller.issueInstructions(1,"a");
			controller.issueInstructions(2,"a");
			iterations = 1;
		}
		while(finishedProducts < iterations){
			while(!controller.checkIfDone()){
				try{
					Thread.sleep(5000);
				}catch(InterruptedException e){}
			}
			System.err.println("[System] all done! Signaling product removal.");
			iterations++;
			model.getPlatform().removeProduct();
		}
		System.err.println("[System] Done with product creation. Cleaning up & shutting down.");
		controller.quit();
	}

}
