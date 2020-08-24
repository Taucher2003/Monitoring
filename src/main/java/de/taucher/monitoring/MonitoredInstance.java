package de.taucher.monitoring;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class MonitoredInstance {

	private static final String OK_SYMBOL = "<a:vrox_yes:716334571708481578>";
	private static final String BAD_SYMBOL = OK_SYMBOL; //Symbol coming soon
	private static final String FAIL_SYMBOL = "<a:vrox_no:716334571515412482>";
	private static final String[] StateNames = new String[] { "Is reachable", "Bad connection", "Not reachable" };

	private String name;
	private String displayname;
	private String host;
	private int port;
	private MessageChannel[] channels;
	private int wasReachable = -1;

	private EmbedBuilder embeds[];

	private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.GERMANY);
	
	private Date lastCheck;

	MonitoredInstance(String name, String displayname, String host, int port, MessageChannel... notify) {
		this.name = name;
		this.displayname = displayname;
		this.host = host;
		this.port = port;
		this.channels = notify;
		this.lastCheck = new Date();
		
		MysqlManager mysql = Monitoring.getInstance().getMysql();
//		if(mysql.getFullResult("SELECT * FROM Stats WHERE system = ? AND month = ?", name, new SimpleDateFormat("yyyy/MM").format(new Date())) == null) {
			HashMap<String, String> result = mysql.getFullResult("SELECT * FROM CurrentStats WHERE system = ?", name);
			if(result == null) {
				mysql.update("INSERT INTO CurrentStats VALUES(?, ?, ?)", name, 0+"", 0+"");
			}
//		}

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

	@SuppressWarnings("deprecation")
	public void check() {

		long start = System.currentTimeMillis();
		int state = checkSystem();
		long end = System.currentTimeMillis();
		Date now = new Date();
		// emit message
		System.out.println("["+format.format(now)+"] Checked ("+(end-start)+"ms) " + host+":"+port + " ("+name+") -> "+ StateNames[state]);
		
		MysqlManager mysql = Monitoring.getInstance().getMysql();
		if(lastCheck.getDate() != now.getDate()) {
			HashMap<String, String> stats = mysql.getFullResult("SELECT * FROM CurrentStats WHERE system = ?", name);
			long online, offline;
			online = Long.valueOf(stats.get("online") != null ? stats.get("online") : "0");
			offline = Long.valueOf(stats.get("offline") != null ? stats.get("offline") : "0");
			HashMap<String, String> already = mysql.getFullResult("SELECT SUM(online), SUM(offline) FROM StatsDaily WHERE system = ? AND day LIKE ?", name, 
					new SimpleDateFormat("yyyy/MM/").format(lastCheck)+"%");
			if(already != null) {
				online -= Long.valueOf(already.get("SUM(online)") != null ? already.get("SUM(online)") : "0");
				offline -= Long.valueOf(already.get("SUM(offline)") != null ? already.get("SUM(offline)") : "0");
			}
			mysql.update("INSERT INTO StatsDaily VALUES(?, ?, ?, ?)", name, new SimpleDateFormat("yyyy/MM/dd").format(lastCheck), online+"", offline+"");
		}
		if(lastCheck.getMonth() != now.getMonth()) {
			HashMap<String, String> stats = mysql.getFullResult("SELECT * FROM CurrentStats WHERE system = ?", name);
			mysql.update("INSERT INTO Stats VALUES(?, ?, ?, ?)", name, new SimpleDateFormat("yyyy/MM").format(lastCheck), stats.get("online"), stats.get("offline"));
			mysql.update("UPDATE CurrentStats SET online = 0, offline = 0 WHERE system = ?", name);
		}
		if(state == 2) {
			mysql.update("UPDATE CurrentStats SET offline = offline + "+(now.getTime()/1000-lastCheck.getTime()/1000)+" WHERE system = ?", name);
		}else if(state != -1) {
			mysql.update("UPDATE CurrentStats SET online = online + "+(now.getTime()/1000-lastCheck.getTime()/1000)+" WHERE system = ?", name);
		}
		
		if(wasReachable != state) {
			Monitoring.getInstance().getMessages(this).forEach(message -> {
				Monitoring.getInstance().updateMessage(message);
			});
			if(wasReachable != -1) {
				boolean allDown = true;
				for(MonitoredInstance mi : Monitoring.getInstance().getMonitoredInstances()) {
					if(mi.getStatus() != 3 && mi.getStatus() != 0) {
						allDown = false;
					}
				}
				MessageEmbed msg = embeds[state].setFooter("Timestamp • "+format.format(now)).build();
				for(MessageChannel messagechannel : channels) {
					if(allDown && state == 0) {
						continue;
					}
					if(messagechannel instanceof PrivateChannel) {
						if(!Monitoring.getInstance().getDiscord().isNotifyEnabled(((PrivateChannel) messagechannel).getUser(), this)) {
							continue;
						}
					}
					try {
						messagechannel.sendMessage(msg).queue();
					}catch(Exception e) {}
				}
			}
		}
		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				wasReachable = state;
			}
		}, 1000);
		lastCheck = new Date();
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
	
	public double getUptime() {
		HashMap<String, String> result = Monitoring.getInstance().getMysql().getFullResult("SELECT * FROM CurrentStats WHERE system = ?", name);
		double up = Long.valueOf(result.get("online"));
		double down = Long.valueOf(result.get("offline"));
		HashMap<String, String> historic = Monitoring.getInstance().getMysql().getFullResult("SELECT SUM(online), SUM(offline) FROM Stats WHERE system = ?", name);
		if(historic.get("SUM(online)") != null) {
			up += Long.valueOf(historic.get("SUM(online)"));
		}
		if(historic.get("SUM(offline)") != null) {
			down += Long.valueOf(historic.get("SUM(offline)"));
		}
		double percent = up / (down <= 0 ? up : down+up);
		percent *= 100;
		return percent;
	}
	
	public double getDowntime() {
		return 100D - getUptime();
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
	
	List<User> getVisibleTo() {
		List<User> list = new ArrayList<>();
		for(MessageChannel mc : channels) {
			if(mc instanceof PrivateChannel) {
				list.add(((PrivateChannel) mc).getUser());
			}else if(mc instanceof GuildChannel) {
				GuildChannel gc = (GuildChannel) mc;
				List<Member> members = gc.getMembers();
				for(Member m : members) {
					list.add(m.getUser());
				}
			}
		}
		return list;
	}
	
}
