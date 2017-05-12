FROM yeasy/hyperledger-fabric-base:latest
LABEL maintainer "aietcn <lovetcn@gmail.com>"

EXPOSE 7050

ENV ORDERER_HOME=/etc/hyperledger/fabric
RUN mkdir $ORDERER_HOME/ledger && rm -rf $ORDERER_HOME/msp && rm -rf $ORDERER_HOME/*.yaml
COPY ./two-orgs.block $ORDERER_HOME/
COPY ./msp $ORDERER_HOME/msp
COPY ./ordererOrg1 $ORDERER_HOME/ordererOrg1
COPY ./peerOrg1 $ORDERER_HOME/peerOrg1
COPY ./peerOrg2 $ORDERER_HOME/peerOrg2
COPY ./*.yaml $ORDERER_HOME/

# install hyperledger fabric orderer
RUN cd $FABRIC_HOME \ 
        && git fetch && git reset --hard fed2efa \
        && cd orderer \
        && CGO_CFLAGS=" " go install -ldflags "$LD_FLAGS -linkmode external -extldflags '-static -lpthread'" \
        && go clean

ENV ORDERER_CFG_PATH=$ORDERER_HOME ORDERER_GENERAL_LEDGERTYPE=file ORDERER_FILELEDGER_LOCATION=$ORDERER_HOME/ledger
WORKDIR $ORDERER_HOME

CMD ["orderer"]