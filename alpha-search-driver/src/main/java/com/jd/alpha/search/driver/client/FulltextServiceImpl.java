package com.jd.alpha.search.driver.client;

import com.jd.epx.ump.UmpJProfiler;
import com.yihaodian.search.common.model.RequestQuery;
import com.yihaodian.search.common.moniter.LogTypeEnum;
import com.yihaodian.search.index.business.index.MemberIndexCreator;
import com.yihaodian.search.index.business.index.ProductIndexCreator;
import com.yihaodian.search.index.business.index.SuggestIndexCreator;
import com.yihaodian.search.index.config.indexSchemaConfig.service.Schema2JsonService;
import com.yihaodian.search.index.config.thresholdConfig.model.Threshold;
import com.yihaodian.search.index.config.thresholdConfig.service.ThresholdService;
import com.yihaodian.search.index.service.IndexInfoService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FulltextServiceImpl implements FulltextService {
    static LogTypeEnum logger = LogTypeEnum.INDEX_TOOLS;

    static LogTypeEnum fullTextLogger = LogTypeEnum.FULL_INDEX;

    private IndexInfoService indexInfoService;

    private Schema2JsonService schema2JsonService;
    private ThresholdService thresholdService;
    private static final String MIN_INDEX_COUNT = "min_index_count";
    public static long MIN_PRODUCT_COUNT = 50000;
    public static long MIN_SUGGEST_COUNT = 3000;
    public static long MIN_MEMBER_COUNT = 5;

    private static final String MIN_DIFF_RATE = "min_diff_rate";
    private static double DEFAULT_DIFF_RATE = 0.1;

    IndexClient indexClient = IndexClient.getInstance();

    ConcurrentMap<String, Boolean> isIndexInitedMap = new ConcurrentHashMap<String, Boolean>();


    @Override
    public String init(String indexName) {
        long currentTimeMillis = System.currentTimeMillis();

        String versionName = indexName + "_" + currentTimeMillis;

        String templateContent = genTemplateContent(indexName);

        indexClient.createIndex(versionName, templateContent);

        indexInfoService.addIndexVersionStatus(indexName, versionName, 1);

        return versionName;
    }

    @Override
    public boolean index(String indexName, String indexVersion,
                         List<Map<String, Object>> indexMap) throws Exception {
        if (isIndexInitedMap.size() > 200) {    //索引是否初始化缓存大小限制在200
            isIndexInitedMap = new ConcurrentHashMap<String, Boolean>();
        }
        Boolean isInited = isIndexInitedMap.get(indexVersion);
        if (isInited == null) {
            synchronized (this) {
                if (!isIndexInitedMap.containsKey(indexVersion)) {
                    isInited = checkVersionIsInited(indexVersion);
                    isIndexInitedMap.put(indexVersion, isInited);
                }else{
                    isInited = isIndexInitedMap.get(indexVersion);
                }
            }
        }

        if (!isInited) {
            return false;
        }
        boolean isSuccess = indexClient.bulkIndex(indexVersion, indexName, indexMap);
        if (!isSuccess) {
            logger.errorPlaceholderWithUmp("create index:{0} error!", indexVersion);
        }

        return isSuccess;
    }

    @Override
    public boolean saveFullIndexCount(String indexName, String indexVersion , long count){
        try {
            indexInfoService.recordIndexedCount(indexName, indexVersion, (int)count, "success");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean checkVersionIsInited(String indexVersion) {
        return indexInfoService.isCurrentIndexVersionInited(indexVersion);
    }

    @UmpJProfiler
    @Override
    public boolean changeIndexVersion(String indexName, String indexVersion) {
        boolean isSuccessed = true;
        boolean isAliasExist = indexClient.isAliasExist(indexName);
        String curentIndexVersion;
        if (!isAliasExist) {
            return indexClient.addAliasIndex(indexVersion, indexName);
        } else {
            curentIndexVersion = indexClient.getAliasIndex(indexName);

            if (curentIndexVersion != null && curentIndexVersion.trim().length() > 0) {
                isSuccessed &= indexClient.repalceIndex(curentIndexVersion, indexVersion, indexName);
                if (!isSuccessed) {
                    logger.errorWithUmp("index switch error!");
                    return false;
                }
            }
        }

        //只保留最近3天的索引
        curentIndexVersion = indexClient.getAliasIndex(indexName);
        List<String> allIndexs = indexClient.getAllIndex(indexName);
        if (curentIndexVersion != null && curentIndexVersion.trim().length() > 0) {
            Set<String> deletedIndics = new HashSet<String>();
            if (allIndexs != null && allIndexs.size() > 3) {
                Collections.sort(allIndexs);
                List<String> needDeleteIndexs = allIndexs.subList(0, allIndexs.size() - 3);
                for (String oldIndex : needDeleteIndexs) {
                    if (oldIndex.equals(curentIndexVersion)) {
                        continue;
                    }
                    deletedIndics.add(oldIndex);
                    indexClient.deleteIndex(oldIndex);
                }
            }

            //其他的索引需要关闭,否则浪费内存空间
            if (allIndexs != null) {
                for (String oldIndex : allIndexs) {
                    if (oldIndex.equals(curentIndexVersion) || deletedIndics.contains(oldIndex)) {
                        continue;
                    }
                    indexClient.closeIndex(oldIndex);
                }
            }
        }

        indexInfoService.updateCurentIndexVersionInit(indexVersion, 2);

        return isSuccessed;
    }


    private String genTemplateContent(String indexName) {
        String content = schema2JsonService.transFormSchema2Json(indexName);
        logger.info(content);
        return content;
    }

    public void setIndexInfoService(IndexInfoService indexInfoService) {
        this.indexInfoService = indexInfoService;
    }

    public Schema2JsonService getSchema2JsonService() {
        return schema2JsonService;
    }

    public void setSchema2JsonService(Schema2JsonService schema2JsonService) {
        this.schema2JsonService = schema2JsonService;
    }

    @UmpJProfiler
    @Override
    public boolean validateNewIndex(String indexName, String indexVersion, List<String> hotwords, String type, String field) {
        boolean isOk = indexClient.flush(indexVersion);
        if (!isOk) {
            return isOk;
        }
        boolean isAliasExist = indexClient.isAliasExist(indexName);
        if (!isAliasExist) {
            return isOk;
        }
        RequestQuery requestQuery = new RequestQuery();
        long newCount = indexClient.getCount(requestQuery, indexVersion, type);
        Threshold indexCountThreshold = thresholdService.getThresholdByKey(MIN_INDEX_COUNT + "_" + indexName);
        Long minIndexCount = null;
        if (indexCountThreshold != null && indexCountThreshold.getThresholdValue() != null) {
            try {
                minIndexCount = Long.parseLong(indexCountThreshold.getThresholdValue().trim());
            } catch (Exception e) {
                logger.errorPlaceholderWithUmp("参数:{0} Error ", MIN_INDEX_COUNT + "_" + indexName);
            }
        }
        if (minIndexCount == null) {
            if (ProductIndexCreator.INDEX_NAME.equals(indexName)) {
                minIndexCount = MIN_PRODUCT_COUNT;
            } else if (SuggestIndexCreator.INDEX_NAME.equals(indexName)) {
                minIndexCount = MIN_SUGGEST_COUNT;
            } else if (MemberIndexCreator.INDEX_NAME.equals(indexName)) {
                minIndexCount = MIN_MEMBER_COUNT;
            }
        }
        if (minIndexCount == null) {
            return false;
        }
        if (newCount < minIndexCount) {
            logger.errorPlaceholderWithUmp("版本:{0}总结果验证不通过,如果确定索引没问题，请手动切换", indexVersion);
            return false;
        }

        double diffRate = genDiffRate(indexName, indexVersion, requestQuery, type);
        Threshold diffRateThreshold = thresholdService.getThresholdByKey(MIN_DIFF_RATE + "_" + indexName);
        Double minDiffRateThreshold = null;
        if (diffRateThreshold != null && diffRateThreshold.getThresholdValue() != null) {
            try {
                minDiffRateThreshold = Double.parseDouble(diffRateThreshold.getThresholdValue().trim());
            } catch (Exception e) {
                logger.errorPlaceholderWithUmp("参数:{0} Error ", MIN_DIFF_RATE + "_" + indexName);
            }
        }
        if (minDiffRateThreshold == null) {
            minDiffRateThreshold = DEFAULT_DIFF_RATE;
        }
        if (diffRate > minDiffRateThreshold) {
            logger.errorPlaceholderWithUmp("版本:{0}差异率验证不通过,如果确定索引没问题，请手动切换", indexVersion);
            return false;
        }

        return isOk;
    }


    private double genDiffRate(String indexName, String indexVersion, RequestQuery request, String type) {
        double diffRate = 0;
        long newCount = indexClient.getCount(request, indexVersion, type);
        fullTextLogger.info(indexVersion + " :  count : " + newCount);
        long currentCount = indexClient.getCount(request, indexName, type);

        long diffCount = currentCount - newCount;

        diffRate = (double) diffCount / (double) currentCount;

        return diffRate;
    }

    @Override
    public boolean updateIndex(String indexVersion, String type,
                               List<Map<String, Object>> indexMap) throws Exception {
        return indexClient.bulkIndex(indexVersion, type, indexMap);
    }

    @Override
    public boolean deleteIndexDocs(String indexVersion, String indexName, List<Long> ids) throws Exception {
        return indexClient.deleteIndexDocs(indexVersion, indexName, ids);
    }

    @Override
    public String getCurentIndexVersion(String indexName) {
        return indexClient.getAliasIndex(indexName);
    }

    @Override
    public boolean deleteIndexByVersion(String indexVersion) {
        return indexClient.deleteIndex(indexVersion);
    }

    @Override
    public boolean checkNewIndexCount(String indexName, String indexVersion) {
        boolean isFlushed = indexClient.flush(indexVersion);
        if (!isFlushed) {
            return isFlushed;
        }

        boolean isAliasExist = indexClient.isAliasExist(indexName);
        if (!isAliasExist) {
            return true;
        }

        Threshold indexCountThreshold = thresholdService.getThresholdByKey(MIN_INDEX_COUNT + "_" + indexName);

        Long minIndexCount = null;
        if (indexCountThreshold != null && indexCountThreshold.getThresholdValue() != null) {
            try {
                minIndexCount = Long.parseLong(indexCountThreshold.getThresholdValue().trim());
            } catch (Exception e) {
                logger.errorPlaceholderWithUmp("参数:{0} Error ", MIN_INDEX_COUNT + "_" + indexName);
            }
        }

        RequestQuery requestQuery = new RequestQuery();
        long newCount = indexClient.getCount(requestQuery, indexVersion, indexName);

        if (minIndexCount == null) {
            minIndexCount = MIN_PRODUCT_COUNT;
        }
        if (newCount < minIndexCount) {
            logger.errorPlaceholderWithUmp("版本:{0}总结果验证不通过,如果确定索引没问题，请手动切换", indexVersion);
            return false;
        }

        /*double diffRate = genDiffRate(indexName, indexVersion, requestQuery, indexName);
        if (diffRate > 0.1) {
            logger.errorPlaceholderWithUmp("版本:{0}总结果验证不通过,如果确定索引没问题，请手动切换", indexVersion);
            return false;
        }*/
        return true;
    }

    public void setThresholdService(ThresholdService thresholdService) {
        this.thresholdService = thresholdService;
    }

}
