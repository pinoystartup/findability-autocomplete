package com.mycompany.autocomplete;

public class Constants {
  public final static String CLUSTER_ID = "<your_cluster_id>";
  public final static String REGION = "<your_region>";
  public final static String ELASTIC_CLOUD_HOST_DOMAIN = "<your_elastic_cloud_host_domain>";
  public final static int TRANSPORT_PORT = 9343;

  public final static String USERNAME = "<your_elastic_username>";
  public final static String PASSWORD = "<your_elastic_password>";

  public final static String INDEX_GROCERY = "grocery";
  public final static String INDEX_GROCERY_TYPE = "product";

  public final static String FIELD_SUGGEST_PREFIX_STEMMED = "suggest_stemmed";
  public final static String FIELD_SUGGEST_PREFIX_RAW = "suggest";
  public final static String FIELD_SUGGEST_PREFIX_MISSPELLING = "suggest";

  public final static String CONTEXT_ALPHA_BRAND = "alpha_brand";
  public final static String CONTEXT_ALPHA_NAME = "alpha_name";

  public final static String FIELD_SORT_BY_NAME = "name";
  public final static String FIELD_PRODUCT_NGRAM = "name.ngram";
  public final static String FIELD_PRODUCT_PHONETIC = "name.sound";
  public final static String FIELD_MULTIMATCH_NAME_TEXT = "name.text";

  public final static int MAX_RESULTS = 20;
  public final static int FUZZY_EDITS_COUNT = 2;
  public final static String ERROR_GROCERY_PRODUCT = "Error > Grocery > Product";

  public final static String[] SOURCE = new String[] {"grp_id","upc14","upc12","brand","name","alpha_brand","alpha_name"};
}
