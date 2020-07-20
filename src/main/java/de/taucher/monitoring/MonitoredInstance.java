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
	private String displayname;
	private String host;
	private int port;
	private MessageChannel[] channels;
	private int wasReachable = 0;
	
	private EmbedBuilder reach;
	private EmbedBuilder bad;
	private EmbedBuilder unreach;
	
	private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.GERMANY);
	
	MonitoredInstance(String name, String displayname, String host, int port, MessageChannel... notify) {
		this.name = name;
		this.displayname = displayname;
		this.host = host;
		this.port = port;
		this.channels = notify;
		
		this.reach = new EmbedBuilder().setDescription("**Serverstate changed**\r\n" + 
				"\r\n" + 
				"<a:vrox_yes:716334571708481578> **"+name+"** is now reachable\r\n\r\n" + 
				":desktop: Address: `"+host+":"+port+"`").setColor(0x15d11c);
		this.bad = new EmbedBuilder().setDescription("**Serverstate changed**\r\n" + 
				"\r\n" + 
				"<a:vrox_yes:716334571708481578> **"+name+"** has a bad connection\r\n\r\n" + 
				":desktop: Address: `"+host+":"+port+"`").setColor(0xec6602);
		this.unreach = new EmbedBuilder().setDescription("**Serverstate changed**\r\n" + 
				"\r\n" + 
				"<a:vrox_no:716334571515412482> **"+name+"** is no longer reachable\r\n\r\n" + 
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
			System.out.println("["+format.format(new Date())+"] Checking " + host+":"+port + " ("+name+")");
			int reachable;
			Socket socket = new Socket();
			try {
		        socket.connect(new InetSocketAddress(host, port), 5000);
		        reachable = 1;
		    }catch (IOException e) {
		    	try {
			    	socket.connect(new InetSocketAddress(host, port), 10000);
			        reachable = 2;
		    	}catch(IOException ex) {
		    		reachable = 3;
		    	}
		    }finally {
		    	socket.close();
		    }
			System.out.println("["+format.format(new Date())+"] Checked " + host+":"+port + " ("+name+") -> "+(reachable == 1 ? "Is reachable" : reachable == 2 ? "Bad performance" : "Not reachable"));
			if(wasReachable != 0) {
				if(reachable == 1 && wasReachable != reachable) {
					Monitoring.getInstance().getMessages(this).forEach(message -> {
						Monitoring.getInstance().updateMessage(message);
					});
					Arrays.asList(channels).forEach(messagechannel -> {
						messagechannel.sendMessage(reach.setFooter("Timestamp • "+format.format(new Date())).build()).queue();
					});
				}
				if(reachable == 2 && wasReachable != reachable) {
					Monitoring.getInstance().getMessages(this).forEach(message -> {
						Monitoring.getInstance().updateMessage(message);
					});
					Arrays.asList(channels).forEach(messagechannel -> {
						messagechannel.sendMessage(reach.setFooter("Timestamp • "+format.format(new Date())).build()).queue();
					});
				}
				if(reachable == 3 && wasReachable != reachable) {
					Monitoring.getInstance().getMessages(this).forEach(message -> {
						Monitoring.getInstance().updateMessage(message);
					});
					Arrays.asList(channels).forEach(messagechannel -> {
						messagechannel.sendMessage(unreach.setFooter("Timestamp • "+format.format(new Date())).build()).queue();
					});
				}
			}else {
				Monitoring.getInstance().getMessages(this).forEach(message -> {
					Monitoring.getInstance().updateMessage(message);
				});
			}
			wasReachable = reachable;
		}catch(Exception e) {
			e.printStackTrace();
		}
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
		return wasReachable;
	}
}
