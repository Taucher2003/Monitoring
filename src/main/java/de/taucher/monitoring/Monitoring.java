package de.taucher.monitoring;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class Monitoring {
	
	private static Monitoring instance;
	private Constants constants;
	private Discord discord;
	private List<MonitoredInstance> monitors;
	private List<MonitoringMessage> messages;
	private List<MonitoringMessage> cooldown;
	
	Monitoring() {
		instance = this;
		constants = new Constants();
		discord = new Discord(constants.botToken);
		monitors = new LinkedList<>();
		messages = new LinkedList<>();
		cooldown = new LinkedList<>();
		MonitoredInstance root = createInstance("Vrox Rootserver", "Hostserver", "rp.vrox.eu", 22, discord.getJDA().retrieveUserById(444889694002741249L).complete().openPrivateChannel().complete());
		MonitoredInstance bungee = createInstance("Vrox Bungeecord", "Bungeecord", "rp.vrox.eu", 25565, discord.getJDA().retrieveUserById(444889694002741249L).complete().openPrivateChannel().complete());
		MonitoredInstance bau = createInstance("Vrox Bauserver", "Bauserver", "rp.vrox.eu", 25544, discord.getJDA().retrieveUserById(444889694002741249L).complete().openPrivateChannel().complete());
		MonitoredInstance ts = createInstance("Vrox TeamSpeak", "TeamSpeak", "rp.vrox.eu", 10011, discord.getJDA().retrieveUserById(444889694002741249L).complete().openPrivateChannel().complete());
//		MonitoredInstance mysql = createInstance("Vrox Datenbank", "Datenbank", "rp.vrox.eu", 3306, discord.getJDA().retrieveUserById(444889694002741249L).complete().openPrivateChannel().complete());
		
		Message m = discord.getJDA().getTextChannelById(734436413302505629L).retrieveMessageById(734827097767280692L).complete();
		createMessage(null, m, root, bungee, bau, ts);
		
		messages.forEach(message -> {
			message.update();
		});
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
	
	public MonitoredInstance createInstance(String name, String displayname, String host, int port, MessageChannel... notify) {
		for(MonitoredInstance mi : monitors) {
			if(mi.getHost().equals(host) && mi.getPort() == port && Arrays.asList(mi.getChannels()).containsAll(Arrays.asList(notify))) {
				return null;
			}
		}
		MonitoredInstance mi = new MonitoredInstance(name, displayname, host, port, notify);
		monitors.add(mi);
		return mi;
	}
	
	public MonitoringMessage createMessage(String category, Message message, MonitoredInstance... instances) {
		for(MonitoringMessage mm : messages) {
			if(mm.getMessage().equals(message) && Arrays.asList(mm.getInstances()).containsAll(Arrays.asList(instances))) {
				return null;
			}
		}
		MonitoringMessage mm = new MonitoringMessage(category, message, instances);
		messages.add(mm);
		return mm;
	}
	
	public final List<MonitoringMessage> getMessages(MonitoredInstance instance) {
		List<MonitoringMessage> result = new LinkedList<>();
		for(MonitoringMessage mm : messages) {
			if(Arrays.asList(mm.getInstances()).contains(instance)) {
				result.add(mm);
			}
		}
		return result;
	}
	
	public void updateMessage(MonitoringMessage message) {
		if(cooldown.contains(message)) {
			return;
		}
		cooldown.add(message);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				message.update();
				cooldown.remove(message);
			}
		}, 5000);
	}
}
