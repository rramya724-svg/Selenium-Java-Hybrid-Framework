FROM selenium/standalone-chrome:latest

USER root

# Install Maven 3.9.16
RUN apt-get update \
    && apt-get install -y wget tar \
    && wget -q https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.tar.gz \
    && tar -xzf apache-maven-3.9.16-bin.tar.gz -C /opt \
    && ln -s /opt/apache-maven-3.9.16 /opt/maven \
    && rm -f apache-maven-3.9.16-bin.tar.gz \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

WORKDIR /app

# Copy the checked-out automation project into the image
COPY . /app

# Remove previous runtime/build output
RUN rm -rf /app/target \
           /app/test-output \
           /app/logs

# Verify Java, Maven and Chrome during image build
RUN java -version \
    && mvn -version \
    && google-chrome --version

CMD ["mvn", "clean", "test", "-Dheadless=true"]
