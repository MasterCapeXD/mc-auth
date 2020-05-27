package me.mastercapexd.auth;

import java.util.List;
import java.util.regex.Pattern;

import me.mastercapexd.auth.email.EmailSettings;
import me.mastercapexd.auth.storage.StorageType;
import net.md_5.bungee.api.config.ServerInfo;

public interface PluginConfig {

	StorageDataSettings getStorageDataSettings();
	
	EmailSettings getEmailSettings();
	
	IdentifierType getActiveIdentifierType();
	
	boolean isNameCaseCheckEnabled();
	
	HashType getActiveHashType();
	
	StorageType getStorageType();
	
	Pattern getNamePattern();
	
	Pattern getPasswordPattern();
	
	List<Server> getAuthServers();
	
	List<Server> getGameServers();
	
	boolean isPasswordConfirmationEnabled();
	
	int getPasswordMinLength();
	
	int getPasswordMaxLength();
	
	int getPasswordAttempts();
	
	long getSessionDurability();
	
	long getAuthTime();
	
	Messages getMessages();
	
	ServerInfo findServerInfo(List<Server> servers);
}