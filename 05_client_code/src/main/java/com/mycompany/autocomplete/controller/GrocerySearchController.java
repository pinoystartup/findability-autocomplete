package com.mycompany.autocomplete.controller;

import com.mycompany.autocomplete.model.ProductSuggestResult;
import com.mycompany.autocomplete.service.GrocerySearchService;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/grocery")
public class GrocerySearchController {

  @Autowired
  private GrocerySearchService service;

  @RequestMapping(value={"products_suggest"}, method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ProductSuggestResult> productsSuggest(
      @RequestParam(value="q", defaultValue="") String query,
      @RequestParam(value="s", defaultValue="0") int start,
      @RequestParam(value="c", defaultValue="5") int count) {

    ProductSuggestResult productSuggests = service.getProducts(query, start, count);
    return new ResponseEntity<ProductSuggestResult>(productSuggests, HttpStatus.OK);
  }
}
