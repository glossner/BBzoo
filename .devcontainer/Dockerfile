FROM mcr.microsoft.com/devcontainers/base:ubuntu-22.04

# Prevent interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install dependencies, Java 21, Python, and Verilator
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    gnupg \
    ca-certificates \
    software-properties-common \
    build-essential \
    ccache \
    verilator \
    openjdk-21-jdk \
    python3 \
    python3-pip \
    python3-venv \
    git \
    && rm -rf /var/lib/apt/lists/*

# Install sbt
RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list \
    && echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list \
    && curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | gpg --dearmor | tee /etc/apt/trusted.gpg.d/sbt.gpg > /dev/null \
    && apt-get update \
    && apt-get install -y sbt \
    && rm -rf /var/lib/apt/lists/*

# Install uv for Python package management system-wide
RUN curl -LsSf https://astral.sh/uv/install.sh | env BINDIR=/usr/local/bin sh

# Set the default shell to bash
SHELL ["/bin/bash", "-c"]
