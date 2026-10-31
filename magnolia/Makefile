# Makefile to facilitate local build, run and debug of magnolia 


# Load environment variables from .env file if it exists
ifneq (,$(wildcard .env))
    include .env
    export
endif

clean:
	mvn clean

install:
	AWS_ACCESS_KEY=dummykey AWS_SECRET_ACCESS_KEY=dummysecret mvn -T 1C install

run:
	if [ -z "${AWS_PROFILE}" ]; then echo "Please provide yopur aws profile for dev in AWS_PROFILE env variable"; exit 1; fi 
	cd magnolia-webapp && \
	  AWS_PROFILE=${AWS_PROFILE} \
	  CLOUDINARY_API_SECRET=${CLOUDINARY_API_SECRET} \
	  mvn -T 1C cargo:run -P tomcat

run-postgres:
	if [ -z "${AWS_PROFILE}" ]; then echo "Please provide yopur aws profile for dev in AWS_PROFILE env variable"; exit 1; fi 
	cd magnolia-webapp && \
	  AWS_PROFILE=${AWS_PROFILE} \
	  CLOUDINARY_API_SECRET=${CLOUDINARY_API_SECRET} \
	  mvn -T 1C cargo:run -P tomcat-postgres

# Helper target to create .env file if it doesn't exist
setup-env:
	@if [ ! -f .env ]; then \
		cp .env.template .env
	else \
		echo ".env file already exists."; \
	fi

