package implementation;

import factory.Logger;
import factory.ModelActions;

import java.util.*;
import java.io.*;

/**
 * The main class for the robot factory implementation for Verification Engineering, a course given
 * at the university of Twente, 2011-2012. 
 * @author Roeland Kegel, Matthijs Hofstra
 */

public class Factory {

	private static FactoryController controller;
	private static FactoryModel model;	
	//private static int iterations;
	//private static int finishedProducts;


	/**
	 * Currently, this program does accept arguments from the command line, but does not use them.
	 * Future implementations will possibly include a custom set of instructions given at startup.
	 * @param args a list of command line arguments.
	 */
	public static void main(String[] args) {
		Logger log = Logger.getInstance();
		//log.setDefaultVisibility();
		log.setDefaultObserver();
		log.setVisible( ModelActions.outgoing_product, ModelActions.incoming_product );
		model = new FactoryModel();
		controller = new FactoryController(model);
		int finishedProducts = 0;
		int iterations = 0;
		boolean interactive = false;
		if (args.length == 0){
			controller.issueInstructions(0,"a");
			controller.issueInstructions(1,"a");
			controller.issueInstructions(2,"a");
			iterations = 1;
		} else if (args.length == 1) {
			interactive = true;
			iterations = 10000;
		} else if (args.length == 3) {
			controller.issueInstructions(0,args[0]);
			controller.issueInstructions(1,args[1]);
			controller.issueInstructions(2,args[2]);
			iterations = 1;
		}
		while(finishedProducts < iterations){
			if (interactive) getInstructions(controller);	// Receive instructions each iteration.
			while(!controller.checkIfDone()){
				try{
					Thread.sleep(5000);
				}catch(InterruptedException e){}
			}
			System.err.println("[System] all done! Signaling product removal.");
			//iterations++;
			finishedProducts++;
			model.getPlatform().removeProduct();
			synchronized(model.getPlatform()) { model.getPlatform().notifyAll(); }
		}
		System.err.println("[System] Done with product creation. Cleaning up & shutting down.");
		controller.quit();
	}
	
	private static void getInstructions(FactoryController fc) {
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		for (int i = 0 ; i < 3 ; i++) {
			try {
				String s = br.readLine();
				if (s == null) {
					System.err.println("Error reading input!");
					System.exit(1);
				}
				if (s.trim() == "") return;
				String[] words = s.split("!");
				for (int j = 0 ; j < words.length ; j++) words[j] = words[j].trim();
				if (words.length != 4 || ! "issue_instructions".equals(words[0]) ) {
					System.err.println("Error: expected issue_instructions!x!y!z but received: "+ s );
					System.err.println("Ignoring and trying again.");
					i--; continue;
				}
				int roboNum = Integer.parseInt(words[1]);
				String zone1 = words[2];
				String zone2 = words[3];
				fc.issueInstructions(roboNum,zone1 + zone2);
			} catch (IOException ioe) {
				System.err.println("Error reading input!");
				System.exit(1);
			} catch (NumberFormatException nfe) {
				System.err.println("Error: received invalid robot number!");
				System.err.println("Ignoring and trying again.");
				i--; continue;
				// System.exit(1);
			}
		}
	}

}
