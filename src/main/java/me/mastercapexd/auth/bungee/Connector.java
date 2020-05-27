package me.mastercapexd.auth.bungee;

import java.util.function.Consumer;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Connector {

	@SuppressWarnings("deprecation")
	public static void connectOrKick(ProxiedPlayer player, ServerInfo serverInfo, String error) {
		connect(player, serverInfo, error, p -> p.disconnect(error));
	}
	
	@SuppressWarnings("deprecation")
	public static void connect(ProxiedPlayer player, ServerInfo serverInfo, String error, Consumer<ProxiedPlayer> onFail) {
		if (serverInfo == null) {
			player.disconnect(error);
			return;
		}
		
		if (player.getServer().getInfo().equals(serverInfo))
			return;
		
		TitleBar.send(player, "", "", 0, 10, 0);
		
		player.connect(serverInfo, new Callback<Boolean>() {
			@Override
			public void done(Boolean success, Throwable throwable) {
				if (!success)
					onFail.accept(player);
			}
		});
	}
}