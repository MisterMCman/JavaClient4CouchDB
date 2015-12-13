package de.hshannover.reps;

import java.util.List;

import org.ektorp.support.CouchDbDocument;

public class Node extends CouchDbDocument{
	private static final long serialVersionUID = 4874142678344927254L;
	
	private int comments;
	private List<String> friends;
	
	public int getComments() {
		return comments;
	}
	public void setComments(int comments) {
		this.comments = comments;
	}
	public List<String> getFriends() {
		return friends;
	}
	public void setFriends(List<String> friends) {
		this.friends = friends;
	}
}
