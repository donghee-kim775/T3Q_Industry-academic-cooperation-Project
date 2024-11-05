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
    cmake \
    xvfb

RUN apt-get install -y python3-venv \
    python3-pip \
    gcc \
    libpq-dev \
    libicu-dev \
    libgmp-dev

# Add the R repository and the GPG key
RUN curl -fsSL https://cloud.r-project.org/bin/linux/ubuntu/marutter_pubkey.asc | apt-key add - && \
    add-apt-repository "deb https://cloud.r-project.org/bin/linux/ubuntu focal-cran40/" && \
    apt-get update

RUN apt-get install -y r-base

# Install R packages
RUN R -e '.libPaths("/usr/local/lib/R/site-library")'

RUN R -q -e 'install.packages("gmp")'

RUN R -q -e 'install.packages("Rcmdr", dependencies = TRUE, lib = "/usr/local/lib/R/site-library",  repos="https://cran.yu.ac.kr/")'

RUN R -q -e 'install.packages("base64enc", dependencies = TRUE, lib = "/usr/local/lib/R/site-library",  repos="https://cran.yu.ac.kr/")'

RUN R -q -e 'install.packages("RcmdrPlugin.DoE", dependencies = TRUE, lib = "/usr/local/lib/R/site-library",  repos="https://cran.yu.ac.kr/")'

# Clean up APT when done to reduce the size of the image
RUN rm -rf /var/lib/apt/lists/*

# Set the working directory
WORKDIR /root

WORKDIR /app

RUN python3 -m venv venv

RUN pip install --upgrade pip setuptools

COPY requirements.txt .

RUN pip install -r requirements.txt

COPY . .

ENV PATH = "/app/venv/bin:$PATH"

# Command to run R
CMD ["sh", "-c", "Xvfb :99 & export DISPLAY=:99 && R -e 'library(Rcmdr)' && flask run --host=0.0.0.0"]