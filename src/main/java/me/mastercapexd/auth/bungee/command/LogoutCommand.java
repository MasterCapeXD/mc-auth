package me.mastercapexd.auth.bungee.command;

import me.mastercapexd.auth.Auth;
import me.mastercapexd.auth.PluginConfig;
import me.mastercapexd.auth.bungee.Connector;
import me.mastercapexd.auth.storage.AccountStorage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class LogoutCommand extends Command {

	private final PluginConfig config;
	private final AccountStorage accountStorage;
	
	public LogoutCommand(PluginConfig config, AccountStorage accountStorage) {
		super("logout");
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
		if (Auth.hasAccount(id)) {
			sender.sendMessage(config.getMessages().getMessage("already-logged-out"));
			return;
		}
		
		accountStorage.getAccount(id)
		.thenAccept(account -> {
			account.logout(config.getSessionDurability());
			accountStorage.saveOrUpdateAccount(account);
			Auth.addAccount(account);
			sender.sendMessage(config.getMessages().getMessage("logout-success"));
			Connector.connectOrKick(player, config.findServerInfo(config.getAuthServers()), config.getMessages().getMessage("auth-servers-connection-refused"));
		});
	}
}