# Dockerfile for Hyperledger peer image. This actually follow yeasy/hyperledger-fabric
# image and add default start cmd.
# Data is stored under /var/hyperledger/db and /var/hyperledger/production

FROM yeasy/hyperledger-fabric-base:latest
LABEL maintainer "aietcn <lovetcn@gmail.com>"

EXPOSE 7051 7053

ENV FABRIC_CFG_PATH /etc/hyperledger/fabric
RUN mkdir -p $FABRIC_CFG_PATH

COPY msp $FABRIC_CFG_PATH/msp

ENV CORE_PEER_GOSSIP_SKIPHANDSHAKE=true CORE_PEER_ID=peer0 CORE_PEER_ADDRESS=peer0:7051 CORE_PEER_GOSSIP_EXTERNALENDPOINT=peer0:7051 CORE_PEER_GOSSIP_BOOTSTRAP=peer0:7051 CORE_PEER_EVENTS_ADDRESS=peer0:7053 CORE_PEER_MSPCONFIGPATH=$FABRIC_CFG_PATH/msp

# install fabric peer 
RUN cd $FABRIC_HOME/peer \
    && CGO_CFLAGS=" " go install -ldflags "$LD_FLAGS -linkmode external -extldflags '-static -lpthread'" \
    && go clean

CMD ["peer","node","start","--peer-defaultchain=false"]