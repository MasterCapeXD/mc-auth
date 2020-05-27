package me.mastercapexd.auth.bungee.command;

import me.mastercapexd.auth.Auth;
import me.mastercapexd.auth.PluginConfig;
import me.mastercapexd.auth.storage.AccountStorage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class AuthCommand extends Command {

	private final Plugin plugin;
	private final PluginConfig config;
	private final AccountStorage accountStorage;
	
	public AuthCommand(Plugin plugin, PluginConfig config, AccountStorage accountStorage) {
		super("auth");
		this.plugin = plugin;
		this.config = config;
		this.accountStorage = accountStorage;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission("auth.admin")) {
			sender.sendMessage(config.getMessages().getMessage("no-permission"));
			return;
		}
		
		accountStorage.getAllAccounts().thenAccept(accounts -> {
			sender.sendMessage(config.getMessages().getMessage("info-registered").replace("%players%", accounts.size() + ""));
			sender.sendMessage(config.getMessages().getMessage("info-auth").replace("%players%", Auth.getAccountIds().size() + ""));
			sender.sendMessage(config.getMessages().getMessage("info-version").replace("%version%", plugin.getDescription().getVersion()));
		});
	}
}