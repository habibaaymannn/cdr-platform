#  Call Detail Records (CDR) Platform

## Overview
This project implements a microservices-based Call Detail Records (CDR) platform. The platform includes three main microservices:

- **Loader Microservice (ms-loader):**  
  Parses CDR files in multiple formats (CSV, JSON, YAML, XML).  
  Persists records to PostgreSQL and publishes events to Apache Kafka.

- **Backend Microservice (ms-backend):**  
  Provides REST APIs for managing CDRs.  
  Stores data in MySQL.  
  Consumes Kafka events for data synchronization.  
  Secured using Keycloak (OpenID Connect/OAuth2).

- **Frontend Application (ms-frontend):**  
  User interface for authentication and managing CDR records.  
  Displays aggregated reports by service and date.

All services are containerized with Docker and can be deployed to a Kubernetes cluster.

---

## Prerequisites

- Java 21 (for backend service)  
- PostgreSQL (for loader service)  
- MySQL (for backend service)  
- Apache Kafka  
- Docker and Docker Compose (for containerization)  
- Kubernetes cluster (optional for deployment)  
- Keycloak server for authentication

