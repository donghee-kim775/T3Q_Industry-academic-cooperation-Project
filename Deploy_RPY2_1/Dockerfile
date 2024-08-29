# Use the official Ubuntu 20.04 as a base image
FROM ubuntu:20.04

# Set environment variables to avoid interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install dependencies
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    software-properties-common \
    dirmngr \
    gnupg \
    ca-certificates \
    curl \
    && apt-get clean

# Add the R repository and the GPG key
RUN curl -fsSL https://cloud.r-project.org/bin/linux/ubuntu/marutter_pubkey.asc | apt-key add - && \
    add-apt-repository "deb https://cloud.r-project.org/bin/linux/ubuntu focal-cran40/" && \
    apt-get update

# Install R
RUN apt-get install -y --no-install-recommends \
    r-base=4.4.1* \
    r-base-dev=4.4.1* \
    && apt-get clean

# Clean up APT when done to reduce the size of the image
RUN rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /root

# Command to run R
CMD ["R", "--no-save"]