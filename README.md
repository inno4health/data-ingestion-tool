# Inno4Health Data Ingestion Tool

## About

This repository includes the mapping definitions for integration of source data of 
data owner organizations to [Inno4Health](https://inno4health.eu/). For testing purposes, 
this repository also contains athlete data provided by Karel in the format/model of the source datasets.

Mappings jobs and schemas are provided as well in this repository.
Data Ingestion Tool uses toFHIR to generate the FHIR resources based on the mapping definitions.
You can get more information about how mappings and mapping jobs are defined from [toFHIR](https://github.com/srdc/tofhir).

## Deployment

For demonstration, a toFHIR instance can be run with the mappings defined in this project. A docker compose file is
provided to start up an onFHIR instance bundled with [Inno4Health Common Data Model](https://github.com/inno4health/common-data-model)
and a toFHIR instance. You can configure the mapping job to run from `docker-compose.yml` file under `docker` folder.
