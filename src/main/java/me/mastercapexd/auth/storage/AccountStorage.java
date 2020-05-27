package me.mastercapexd.auth.storage;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import me.mastercapexd.auth.Account;

public interface AccountStorage {

	CompletableFuture<Account> getAccount(String id);
	
	CompletableFuture<Collection<Account>> getAccounts(int limit);
	
	CompletableFuture<Collection<Account>> getAllAccounts();
	
	CompletableFuture<Collection<String>> getEmails();
	
	void saveOrUpdateAccount(Account account);
	
	void deleteAccount(String id);
	
	default void deleteAccount(Account account) {
		deleteAccount(account.getId());
	}
}