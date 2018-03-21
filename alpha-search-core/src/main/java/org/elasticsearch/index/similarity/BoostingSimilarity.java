package org.elasticsearch.index.similarity;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

/**
 * Created by yipeng on 2016/11/7.
 */
public class BoostingSimilarity extends Similarity{

    @Override
    public long computeNorm(FieldInvertState fieldInvertState) {
        return 0;
    }

    @Override
    public SimWeight computeWeight(CollectionStatistics collectionStatistics, TermStatistics... termStatisticses) {
        return new BoostSimWeight();
    }

    @Override
    public SimScorer simScorer(SimWeight simWeight, LeafReaderContext leafReaderContext) throws IOException {
        return new BoostSimScorer((BoostSimWeight)simWeight);
    }


    static class BoostSimWeight extends SimWeight {
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

    private class BoostSimScorer extends SimScorer {
        private BoostSimWeight boostSimWeight;

        BoostSimScorer(BoostSimWeight boostSimWeight) {
            this.boostSimWeight = boostSimWeight;
        }

        @Override
        public float score(int doc, float freq) {
            return boostSimWeight.value;
        }

        @Override
        public float computeSlopFactor(int i) {
            return 1f;
        }

        @Override
        public float computePayloadFactor(int i, int i1, int i2, BytesRef bytesRef) {
            return 1f;
        }

        @Override
        public Explanation explain(int doc, Explanation freq) {

            return Explanation.match(
                    score(doc, freq.getValue()),
                    "score(doc=" + doc + ",boost=" + boostSimWeight.boost + ")");
        }
    }
}
