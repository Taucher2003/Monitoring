package de.taucher.monitoring;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.api.entities.MessageChannel;

public class MonitoredInstance {

	private String host;
	private int port;
	private MessageChannel channel;
	
	MonitoredInstance(String host, int port, MessageChannel notify) {
		this.host = host;
		this.port = port;
		this.channel = notify;
		scheduleCheck();
	}
	
	private void scheduleCheck() {
		new Timer("Monitoring "+host, true).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				check();
			}
		}, 1000, 60*1000);
	}
	
	public void check() {
		try {
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
			if(reachable) {
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
