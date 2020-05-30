package me.mastercapexd.auth.bungee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import lombok.Getter;
import me.mastercapexd.auth.HashType;
import me.mastercapexd.auth.IdentifierType;
import me.mastercapexd.auth.Messages;
import me.mastercapexd.auth.PluginConfig;
import me.mastercapexd.auth.Server;
import me.mastercapexd.auth.StorageDataSettings;
import me.mastercapexd.auth.TimeUtils;
import me.mastercapexd.auth.email.EmailLetterType;
import me.mastercapexd.auth.email.EmailSettings;
import me.mastercapexd.auth.email.LetterSettings;
import me.mastercapexd.auth.storage.StorageType;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

@Getter
public class BungeePluginConfig implements PluginConfig {

	private final IdentifierType activeIdentifierType;
	private final boolean nameCaseCheckEnabled, passwordConfirmationEnabled, safeStartEnabled;
	private final HashType activeHashType;
	private final StorageType storageType;
	private final Pattern namePattern, passwordPattern;
	private final int passwordMinLength, passwordMaxLength, passwordAttempts;
	private final long sessionDurability, authTime;
	
	private final List<Server> authServers, gameServers;
	
	private final StorageDataSettings storageDataSettings;
	private final int maximumCacheSize, accountCacheLimitOnServerStart;
	private final EmailSettings emailSettings;
	
	private final Messages messages;
	
	public BungeePluginConfig(Plugin plugin) {
		Configuration config = loadConfiguration(plugin.getDataFolder(), plugin.getResourceAsStream("config.yml"));
		this.activeIdentifierType = IdentifierType.valueOf(config.getString("id-type").toUpperCase());
		this.nameCaseCheckEnabled = config.getBoolean("check-name-case");
		this.passwordConfirmationEnabled = config.getBoolean("enable-password-confirm");
		this.activeHashType = HashType.valueOf(config.getString("hash-type").toUpperCase());
		this.storageType = StorageType.valueOf(config.getString("storage-type").toUpperCase());
		this.namePattern = Pattern.compile(config.getString("name-regex-pattern"));
		this.passwordPattern = Pattern.compile(config.getString("password-regex-pattern"));
		this.passwordMinLength = config.getInt("password-min-length");
		this.passwordMaxLength = config.getInt("password-max-length");
		this.passwordAttempts = config.getInt("password-attempts");
		this.sessionDurability = TimeUtils.parseTime(config.getString("session-durability"));
		this.authTime = config.getLong("auth-time");
		this.safeStartEnabled = config.getBoolean("safe-start");
		
		this.authServers = ImmutableList.copyOf(config.getStringList("auth-servers")
				.stream().map(stringFormat -> new Server(stringFormat)).collect(Collectors.toList()));
		this.gameServers = ImmutableList.copyOf(config.getStringList("game-servers")
				.stream().map(stringFormat -> new Server(stringFormat)).collect(Collectors.toList()));
		
		Configuration data = config.getSection("data");
		this.storageDataSettings = new StorageDataSettings(
				data.getString("host"),
				data.getString("database"),
				data.getString("username"),
				data.getString("password"),
				data.getInt("port"),
				data.getInt("pool-settings.maximum-pool-size"),
				data.getInt("pool-settings.minimum-idle"),
				TimeUtils.parseTime(data.getString("pool-settings.maximum-lifetime")),
				TimeUtils.parseTime(data.getString("pool-settings.connection-timeout")));
		
		this.maximumCacheSize = config.getInt("maximum-cache-size");
		this.accountCacheLimitOnServerStart = config.getInt("account-cache-limit-on-server-start");
		
		Configuration email = config.getSection("email");
		Builder<EmailLetterType, LetterSettings> builder = ImmutableMap.<EmailLetterType, LetterSettings> builder();
		
		for (EmailLetterType type : EmailLetterType.values())
			builder.put(type, new LetterSettings(
					email.getString("letters." + type.name() + ".subject"),
					email.getString("letters." + type.name() + ".text")));
		
		this.emailSettings = new EmailSettings(
				email.getBoolean("enabled"),
				email.getString("smtp"),
				email.getString("username"),
				email.getString("password"),
				email.getString("company"),
				builder.build());
		
		this.messages = new BungeeMessages(config.getSection("messages"));
	}
	
	@Override
	public ServerInfo findServerInfo(List<Server> servers) {
		ServerInfo optimal = null;
		for (Server server : servers) {
			ServerInfo serverInfo = BungeeCord.getInstance().getServerInfo(server.getId());
			if (serverInfo.getPlayers().size() >= server.getMaxPlayers())
				continue;
			optimal = serverInfo;
			break;
		}
		return optimal;
	}
	
	private Configuration loadConfiguration(File folder, InputStream resourceAsStream) {
		try {
			if (!folder.exists())
				folder.mkdir();
			
			File config = new File(folder + File.separator + "config.yml");
			if (!config.exists())
				Files.copy(resourceAsStream, config.toPath(), new CopyOption[0]);
			
			return ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		return null;
	}
}