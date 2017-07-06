---
layout: doc
title: RDF Delta - Publishing Changes for RDF Datasets
---

# RDF Delta

RDF Delta is a system for recording and publishing changes to RDF
Datasets. 

It is built on top of the idea of change logs:


* _RDF Patch_ -  a format for recording changes to an RDF Dataset
* _RDF Patch Log_ - organise patches in to a log of changes 
to an RDF Dataset with HTTP access. 

which can be useful in their own right.

RDF Patch Logs can be used for:

* Replicated datasets - 2 or more copies of a single dataset for high
availability of the data.
* Incremental backup of a dataset.
* Recording changes 
* Generate alerts based on changes, either to the dataset as a whole or
specific resources within the dataset.

RDF Delta provides a server for RDF Patch Logs to support
replicated datasets.

See "[Delta](delta.md)" for a overview of the Delta system for
distributing changes to RDF datasets. 

## Documentation

* "[Delta](delta.html)" for a overview of the Delta system
* "[RDF Patch](rdf-patch.html)" for the format for recording changes.
* "[RDF Patch Logs](rdf-patch-logs.html)" for organising and accessing RDF Patches 

## Code

[https://github.com/afs/rdf-delta](https://github.com/afs/rdf-delta)