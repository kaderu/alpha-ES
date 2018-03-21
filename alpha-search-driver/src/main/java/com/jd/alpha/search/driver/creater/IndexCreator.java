package com.jd.alpha.search.driver.creater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class IndexCreator {
	
	public abstract boolean isRunning();
	
	public abstract void reset();
	
	public abstract boolean genData();
	
	public abstract String initVersion() throws Exception;
	
	public abstract boolean index(String indexVersion)throws Exception;
	
	public abstract boolean changeIndexVersion(String indexVersion)throws Exception;
	
	public abstract boolean validateNewIndex(String indexVersion)throws Exception;

	private static final Log logger = LogFactory.getLog(IndexCreator.class);

	 public  boolean create(){
		 if(isRunning()){
			 return true;
		 }
		try {
			boolean isOk=genData();
			if(!isOk){
				logger.error("gen data error!");;
				reset();
				return false;
			}
			String	indexVersion = initVersion();
			if(indexVersion!=null&&indexVersion.length()>0){
				 isOk=isOk&& index(indexVersion);
			 }else{
				 isOk=false;
			 }
            if(!isOk){
            	return false;
            }
			if(!validateNewIndex(indexVersion)){
				return false;
			}
			if(!changeIndexVersion(indexVersion)){
				return false;
			}
			logger.info("indexVersion:" + indexVersion + " build success!");
		} catch (Exception e) {
			logger.error("create index error", e);
			reset();
		}finally{
			reset();
		}
		
		 
		return true;
		 
  }
}


