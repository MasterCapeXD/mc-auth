package me.mastercapexd.auth;

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Server {

	private final String id;
	private final int maxPlayers;
	
	public Server(String stringFormat) {
		String[] args = stringFormat.split(":");
		Preconditions.checkArgument(args.length >= 2, String.format("Wrong server format in config.yml: %s.", stringFormat));
		this.id = args[0];
		this.maxPlayers = Integer.parseInt(args[1]);
	}
}