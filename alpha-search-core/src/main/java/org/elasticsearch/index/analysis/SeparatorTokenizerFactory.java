package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettingsService;

/**
 * Created by yipeng on 2016/11/22.
 */
public class SeparatorTokenizerFactory extends AbstractTokenizerFactory{


    @Inject
    public SeparatorTokenizerFactory(Index index, IndexSettingsService indexSettingsService, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettingsService.getSettings(), name, settings);
    }

    @Override
    public Tokenizer create() {
        return new SeparatorTokenizer();
    }
}
