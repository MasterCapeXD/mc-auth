package me.mastercapexd.auth.bungee.command;

import me.mastercapexd.auth.Account;
import me.mastercapexd.auth.Auth;
import me.mastercapexd.auth.PluginConfig;
import me.mastercapexd.auth.SessionResult;
import me.mastercapexd.auth.bungee.Connector;
import me.mastercapexd.auth.storage.AccountStorage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class LoginCommand extends Command {

	private final PluginConfig config;
	private final AccountStorage accountStorage;
	
	public LoginCommand(PluginConfig config, AccountStorage accountStorage) {
		super("login", null, "l");
		this.config = config;
		this.accountStorage = accountStorage;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(config.getMessages().getMessage("players-only"));
			return;
		}
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		String id = config.getActiveIdentifierType().getId(player);
		if (!Auth.hasAccount(id)) {
			sender.sendMessage(config.getMessages().getMessage("already-logged-in"));
			return;
		}
		
		Account account = Auth.getAccount(id);
		if (!account.isRegistered()) {
			sender.sendMessage(config.getMessages().getMessage("account-not-found"));
			return;
		}
		
		String password = null;
		if (args.length < 1) {
			sender.sendMessage(config.getMessages().getMessage("enter-password"));
			return;
		}
		
		password = args[0];
		
		SessionResult result = account.newSession(config.getActiveHashType(), password);
		if (result == SessionResult.LOGIN_WRONG_PASSWORD) {
			if (config.getPasswordAttempts() < 1) {
				sender.sendMessage(config.getMessages().getMessage("wrong-password"));
				return;
			}
			Auth.incrementAttempts(id);
			int attempts = Auth.getPlayerAttempts(id);
			if (attempts < config.getPasswordAttempts())
				sender.sendMessage(config.getMessages().getMessage("wrong-password").replace("%attempts%", (config.getPasswordAttempts() - attempts) + ""));
			else
				player.disconnect(config.getMessages().getMessage("attempts-limit"));
			return;
		}
		
		if (result != SessionResult.LOGIN_SUCCESS)
			return;
		
		Auth.removeAccount(id);
		accountStorage.saveOrUpdateAccount(account);
		sender.sendMessage(config.getMessages().getMessage("login-success"));
		Connector.connectOrKick(player, config.findServerInfo(config.getGameServers()), config.getMessages().getMessage("game-servers-connection-refused"));
	}
}