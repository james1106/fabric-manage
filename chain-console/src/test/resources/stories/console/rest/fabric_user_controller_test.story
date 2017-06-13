fabric admin manage users via console

Meta:

Narrative:
As a fabric console manager
I want to see current users
So that I can manager them

Scenario: console manager can enroll with the right account info and manage users
Given manager account #managername : #managerpass on CA Org1CA at #caserver0
And org1admin : admin as admin of org org1, CA Org1CA at #caserver0 with msp Org1MSP

When I enroll with org1admin : randompass of org org1, CA Org1CA at #caserver0 with msp Org1MSP
Then enrollment failed
When I enroll with org2admin : admin of org org1, CA Org1CA at #caserver0 with msp Org1MSP
Then enrollment failed
When I enroll with org1admin : admin of org org2, CA Org1CA at #caserver0 with msp Org1MSP
Then enrollment failed
When I enroll with org1admin : admin of org org1, CA Org2CA at #caserver0 with msp Org2MSP
Then enrollment failed
When I enroll with org1admin : admin of org org1, CA Org2CA at http://127.0.0.1:7055 with msp Org2MSP
Then enrollment failed

When I enroll with the given account
Then token should be in the response
When I register user demo : demo without authentication
Then registration failed
When I register user demo : demo with authentication
Then demo should be in the response
When I get current users
Then demo should be included

When I enroll with demo : demo of org org1, CA Org1CA at #caserver0 with msp Org1MSP
Then token should be in the response
When I get current users
Then request rejected
When I register user demo2 : demo2 with authentication
Then request rejected
When I revoke user demo
Then request rejected

When I enroll with the given account
Then token should be in the response
When I revoke user demo
Then revocation should succeed
When I get current users
Then demo should not be included
When I enroll with demo : demo of org Org1, CA Org1CA at #caserver0 with msp Org1MSP
Then enrollment failed
