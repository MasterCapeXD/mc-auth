package me.mastercapexd.auth.bungee;

import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import me.mastercapexd.auth.Account;
import me.mastercapexd.auth.Auth;
import me.mastercapexd.auth.AuthEngine;
import me.mastercapexd.auth.PluginConfig;
import me.mastercapexd.auth.Server;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

@RequiredArgsConstructor
public class BungeeAuthEngine implements AuthEngine {

	private final Plugin plugin;
	private final PluginConfig config;
	private ScheduledTask authTask;
	
	@SuppressWarnings("deprecation")
	@Override
	public void start() {
		this.authTask = BungeeCord.getInstance().getScheduler().schedule(plugin, () -> {
			
			long now = System.currentTimeMillis();
			
			for (Server server : config.getAuthServers()) {
				
				ServerInfo serverInfo = BungeeCord.getInstance().getServerInfo(server.getId());
				if (serverInfo == null)
					continue;
				
				for (ProxiedPlayer player : serverInfo.getPlayers()) {
					String id = config.getActiveIdentifierType().getId(player);
					Account account = Auth.getAccount(id);
					if (account != null) {
						if ((now - Auth.getJoinTime(id)) / 1000 >= (Auth.hasRecoveryRequest(id) ? config.getAuthTime() * 10 : config.getAuthTime())) {
							player.disconnect(config.getMessages().getMessage("time-left"));
							Auth.removeAccount(id);
							continue;
						}
						if (account.isRegistered()) {
							player.sendMessage(config.getMessages().getMessage("login-chat"));
							TitleBar.send(player, config.getMessages().getMessage("login-title"), config.getMessages().getMessage("login-subtitle"), 0, 120, 0);
						} else {
							player.sendMessage(config.getMessages().getMessage("register-chat"));
							TitleBar.send(player, config.getMessages().getMessage("register-title"), config.getMessages().getMessage("register-subtitle"), 0, 120, 0);
						}
					} else {
						player.sendMessage(config.getMessages().getMessage("autoconnect"));
						Connector.connectOrKick(player, config.findServerInfo(config.getGameServers()), config.getMessages().getMessage("game-servers-connection-refused"));
					}
				}
				
			}
		}, 0L, 1000L, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void stop() {
		if (authTask != null) {
			authTask.cancel();
			authTask = null;
		}
	}
}