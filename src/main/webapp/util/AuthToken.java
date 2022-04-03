package pt.unl.fct.di.adc.secondwebapp.util;

import java.util.UUID;

public class AuthToken {

	public String username;
	public String tokenID;
	public long creationDate;
	public long expirationDate;
	
	public static final long EXPIRATION_TIME = 1000*60*60*2; //2h
	
	public AuthToken() { //nothing to be done here
		
	}
	
	public AuthToken(String username) {
		this.username = username;
		this.tokenID = UUID.randomUUID().toString();
		this.creationDate = System.currentTimeMillis();
		this.expirationDate = this.creationDate + EXPIRATION_TIME;
		
	}
}
