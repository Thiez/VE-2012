package factory;

import implementation.*;

public class Platform implements Runnable{
	private FactoryModel factory;
	private boolean shutdown;
	
	public Platform(FactoryModel factory){
		shutdown = false;
		this.factory = factory;
	}
	
	public void run(){
		System.err.println("[System] platform starting...");
		while(!shutdown){
			newProduct();
			synchronized(this){
				try{this.wait();}
				catch(InterruptedException e){System.err.println("an interruptedexception happened, wut?");}
			}
		}
	}
	
	private void setOnlineStatus(boolean online){
		if (online) {
			//System.out.println("bringOnline");
			Logger.getInstance().inform( ModelActions.bring_online );
		} else {
			//System.out.println("bringOffline");
			Logger.getInstance().inform( ModelActions.bring_offline );
		}
		for(int i = 0; i<FactoryModel.NR_OF_ROBOTS; i++){
			factory.getRobot(i).setOnline(online);
		}
	}
	
	public void newProduct(){
		//Product is now available.
		Logger.getInstance().inform( ModelActions.incoming_product );
		setOnlineStatus(true);
	}
		
	public void removeProduct(){
		Logger.getInstance().inform( ModelActions.outgoing_product );
		setOnlineStatus(false);
		//Product is now no longer available.
	}
	
	public void shutdown(){
		shutdown = true;
		synchronized(this){this.notify();}
	}
	
}
