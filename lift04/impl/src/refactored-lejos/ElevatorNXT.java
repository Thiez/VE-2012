import lejos.nxt.*;
import lejos.nxt.addon.*;
import lejos.nxt.comm.*;
import java.io.*;
import java.util.ArrayList;


public class ElevatorNXT implements Handler, LiftMachine {

	private int nLevels = 4;

	private LiftController liftController;

	private int oneLevel;

	static TouchSensor botSensor = new TouchSensor(SensorPort.S1);
	static TouchSensor topSensor = new TouchSensor(SensorPort.S2);

	private ArrayList<String> obsQ = new ArrayList<String>();
	private ArrayList<String> visitQ = new ArrayList<String>();

        private USBConnection con;
        private DataOutputStream dOut;
        private DataInputStream dIn;

	private TorXIOHandler iohandler;


	public static void main(String[] args) {

                try {
                        Button.ESCAPE.addButtonListener(new ButtonListener() {
                                public void buttonPressed(Button b) {
                                        System.exit(0);
                                }

                                public void buttonReleased(Button b) {
                                }
                        });
                }
                catch (Exception e) {}

                while(true) {
                        LCD.drawString("waiting for USB", 0,7 );
                        USBConnection conn = USB.waitForConnection();
                        LCD.scroll();
                        LCD.drawString("connected to USB", 0,7 );

                        //stop = false;
			ElevatorNXT elevator = new ElevatorNXT(conn);
			elevator.go();
                }
	}

        public ElevatorNXT(USBConnection c) {
                con = c;
                dOut = c.openDataOutputStream();
                dIn = c.openDataInputStream();
                LCD.scroll();
                LCD.drawString("connection made",0,7 );
		liftController = new LiftController(nLevels, this);
		reset();
		iohandler = new TorXIOHandler(dIn, dOut, this);
	}


//  {-----  Methods that implement Handler interface and LiftMachine interface

	public void reset() {
		obsQ.clear();
		visitQ.clear();
	}

//  }-----  Methods that implement Handler interface and LiftMachine interface

//  {-----  Methods to implement Handler interface

	public void stop() {
	}
	public String applyStim(String s) {
		int i = s.indexOf('!');
		if (i >= 0) {
			visitQ.add(s.substring(i+1));
		} else {
			visitQ.add(s);
		}
		return s;
	}
	public boolean hasObs() {
		return obsQ.size() > 0;
	}
	public String nextObs() {
		if (obsQ.size() == 0) {
			return null;
		}
		String o = obsQ.get(0);
		obsQ.remove(0);
		return o;
	}
	public boolean hasToStop() {
		return false;
	}

	public int getSleepTime() { return 20; }


//  }------  Methods to implement Handler interface


//  {------  Methods to implement LiftMchine interface

	public void openDoor() {
		moveDoor(true);
	}
	public void closeDoor() {
		moveDoor(false);
	}
	
	public void up() {
		goOneLevel(1);
	}
	public void down() {
		goOneLevel(-1);
	}

	public void doSleep(long n) {
		try {
			Thread.sleep(n);
		} catch(Exception e) {
			LCD.drawString("sleep interrupt", 0, 3);
		}
	}

	public void output(String s) {
		obsQ.add(s);
	}
	public void message(String s) {
		LCD.drawString(s, 0, 0);
	}

	public void updateRequests(Requests req) {
		String s;
		while(visitQ.size() > 0) {
			s = visitQ.get(0);
			visitQ.remove(0);
			int i = Integer.parseInt(s);
			req.add(i);
		}
	}

//  }------  Methods to implement LiftMchine interface

	private void calibrate() {
		LCD.drawString("Calibration", 0, 0);

		Motor.A.forward();
		while(!topSensor.isPressed()) {
			doSleep(50);
		}
		Motor.A.stop();
		Motor.A.resetTachoCount();
		Motor.A.backward();
		while(!botSensor.isPressed()) {
			doSleep(50);
		}
		Motor.A.stop();
		int count = Motor.A.getTachoCount();
		LCD.drawString("count "+count, 0, 0);
		doSleep(1000);
		oneLevel = -1*count/(nLevels-1);
	}

	private void moveDoor(boolean open) {
		Motor.B.setPower(100);
		if(open)
			Motor.B.forward();
		else
			Motor.B.backward();
		doSleep(3500);
		Motor.B.stop();
	}

	private void goOneLevel(int d) {
		Motor.A.rotate(d*oneLevel, false);
	}
		
	private void go() {
		boolean hasToStop = false;
		boolean idled;

		calibrate(); // after 1t 0

		LCD.drawString("Calibration done", 0, 0);
		doSleep(1000);

		iohandler.start();
		LCD.drawString("iohandler started", 0, 0);

		liftController.go();
	}
}
