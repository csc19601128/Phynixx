package org.csc.phynixx.loggersystem;

public interface ILogRecordReplayListener {
	
	void onRecord(short type, byte[][] message) ; 	   

}
