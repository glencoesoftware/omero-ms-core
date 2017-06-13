[![AppVeyor status](https://ci.appveyor.com/api/projects/status/github/omero-ms-core)](https://ci.appveyor.com/project/gs-jenkins/omero-ms-core)

OMERO Microservice Core
=======================

OMERO Vert.x asynchronous microservice server endpoint for OMERO.web core
components.

Requirements
============

* OMERO 5.3.x+
* OMERO.web 5.3.x+
* Redis backed sessions
* Java 8+

Development Installation
========================

1. Clone the repository::

        git clone git@github.com:glencoesoftware/omero-ms-thumbnail.git

1. Run the Gradle build and utilize the artifacts as required::

        ./gradlew jar
        ...

Eclipse Configuration
=====================

1. Run the Gradle Eclipse task::

        ./gradlew eclipse

Running Tests
=============

Using Gradle run the unit tests:

    ./gradlew test

Reference
=========

* https://lettuce.io/
* http://vertx.io/
