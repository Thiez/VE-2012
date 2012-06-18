interface Handler {
	public void reset();
	public void stop();
	public boolean hasToStop();
	public String applyStim(String s);
	public boolean hasObs();
	public String nextObs();
	public int getSleepTime();
}
