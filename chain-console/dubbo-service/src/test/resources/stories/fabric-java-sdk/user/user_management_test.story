fabric admin manage users

Meta:

Narrative:
As a fabric manager
I want to enroll
So that I can manager fabric users

Scenario: fabric manager should enroll with the right account info
Given fabric ca server #caserver0
And manager account #managername : #managerpass
When I enroll admin with wrong account fakeadmin
Then admin enrollment fail
When I enroll admin with wrong password fakeadmin
Then admin enrollment fail
When I enroll admin
Then admin enrollment succeed

Scenario: fabric manager should enroll to register users
Given fabric ca server #caserver0
And manager account #managername : #managerpass
When I register a new user test : test
Then register failed
When I enroll admin
Then admin enrollment succeed
When I register a new user test : test
Then registration succeed

Scenario: registered user can enroll
Given fabric ca server #caserver0
And registered user test : test
When I enroll user
Then user enrollment succeed

Scenario: fabric manager should enroll to revoke users
Given fabric ca server #caserver0
And manager account #managername : #managerpass
And registered user test : test
When I revoke a user test
Then revocation failed
When I enroll admin
Then admin enrollment succeed
When I revoke a user fakeadmin
Then revocation failed
When I revoke a user test
Then revocation succeed
When I enroll user
Then user enrollment failed
