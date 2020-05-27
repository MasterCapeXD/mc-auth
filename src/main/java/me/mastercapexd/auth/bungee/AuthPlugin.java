package me.mastercapexd.auth.bungee;

import me.mastercapexd.auth.AccountFactory;
import me.mastercapexd.auth.AuthEngine;
import me.mastercapexd.auth.bungee.command.AuthCommand;
import me.mastercapexd.auth.bungee.command.ChangePasswordCommand;
import me.mastercapexd.auth.bungee.command.EmailCommand;
import me.mastercapexd.auth.bungee.command.LoginCommand;
import me.mastercapexd.auth.bungee.command.LogoutCommand;
import me.mastercapexd.auth.bungee.command.RecoveryCommand;
import me.mastercapexd.auth.bungee.command.RegisterCommand;
import me.mastercapexd.auth.email.EmailService;
import me.mastercapexd.auth.storage.AccountStorage;
import me.mastercapexd.auth.storage.StorageType;
import me.mastercapexd.auth.storage.mysql.MySQLAccountStorage;
import me.mastercapexd.auth.storage.sqlite.SQLiteAccountStorage;
import net.md_5.bungee.api.plugin.Plugin;

public class AuthPlugin extends Plugin {

	private BungeePluginConfig config;
	private AccountFactory accountFactory;
	private AccountStorage accountStorage;
	private EmailService emailService;
	private volatile AuthEngine authEngine;
	private EventListener eventListener;
	
	@Override
	public void onEnable() {
		this.config = new BungeePluginConfig(this);
		this.accountFactory = new BungeeAccountFactory();
		this.accountStorage = loadAccountStorage(config.getStorageType());
		this.authEngine = new BungeeAuthEngine(this, config);
		authEngine.start();
		
		this.eventListener = new EventListener(config, accountFactory, accountStorage);
		this.getProxy().getPluginManager().registerListener(this, eventListener);
		
		this.getProxy().getPluginManager().registerCommand(this, new RegisterCommand(config, accountStorage));
		this.getProxy().getPluginManager().registerCommand(this, new LoginCommand(config, accountStorage));
		this.getProxy().getPluginManager().registerCommand(this, new LogoutCommand(config, accountStorage));
		this.getProxy().getPluginManager().registerCommand(this, new ChangePasswordCommand(config, accountStorage));
		this.getProxy().getPluginManager().registerCommand(this, new AuthCommand(this, config, accountStorage));
		
		if (config.getEmailSettings().isEnabled())
			this.emailService = new EmailService(config.getEmailSettings());
		this.getProxy().getPluginManager().registerCommand(this, new EmailCommand(config, accountStorage, emailService));
		this.getProxy().getPluginManager().registerCommand(this, new RecoveryCommand(config, accountStorage, emailService));
	}
	
	private AccountStorage loadAccountStorage(StorageType storageType) {
		switch (storageType) {
		case SQLITE:
			return new SQLiteAccountStorage(config, accountFactory, this.getDataFolder());
		case MYSQL:
			return new MySQLAccountStorage(config, accountFactory);
		}
		throw new NullPointerException("Wrong account storage!");
	}
}