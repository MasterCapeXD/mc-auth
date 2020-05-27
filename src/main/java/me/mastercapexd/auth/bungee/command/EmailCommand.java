package me.mastercapexd.auth.bungee.command;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import me.mastercapexd.auth.Auth;
import me.mastercapexd.auth.EmailConfirmationEntry;
import me.mastercapexd.auth.PluginConfig;
import me.mastercapexd.auth.email.EmailLetterType;
import me.mastercapexd.auth.email.EmailService;
import me.mastercapexd.auth.email.EmailSettings;
import me.mastercapexd.auth.email.LetterSettings;
import me.mastercapexd.auth.storage.AccountStorage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class EmailCommand extends Command {

	private final PluginConfig config;
	private final AccountStorage accountStorage;
	private final EmailService emailService;
	private final String codeChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	private final SecureRandom random = new SecureRandom();
	
	private String generateCode() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			int index = random.nextInt(codeChars.length());
			stringBuilder.append(codeChars.charAt(index));
		}
		return stringBuilder.toString();
	}
	
	public EmailCommand(PluginConfig config, AccountStorage accountStorage, EmailService emailService) {
		super("email");
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
		
		if (args.length == 0) {
			sender.sendMessages(ChatColor.YELLOW + "/email register", ChatColor.YELLOW +  "/email delete", ChatColor.YELLOW + "/email confirm");
			return;
		}
		
		String id = config.getActiveIdentifierType().getId((ProxiedPlayer) sender);
		String sub = args[0];
		if (sub.equalsIgnoreCase("register")) {
			
			if (args.length < 2) {
				sender.sendMessage(config.getMessages().getMessage("enter-email"));
				return;
			}
			
			if (Auth.getEmailConfirmationEntry(id) != null) {
				sender.sendMessage(config.getMessages().getMessage("email-already-sent"));
				return;
			}
			
			String email = args[1];
			
			InternetAddress[] address = null;
			
			try {
				address = InternetAddress.parse(email);
			} catch (AddressException e) {}
			
			if (address == null) {
				sender.sendMessage(config.getMessages().getMessage("wrong-email-format"));
				return;
			}
			
			accountStorage.getEmails().thenAccept(emails -> {
				if (emails.contains(email)) {
					sender.sendMessage(config.getMessages().getMessage("duplicated-email"));
					return;
				}
				
				String code = generateCode();
				EmailSettings emailSettings = config.getEmailSettings();
				LetterSettings letterSettings = emailSettings.getLetterSettingsMap().get(EmailLetterType.CONFIRM_CODE);
				try {
					emailService.sendEmail(email, letterSettings.getSubject(), letterSettings.getText().replace("%code%", code).replace("%player%", sender.getName()));
					Auth.addEmailConfirmationEntry(id, email, code);
					sender.sendMessage(config.getMessages().getMessage("confirmation-email-sent"));
				} catch (UnsupportedEncodingException | MessagingException e) {
					sender.sendMessage(config.getMessages().getMessage("email-error"));
					return;
				}
			});
			
		} else if (sub.equalsIgnoreCase("delete")) {
			
			accountStorage.getAccount(id).thenAccept(account -> {
				if (account == null) {
					sender.sendMessage(config.getMessages().getMessage("account-not-found"));
					return;
				}
				
				if (account.getEmail() == null) {
					sender.sendMessage(config.getMessages().getMessage("no-email"));
					return;
				}
				
				account.setEmail(null);
				accountStorage.saveOrUpdateAccount(account);
				sender.sendMessage(config.getMessages().getMessage("email-deleted"));
			});
			
		} else if (sub.equalsIgnoreCase("confirm")) {
			
			EmailConfirmationEntry emailConfirmationEntry = Auth.getEmailConfirmationEntry(id);
			if (emailConfirmationEntry == null) {
				sender.sendMessage(config.getMessages().getMessage("no-code"));
				return;
			}
			
			if (args.length < 2) {
				sender.sendMessage(config.getMessages().getMessage("enter-code"));
				return;
			}
			
			String code = args[1];
			if (!emailConfirmationEntry.getCode().equals(code)) {
				sender.sendMessage(config.getMessages().getMessage("wrong-code"));
				return;
			}
			
			accountStorage.getAccount(id).thenAccept(account -> {
				if (account == null) {
					sender.sendMessage(config.getMessages().getMessage("account-not-found"));
					return;
				}
				
				account.setEmail(emailConfirmationEntry.getEmail());
				accountStorage.saveOrUpdateAccount(account);
				sender.sendMessage(config.getMessages().getMessage("email-registered"));
			});
		}
	}
}