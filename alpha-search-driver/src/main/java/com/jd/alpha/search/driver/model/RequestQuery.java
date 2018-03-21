package com.jd.alpha.search.driver.model;

/**
 * Created by zhangyingying2 on 2016/11/22.
 */
public class RequestQuery {
//    private String indexName;
    private String termQueryKey;
    private String termQueryValue;
    private String termsName;
    private String fieldName;
    private int limit;

//    public String getIndexName() {
//        return indexName;
//    }
//
//    public void setIndexName(String indexName) {
//        this.indexName = indexName;
//    }

    public String getTermQueryKey() {
        return termQueryKey;
    }

    public void setTermQueryKey(String termQueryKey) {
        this.termQueryKey = termQueryKey;
    }

    public String getTermQueryValue() {
        return termQueryValue;
    }

    public void setTermQueryValue(String termQueryValue) {
        this.termQueryValue = termQueryValue;
    }

    public String getTermsName() {
        return termsName;
    }

    public void setTermsName(String termsName) {
        this.termsName = termsName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
