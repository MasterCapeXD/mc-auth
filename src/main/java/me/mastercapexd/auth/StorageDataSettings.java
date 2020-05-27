package me.mastercapexd.auth;

import lombok.Data;

@Data
public class StorageDataSettings {

	private final String host, database, user, password;
	private final int port, maximumPoolSize, minimumIdle;
	private final long maximumLifetime, connectionTimeout;
}