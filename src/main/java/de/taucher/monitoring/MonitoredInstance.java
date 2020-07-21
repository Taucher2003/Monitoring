package de.taucher.monitoring;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class MonitoredInstance {

	private static final String OK_SYMBOL = "<a:vrox_yes:716334571708481578>";
	private static final String BAD_SYMBOL = OK_SYMBOL; //Symbol coming soon
	private static final String FAIL_SYMBOL = "<a:vrox_no:716334571515412482>";
	private static final String[] StateNames = new String[] { "Is reachable", "Bad connections", "Not reachable" };

	private String name;
	private String displayname;
	private String host;
	private int port;
	private MessageChannel[] channels;
	private int wasReachable = -1;

	EmbedBuilder embeds[];

	private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.GERMANY);

	MonitoredInstance(String name, String displayname, String host, int port, MessageChannel... notify) {
		this.name = name;
		this.displayname = displayname;
		this.host = host;
		this.port = port;
		this.channels = notify;

		embeds = new EmbedBuilder[3];
		embeds[0] = getEmbedBuilder("is now reachable", OK_SYMBOL, 0x15d11c);
		embeds[1] = getEmbedBuilder("has a bad connection", BAD_SYMBOL, 0xec6602);
		embeds[2] = getEmbedBuilder("is no longer reachable", FAIL_SYMBOL, 0xd11a15);

		scheduleCheck();
	}

	private void scheduleCheck() {
		new Timer("Monitoring " + host + ":" + port, true).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				check();
			}
		}, 1000, 60 * 1000);
	}

	public void check() {

		int state = checkSystem();
	
		// emit message
		System.out.println("["+format.format(new Date())+"] Checked " + host+":"+port + " ("+name+") -> "+ StateNames[state]);
		
		if(wasReachable != state) {
			Monitoring.getInstance().getMessages(this).forEach(message -> {
				Monitoring.getInstance().updateMessage(message);
			});
			if(wasReachable != -1) {
				MessageEmbed msg = embeds[state].setFooter("Timestamp • "+format.format(new Date())).build();
				for(MessageChannel messagechannel : channels) { 
					messagechannel.sendMessage(msg).queue();
				}
			}
		}
			
		wasReachable = state;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayname;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public MessageChannel[] getChannels() {
		return channels;
	}

	public int getStatus() {
		return wasReachable + 1; // +1 to keep original semantics
	}
	
	public void resetStatus() {
		wasReachable = -1;
	}

	private EmbedBuilder getEmbedBuilder(String state, String symbol, int color) {
		return new EmbedBuilder().setDescription("**Serverstate changed**\r\n" + "\r\n" + symbol + " **" + name + "** "
				+ state + "\r\n\r\n" + ":desktop: Address: `" + host + ":" + port + "`").setColor(color);
	}
	
	private int checkSystem() {
		// check system state
		System.out.println("["+format.format(new Date())+"] Checking " + host+":"+port + " ("+name+")");
		int reachable = 0;
		Socket socket = new Socket();
		do {
			try {
		       socket.connect(new InetSocketAddress(host, port), (reachable+1) * 5000);
		       socket.close();
		       break;
			}catch(IOException e) {
		    	reachable++;
			}
		}while(reachable < 2);
		return reachable;
	}
	
}
