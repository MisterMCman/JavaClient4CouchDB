package de.hshannover.reps;

import org.ektorp.support.CouchDbDocument;

public class Edge extends CouchDbDocument{
	private static final long serialVersionUID = 4874142678344927254L;
	
	private int lastActive;
	private String title;
	
	public int getLastActive() {
		return lastActive;
	}
	public void setLastActive(int lastActive) {
		this.lastActive = lastActive;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
