package com.mycompany.autocomplete.service;

import com.mycompany.autocomplete.model.ProductSuggestResult;
import com.mycompany.autocomplete.Constants;

import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.completion.context.CategoryQueryContext;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Service
public class GrocerySearchService {
  private final static org.slf4j.Logger logger = LoggerFactory.getLogger(GrocerySearchService.class);

  private TransportClient client;

  @PostConstruct
  public void initClient() {
    Settings settings = Settings.builder()
        .put("client.transport.nodes_sampler_interval", "5s")
        .put("client.transport.sniff", false)
        .put("transport.tcp.compress", true)
        .put("cluster.name", Constants.CLUSTER_ID)
        .put("xpack.security.transport.ssl.enabled", true)
        .put("request.headers.X-Found-Cluster", "${cluster.name}")
        .put("xpack.security.user", Constants.USERNAME + ":" + Constants.PASSWORD)
        .build();

    client = new PreBuiltXPackTransportClient(settings);

    try {
      String hostname = Constants.CLUSTER_ID + "." + Constants.REGION + "." + Constants.ELASTIC_CLOUD_HOST_DOMAIN;
      client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostname), Constants.TRANSPORT_PORT));
    } catch (UnknownHostException e) {
      logger.error(e.getMessage());
    }
  }

  public ProductSuggestResult getProducts(String query, int start, int count) {
    if (count > Constants.MAX_RESULTS) count = Constants.MAX_RESULTS;
    query = query.toLowerCase().replaceAll("[^-\\s0-9a-z]", "");

    ProductSuggestResult result = new ProductSuggestResult(Constants.ERROR_GROCERY_PRODUCT);
    if (StringUtils.isEmpty(query)) return result;

    try {
      result = findabilityAutocomplete(query, start, count);
    } catch (Exception e  ) {
      logger.error(e.getMessage());
    }
    return result;
  }

  private ProductSuggestResult findabilityAutocomplete(String query, int start, int count) {

    SearchRequestBuilder srbPrefixStemmed, srbPrefixRaw, srbNgram, srbMisspelling, srbPhonetic, srbFulltext;

    String queryFirstChar = String.valueOf(query.charAt(0));

    Map<String, List<? extends ToXContent>> contexts = new HashMap<>();
    contexts.put(Constants.CONTEXT_ALPHA_BRAND,
        Collections.singletonList(CategoryQueryContext.builder().setCategory(queryFirstChar).build()));
    contexts.put(Constants.CONTEXT_ALPHA_NAME,
        Collections.singletonList(CategoryQueryContext.builder().setCategory(queryFirstChar).build()));

    //*******************
    // 01_PREFIX_STEMMED
    //*******************
    CompletionSuggestionBuilder prefixStemmed =
        SuggestBuilders.completionSuggestion(Constants.FIELD_SUGGEST_PREFIX_STEMMED)
        .prefix(query)
        .size(count)
        .contexts(contexts);

    srbPrefixStemmed = client.prepareSearch(Constants.INDEX_GROCERY).setTypes(Constants.INDEX_GROCERY_TYPE)
        .setFetchSource(Constants.SOURCE, null)
        .suggest(new SuggestBuilder().addSuggestion(Constants.FIELD_SUGGEST_PREFIX_STEMMED, prefixStemmed))
        .addSort(SortBuilders.scoreSort())
        .addSort(SortBuilders.fieldSort(Constants.FIELD_SORT_BY_NAME));

    //*******************
    // 02_PREFIX_RAW
    //*******************
    CompletionSuggestionBuilder prefixRaw =
        SuggestBuilders.completionSuggestion(Constants.FIELD_SUGGEST_PREFIX_RAW)
        .prefix(query)
        .size(count)
        .contexts(contexts);

    srbPrefixRaw = client.prepareSearch(Constants.INDEX_GROCERY).setTypes(Constants.INDEX_GROCERY_TYPE)
        .setFetchSource(Constants.SOURCE, null)
        .suggest(new SuggestBuilder().addSuggestion(Constants.FIELD_SUGGEST_PREFIX_RAW, prefixRaw))
        .addSort(SortBuilders.scoreSort())
        .addSort(SortBuilders.fieldSort(Constants.FIELD_SORT_BY_NAME));

    //*******************
    // 03_NGRAM
    //*******************
    BoolQueryBuilder bqbNgram = QueryBuilders.boolQuery();
    bqbNgram.must(QueryBuilders.matchQuery(Constants.FIELD_PRODUCT_NGRAM, query).operator(Operator.AND));

    srbNgram = client.prepareSearch(Constants.INDEX_GROCERY).setTypes(Constants.INDEX_GROCERY_TYPE)
        .setFetchSource(Constants.SOURCE, null)
        .setFrom(start)
        .setSize(count)
        .setQuery(bqbNgram)
        .addSort(SortBuilders.scoreSort())
        .addSort(SortBuilders.fieldSort(Constants.FIELD_SORT_BY_NAME));

    //*******************
    // 04_MISSPELLING
    //*******************
    CompletionSuggestionBuilder misspelling =
        SuggestBuilders.completionSuggestion(Constants.FIELD_SUGGEST_PREFIX_MISSPELLING)
        .prefix(query, Fuzziness.fromEdits(Constants.FUZZY_EDITS_COUNT))
        .size(count)
        .contexts(contexts);

    srbMisspelling = client.prepareSearch(Constants.INDEX_GROCERY).setTypes(Constants.INDEX_GROCERY_TYPE)
        .setFetchSource(Constants.SOURCE, null)
        .suggest(new SuggestBuilder().addSuggestion(Constants.FIELD_SUGGEST_PREFIX_MISSPELLING, misspelling))
        .addSort(SortBuilders.scoreSort())
        .addSort(SortBuilders.fieldSort(Constants.FIELD_SORT_BY_NAME));

    //*******************
    // 05_PHONETIC
    //*******************
    BoolQueryBuilder bqbPhonetic = QueryBuilders.boolQuery();
    bqbPhonetic.must(QueryBuilders.matchQuery(Constants.FIELD_PRODUCT_PHONETIC, query).operator(Operator.AND));

    srbPhonetic = client.prepareSearch(Constants.INDEX_GROCERY).setTypes(Constants.INDEX_GROCERY_TYPE)
        .setFetchSource(Constants.SOURCE, null)
        .setFrom(start)
        .setSize(count)
        .setQuery(bqbPhonetic)
        .addSort(SortBuilders.scoreSort())
        .addSort(SortBuilders.fieldSort(Constants.FIELD_SORT_BY_NAME));

    //*******************
    // 06_FULLTEXT
    //*******************
    BoolQueryBuilder bqbFulltext = QueryBuilders.boolQuery();
    bqbFulltext.must(QueryBuilders.multiMatchQuery(query, Constants.FIELD_MULTIMATCH_NAME_TEXT));

    srbFulltext = client.prepareSearch(Constants.INDEX_GROCERY).setTypes(Constants.INDEX_GROCERY_TYPE)
        .setFetchSource(Constants.SOURCE, null)
        .setFrom(start)
        .setSize(count)
        .setQuery(bqbFulltext)
        .addSort(SortBuilders.scoreSort())
        .addSort(SortBuilders.fieldSort(Constants.FIELD_SORT_BY_NAME));

    //*******************
    // MULTISEARCH
    //*******************
    MultiSearchResponse findabilityResponse = client.prepareMultiSearch()
        .add(srbPrefixStemmed) //1
        .add(srbPrefixRaw)     //2
        .add(srbNgram)         //3
        .add(srbMisspelling)   //4
        .add(srbPhonetic)      //5
        .add(srbFulltext)      //6
        .get();

    Set<Integer> completionSuggesters = new HashSet<>(Arrays.asList(1, 2, 4));

    ProductSuggestResult result = getFindabilityResult(findabilityResponse, completionSuggesters, start, count);
    return result;
  }

  private ProductSuggestResult getFindabilityResult(MultiSearchResponse response,
                                                    Set<Integer> completionSuggesters, int start, int count) {
    ProductSuggestResult result = null;
    MultiSearchResponse.Item[] items = response.getResponses();
    int itemsLength = items.length;

    if (itemsLength > 0) {
      for (int i=0; i<=itemsLength; i++) {
        SearchResponse sr = items[i].getResponse();

        //**********************************
        // Handle Completion Suggesters
        //**********************************
        if (completionSuggesters.contains(i+1)) {
          Suggest s = sr.getSuggest();
          Iterator itr = s.iterator();
          if (itr.hasNext()) {
            CompletionSuggestion cs = (CompletionSuggestion) itr.next();
            List<CompletionSuggestion.Entry.Option> os = cs.getOptions();
            if (os.size() > 0) {
              long took = sr.getTookInMillis();
              int pageTotal = os.size();
              int total = pageTotal;
              int end = Math.min(start + count, total);

              result = new ProductSuggestResult(
                  Long.toString(took),
                  Integer.toString(start),
                  Long.toString(end),
                  Long.toString(total),
                  Integer.toString(i+1)
              );

              for (CompletionSuggestion.Entry.Option o : os) {
                addGroceryProduct(result, o.getHit().getScore(), o.getHit().getSource());
              }
              break;
            }
          }
        }

        //**********************************
        // Handle Non-Completion Suggesters
        //**********************************
        else {
          if (sr.getHits().getTotalHits() > 0) {
            long took = sr.getTookInMillis();
            int pageTotal = sr.getHits().getHits().length;
            long total = sr.getHits().getTotalHits();
            long end = start + pageTotal;

            result = new ProductSuggestResult(
                Long.toString(took),
                Integer.toString(start),
                Long.toString(end),
                Long.toString(total),
                Integer.toString(i+1)
            );

            for (SearchHit h : sr.getHits().getHits()) {
              addGroceryProduct(result, h.getScore(), h.getSource());
            }
            break;
          }
        }
      }
    }

    if (result == null) result = new ProductSuggestResult("");

    return result;
  }

  private void addGroceryProduct(ProductSuggestResult psr, float score, Map<String, Object> source) {
    if (source != null) {
      ProductSuggestResult.Product product = new ProductSuggestResult.Product();
      product.score = Float.toString(score);
      product.grpId = source.get("grp_id") == null ? "" : source.get("grp_id").toString();
      product.upc14 = source.get("upc14") == null ? "" : source.get("upc14").toString();
      product.upc12 = source.get("upc12") == null ? "" : source.get("upc12").toString();
      product.brand = source.get("brand") == null ? "" : source.get("brand").toString();
      product.name = source.get("name") == null ? "" : source.get("name").toString();
      psr.addSuggest(product);
    }
  }
}
