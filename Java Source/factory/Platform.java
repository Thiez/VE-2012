package factory;

import implementation.*;

public class Platform implements Runnable{
	private boolean product;
	private FactoryModel factory;
	
	public Platform(FactoryModel factory){
		this.factory = factory;
		product = false;
	}
	
	public void run(){
		System.out.println("platform starting...");
		newProduct();
		while(product){
			//if(!product) newProduct();
			try{
			Thread.sleep(5000);
			}catch(InterruptedException e){System.out.println("platform was unexpectedly interrupted.");}
		}
	}
	
	private void setOnlineStatus(boolean online){
		for(int i = 0; i<FactoryModel.NR_OF_ROBOTS; i++){
			factory.getRobot(i).setOnline(online);
		}
	}
	
	public void newProduct(){
		product = true;
		setOnlineStatus(true);
	}
		
	public void removeProduct(){
		setOnlineStatus(false);
		product = false;
	}
	
}
