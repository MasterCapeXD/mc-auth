package me.mastercapexd.auth.storage.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.common.collect.Sets;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.mastercapexd.auth.Account;
import me.mastercapexd.auth.AccountFactory;
import me.mastercapexd.auth.HashType;
import me.mastercapexd.auth.IdentifierType;
import me.mastercapexd.auth.PluginConfig;
import me.mastercapexd.auth.storage.AccountStorage;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SQLAccountStorage implements AccountStorage {

	private final PluginConfig config;
	private final AccountFactory accountFactory;
	private final String CREATE_TABLE, SELECT_BY_ID, SELECT_BY_LAST_QUIT_ORDERED, SELECT_EMAILS, SELECT_ALL, UPDATE_ID, DELETE;
	
	protected abstract Connection getConnection() throws SQLException;
	
	protected void createTable() {
		try (Connection connection = this.getConnection()) {
			connection.createStatement().execute(CREATE_TABLE);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void updateAccount(Account account) {
		try (Connection connection = this.getConnection()) {
			PreparedStatement statement = connection.prepareStatement(UPDATE_ID);
			statement.setString(1, account.getId());
			statement.setString(2, account.getUniqueId().toString());
			statement.setString(3, account.getName());
			statement.setString(4, account.getPasswordHash());
			statement.setString(5, account.getEmail());
			statement.setLong(6, account.getLastQuitTime());
			statement.setString(7, account.getLastIpAddress());
			statement.setLong(8, account.getLastSessionStart());
			statement.setString(9, account.getIdentifierType().name());
			statement.setString(10, account.getHashType().name());
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected Account selectAccount(String id) {
		Account account = null;
		try (Connection connection = this.getConnection()) {
			PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID);
			statement.setString(1, id);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				account = accountFactory.createAccount(
						id,
						IdentifierType.valueOf(resultSet.getString("id_type")),
						UUID.fromString(resultSet.getString("uuid")),
						resultSet.getString("name"),
						HashType.valueOf(resultSet.getString("hash_type")),
						resultSet.getString("password"),
						resultSet.getString("email"),
						resultSet.getLong("last_quit"),
						resultSet.getString("last_ip"),
						resultSet.getLong("last_session_start"),
						config.getSessionDurability());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return account;
	}
	
	@Override
	public void saveOrUpdateAccount(Account account) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(() -> updateAccount(account));
		executorService.shutdown();
	}
	
	@Override
	public CompletableFuture<Account> getAccount(String id) {
		return CompletableFuture.supplyAsync(() -> selectAccount(id));
	}
	
	@Override
	public CompletableFuture<Collection<Account>> getAccounts(int limit) {
		return CompletableFuture.supplyAsync(() -> {
			Collection<Account> accounts = Sets.newHashSet();
			try (Connection connection = this.getConnection()) {
				PreparedStatement statement = connection.prepareStatement(SELECT_BY_LAST_QUIT_ORDERED);
				statement.setInt(1, limit);
				ResultSet resultSet = statement.executeQuery();
				while (resultSet.next()) {
					Account account = accountFactory.createAccount(
							resultSet.getString("id"),
							IdentifierType.valueOf(resultSet.getString("id_type")),
							UUID.fromString(resultSet.getString("uuid")),
							resultSet.getString("name"),
							HashType.valueOf(resultSet.getString("hash_type")),
							resultSet.getString("password"),
							resultSet.getString("email"),
							resultSet.getLong("last_quit"),
							resultSet.getString("last_ip"),
							resultSet.getLong("last_session_start"),
							config.getSessionDurability());
					accounts.add(account);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return accounts;
		});
	}
	
	@Override
	public CompletableFuture<Collection<Account>> getAllAccounts() {
		return CompletableFuture.supplyAsync(() -> {
			Collection<Account> accounts = Sets.newHashSet();
			try (Connection connection = this.getConnection()) {
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(SELECT_ALL);
				while (resultSet.next()) {
					Account account = accountFactory.createAccount(
							resultSet.getString("id"),
							IdentifierType.valueOf(resultSet.getString("id_type")),
							UUID.fromString(resultSet.getString("uuid")),
							resultSet.getString("name"),
							HashType.valueOf(resultSet.getString("hash_type")),
							resultSet.getString("password"),
							resultSet.getString("email"),
							resultSet.getLong("last_quit"),
							resultSet.getString("last_ip"),
							resultSet.getLong("last_session_start"),
							config.getSessionDurability());
					accounts.add(account);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return accounts;
		});
	}
	
	@Override
	public CompletableFuture<Collection<String>> getEmails() {
		return CompletableFuture.supplyAsync(() -> {
			Collection<String> emails = Sets.newHashSet();
			try (Connection connection = this.getConnection()) {
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(SELECT_EMAILS);
				while (resultSet.next()) {
					String email = resultSet.getString("email");
					if (email == null)
						continue;
					emails.add(email);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return emails;
		});
	}
	
	@Override
	public void deleteAccount(String id) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(() -> {
			try (Connection connection = this.getConnection()) {
				connection.createStatement().execute(DELETE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
}