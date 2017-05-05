fabric manager check chain stats

Meta:

Narrative:
As a fabric manager
I want to see current chain
So that I can know how it runs

Scenario: console manager can check current chain data
Given fabric console for chain
And peer0 at #peer0 joined chain
When I get chain info
Then there are height, hash in chain
When I get chain block
Then there are size, hash, previous in block
When I get block 0 of chain
Then there are txid, version, channel in block datalist
