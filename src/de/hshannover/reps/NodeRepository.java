package de.hshannover.reps;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;

public class NodeRepository extends CouchDbRepositorySupport<Node>{

	public NodeRepository(Class<Node> type, CouchDbConnector db) {
		super(type, db);
	}

}
