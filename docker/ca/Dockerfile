# Dockerfile for Hyperledger fabric ca image.
# If you need a peer node to run, please see the yeasy/hyperledger-peer image.
# Workdir is set to $GOPATH/src/github.com/hyperledger/fabric-ca
# More usage infomation, please see https://github.com/hyperledger/fabric-ca.

FROM golang:1.8
LABEL maintainer "aietcn <lovetcn@gmail.com>"

# ca-server and ca-client will try checking the following env in order, to get the home directory
ENV FABRIC_CA_HOME=/etc/hyperledger/fabric-ca-server FABRIC_CA_SERVER_HOME=/etc/hyperledger/fabric-ca-server FABRIC_CA_CLIENT_HOME=$HOME/.fabric-ca-client CA_CFG_PATH=/etc/hyperledger/fabric-ca FABRIC_CA_CODE=$GOPATH/src/github.com/hyperledger/fabric-ca

# Usually the cmd will be installed into $GOPATH/bin, but we add local build path 
ENV PATH=$FABRIC_CA_CODE/bin:$PATH

EXPOSE 7054

#VOLUME $FABRIC_CA_SERVER_HOME
#VOLUME $FABRIC_CA_CLIENT_HOME

RUN mkdir -p $GOPATH/src/github.com/hyperledger \
        $FABRIC_CA_SERVER_HOME \
        $FABRIC_CA_CLIENT_HOME \
        $CA_CFG_PATH \
        /var/hyperledger/fabric-ca-server

# The base image has libltdl-dev already, but we still need libtool to provide the header file ltdl.h
RUN apt-get update \
        && apt-get install -y libtool \
        && rm -rf /var/cache/apt

COPY 5a0981ca.diff $FABRIC_CA_SERVER_HOME/
COPY 6204e6dc.diff $FABRIC_CA_SERVER_HOME/

# clone and build ca
RUN cd $GOPATH/src/github.com/hyperledger \
    && git clone --single-branch -b master --depth 1 https://github.com/hyperledger/fabric-ca \
    && cd fabric-ca \
    && git apply $FABRIC_CA_SERVER_HOME/5a0981ca.diff \
    && git apply $FABRIC_CA_SERVER_HOME/6204e6dc.diff \
# This will install fabric-ca-server and fabric-ca-client which are under cmd into $GOPATH/bin/
    && go install -ldflags " -linkmode external -extldflags '-static -lpthread'" github.com/hyperledger/fabric-ca/cmd/... 
    
    # && cp $FABRIC_CA_CODE/images/fabric-ca/payload/*.pem $FABRIC_CA_HOME/

COPY *.pem $FABRIC_CA_HOME/

# Disable the tls in the existing cfg file

WORKDIR $FABRIC_CA_CODE

# if no config exists under $FABRIC_CA_HOME, will init fabric-ca-server-config.yaml and fabric-ca-server.db
# by default, the server will enable '-address 0.0.0.0'
CMD ["fabric-ca-server","start","-b","admin:adminpw"]