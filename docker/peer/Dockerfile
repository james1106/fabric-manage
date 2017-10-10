# Dockerfile for Hyperledger peer image. This actually follow yeasy/hyperledger-fabric
# image and add default start cmd.
# Data is stored under /var/hyperledger/db and /var/hyperledger/production

FROM hyperledger/fabric-peer:latest
LABEL maintainer "aietcn <lovetcn@gmail.com>"

ENV FABRIC_CFG_PATH /etc/hyperledger/fabric
RUN mkdir -p $FABRIC_CFG_PATH

COPY core.yaml $FABRIC_CFG_PATH/
COPY msp $FABRIC_CFG_PATH/msp

ENV CORE_PEER_GOSSIP_SKIPHANDSHAKE=true CORE_LOGGING_LEVEL=DEBUG

CMD ["peer","node","start","--peer-defaultchain=false"]