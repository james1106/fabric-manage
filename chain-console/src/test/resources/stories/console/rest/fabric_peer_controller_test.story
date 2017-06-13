fabric manager operate and monitor peers

Meta:

Narrative:
As a fabric manager
I want to see current peers
So that I can manage them

Scenario: console manager can start or stop peers
Given manager account #managername : #managerpass on CA Org1CA at #caserver0
And org1peerAdmin : peerAdmin as admin of org org1, CA Org1CA at #caserver0 with msp Org1MSP
And peer admin org1peerAdmin of org org1 enrolled
When I get current peers
Then there is no peer peer1 yet

When I add peer peer1 at #peer1 with eventhub at #eventhub1, password peerpass
Then new peer added
When I get current peers
Then peer peer1 should be found
And peer peer1 is not connected
When I enroll peer peer1 : peerpass from org org1, CA Org1CA at #caserver0 with msp Org1MSP
Then peer peer1 enrolled
!--When connect to peer peer1
!--Then peer peer1 connected
!--When I get current peers
!--Then peer peer1 should be connected

When I remove peer peer1
Then peer peer1 removed
When I get current peers
Then there is no peer peer1 yet


