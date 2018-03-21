package org.apache.lucene;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.index.analysis.SeparatorTokenizer;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by yipeng on 2016/11/22.
 */
public class TestSeparatorTokenizer {
    public static void main(String[] args) {
        SeparatorTokenizer tokenizer = new SeparatorTokenizer();
        tokenizer.setReader(new StringReader("wo men \001 a b c d\001f g"));
        try {
            tokenizer.reset();
            while (tokenizer.incrementToken()) {
                CharTermAttribute termAtt = (CharTermAttribute) tokenizer.getAttribute(CharTermAttribute.class);
                System.out.println(termAtt.toString());
            }
            tokenizer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
