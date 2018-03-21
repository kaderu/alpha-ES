package com.jd.alpha.search.driver.client;

import com.google.common.collect.UnmodifiableIterator;
import com.jd.alpha.search.driver.model.RequestQuery;
import com.jd.alpha.search.driver.utils.PropertiesUtil;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.cluster.node.stats.NodeStats;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsRequest;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.alias.exists.AliasesExistResponse;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesResponse;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IndexClient {
    private static IndexClient indexClient;
    private static final Log logger = LogFactory.getLog(IndexClient.class);

    static TransportClient client = null;
    private static Properties props = PropertiesUtil.getProperties();

    public static synchronized IndexClient getInstance() {
        if (client == null) {
            initClient();
        }
        return indexClient;
    }

    public void close() {
        client.close();
    }

    public static boolean deleteIndex(String index) {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        DeleteIndexResponse response = indicesAdminClient.prepareDelete(index).execute().actionGet();
        return response.isAcknowledged();
    }


    public static boolean closeIndex(String... indices) {
        CloseIndexRequest request = new CloseIndexRequest(indices);
        CloseIndexResponse response = client.admin().indices().close(request).actionGet();
        return response.isAcknowledged();
    }


    public static boolean openIndex(String index) {
        OpenIndexRequest request = new OpenIndexRequest(index);
        OpenIndexResponse response = client.admin().indices().open(request).actionGet();
        return response.isAcknowledged();
    }

    public static boolean repalceIndex(String oldIndex, String newIndex, String alias) {
        boolean isAliasExist = isAliasExist(alias);
        boolean isSucessed = false;
        if (!isAliasExist) {
            isSucessed = addAliasIndex(newIndex, alias);
        } else {
            isSucessed = addAliasIndex(oldIndex, newIndex, alias);
        }
        return isSucessed;
    }

    public static String getAliasIndex(String aliases) { //获取当前索引版本名称
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        List<String> indexNames = new ArrayList<String>();
        GetAliasesResponse response = indicesAdminClient.prepareGetAliases(aliases).get();
        ImmutableOpenMap<String, List<AliasMetaData>> aliasesMap = response.getAliases();
        UnmodifiableIterator<String> iterator = aliasesMap.keysIt();
        while (iterator.hasNext()) {
            String key = iterator.next();

            indexNames.add(key);
        }
        if (indexNames.isEmpty()) {
            return null;
        }
        return indexNames.get(0);
    }

    public static List<String> getAllIndex(String aliases) { //所有的版本
        List<String> indexs = new ArrayList<String>();
        ClusterStateRequest clusterStateRequest = new ClusterStateRequest();
        ClusterStateResponse clusterStateResponse =
                client.admin().cluster().state(clusterStateRequest).actionGet();

        ImmutableOpenMap<String, IndexMetaData> indexMap =
                clusterStateResponse.getState().metaData().indices();


        if (indexMap == null || indexMap.size() == 0) {
            return indexs;
        }

        UnmodifiableIterator<IndexMetaData> indexIterator = indexMap.valuesIt();

        while (indexIterator.hasNext()) {
            IndexMetaData indexMetaData = indexIterator.next();
            if (indexMetaData.getIndex().matches(aliases + "_" + "[0-9]*")) {
                indexs.add(indexMetaData.getIndex());
            }
        }
        return indexs;
    }


    public static boolean isAliasExist(String... aliases) {//判断别名是否存在
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        AliasesExistResponse response = indicesAdminClient.prepareAliasesExist(aliases).get();
        return response.isExists();
    }

    public static boolean addAliasIndex(String newIndex, String alias) {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        IndicesAliasesResponse response = indicesAdminClient.prepareAliases().addAlias(newIndex, alias).get();
        return response.isAcknowledged();
    }

    //product_x ---> product
    public static boolean addAliasIndex(String oldIndex, String newIndex, String alias) {
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        IndicesAliasesResponse response = indicesAdminClient.prepareAliases().addAlias(newIndex, alias).removeAlias(oldIndex, alias).get();
        return response.isAcknowledged();
    }

    public void createTempelate(String indexName, String templateContent) {
        PutIndexTemplateRequest request = new PutIndexTemplateRequest(indexName)
                .source(templateContent);
        client.admin().indices().putTemplate(request).actionGet();
    }

    public void createIndex(String indexName, String templateContent) {
        CreateIndexRequest request = new CreateIndexRequest(indexName).source(templateContent);
        client.admin().indices().create(request).actionGet();
    }


    public void deleteTempelate(String vserion) {
        DeleteIndexTemplateRequest request = new DeleteIndexTemplateRequest(vserion);
        client.admin().indices().deleteTemplate(request);
    }


    public static boolean bulkIndex(String indexName,
                                    String type, List<Map<String, Object>> indexMap) throws Exception {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        Object id;
        JSONObject json;
        for (Map<String, Object> map : indexMap) {
            id = map.get("id");
            if (id == null) {
                continue;
            }
            json = JSONObject.fromObject(map);
            IndexRequestBuilder req = client.prepareIndex(indexName, type, null).setSource(json.toString()).setId(String.valueOf(id));
            bulkRequest.add(req);
        }
        BulkResponse res = bulkRequest.execute().actionGet();
        if (res.hasFailures()) {
            logger.error("bulkIndex res.hasFailures():" + res.buildFailureMessage());
            return false;
        }
        return true;
    }

    private static void initClient() {
        try {
            String nodes = props.getProperty("nodes");
            String esName = props.getProperty("cluster.name");

            client = TransportClient.builder().settings(settings(esName)).build();

            if (nodes != null && nodes.length() > 0) {
                String ipports[] = nodes.split(",");
                if (ipports != null && ipports.length > 0) {
                    for (String ipport : ipports) {
                        if (ipport != null && ipport.length() > 0) {
                            String str[] = ipport.split(":");
                            if (str != null && str.length == 2) {
                                String str0 = str[0].trim();
                                String str1 = str[1].trim();
                                client.addTransportAddress(new InetSocketTransportAddress(
                                        new InetSocketAddress(str0, Integer.valueOf(str1))));
                            }
                        }
                    }
                }
            }
            indexClient = new IndexClient();
        } catch (Exception e) {
            logger.error("init error:", e);
        }
    }


    public static Set<String> getNodeIPSet() {
        Set<String> nodeIpSet = new HashSet<String>();
        String nodes = props.getProperty("nodes");
        String esName = props.getProperty("cluster.name");
        if (nodes != null && nodes.length() > 0) {
            String ipAndPortNodes[] = nodes.split(",");
            for (int i = 0; i < ipAndPortNodes.length; i++) {
                nodeIpSet.add(ipAndPortNodes[i].split(":")[0]);
            }
            return nodeIpSet;
        }
        return null;
    }


    private static Settings settings(String elasticsearchName) {
        return Settings.settingsBuilder().put("cluster.name", elasticsearchName)
                .put("client.transport.sniff", true)
                .put("client.transport.ignore_cluster_name", false)
                .put("client.transport.ping_timeout", "5s")
                .put("client.transport.nodes_sampler_interval", "5s").build();
    }

    public static boolean flush(String indexVersion) {
        RefreshResponse response = client.admin().indices()
                .prepareRefresh(indexVersion)
                .execute().actionGet();
        return response.getFailedShards() == 0;
    }


    private static SearchRequestBuilder genSearchRequestBuilder(RequestQuery request, String indexName, String type) {
        String termQueryKey = request.getTermQueryKey();
        String termQueryValue = request.getTermQueryValue();
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName).setTypes(type).setFrom(0).setSize(1).setTimeout(new TimeValue(1000));
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (termQueryKey != null) {
            QueryBuilder query = QueryBuilders.termQuery(termQueryKey, termQueryValue);
            if (query != null) {
                boolQueryBuilder.must(query);   //构建keyword Query
            }
        }
        searchRequestBuilder.setQuery(boolQueryBuilder);
        searchRequestBuilder.setExplain(true);
        return searchRequestBuilder;
    }


    public static List<Terms.Bucket> getAggregationByQuery(RequestQuery request, String indexName, String type) {
        SearchRequestBuilder searchRequestBuilder = genSearchRequestBuilder(request, indexName, type);
        String termsName = request.getTermsName();
        String fieldName = request.getFieldName();
        int limit = request.getLimit();
        searchRequestBuilder.addAggregation(AggregationBuilders.terms(termsName).field(fieldName).size(Integer.MAX_VALUE));
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        Map<String, Aggregation> aggMap = searchResponse.getAggregations().asMap();
        LongTerms aggregation = (LongTerms) aggMap.get(termsName);

        if (aggregation != null) {
            List<Terms.Bucket> bucketList = aggregation.getBuckets();
            if (!bucketList.isEmpty()) {
                int length = bucketList.size() > limit ? limit : bucketList.size();
                return bucketList.subList(0, length);
            }
        }
        return null;
    }


    public static SearchHits getSearchHits(RequestQuery request, String indexName, String type) {
        SearchRequestBuilder searchRequestBuilder = genSearchRequestBuilder(request, indexName, type);
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        if (searchResponse == null) {
            return null;
        }
        SearchHits searchHits = searchResponse.getHits();

        return searchHits;
    }


    public static void main(String[] args) throws InterruptedException {


    }


    public static long getCount(RequestQuery request, String indexName, String type) {


        SearchHits searchHits = getSearchHits(request, indexName, type);
        if (searchHits == null) {
            return 0;
        }
        return searchHits.getTotalHits();
    }


    public static List<String> getAllClientsAddresses() {
        List<String> addresses = new ArrayList<String>();
        NodesInfoRequest nodesInfoRequest = new NodesInfoRequest().transport(true);
        NodesInfoResponse response = client.admin().cluster().nodesInfo(nodesInfoRequest).actionGet();
        NodeInfo nodeInfos[] = response.getNodes();
        String address;
        if (nodeInfos != null && nodeInfos.length > 0) {
            for (NodeInfo nodeInfo : nodeInfos) {
                if (nodeInfo.getNode().isClientNode()) {
                    address = nodeInfo.getNode().getHostName() + ":" + props.get("client.port");
                    addresses.add(address);
                }
            }
        }
        return addresses;
    }


    public static int getAliveDataNodesCount() {
        Map<String, List<String>> map = getAliveNodes();
        if (map != null) {
            List<String> dataNodes = map.get("dataNode");
            if (dataNodes != null) {
                return dataNodes.size();
            }
        }

        return 0;
    }

    public static Map<String, List<String>> getAliveNodes() {
        Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
        NodesStatsRequest request = new NodesStatsRequest();
        NodesStatsResponse response = client.admin().cluster().nodesStats(request).actionGet();
        NodeStats nodeStats[] = response.getNodes();
        if (nodeStats != null && nodeStats.length > 0) {
            List<String> address = null;
            for (NodeStats info : nodeStats) {
                if (info.getNode().dataNode()) {
                    address = map.get("dataNode");
                    if (address == null) {
                        address = new ArrayList<String>();
                        map.put("dataNode", address);
                    }
                } else if (info.getNode().clientNode()) {
                    address = map.get("clientNode");
                    if (address == null) {
                        address = new ArrayList<String>();
                        map.put("clientNode", address);
                    }
                }
                if (address != null) {
                    address.add(info.getNode().getHostAddress());
                }
            }
        }
        return map;
    }


    public static NodesStatsResponse getNodesStatsResponse(NodesStatsRequest request) {
        return client.admin().cluster().nodesStats(request).actionGet();
    }

    public static NodesInfoResponse getNodesInfoResponse(NodesInfoRequest request) {
        return client.admin().cluster().nodesInfo(request).actionGet();
    }


    public boolean deleteIndexDocs(String indexName, String type, List<Long> ids) throws Exception {
        if (ids == null || ids.size() == 0) {
            return true;
        }
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (Long key : ids) {
            DeleteRequestBuilder req = client.prepareDelete(indexName, type, null).setId(String.valueOf(key));
            bulkRequest.add(req);
        }
        BulkResponse res = bulkRequest.execute().actionGet();
        if (res.hasFailures()) {
            logger.error("bulkIndex res.hasFailures():" + res.buildFailureMessage());
            return false;
        }
        return true;
    }


}
