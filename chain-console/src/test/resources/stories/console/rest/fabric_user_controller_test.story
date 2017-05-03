fabric admin manage users via console

Meta:

Narrative:
As a fabric console manager
I want to see current users
So that I can manager them

Scenario: console manager should see all current fabric users
Given fabric console
When I get current users
Then there is nothing

Scenario: console manager can enroll with the right account info and manage users
Given admin : adminpw as admin
When I enroll with admin : randompass
Then enrollment failed
When I enroll with admin : adminpw
Then token should be in the response
When I register user demo : demo
Then demo should be in the response
When I get current users
Then demo should be included
When I enroll with demo : demo
Then token should be in the response
When I revoke user demo
Then revocation should succeed
When I get current users
Then demo should not be included
When I enroll with demo : demo
Then enrollment failed
