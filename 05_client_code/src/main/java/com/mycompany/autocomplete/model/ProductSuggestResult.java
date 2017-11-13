package com.mycompany.autocomplete.model;

import java.util.List;
import java.util.ArrayList;

public class ProductSuggestResult {

    private String took;
    private String start;
    private String end;
    private String total;
    private String error;
    private String type;

    private List<Product> suggests = new ArrayList<>();

    public ProductSuggestResult(String took, String start, String end, String total, String type) {
        this.took = took;
        this.start = start;
        this.end = end;
        this.total = total;
        this.error = "";
        this.type = type;
    }

    public ProductSuggestResult(String error) {
        this.took = "0";
        this.start = "0";
        this.end = "0";
        this.total = "0";
        this.error = "";
        this.type = "";
    }

    public String getTook() {
        return took;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getTotal() {
        return total;
    }

    public String getError() {
        return error;
    }

    public String getType() {
        return type;
    }

    public List<Product> getSuggests() {
        return suggests;
    }

    public void addSuggest(Product product) {
        suggests.add(product);
    }

    public static class Product {
        public String score;

        public String grpId;
        public String upc14;
        public String upc12;
        public String brand;
        public String name;
    }
}
