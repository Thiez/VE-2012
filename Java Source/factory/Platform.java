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
		if (online) System.out.println("bringOnline");
			else System.out.println("bringOffline");
		for(int i = 0; i<FactoryModel.NR_OF_ROBOTS; i++){
			factory.getRobot(i).setOnline(online);
		}
	}
	
	public void newProduct(){
		//Product is now available.
		setOnlineStatus(true);
	}
		
	public void removeProduct(){
		setOnlineStatus(false);
		//Product is now no longer available.
	}
	
	public void shutdown(){
		shutdown = true;
		synchronized(this){this.notify();}
	}
	
}
