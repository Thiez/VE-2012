package factory;

import factory.ModelActions;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.HashSet;

/**
 * User: Thiez
 * Date: 17-6-12
 * Time: 15:58
 */
public class Logger extends Observable {

	private static final Logger instance = new Logger();
	private Set<ModelActions> visible = new HashSet<ModelActions>();
	
	private Logger() {}

	public void setDefaultVisibility() {
		visible.clear();
		//addToVisible( ModelActions.values() );
		addToVisible(	ModelActions.bring_offline,
						ModelActions.bring_online,
						ModelActions.forw_token,
						ModelActions.doneWork,
						ModelActions.confirmation,
						ModelActions.requestClearance,
						ModelActions.denyClearance,
						ModelActions.grantClearance,
						ModelActions.moveToZone,
						ModelActions.issue_instructions,
						ModelActions.all_clear,
						ModelActions.all_clear_conf,
						ModelActions.incoming_product,
						ModelActions.outgoing_product,
						ModelActions.all_done,
						ModelActions.goError,
						ModelActions.fault,
						ModelActions.rejection
				);
	}
	
	private class DefaultObserver implements Observer {
		@Override
		public void update(Observable o, Object arg) {
			//if (arg == null || !(arg instanceof LoggerEvent)) return;
			LoggerEvent le = (LoggerEvent)arg;
			System.out.println( le.toString() );
		}
	}
	
	public void setDefaultObserver() {
		addObserver( new DefaultObserver() );
	}

	public static Logger getInstance() {
		return instance;
	}
	
	public void setVisible(ModelActions ... as) {
		visible.clear();
		addToVisible(as);
	}
	
	public void addToVisible(ModelActions ... as) {
		for (ModelActions a : as)
			visible.add(a);
	}
	
	public void removeFromVisible(ModelActions ... as) {
		for (ModelActions a : as)
			visible.remove(a);
	}
	
	public void inform(ModelActions a, Object ... args) {
		if (visible.contains(a)) {
			setChanged();
			notifyObservers( new LoggerEvent(a, args) );
		}
	}
	
	public static class LoggerEvent {
		public final ModelActions action;
		public final Object[] args;
		
		public LoggerEvent(ModelActions action, Object ... args) {
			if (action == null) throw new NullPointerException("Action cannot be null!");
			this.action = action;
			Object[] myArgs = new Object[args.length];
			for (int i = 0 ; i < args.length ; i++)
				myArgs[i] = args[i];
			this.args = myArgs;
		}
		
		@Override
		public String toString() {
			String result = action.name();
			for (Object arg : args)
				result += "!" + arg.toString();
			result += "";	// ")" ?
			return result;
		}
	}
	
	public static void main(String[] args) {
		java.util.List<Character> li = new java.util.ArrayList<Character>();
		li.add('a'); li.add('b');
		System.out.println(li);
		LoggerEvent le = new LoggerEvent(ModelActions.requestClearance, new Integer(1), 2, 'a',li);
		System.out.println(le.toString());
		Logger.instance.setDefaultVisibility();
		Logger.instance.setDefaultObserver();
		Logger.instance.inform(ModelActions.denyClearance, 666);
	}
}
