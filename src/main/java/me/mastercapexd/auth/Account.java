package me.mastercapexd.auth;

import java.util.UUID;

import lombok.NonNull;

public interface Account {

	@NonNull
	String getId();
	
	@NonNull
	IdentifierType getIdentifierType();
	
	@NonNull
	HashType getHashType();
	
	@NonNull
	UUID getUniqueId();
	
	@NonNull
	String getName();
	
	String getPasswordHash();
	
	void setPasswordHash(String passwordHash);
	
	String getEmail();
	
	void setEmail(String email);
	
	boolean isOnline();
	
	long getLastQuitTime();
	
	void setLastQuitTime(long time);
	
	String getLastIpAddress();
	
	long getLastSessionStart();
	
	SessionResult newSession(HashType hashType, String password);
	
	SessionResult logout(long sessionDurability);
	
	boolean isSessionActive(long sessionDurability);
	
	default boolean isRegistered() {
		return getPasswordHash() != null;
	}
}