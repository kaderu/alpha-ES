package com.jd.alpha.search.driver.creater;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

public class IndexCreatorManger {
	private static IndexCreatorManger instance ;

	private Vector<Creator> indexCreators;
	private Map<String, Creator> indexCreatorsMap;
	
	
	
	public static synchronized IndexCreatorManger getInstance() {
		if (instance == null) {
			instance=new IndexCreatorManger();
		}
		return instance;
	}

	private IndexCreatorManger() {
		indexCreators = new Vector<Creator>();
		indexCreatorsMap = new Hashtable<String, Creator>();
	}

	public synchronized void registe(Creator creator) {
		if (creator != null) {
			indexCreators.add(creator);
			indexCreatorsMap.put(creator.getName(), creator);

		}
	}

	public Creator getCreator(String name) {
		if (indexCreatorsMap != null) {
			return indexCreatorsMap.get(name);
		}
		return null;
	}
}
