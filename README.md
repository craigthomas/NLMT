# Natural Language Modeling Toolkit (NLMT)

[![Build Status](https://travis-ci.org/craigthomas/NLMT.svg?branch=master)](https://travis-ci.org/craigthomas/NLMT) 
[![Coverage Status](http://coveralls.io/repos/craigthomas/NLMT/badge.svg?branch=master)](http://coveralls.io/r/craigthomas/NLMT?branch=master) 
[![Dependency Status](https://www.versioneye.com/user/projects/55d83bc18d9c4b001b000008/badge.svg)](https://www.versioneye.com/user/projects/55d83bc18d9c4b001b000008)
[![Apache 2 License](https://img.shields.io/badge/license-apache_2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

# What is it?

This project contains a number of simple language modeling tools. The tools contained
in the toolkit are meant to be used for rapid prototypes. Currently, most algorithms are 
single threaded. For highly distributed environments, please see other projects such 
as [Apache Spark](http://spark.apache.org/). The toolkit is designed to 
be easy to use and understand in order to get you up and performing language analyses
within minutes.

# Topic Modeling

## Latent Dirichlet Allocation

Recover topics that appear in a collection of documents. To extract 10 topics from a
list of documents (each document is a list of strings), over 1000 iterations:

```java
LDAModel model = new LDAModel(10);
model.readDocuments(documentList);
model.doGibbsSampling(1000);
```
    
To get the top 5 words that describe each topic:

```java
List<List<String>> topics = model.getTopics(5);
System.out.println("Top 5 words for the first topic: " + topics.get(0));
```

