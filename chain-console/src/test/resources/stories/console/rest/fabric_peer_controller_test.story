fabric manager operate and monitor peers

Meta:

Narrative:
As a fabric manager
I want to see current peers
So that I can manage them

Scenario: console manager can start or stop peers
Given fabric console for peer
When I start peer1
Then operation success
When I stop peer1
Then operation success

Scenario: console manager should see all current peers
Given fabric console for peer
When I get current peers
Then there is no peer yet

Scenario: console manager can add peer
Given fabric console for peer
When I start peer1
Then operation success
When I add peer peer1 at #peerx with eventhub at #eventhubx
Then new peer added
When I get current peers
Then peer peer1 should be found
And chain base-chain should be found
When I stop peer1
Then operation success

