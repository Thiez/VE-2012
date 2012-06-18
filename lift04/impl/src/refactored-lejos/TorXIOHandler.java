import lejos.nxt.*;
import lejos.nxt.addon.*;
import lejos.nxt.comm.*;
import java.io.*;
import java.util.ArrayList;


class TorXIOHandler extends Thread {
	protected DataInputStream dIn;
	protected DataOutputStream dOut;
	private long lastTime = System.currentTimeMillis();
	private Handler handler;
	
	public TorXIOHandler(DataInputStream in, DataOutputStream out, Handler handler){
		dIn = in;
		dOut = out;
		this.handler = handler;
		LCD.drawString("iohandler, cons",0,7);
	}

	public void reset() {
	}
	
	public void run() {
		LCD.drawString("iohandler run ",0,7);
		String c_inp = "C_INPUT";
		String c_outp = "C_OUTPUT";
		while (!handler.hasToStop()) {
			LCD.scroll();
			LCD.drawString("waiting for input...",0,7);
            		String s = "";
			StringBuffer strBuffer = new StringBuffer();
			char ch;
		 	try {
                                ch = (char)dIn.readByte();
                                while(ch != '\n') {
                                        strBuffer.append(ch);
                                        ch = (char)dIn.readByte();
                                }
                                s = strBuffer.toString();
                                strBuffer.delete(0, strBuffer.length());
                                LCD.scroll();
                                LCD.drawString("read: "+s,0,7);
				
				if (s.length() >= c_inp.length()+1 &&
				    s.substring(0, c_inp.length()+1).equals(c_inp+" ")) {
					String rest = s.substring(c_inp.length()+1);
                                	LCD.scroll();
                                	LCD.drawString("stim: "+rest,0,7);
					String res;
					if(handler != null && handler.hasObs()) {
						String o = handler.nextObs();
                                		LCD.scroll();
                                		LCD.drawString("got obs: "+o,0,7);
						writeOut("A_OUTPUT event="+o+"\n");
						lastTime = System.currentTimeMillis();
					} else if (handler != null &&
					    (res = handler.applyStim(rest)) != null) {
						writeOut("A_INPUT event="+res+"\n");
						lastTime = System.currentTimeMillis();
					} else {
						writeOut("A_INPUT_ERROR"+"\n");
					}
				} else if (s.length() >= c_outp.length()+1 &&
				    s.substring(0, c_outp.length()+1).equals(c_outp+" ")) {
					String rest = s.substring(c_outp.length()+1);
					long timeOut;
					try {
						timeOut = Long.parseLong(rest);
					} catch (Exception e) {
						writeOut("A_OUTPUT_ERROR"+"\n");
						continue;
					}
                                	LCD.scroll();
                                	LCD.drawString("get obs",0,7);
					while(!handler.hasObs() &&
				              System.currentTimeMillis() < (lastTime + timeOut)) {
                                		//LCD.scroll();
                                		LCD.drawString("obs... delay",0,7);
						try { 
							Thread.sleep(handler.getSleepTime());
						} catch(java.lang.InterruptedException e) {
                                			LCD.scroll();
                                			LCD.drawString("sleep interrupted",0,7);
							break;
						}
					}
					if(handler.hasObs()) {
						String o = handler.nextObs();
                                		LCD.scroll();
                                		LCD.drawString("got obs: "+o,0,7);
						writeOut("A_OUTPUT event="+o+"\n");
						lastTime = System.currentTimeMillis();
					} else {
						writeOut("A_OUTPUT event=delta"+"\n");
                                		LCD.scroll();
                                		LCD.drawString("no obs: delta",0,7);
					}
				} else if (s.equals("C_IOKIND")) {
					writeOut("A_IOKIND \n"); // NOTE: space after keyword is necessary!
				} else if (s.equals("C_RESET")) {
					resetHandler();
					reset();
					//writeOut("A_RESET\n");
				} else if (s.equals("C_QUIT")) {
					stopHandler();
					writeOut("A_QUIT\n");
					dOut.close();
					dIn.close();
				} else {
					writeOut("A_INPUT_ERROR"+"\n");
				}
                        } catch (EOFException e) {
                                LCD.scroll();
                                LCD.drawString("read eof exception", 0, 7);
				stopHandler();
				try {
					dOut.close();
					dIn.close();
				} catch (IOException usbe) {}
                                break;
                        } catch (IOException e) {
                                LCD.scroll();
                                LCD.drawString("read io exception", 0, 7);
				stopHandler();
				try {
					dOut.close();
					dIn.close();
				} catch (IOException usbe) {}
                                break;
                        }

		}
	}

	public void writeOut(String s) {
		try {
			dOut.writeBytes(s);
			dOut.flush();
		} catch (java.io.IOException e) {
		}
	}
	public void stopHandler() {
		if (handler != null) {
			handler.stop();
		}
	}
	public void resetHandler() {
		if (handler != null) {
			handler.reset();
		}
	}

}
