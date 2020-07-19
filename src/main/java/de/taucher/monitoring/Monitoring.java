package de.taucher.monitoring;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.dv8tion.jda.api.entities.MessageChannel;

public class Monitoring {
	
	private static Monitoring instance;
	private Constants constants;
	private Discord discord;
	private List<MonitoredInstance> monitors;
	
	Monitoring() {
		instance = this;
		constants = new Constants();
		discord = new Discord(constants.botToken);
		monitors = new LinkedList<>();
		createInstance("Vrox", "rp.vrox.eu", 25565, discord.getJDA().retrieveUserById(444889694002741249L).complete().openPrivateChannel().complete());
	}
	
	public static Monitoring getInstance() {
		return instance;
	}

	Constants getConstants() {
		return constants;
	}
	
	public Discord getDiscord() {
		return discord;
	}
	
	public MonitoredInstance createInstance(String name, String host, int port, MessageChannel... notify) {
		for(MonitoredInstance mi : monitors) {
			if(mi.getHost().equals(host) && mi.getPort() == port && Arrays.asList(mi.getChannels()).containsAll(Arrays.asList(notify))) {
				return null;
			}
		}
		MonitoredInstance mi = new MonitoredInstance(name, host, port, notify);
		monitors.add(mi);
		return mi;
	}
}
