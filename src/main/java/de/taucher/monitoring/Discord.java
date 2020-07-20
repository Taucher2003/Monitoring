package de.taucher.monitoring;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Discord {

	private long id;
	private JDA jda;
	
	Discord(String token) {
		JDABuilder builder = JDABuilder.createLight(token);
		builder.enableIntents(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES);
		builder.addEventListeners(new MessageReceivedListener());
		try {
			jda = builder.build();
			jda.awaitReady();
		} catch (LoginException | InterruptedException e) {
			e.printStackTrace();
			return;
		}
		id = jda.getSelfUser().getIdLong();
	}
	
	public JDA getJDA() {
		return jda;
	}
	
	public long getBotId() {
		return id;
	}
}