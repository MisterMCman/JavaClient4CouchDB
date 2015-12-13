package de.hshannover.reps;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;

public class EdgeRepository extends CouchDbRepositorySupport<Edge>{

	public EdgeRepository(Class<Edge> type, CouchDbConnector db) {
		super(type, db);
	}

}
