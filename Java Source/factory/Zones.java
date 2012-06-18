package factory;

public class Zones {
	public static final char IDLE = 'i';
	public static final char A = 'a';
	public static final char B = 'b';
	public static final char C = 'c';
	public static final char D = 'd';
	
	public static String errorTypes(int error){
		String result = "No Error";
		if (error == 0) result = "Sensory Error";
		else if(error == 1) result = "Actuator Error";
		else if(error == 2) result = "Co-Error";
		return result;
	}
	
	public static String zoneType(char zone){
		String result = "null";
		if (zone == A) result = "a";
		if (zone == B) result = "b";
		if (zone == C) result = "c";
		if (zone == D) result = "d";
		if (zone == IDLE) result = "idle";
		return result;
	}
}
