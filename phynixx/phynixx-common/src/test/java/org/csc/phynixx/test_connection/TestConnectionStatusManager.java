package org.csc.phynixx.test_connection;

import java.util.HashMap;
import java.util.Map;

public class TestConnectionStatusManager {
	
	private static Map connectionStati= new HashMap();
	
	public static synchronized TestStatusStack getStatusStack(Object id) {
		return (TestStatusStack)(connectionStati.get(id));
	}
	
	public static synchronized void addStatusStack(Object id) {
		if(!connectionStati.containsKey(id)) {
			connectionStati.put(id,new TestStatusStack(id));
		}
	}
	
	public synchronized static void clear() {
		connectionStati.clear();
	}
	
	

}
