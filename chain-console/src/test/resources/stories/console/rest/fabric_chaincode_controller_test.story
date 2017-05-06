fabric manager manipulate chaincodes

Meta:

Narrative:
As a fabric manager
I want to upload a chaincode
So that I can install, instantiate, invoke it

Scenario: console manager can upload chaincode
Given fabric console for chaincode
And added peer peer0 at #peer0 with eventhub at #eventhub0
When I upload chaincode sample
Then upload succeed
When I check all chaincodes
Then there is chaincode sample
When I install chaincode sample on peer0
Then chaincode sample installed on peer0
When I check all chaincodes
Then chaincode sample is marked installed
When I instantiate sample chaincode with: init a 10 b 20
Then sample chaincode intantiation succeed
When I trigger a transaction on sample chaincode with: invoke a b 5
Then transaction on sample chaincode succeed
When I make a query on sample chaincode with: query a
Then result of sample chaincode has payload of 5
