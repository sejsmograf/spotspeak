# SpotSpeak

## Introduction
SpotSpeak is a mobile application designed to encourage users to explore their surroundings and interact with others. The app allows users to place "traces" in specific locations—markers on a map that contain text, images, or short videos. These markers are only accessible when users are within a certain distance, promoting movement and discovery.
This repo contains only source code required for the backend. Frontend is written in Flutter and can be found at [Frontend](https://github.com/jakubkrapiec/spotspeak-mobile)
Deployment requires some prerequesites, for detailed instructions see the [deployment guide](DEPLOY.md)

## Architecture
The backend of SpotSpeak consists of the following components:
- **Application Server**: Built with [Spring Boot](https://spring.io/projects/spring-boot), responsible for handling business logic.
- **Authentication Server**: Managed with [Keycloak](https://www.keycloak.org/) for user authentication and authorization.
- **Database Server**: Uses [PostgreSQL](https://www.postgresql.org/) with the [PostGIS](https://postgis.net/) extension, hosted on [Amazon RDS](https://aws.amazon.com/rds/).
- **Cloud Storage**: Media files are stored on [Amazon S3](https://aws.amazon.com/s3/).
- **Content Delivery Network (CDN)**: Uses [Amazon CloudFront](https://aws.amazon.com/cloudfront/) for fast and secure content delivery.
- **AI Integration**: Leverages [Groq API](https://groq.com/) as an OpenAI-compatible language model API.
- **Push Notifications**: Integrated with [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging).

The infrastructure is deployed and managed using:
- **Terraform**: Automates the provisioning of cloud resources.
- **Docker & Docker Compose**: Simplifies containerized deployment.

## Deployment
For detailed deployment instructions, refer to the [deployment guide](DEPLOY.md). The high-level steps are:
1. Clone the Terraform repository and initialize the infrastructure.
2. Deploy the Keycloak authentication server and configure reverse proxy with Nginx.
3. Deploy the application server, database, and connect necessary services.
4. Configure SSL certificates and DNS settings.
5. Set up Firebase Cloud Messaging for push notifications.
6. Build and run the application using Docker.

## Prerequisites
- An [AWS](https://aws.amazon.com/) account with access to RDS, S3, CloudFront, EC2, and IAM.
- Installed [Terraform](https://www.terraform.io/) with configured AWS credentials.
- Two domain names with DNS management capabilities.
- Access to an OpenAI-compatible API such as [Groq API](https://groq.com/).
- A Firebase project with Cloud Messaging enabled.
- SSH key pair for accessing EC2 instances.
- Installed [Git](https://git-scm.com/).


