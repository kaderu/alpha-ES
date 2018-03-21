package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettingsService;

/**
 * Created by yipeng on 2016/11/22.
 */
public class SeparatorAnalyzerProvider extends AbstractIndexAnalyzerProvider<SeparatorAnalyzer> {
    private final SeparatorAnalyzer analyzer;

    @Inject
    public SeparatorAnalyzerProvider(Index index, IndexSettingsService indexSettingsService, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettingsService.getSettings(), name, settings);
        this.analyzer = new SeparatorAnalyzer();
        this.analyzer.setVersion(version);
    }

    @Override
    public SeparatorAnalyzer get() {
        return this.analyzer;
    }
}
