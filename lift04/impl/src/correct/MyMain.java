import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;


public class MyMain {
  public static void main (String[] args) {
    int miliseconds = 1*750;
    if (args.length == 1) {
        try {
            miliseconds = Integer.parseInt(args[0]);
            if (miliseconds < 0) {
                miliseconds =  -miliseconds;
            }
        } catch(NumberFormatException nfe) {
            report("ignoring invalid timeout argument: \""+args[0]+"\""+
                "; using default timeout ("+miliseconds+")");
        }
    }
    Lift lift = new Lift(3, false);
    lift.start();
    new UI(lift).play();
  }

  public static void report(String s) {
    System.err.println("lift: "+s);
  }
}
class UI {
  Lift lift;
  SimpleDateFormat fmt;

  public UI(Lift lift) {
    this.lift = lift;
    fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  }

  public void play() {
    BufferedReader r = new BufferedReader( new InputStreamReader(System.in));
    String s;
    try { 
     while((s = r.readLine()) != null) {
       String[] words = s.split("!");
       if (words.length==2 &&
           (words[0].trim().equals("press_cabin") || words[0].trim().equals("press_corridor"))) {
	   String k = words[0].trim();
	   String w = words[1].trim();
           try {
               int i = java.lang.Integer.parseInt(w);
               lift.getQ().add(new Integer(i));
               String src = (k.equals("press_cabin") ? "cab" : "cor");
	       MyMain.report("Input: ["+src+" "+i+"]");
           } catch(NumberFormatException nfe) {
                MyMain.report("ignoring invalid line: \""+w+"\"");
           }
       } else {
	    MyMain.report("skipping unexpected line: "+s);
       }
     }
    } catch(java.io.IOException e) {
            MyMain.report("io exception: "+e.toString());
	    System.exit(1);
    }
  }
}
