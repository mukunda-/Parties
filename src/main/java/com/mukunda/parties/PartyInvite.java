package com.mukunda.parties; 

public class PartyInvite {
	public long time; 
	public Party party;
	
	public static final int TIMEOUT = 30*1000;
	
	PartyInvite( Party party ) {
		time = System.currentTimeMillis(); 
		this.party = party;
	}
	
	public boolean isValid() {
		if( System.currentTimeMillis() > (time + TIMEOUT) ) return false;
		if( party != null ) {
			if( party.stale ) return false; 
		}
		return true;
	}
}
