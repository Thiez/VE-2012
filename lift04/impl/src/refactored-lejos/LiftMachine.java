public interface LiftMachine {
	void reset();
	void openDoor();
	void closeDoor();
	void up();
	void down();
	void doSleep(long n);
	void output(String s);
	void message(String s);
	void updateRequests(Requests r);
}
