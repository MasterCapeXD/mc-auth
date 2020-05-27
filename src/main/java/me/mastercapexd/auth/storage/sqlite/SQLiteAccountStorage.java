package me.mastercapexd.auth.storage.sqlite;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import me.mastercapexd.auth.AccountFactory;
import me.mastercapexd.auth.PluginConfig;
import me.mastercapexd.auth.storage.sql.SQLAccountStorage;

public class SQLiteAccountStorage extends SQLAccountStorage {

	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `auth` (`id` VARCHAR(50) PRIMARY KEY, `uuid` VARCHAR(64) NOT NULL, `name` VARCHAR(32) NOT NULL, `password` VARCHAR(255), `email` VARCHAR(255), `last_quit` INTEGER, `last_ip` VARCHAR(64), `last_session_start` INTEGER, `id_type` VARCHAR(32) NOT NULL, `hash_type` VARCHAR(32) NOT NULL);";
	private static final String SELECT_BY_ID = "SELECT * FROM `auth` WHERE `id` = ? LIMIT 1;";
	private static final String SELECT_BY_LAST_QUIT_ORDERED = "SELECT * FROM `auth` ORDER BY `last_quit` DESC LIMIT ?;";
	private static final String SELECT_ALL = "SELECT * FROM `auth`;";
	private static final String SELECT_EMAILS = "SELECT `email` FROM `auth`;";
	private static final String UPDATE_ID = "REPLACE INTO `auth` (`id`, `uuid`, `name`, `password`, `email`, `last_quit`, `last_ip`, `last_session_start`, `id_type`, `hash_type`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	private static final String DELETE = "DELETE FROM `auth` WHERE `id`=?;";
	
	private final HikariDataSource dataSource;
	
	public SQLiteAccountStorage(PluginConfig config, AccountFactory accountFactory, File parent) {
		super(config, accountFactory, CREATE_TABLE, SELECT_BY_ID, SELECT_BY_LAST_QUIT_ORDERED, SELECT_EMAILS, SELECT_ALL, UPDATE_ID, DELETE);
		
		File file = new File(parent + File.separator + "auth.db");
		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
		hikariConfig.addDataSourceProperty("cachePrepStmts", true);
		hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
		hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
		
		this.dataSource = new HikariDataSource(hikariConfig);
		this.createTable();
	}
	
	@Override
	protected Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
}