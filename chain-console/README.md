# Fabric Management Console


## Features

- [x] fabric-ca user enroll, register, revoke
- [x] fabric peer start, stop, status, list (including eventHub)
- [x] fabric chain status
- [x] fabric chaincode list, upload, install, instantiate, query, invoke
- [x] support system context restore between restart
- [x] fabric chain management: new-chain

### TODO

- [ ] fabric transaction performance visualization


## Test

All the following requirements can be modified in `src/test/resources/test.properties`.

1. an orderer service running with [orderer-configuration](./src/test/resources/orderer_configuration)
2. a fabric-ca server running with [ca-configuration](./src/test/resources/ca_configuration)
3. peer nodes with configuration [core.yaml](./src/test/resources/peer_configuration/core.yaml)
    1. start `peer0`
    2. `peer1` ready to run on a remote host, accessible via ssh


Run `mvn clean verify` to see test result in `target/site/serenity/index.html`.


## Implementations

Before the system starts, we need some preparation.

What admin can do:

- to enroll admin of the organization and manage peers:
    1. CA endpoints
    2. CA organization admin account, created on organization CA server

- after enrolling in admin, we have admin's MSP certificates saved: cacerts, keystore, signcerts
    1. register peers and users
    2. download the certs to configure peer, and start the peer manually
    3. add the peer to peer list
    4. view peer detailed status
    
- construct a chain by uploading the chain configuration and restrict the chain to only specific organizations (no restriction by default)
    1. select a peer to join the chain
    2. view details of chain, blocks and transactions
    3. install and instantiate chaincode

What users can do:

- enroll with username and password provided by admin
    1. invoke chaincode
    2. query chaincode
    3. view peer list and status
    4. view details of chain, blocks and transactions

### Membership/Identity Management

based on fabric-ca

every organization can have a independent CA server, i.e. 

    - org1 : Org1CA
    - org2 : Org2CA
    - orderer : OrgOrdererCA

CA server generates key pairs for admin and user. To make use of the keys generated in unit tests, the private key needs to be converted using the following command:

```commandline
openssl pkcs8 -topk8 -inform pem -in generated_key.pem -outform pem -nocrypt -out private_key_for_test.pem
```

To use the key pair generated on enrollment:

1. split the private key into lines each has 64 characters at most
2. prepend `-----BEGIN PRIVATE KEY-----` and append `-----END PRIVATE KEY-----`.
3. say we have the result in file `enrollment_private_key.pem`, we can put `keystore.pem` under `msp/keystore/` after running:

```commandline
openssl ec -in enrollment_private_key.pem -out keystore.pem
```

4. put public key directly into `msp/signcerts/signcerts.pem`

### Chaincode Manipulation

**chaincode upgrade not supported yet**

### Chain-Console JWT Token

1. KeyPair Generation

```commandline
keytool -genkeypair -keyalg EC -keystore oxchain.jks -keysize 256 -alias oxecc
keytool -export -alias oxecc -keystore oxchain.jks -file oxchain.cer
```
