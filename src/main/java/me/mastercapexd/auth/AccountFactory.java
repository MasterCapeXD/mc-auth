package me.mastercapexd.auth;

import java.util.UUID;

public interface AccountFactory {

	Account createAccount(String id, IdentifierType identifierType, UUID uuid, String name, HashType hashType, String password, String email, long lastQuit, String lastIp, long lastSessionStart, long sessionTime);
	
	default Account createAccount(String id, IdentifierType identifierType, UUID uuid, String name, HashType hashType, String password, String email, long sessionTime) {
		return createAccount(id, identifierType, uuid, name, hashType, password, email, 0, null, 0, sessionTime);
	}
	
	default Account createAccount(String id, IdentifierType identifierType, UUID uuid, String name, HashType hashType, String password, long sessionTime) {
		return createAccount(id, identifierType, uuid, name, hashType, password, null, 0, null, 0, sessionTime);
	}
	
	default Account createAccount(String id, IdentifierType identifierType, UUID uuid, String name, HashType hashType, long sessionTime) {
		return createAccount(id, identifierType, uuid, name, hashType, null, null, 0, null, 0, sessionTime);
	}
}