package de.taucher.monitoring;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class Monitoring {
	
	private static Monitoring instance;
	private Constants constants;
	private Discord discord;
	private MysqlManager mysql;
	private List<MonitoredInstance> monitors;
	private List<MonitoringMessage> messages;
	private List<MonitoringMessage> cooldown;
	
	Monitoring() {
		instance = this;
		monitors = new LinkedList<>();
		messages = new LinkedList<>();
		cooldown = new LinkedList<>();
		constants = new Constants();
		discord = new Discord(constants.botToken);
		mysql = new MysqlManager(constants.mysqlHost, constants.mysqlPort, constants.mysqlUser, constants.mysqlPass, constants.mysqlDatabase).connect();
		mysql.update("CREATE TABLE IF NOT EXISTS CurrentStats(system varchar(255), online bigint, offline bigint, PRIMARY KEY(system))");
		mysql.update("CREATE TABLE IF NOT EXISTS Stats(system varchar(255), month varchar(10), online bigint, offline bigint, PRIMARY KEY(system, month))");
		mysql.update("CREATE TABLE IF NOT EXISTS StatsDaily(system varchar(255), day varchar(10), online bigint, offline bigint, PRIMARY KEY(system, day))");
		
		MessageChannel taucherPrivate = discord.getJDA().retrieveUserById(444889694002741249L).complete().openPrivateChannel().complete();
		MessageChannel lukPrivate = discord.getJDA().retrieveUserById(414445757408477205L).complete().openPrivateChannel().complete();
		
		MonitoredInstance root = createInstance("Vrox Rootserver", "Hostserver", "rp.vrox.eu", 22, taucherPrivate);
		MonitoredInstance bungee = createInstance("Vrox Bungeecord", "Bungeecord", "rp.vrox.eu", 25565, taucherPrivate);
		MonitoredInstance bau = createInstance("Vrox Bauserver", "Bauserver", "rp.vrox.eu", 25544, taucherPrivate);
		MonitoredInstance ts = createInstance("Vrox TeamSpeak", "TeamSpeak", "rp.vrox.eu", 10011, taucherPrivate);
//		MonitoredInstance mysql = createInstance("Vrox Datenbank", "Datenbank", "rp.vrox.eu", 3306, discord.getJDA().retrieveUserById(444889694002741249L).complete().openPrivateChannel().complete());

		createInstance("GameFM Systembot", "Systembot", "134.255.234.15", 2275, taucherPrivate, lukPrivate);
		createInstance("GameFM Uptimerobot", "Uptime Robot", "134.255.231.232", 2000, taucherPrivate, lukPrivate);
		
		createInstance("Lostrocket Cloud 01", "cloud-01", "lostrocket.info", 22, taucherPrivate);
		
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
	
	MysqlManager getMysql() {
		return mysql;
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
	
	public List<MonitoredInstance> getMonitoredInstances() {
		return monitors;
	}
	
	public List<MonitoredInstance> getMonitoredInstances(MessageChannel channel) {
		List<MonitoredInstance> list = new LinkedList<>();
		for(MonitoredInstance mi : monitors) {
			if(Arrays.asList(mi.getChannels()).contains(channel)) {
				list.add(mi);
			}
		}
		return list;
	}
	
	public List<MonitoredInstance> getMonitoredInstances(User user) {
		List<MonitoredInstance> list = new LinkedList<>();
		for(MonitoredInstance mi : monitors) {
			for(MessageChannel mc : mi.getChannels()) {
				if(mc instanceof PrivateChannel) {
					if(((PrivateChannel) mc).getUser().getIdLong() == user.getIdLong()) {
						list.add(mi);
						break;
					}
				}else if(mc instanceof GuildChannel) {
					GuildChannel gc = (GuildChannel) mc;
					Member member = gc.getGuild().retrieveMember(user).complete();
					if(member != null) {
						if(member.hasPermission(gc, Permission.MESSAGE_READ)) {
							list.add(mi);
							break;
						}
					}
				}
			}
		}
		return list;
	}
}
