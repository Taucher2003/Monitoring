package de.taucher.monitoring;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

public class MonitoredInstance {

	private String name;
	private String host;
	private int port;
	private MessageChannel[] channels;
	private int wasReachable = 0;
	
	private EmbedBuilder reach;
	private EmbedBuilder unreach;
	
	private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.GERMANY);
	
	MonitoredInstance(String name, String host, int port, MessageChannel... notify) {
		this.name = name;
		this.host = host;
		this.port = port;
		this.channels = notify;
		
		this.reach = new EmbedBuilder().setDescription("**Serverstate changed**\r\n" + 
				"\r\n" + 
				"<a:vrox_yes:716334571708481578> **"+name+"** is now reachable\r\n" + 
				":desktop: Address: `"+host+":"+port+"`").setColor(0x15d11c);
		this.unreach = new EmbedBuilder().setDescription("**Serverstate changed**\r\n" + 
				"\r\n" + 
				"<a:vrox_no:716334571515412482> **"+name+"** is no longer reachable\r\n" + 
				":desktop: Address: `"+host+":"+port+"`").setColor(0xd11a15);
		scheduleCheck();
	}
	
	private void scheduleCheck() {
		new Timer("Monitoring "+host+":"+port, true).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				check();
			}
		}, 1000, 60*1000);
	}
	
	public void check() {
		try {
			System.out.print("Checking " + host+":"+port + " ("+name+") -> ");
			boolean reachable;
			Socket socket = new Socket();
			try {
		        socket.connect(new InetSocketAddress(host, port), 5000);
		        reachable = true;
		    }catch (IOException e) {
		        reachable = false;
		    }finally {
		    	socket.close();
		    }
			System.out.println(reachable ? "Is reachable" : "Not reachable");
			if(wasReachable != 0) {
				if(reachable && wasReachable == 2) {
					Arrays.asList(channels).forEach(messagechannel -> {
						messagechannel.sendMessage(reach.setFooter("Timestamp • "+format.format(new Date())).build()).queue();
					});
				}
				if(!reachable && wasReachable == 1) {
					Arrays.asList(channels).forEach(messagechannel -> {
						messagechannel.sendMessage(unreach.setFooter("Timestamp • "+format.format(new Date())).build()).queue();
					});
				}
			}
			wasReachable = reachable ? 1 : 2;
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getName() {
		return name;
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
}
