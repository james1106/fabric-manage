create chain bar and let peers join

Meta:

Narrative:
As a fabric user
I want to have peers
So that I can join a chain for transactions

Scenario: Fabric peer can join built chain
Given admin user context
And chain bar created at orderer #orderer0
And peer peer0 at #peer0
When peer0 joins chain bar
Then peer0 has joined bar
When query chain of peer0
Then chains include bar

Scenario: Invalid peer cannot join built channel
Given peer peerx at grpc://127.0.0.1:17051
When peerx joins chain bar
Then peerx cannot join bar
