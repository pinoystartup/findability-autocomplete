package com.mycompany.autocomplete.controller;

import com.mycompany.autocomplete.service.GrocerySearchService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class GroceryViewController implements ErrorController {

  @Autowired
  private GrocerySearchService service;

  @RequestMapping(value={"", "/", "grocery/products"}, method= RequestMethod.GET)
  public String groceryProducts() {
    return "grocery-products";
  }

  @RequestMapping(value={"/error"})
  public String error() {
    return "error";
  }

  @Override
  public String getErrorPath() {
    return "/error";
  }
}
