package me.mastercapexd.auth.storage.mysql;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import me.mastercapexd.auth.AccountFactory;
import me.mastercapexd.auth.PluginConfig;
import me.mastercapexd.auth.StorageDataSettings;
import me.mastercapexd.auth.storage.sql.SQLAccountStorage;

public class MySQLAccountStorage extends SQLAccountStorage {

	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `auth` (`id` VARCHAR(64) PRIMARY KEY, `uuid` VARCHAR(64) NOT NULL, `name` VARCHAR(32) NOT NULL, `password` VARCHAR(255), `email` VARCHAR(255), `last_quit` BIGINT, `last_ip` VARCHAR(64), `last_session_start` BIGINT, `id_type` VARCHAR(32) NOT NULL, `hash_type` VARCHAR(32) NOT NULL);";
	private static final String SELECT_BY_ID = "SELECT * FROM `auth` WHERE `id` = ? LIMIT 1;";
	private static final String SELECT_BY_LAST_QUIT_ORDERED = "SELECT * FROM `auth` ORDER BY `last_quit` DESC LIMIT ?;";
	private static final String SELECT_ALL = "SELECT * FROM `auth`;";
	private static final String SELECT_EMAILS = "SELECT `email` FROM `auth`;";
	private static final String UPDATE_ID = "INSERT INTO `auth` (`id`, `uuid`, `name`, `password`, `email`, `last_quit`, `last_ip`, `last_session_start`, `id_type`, `hash_type`) VALUES "
			+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
			+ "`id` = VALUES(`id`), `uuid` = VALUES(`uuid`), `name` = VALUES(`name`), "
			+ "`password` = VALUES(`password`), `email` = VALUES(`email`), `last_quit` = VALUES(`last_quit`), `last_ip` = VALUES(`last_ip`), `last_session_start` = VALUES(`last_session_start`), `id_type` = VALUES(`id_type`), `hash_type` = VALUES(`hash_type`);";
	private static final String DELETE = "DELETE FROM `auth` WHERE `id`=?;";
	
	private final HikariDataSource dataSource;
	
	public MySQLAccountStorage(PluginConfig config, AccountFactory accountFactory) {
		super(config, accountFactory, CREATE_TABLE, SELECT_BY_ID, SELECT_BY_LAST_QUIT_ORDERED, SELECT_EMAILS, SELECT_ALL, UPDATE_ID, DELETE);
		
		StorageDataSettings dataSettings = config.getStorageDataSettings();
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl("jdbc:mysql://" + dataSettings.getHost() + ":" + dataSettings.getPort() + "/" + dataSettings.getDatabase() + "?useUnicode=yes&useSSL=false&characterEncoding=UTF-8");
		hikariConfig.setUsername(dataSettings.getUser());
		hikariConfig.setPassword(dataSettings.getPassword());
		hikariConfig.setMaximumPoolSize(dataSettings.getMaximumPoolSize());
		hikariConfig.setMinimumIdle(dataSettings.getMinimumIdle());
		hikariConfig.setMaxLifetime(dataSettings.getMaximumLifetime());
		hikariConfig.setConnectionTimeout(dataSettings.getConnectionTimeout());
		
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