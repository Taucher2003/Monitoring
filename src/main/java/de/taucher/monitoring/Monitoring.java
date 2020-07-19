package de.taucher.monitoring;

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
	
	public MonitoredInstance createInstance(String host, int port, MessageChannel notify) {
		MonitoredInstance mi = new MonitoredInstance(host, port, notify);
		monitors.add(mi);
		return mi;
	}
}
