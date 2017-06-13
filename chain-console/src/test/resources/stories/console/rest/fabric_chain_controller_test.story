fabric manager check chain stats

Meta:

Narrative:
As a fabric manager
I want to see current chain
So that I can know how it runs

Scenario: console manager can check current chain data
Given manager account #managername : #managerpass on CA Org1CA at #caserver0
And org1peerAdmin : peerAdmin as admin of org org1, CA Org1CA at #caserver0 with msp Org1MSP
And chain admin org1peerAdmin of org org1 enrolled
And chain configuration testchain
And chain peer peer1 at #peer1 with eventhub at #eventhub1, password peerpass added from org org1, ca Org1CA at #caserver0 with msp Org1MSP

When construct chain testchain
Then testchain constructed
When I get chain info of testchain
Then there is nothing on the chain

When chain peer peer1 joins testchain
Then chain peer peer1 has joined testchain

When I get chain info of testchain
Then there are height, hash in chain
When I get block of chain testchain
Then there are size, hash, previous in block
When I get block 0 of chain testchain
Then there are txid, version, channel in block datalist
