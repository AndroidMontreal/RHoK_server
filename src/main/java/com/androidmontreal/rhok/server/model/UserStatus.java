package com.androidmontreal.rhok.server.model;

/**
 * <p>UserStatus
 * 
@formatter:off
State diagram 
@startuml

UNCONFIRMED: Email address not yet confirmed.
INVITED: Invited by confirmed, but email address not yet confirmed.
CONFIRMED: Email address confirmed.
 
[*] --> UNCONFIRMED 
UNCONFIRMED --> CONFIRMED


CONFIRMED --> [*]
@enduml
@formatter:on

Hmm actually not convinced we'll have invited status.
[*]-->INVITED 
INVITED-->CONFIRMED
*/
public enum UserStatus {
	UNCONFIRMED, 
	// INVITED, 
	CONFIRMED
}