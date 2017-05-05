# Fabric Management Console


## Features

- [x] fabric-ca user enroll, register, revoke
- [x] fabric peer start, stop, status
- [ ] fabric chain status
- [ ] fabric chaincode upload, install, instantiate, query, invoke
- [x] fabric eventHub list

## Test

All the following requirements can be modified in `src/test/resources/test.properties`.

1. an orderer service running with [orderer-configuration](./src/test/resources/orderer_configuration)
2. a fabric-ca server running with [ca-configuration](./src/test/resources/ca_configuration)
3. peer nodes
    1. start peer0
    2. peer1 ready to run on a remote host accessible via ssh


Run `mvn clean verify` to see test result in `target/site/serenity/index.html`.
