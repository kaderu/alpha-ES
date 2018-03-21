package org.elasticsearch.index.similarity;

import org.apache.lucene.search.similarities.Similarity;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;

/**
 * Created by yipeng on 2016/11/7.
 */
public class BoostingSimilarityProvider extends AbstractSimilarityProvider{

    /**
     * Creates a new AbstractSimilarityProvider with the given name
     *
     * @param name Name of the Provider
     */
    @Inject
    public BoostingSimilarityProvider(@Assisted String name, @Assisted Settings settings) {
        super(name);
        boolean discountOverlaps = settings.getAsBoolean("discount_overlaps", true);
    }

    @Override
    public Similarity get() {
        return new BoostingSimilarity();
    }
}
