create example chain foo

Meta:

Narrative:
As a fabric user
I want to build a chain
So that I can make trasactions on it

Scenario: fabric client can build chain with chain configuration
Given admin user context
And orderer at #orderer0
When I construct a chain foo
Then the chain foo is created