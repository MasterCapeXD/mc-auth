package me.mastercapexd.auth;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Auth {

	private static final Map<String, Account> accounts = Maps.newConcurrentMap();
	private static final Map<String, Long> accountTimes = Maps.newConcurrentMap();
	private static final Map<String, Integer> attempts = Maps.newConcurrentMap();
	private static final Map<String, EmailConfirmationEntry> emailConfirmationCodes = Maps.newConcurrentMap();
	private static final Collection<String> recoveryRequests = Sets.newConcurrentHashSet();
	
	public static synchronized Collection<String> getAccountIds() {
		return ImmutableSet.copyOf(accounts.keySet());
	}
	
	public static synchronized void addAccount(Account account) {
		accounts.put(account.getId(), account);
		accountTimes.put(account.getId(), System.currentTimeMillis());
	}
	
	public static synchronized void removeAccount(String id) {
		accounts.remove(id);
		accountTimes.remove(id);
		attempts.remove(id);
	}
	
	public static synchronized boolean hasAccount(String id) {
		return accounts.containsKey(id);
	}
	
	public static synchronized Account getAccount(String id) {
		return accounts.getOrDefault(id, null);
	}
	
	public static synchronized long getJoinTime(String id) {
		return accountTimes.getOrDefault(id, 0L);
	}
	
	public static synchronized void incrementAttempts(String id) {
		attempts.put(id, getPlayerAttempts(id) + 1);
	}
	
	public static synchronized void decrementAttempts(String id) {
		attempts.put(id, getPlayerAttempts(id) - 1);
	}
	
	public static synchronized int getPlayerAttempts(String id) {
		return attempts.getOrDefault(id, 0);
	}
	
	public static synchronized void addEmailConfirmationEntry(String id, String email, String code) {
		emailConfirmationCodes.put(id, new EmailConfirmationEntry(email, code));
	}
	
	public static synchronized void removeEmailConfirmationEntry(String id) {
		emailConfirmationCodes.remove(id);
	}
	
	public static synchronized EmailConfirmationEntry getEmailConfirmationEntry(String id) {
		return emailConfirmationCodes.getOrDefault(id, null);
	}
	
	public static synchronized boolean hasRecoveryRequest(String id) {
		return recoveryRequests.contains(id);
	}
	
	public static synchronized void addRecoveryRequest(String id) {
		recoveryRequests.add(id);
	}
	
	public static synchronized void removeRecoveryRequest(String id) {
		recoveryRequests.remove(id);
	}
}