FROM mlaccetti/sparklet
MAINTAINER Michael Laccetti <mlaccetti@uncharted.software>

RUN apt-get install -y bash ash

ENV IVY_HOME /cache
ENV GRADLE_VERSION 2.13
ENV GRADLE_HOME /usr/local/gradle
ENV PATH ${PATH}:${GRADLE_HOME}/bin
ENV GRADLE_OPTS -Dorg.gradle.native=false

# Install gradle
WORKDIR /usr/local
RUN curl -LSO  https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip && \
    unzip gradle-$GRADLE_VERSION-bin.zip && \
    rm -f gradle-$GRADLE_VERSION-bin.zip && \
    ln -s gradle-$GRADLE_VERSION gradle && \
    echo -ne "- with Gradle $GRADLE_VERSION\n" >> /root/.built

WORKDIR /opt/sparkplug
