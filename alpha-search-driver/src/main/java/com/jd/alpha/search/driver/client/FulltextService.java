package com.jd.alpha.search.driver.client;

import java.util.List;
import java.util.Map;

public interface FulltextService {
	
	String init(String indexName)throws Exception;
	
	boolean index(String indexName, String indexVersion, List<Map<String, Object>> indexMap)throws Exception;
	
	boolean changeIndexVersion(String indexName, String indexVersion)throws Exception;
	
	boolean validateNewIndex(String indexName, String indexVersion, List<String> hotwords, String type, String field)throws Exception;
	
	boolean updateIndex(String indexVersion, String type, List<Map<String, Object>> indexMap)throws Exception;
	
	boolean deleteIndexDocs(String indexVersion, String type, List<Long> ids) throws Exception;
	
	String getCurentIndexVersion(String indexName);

	boolean deleteIndexByVersion(String indexVersion);

	boolean checkNewIndexCount(String indexName, String indexVersion);

	public boolean saveFullIndexCount(String indexName, String indexVersion, long count);

}
