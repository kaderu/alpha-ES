package org.apache.lucene;

import java.io.IOException;
import java.util.ArrayList;


import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.elasticsearch.index.similarity.BoostingSimilarity;
import org.junit.Before;
import org.junit.Test;

public class TestSearchUseBootSimilarity {
    Directory dir = new RAMDirectory();

    public static final FieldType TYPE_STORED = new FieldType();

    static {
        TYPE_STORED.setTokenized(true);
        TYPE_STORED.setOmitNorms(true);
        TYPE_STORED.setIndexOptions(IndexOptions.DOCS);
        TYPE_STORED.setNumericType(FieldType.NumericType.LONG);
        TYPE_STORED.setStored(true);
        TYPE_STORED.setDocValuesType(DocValuesType.NUMERIC);
        TYPE_STORED.freeze();
    }


    @Before
    public void indexSchool() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
        IndexWriter writer = new IndexWriter(dir, config);
        Document doc1 = new Document();
        doc1.add(new StringField("name", "武汉大学", Store.YES));
        doc1.add(new TextField("info", "湖北  武汉", Store.NO));
        doc1.add(new NumericDocValuesField("rank", 1));
        writer.addDocument(doc1);

        Document doc2 = new Document();
        doc2.add(new StringField("name", "华中科技大学", Store.YES));
        doc2.add(new TextField("info", "中国 武汉", Store.NO));
        doc2.add(new NumericDocValuesField("rank", 2));
        writer.addDocument(doc2);
        writer.commit();
        writer.close();
    }

    @Test
    public void searchSchool() throws IOException {
        IndexReader indexReader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        BoostingSimilarity similarity = new BoostingSimilarity();
        searcher.setSimilarity(similarity);


        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        TermQuery tq = new TermQuery(new Term("info", "武汉"));
        BoostQuery boostQuery = new BoostQuery(tq,10f);

        TermQuery tq1 = new TermQuery(new Term("info", "中国"));
        BoostQuery boostQuery1 = new BoostQuery(tq1,2f);

        ArrayList<Query> queries = new ArrayList<>();
        queries.add(boostQuery);
        queries.add(boostQuery1);
        DisjunctionMaxQuery maxQuery = new DisjunctionMaxQuery(queries,0);
        BoostQuery maxBQ = new BoostQuery(maxQuery,3f);


        
        TermQuery tq3 = new TermQuery(new Term("info", "湖北"));

        
        BooleanQuery.Builder topBuilder = new BooleanQuery.Builder();
        topBuilder.add(maxBQ, Occur.SHOULD);
        
        topBuilder.add(tq3, Occur.SHOULD);
        
        BooleanQuery topBQ = topBuilder.build();
        
        topBQ.setBoost(4);
        
        TopDocs tfd = searcher.search(topBQ, 2);
        System.out.println(topBQ);
        for (ScoreDoc sd : tfd.scoreDocs) {
            System.out.println(searcher.doc(sd.doc).get("name"));
            System.out.println(sd.score);
            System.out.println(searcher.explain(topBQ, sd.doc));
        }

    }
}
