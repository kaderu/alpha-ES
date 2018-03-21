package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;

/**
 * Created by yipeng on 2016/11/22.
 */
public class SeparatorAnalyzer extends Analyzer{
    public SeparatorAnalyzer() {
    }

    protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
        return new Analyzer.TokenStreamComponents(new SeparatorTokenizer());
    }
}
