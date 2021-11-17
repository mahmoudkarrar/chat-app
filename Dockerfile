FROM adoptopenjdk/openjdk11:jre
LABEL description="OpenJDK JRE 11 image for running Java applications"

# Install basic software useful during further steps and a dumb-init script to make running single purpose containers easier and more stable
## see https://github.com/Yelp/dumb-init
## see https://packages.debian.org/buster/dumb-init
## see https://packages.ubuntu.com/focal/dumb-init
RUN apt-get update -q=2 && \
    apt-get install -q=2 -y -o Dpkg::Options::="--force-confdef" -o Dpkg::Options::="--force-confnew" curl vim less zip unzip dumb-init && \
    apt-get autoremove -q=2 && \
    apt-get autoclean -q=2

RUN adduser app --home /app --gecos ",,," --disabled-password --system --group

# Enable dynamic DNS resolution at runtime using sed and modifying the necessary Java settings
RUN sed -i -r 's/^.*networkaddress\.cache\.ttl=.*$/networkaddress.cache.ttl=30/' $JAVA_HOME/conf/security/java.security && \
    sed -i -r 's/^.*networkaddress\.cache\.negative\.ttl=.*$/networkaddress.cache.negative.ttl=5/' $JAVA_HOME/conf/security/java.security && \
    chown app $JAVA_HOME/lib/security/cacerts

ENTRYPOINT ["/usr/bin/dumb-init", "--"]

WORKDIR /app

USER app

CMD java ${JAVA_OPTS} -jar ./*.jar

EXPOSE 8080

COPY ./target/*.jar .
