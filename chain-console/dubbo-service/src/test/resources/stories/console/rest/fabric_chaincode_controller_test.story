fabric manager manipulate chaincodes

Meta:

Narrative:
As a fabric manager
I want to upload a chaincode
So that I can install, instantiate, invoke it

Scenario: console manager can upload chaincode
Given manager account #managername : #managerpass on CA Org1CA at #caserver0
And org1peerAdmin : peerAdmin as admin of org org1, CA Org1CA at #caserver0 with msp Org1MSP
And token of chain admin org1peerAdmin from org org1
And chain admin org1peerAdmin of org org1 enrolled
And chain configuration testchain
And chain peer peer1 at #peer1 with eventhub at #eventhub1, password peerpass added from org org1, ca Org1CA at #caserver0 with msp Org1MSP
And chain testchain constructed
And chain peer peer1 joined testchain and listened

When I upload chaincode sample
Then upload succeed
When I check all chaincodes
Then there is chaincode sample
When I install chaincode sample on peer1 of chain testchain
Then chaincode sample installed on peer1
When I check all chaincodes
Then chaincode sample is marked installed
When I instantiate sample chaincode of chain testchain with: init a 10 b 20
Then sample chaincode intantiation succeed
When I check all chaincodes
Then chaincode sample is marked instantiated
When I trigger a transaction on sample chaincode of chain testchain with: invoke a b 5
Then transaction on sample chaincode succeed
When I make a query on sample chaincode of chain testchain with: query a
Then result of sample chaincode has payload of 5
