package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by yipeng on 2016/11/22.
 */
public class SeparatorTokenizer extends CharTokenizer {
    public static final char separator = '\001';
    public SeparatorTokenizer() {
    }

    public SeparatorTokenizer(AttributeFactory factory) {
        super(factory);
    }

    protected boolean isTokenChar(int c) {
        return !(c == separator);
    }
}
