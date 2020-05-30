package me.mastercapexd.auth;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public enum IdentifierType {

	UUID {
		@Override
		public String getId(ProxiedPlayer proxiedPlayer) {
			return proxiedPlayer.getUniqueId().toString();
		}
		
		@Override
		public ProxiedPlayer getPlayer(String id) {
			return BungeeCord.getInstance().getPlayer(java.util.UUID.fromString(id));
		}
	},
	NAME {
		@Override
		public String getId(ProxiedPlayer proxiedPlayer) {
			return proxiedPlayer.getName().toLowerCase();
		}
		
		@Override
		public ProxiedPlayer getPlayer(String id) {
			return BungeeCord.getInstance().getPlayer(id);
		}
	};
	
	public abstract String getId(ProxiedPlayer proxiedPlayer);
	
	public abstract ProxiedPlayer getPlayer(String id);
}