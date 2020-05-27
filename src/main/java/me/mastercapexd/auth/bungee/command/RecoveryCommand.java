package me.mastercapexd.auth.bungee.command;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import javax.mail.MessagingException;

import com.google.common.base.Charsets;

import me.mastercapexd.auth.Auth;
import me.mastercapexd.auth.PluginConfig;
import me.mastercapexd.auth.bungee.BungeeAccount;
import me.mastercapexd.auth.email.EmailLetterType;
import me.mastercapexd.auth.email.EmailService;
import me.mastercapexd.auth.email.EmailSettings;
import me.mastercapexd.auth.email.LetterSettings;
import me.mastercapexd.auth.storage.AccountStorage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class RecoveryCommand extends Command {

	private final PluginConfig config;
	private final AccountStorage accountStorage;
	private final EmailService emailService;
	
	private final String passwordChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	private final SecureRandom random = new SecureRandom();
	
	private String generatePassword() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			int index = random.nextInt(passwordChars.length());
			stringBuilder.append(passwordChars.charAt(index));
		}
		return stringBuilder.toString();
	}
	
	public RecoveryCommand(PluginConfig config, AccountStorage accountStorage, EmailService emailService) {
		super("recovery");
		this.config = config;
		this.accountStorage = accountStorage;
		this.emailService = emailService;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(config.getMessages().getMessage("players-only"));
			return;
		}
		
		if (!config.getEmailSettings().isEnabled()) {
			sender.sendMessage(config.getMessages().getMessage("email-disabled"));
			return;
		}
		
		String id = config.getActiveIdentifierType().getId((ProxiedPlayer) sender);
		accountStorage.getAccount(id).thenAccept(account -> {
			
			if (account == null) {
				sender.sendMessage(config.getMessages().getMessage("account-not-found"));
				return;
			}
			
			if (account.getEmail() == null) {
				sender.sendMessage(config.getMessages().getMessage("no-email"));
				return;
			}
			
			if (args.length == 0) {
				sender.sendMessage(config.getMessages().getMessage("enter-email"));
				return;
			}
			
			String email = args[0];
			
			if (!account.getEmail().equalsIgnoreCase(email)) {
				sender.sendMessage("wrong-email");
				return;
			}
			
			if (Auth.hasRecoveryRequest(id)) {
				sender.sendMessage(config.getMessages().getMessage("recovery-request-already-sent"));
				return;
			}
			
			String password = generatePassword();
			EmailSettings emailSettings = config.getEmailSettings();
			LetterSettings letterSettings = emailSettings.getLetterSettingsMap().get(EmailLetterType.NEW_PASSWORD);
			try {
				emailService.sendEmail(account.getEmail(), letterSettings.getSubject(), letterSettings.getText().replace("%password%", password).replace("%player%", sender.getName()));
				Auth.addRecoveryRequest(id);
				BungeeAccount bungeeAccount = (BungeeAccount) account;
				if (account.getHashType() != config.getActiveHashType())
					bungeeAccount.setHashType(config.getActiveHashType());
				
				bungeeAccount.setPasswordHash(config.getActiveHashType().getHashFunction().newHasher().putString(password, Charsets.UTF_8).hash().toString());
				accountStorage.saveOrUpdateAccount(account);
				sender.sendMessage(config.getMessages().getMessage("new-password-email-sent"));
			} catch (UnsupportedEncodingException | MessagingException e) {
				sender.sendMessage(config.getMessages().getMessage("email-error"));
				return;
			}
		});
	}
}