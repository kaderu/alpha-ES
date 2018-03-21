package org.elasticsearch.index.similarity;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by wangjianghong on 2017/5/31.
 */
public class PayLoadSimilarity extends Similarity {

    @Override
    public long computeNorm(FieldInvertState fieldInvertState) {
        return 0;
    }

    @Override
    public SimWeight computeWeight(CollectionStatistics collectionStatistics, TermStatistics... termStatistics) {
        return new PayloadSimWeight();
    }

    @Override
    public SimScorer simScorer(SimWeight simWeight, LeafReaderContext leafReaderContext) throws IOException {
        return new PayLoadSimScorer();
    }

    static class PayloadSimWeight extends SimWeight {
        private float boost;
        private float value;


        @Override
        public float getValueForNormalization() {
            return 1;
        }

        @Override
        public void normalize(float queryNorm, float boost) {
            this.boost = boost;
            value = boost;
        }
    }

    private class PayLoadSimScorer extends SimScorer {

    private float value;

        PayLoadSimScorer() {

        }

        @Override
        public float score(int doc, float freq) {
            return 1f;
        }

        @Override
        public float computeSlopFactor(int i) {
            return 1f;
        }

        @Override
        public float computePayloadFactor(int i, int i1, int i2, BytesRef bytesRef) {

        	 value=PayloadHelper.decodeFloat(bytesRef.bytes,bytesRef.offset);
            return value;
        }

        @Override
        public Explanation explain(int doc, Explanation freq) {

            return Explanation.match(
                    score(doc, freq.getValue()),
                    "score(doc=" + doc + ",boost=" + value + ")");
        }
    }


}
