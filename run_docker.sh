#!/bin/bash

required_vars=(
	ENV_SPRING_DATASOURCE_URL
	ENV_SPRING_DATASOURCE_USERNAME
	ENV_SPRING_DATASOURCE_PASSWORD
	KEYCLOAK_CLIENT_ID
	KEYCLOAK_CLIENT_SECRET
	AWS_S3_BUCKET_NAME
	AWS_CLOUDFRONT_URL

	GROQ_API_KEY
	GROQ_BASE_URL
	GROQ_CHAT_MODEL
	SERVER_PORT
	EC2_ENV
)

missing_vars=()
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        missing_vars+=("$var")
    fi
done

if [ "${#missing_vars[@]}" -gt 0 ]; then
    echo "Error: The following required environment variables are missing:"
    for var in "${missing_vars[@]}"; do
        echo "  - $var"
    done
    echo "Please set these variables and try again."
    exit 1
fi

export FIREBASE_CONFIG_PATH=/app/firebase.json

docker stop spotspeak

docker run -d --rm --name spotspeak -p $(echo $SERVER_PORT):8080 \
	-e ENV_SPRING_DATASOURCE_URL=${ENV_SPRING_DATASOURCE_URL} \
	-e ENV_SPRING_DATASOURCE_USERNAME=${ENV_SPRING_DATASOURCE_USERNAME} \
	-e ENV_SPRING_DATASOURCE_PASSWORD=${ENV_SPRING_DATASOURCE_PASSWORD} \
	-e KEYCLOAK_CLIENT_ID=${KEYCLOAK_CLIENT_ID} \
	-e KEYCLOAK_CLIENT_SECRET=${KEYCLOAK_CLIENT_SECRET} \
	-e AWS_S3_BUCKET_NAME=${AWS_S3_BUCKET_NAME} \
	-e AWS_CLOUDFRONT_URL=${AWS_CLOUDFRONT_URL} \
	-e GROQ_API_KEY=${GROQ_API_KEY} \
	-e GROQ_BASE_URL=${GROQ_BASE_URL} \
	-e GROQ_CHAT_MODEL=${GROQ_CHAT_MODEL} \
	-e FIREBASE_CONFIG_PATH=${FIREBASE_CONFIG_PATH} \
	-e EC2_ENV=${EC2_ENV} \
	spotspeak

