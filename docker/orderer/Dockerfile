# Dockerfile for Hyperledger fabric-orderer image.

FROM yeasy/hyperledger-fabric-base:latest
LABEL maintainer "aietcn <lovetcn@gmail.com>"

EXPOSE 7050

#ENV FABRIC_CFG_PATH /etc/hyperledger/fabric
ENV ORDERER_GENERAL_GENESISPROFILE=SampleInsecureSolo
ENV ORDERER_GENERAL_LOCALMSPDIR $FABRIC_CFG_PATH/msp
ENV ORDERER_GENERAL_LISTENADDRESS 0.0.0.0
ENV CONFIGTX_ORDERER_ORDERERTYPE=solo

RUN mkdir -p $FABRIC_CFG_PATH $ORDERER_GENERAL_LOCALMSPDIR

# install hyperledger fabric orderer
RUN cd $FABRIC_HOME/orderer \
        && CGO_CFLAGS=" " go install -ldflags "$LD_FLAGS -linkmode external -extldflags '-static -lpthread'" \
        && go clean

CMD ["orderer"]