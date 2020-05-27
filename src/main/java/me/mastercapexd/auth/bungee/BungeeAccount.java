package me.mastercapexd.auth.bungee;

import java.util.UUID;

import com.google.common.base.Charsets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.mastercapexd.auth.Account;
import me.mastercapexd.auth.HashType;
import me.mastercapexd.auth.IdentifierType;
import me.mastercapexd.auth.SessionResult;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@Getter
@RequiredArgsConstructor
public class BungeeAccount implements Account {

	private final String id;
	private final IdentifierType identifierType;
	
	@Setter private HashType hashType;
	
	private final UUID uniqueId;
	private final String name;
	
	@Setter private String passwordHash, email, lastIpAddress;
	@Setter private long lastQuitTime, lastSessionStart;
	
	private boolean online;
	
	@SuppressWarnings("deprecation")
	@Override
	public SessionResult newSession(HashType hashType, String password) {
		ProxiedPlayer proxiedPlayer = identifierType.getPlayer(getId());
		String passwordHash = getHashType().getHashFunction().newHasher().putString(password, Charsets.UTF_8).hash().toString();
		
		if (!isRegistered()) {
			setPasswordHash(passwordHash);
			setLastIpAddress(proxiedPlayer.getAddress().getHostString());
			setLastSessionStart(System.currentTimeMillis());
			return SessionResult.REGISTER_SUCCESS;
		}
		
		if (passwordHash.equals(getPasswordHash())) {
			if (getHashType() != hashType)
				setHashType(hashType);
			
			setPasswordHash(hashType.getHashFunction().newHasher().putString(password, Charsets.UTF_8).hash().toString());
			setLastIpAddress(proxiedPlayer.getAddress().getHostString());
			setLastSessionStart(System.currentTimeMillis());
			return SessionResult.LOGIN_SUCCESS;
		}
		
		return SessionResult.LOGIN_WRONG_PASSWORD;
	}
	
	@Override
	public SessionResult logout(long sessionDurability) {
		if (!isSessionActive(sessionDurability))
			return SessionResult.LOGOUT_FAILED_NOT_LOGGED_IN;
		
		setLastSessionStart(0);
		return SessionResult.LOGOUT_SUCCESS;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean isSessionActive(long sessionDurability) {
		ProxiedPlayer proxiedPlayer = identifierType.getPlayer(getId());
		return proxiedPlayer.getAddress().getHostString().equals(getLastIpAddress()) && (getLastSessionStart() + sessionDurability >= System.currentTimeMillis());
	}
}