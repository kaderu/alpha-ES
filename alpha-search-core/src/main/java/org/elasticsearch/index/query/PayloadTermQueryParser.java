package org.elasticsearch.index.query;

import org.apache.lucene.index.Term;
import org.apache.lucene.queries.payloads.MaxPayloadFunction;
import org.apache.lucene.queries.payloads.PayloadScoreQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.MappedFieldType;

import java.io.IOException;

/**
 * Created by wangjianghong on 2017/6/1.
 */
public class PayloadTermQueryParser implements QueryParser {

    static final String NAME = "payload_term";

    @Inject
    public PayloadTermQueryParser() {
    }

    @Override
    public String[] names() {
        return new String[]{NAME, Strings.toCamelCase(NAME)};
    }

    @Override
    public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
        XContentParser parser = parseContext.parser();

        XContentParser.Token token = parser.currentToken();
        if (token == XContentParser.Token.START_OBJECT) {
            token = parser.nextToken();
        }
        assert token == XContentParser.Token.FIELD_NAME;
        String fieldName = parser.currentName();


        String value = null;
        String queryName = null;
        token = parser.nextToken();
        if (token == XContentParser.Token.START_OBJECT) {
            String currentFieldName = null;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else {
                    if ("term".equals(currentFieldName)) {
                        value = parser.text();
                    } else if ("value".equals(currentFieldName)) {
                        value = parser.text();
                    } else if ("_name".equals(currentFieldName)) {
                        queryName = parser.text();
                    } else {
                        throw new QueryParsingException(parseContext, "[payload_term] query does not support [" + currentFieldName + "]");
                    }
                }
            }
            parser.nextToken();
        } else {
            value = parser.text();
            // move to the next token
            parser.nextToken();
        }
        if (value == null) {
            throw new QueryParsingException(parseContext, "No value specified for term query");
        }
        MappedFieldType mappedFieldType = parseContext.fieldMapper(fieldName);
        BytesRef valueBytes = mappedFieldType.indexedValueForSearch(value);
        if (valueBytes == null) {
            valueBytes = new BytesRef(value);
        }
        PayloadScoreQuery query = new PayloadScoreQuery(new SpanTermQuery(new Term(fieldName, valueBytes)),
                new MaxPayloadFunction());
        if (queryName != null) {
            parseContext.addNamedQuery(queryName, query);
        }
        return query;
    }
}