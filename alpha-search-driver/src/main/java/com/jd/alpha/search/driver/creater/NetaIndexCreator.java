package com.jd.alpha.search.driver.creater;

import com.jd.alpha.search.driver.client.FulltextService;
import com.jd.alpha.search.driver.client.IndexClient;
import com.jd.alpha.search.driver.model.Neta;
import com.jd.alpha.search.driver.utils.LocalFileDealer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NetaIndexCreator extends IndexCreator {
    private static final Log logger = LogFactory.getLog(NetaIndexCreator.class);

    public final static String INDEX_NAME = "neta";
    private volatile boolean isRunning = false;

    private FulltextService fulltextService;

    private AtomicInteger atomicInteger = new AtomicInteger(0);
    private static final int FLOOR_SIZE = 1000;
    private static final String separte = "\001";

    private static final int relaCateListSize = 3;
    private static final int venderListSize = 6;

    private static IndexClient client = IndexClient.getInstance();

    public NetaIndexCreator() {
        Creator creator = new Creator(this, INDEX_NAME);
        IndexCreatorManger.getInstance().registe(creator);
    }

    @Override
    public  boolean genData(){
        return true;
    }


    @Override
    public boolean index(String indexVersion) throws Exception {
        List<Neta> netaList = null;
        if (indexVersion == null ||
                indexVersion.trim().isEmpty()) {
            throw new RuntimeException("suggest init error");
        }
        try {
            netaList = this.loadData();
        } catch (Exception e) {
            throw new RuntimeException("load suggest data from mysql error", e);
        }
        if (!validateIndexDoc(netaList)) {
            throw new RuntimeException("vertification error,total num is " + netaList.size() + " less than " + FLOOR_SIZE);
        }
        boolean isSucceed = true;
        try {
            int i = 0, sep = 500, loop = netaList.size() / sep;
            for (; i < loop; i++) {
                isSucceed = isSucceed && updateDocs(netaList, indexVersion, i * sep, i * sep + sep);
            }
            isSucceed = isSucceed && updateDocs(netaList, indexVersion, loop * sep, netaList.size());
            // new size-save-method
            isSucceed = isSucceed && fulltextService.saveFullIndexCount(INDEX_NAME, indexVersion, netaList.size());
        } catch (Exception e) {
            throw e;
        }
        return isSucceed;
    }


    @Override
    public boolean isRunning() {
        if (isRunning) {
            logger.info("suggest reindex....");
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        isRunning = false;
        atomicInteger.set(0);
        logger.info("reset running state");
    }


    @Override
    public String initVersion() throws Exception {
        String indexVersion = null;
        try {
            indexVersion = fulltextService.init(INDEX_NAME);
        } catch (Exception e) {
            throw new RuntimeException("suggest init error", e);
        }
        return indexVersion;
    }


    @Override
    public boolean validateNewIndex(String indexVersion) throws Exception {
        boolean isSucceed=fulltextService.validateNewIndex(INDEX_NAME, indexVersion,null,INDEX_NAME,"");
        if(!isSucceed){
            logger.error("index error: validateNewIndex fail!IndexName:" + INDEX_NAME);
        }
        return isSucceed;
    }

    @Override
    public boolean changeIndexVersion(String indexVersion) throws Exception {
        boolean isSucceed = false;
        try {
            isSucceed = fulltextService.changeIndexVersion(INDEX_NAME, indexVersion);
            if (!isSucceed) {
                throw new RuntimeException("suggest changeIndexVersion error");
            }
        } catch (Exception e) {
            throw new RuntimeException("suggest changeIndexVersion error", e);
        }
        return isSucceed;
    }

    private List<Neta> loadData() {
        int tryTimes = 3;
        int i = 0;
        List<Neta> netaList = LocalFileDealer.readNetaData();
        return netaList;
    }

    private List<Map<String, Object>> transform(List<Neta> netaList) {
        if (null == netaList)
            return null;
        List<Map<String, Object>> docs = new ArrayList<>(netaList.size());
        for (Neta neta : netaList) {
            Map<String, Object> doc = new HashMap<>();
            doc.put("id", neta.getId());
            doc.put("cellIdInTopic", neta.getCellIdInTopic());
            doc.put("charactor", neta.getCharactor());
            doc.put("content", neta.getContent());
            doc.put("popular", neta.getPopular());
            doc.put("provenance", neta.getProvenance());
            doc.put("topicId", neta.getTopicId());

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String updateTime = simpleDateFormat.format(new Date());
            doc.put("updateTime", updateTime);
            doc.put("type", 0);

            docs.add(doc);
        }
        return docs;
    }


    private boolean validateIndexDoc(List<Neta> suggestList) {
        return null != suggestList && suggestList.size() > FLOOR_SIZE;
    }

    private boolean updateDocs(List<Neta> netaList, String indexVersion, int startIndex, int endIndex) {
        boolean success = false;
        try {
            List<Neta> subNetaList = netaList.subList(startIndex, endIndex);
            List<Map<String, Object>> indexMap = transform(subNetaList);
            if (indexMap.size() == 0) {
                success = true;
            } else {
                success = fulltextService.index(INDEX_NAME, indexVersion, indexMap);
            }

        } catch (Exception e) {
            throw new RuntimeException("update suggest data error", e);
        }
        if (!success) {
            throw new RuntimeException("update suggest data error");
        }
        return success;
    }


    public FulltextService getFulltextService() {
        return fulltextService;
    }

    public void setFulltextService(FulltextService fulltextService) {
        this.fulltextService = fulltextService;
    }

}




