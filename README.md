## Findability Autocomplete 

**A fast findability-focused lexical autocomplete through ensemble of searches.**

<img src="https://github.com/pinoystartup/findability-autocomplete/blob/master/06_diagrams/findability_autocomplete_view.png" width="450" height="422">

### Overview

We issue searches in **parallel** to a **single shard** data to meet **latency-sensitivity** of autocomplete.

We utilize **Elasticsearch**, its caching, and its near-real time updates.

We use Elasticsearch's **Completion Suggester** for prefix searches. It's based on the state-of-art **Finite State Transducer**.

Given above configurations, search results are in less than 10ms and most of the times actually just **few milliseconds**.

You usually specify a **single or a few fields** to autocomplete on. You can do this via multiple "suggest inputs".

You can design your application to return results on **first match-producing search** and exit in a fallbacking manner.

You can use the stemmed fulltext search as your **ultimate fallback**. It can be assigned the doc body or other doc attributes.

Below are parts of the index we have designed. It is **general enough baseline** and based on our grocery demo data.

We are going to **demonstarte** this in an open data project called [**Open Grocery Database Project**](http://www.grocery.com/open-grocery-database-project/).

We use Elasticsearch's Query DSL for our **search query designs**. Note our use of **filters** prior to match searches for **speed**.

We demonstrate the full application with the use of **Elastic Cloud**, Elastic's **Logstash**, and **Elastic Java API Client**.

The **API and UI** is a simple Spring Boot, Bootstrap, jQuery application served over AWS Elastic Beanstalk.

1. **PREFIX_STEMMED**
1. **PREFIX_RAW**
1. **NGRAM**
1. **MISSPELLING**
1. **PHONETIC**
1. **FULLTEXT_STEMMED**

### Examples

Here are **example query & matches** exhibiting **findability**. We are no longer constrained by **rigid prefix**, **exact spelling**.

1. **munchy** - Munchie Mrshmllw Smore Dsp 144
1. **petmate bood** - Petmate Booda Bones Steak, Bacon & Chicken Flavors - 9 Ct
1. **offee latt** - Hood Latte Iced Coffee Drink Vanilla Latte
1. **le kim ke** - Lee Kum Kee Black Bean Garlic Sauce
1. **coleos forcecohli** - Paradise Herbs Coleus Forskolii - 60 Vegetarian Capsules
1. **curcumin c3 1000** - Doctor's Best Best Curcumin C3 Complex 1000mg Tablets - 120 Ct
