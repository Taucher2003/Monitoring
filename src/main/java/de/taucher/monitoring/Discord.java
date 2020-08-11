package de.taucher.monitoring;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Discord extends ListenerAdapter {
	
	private static final String OK_SYMBOL = "<a:vrox_yes:716334571708481578>";
	private static final String FAIL_SYMBOL = "<a:vrox_no:716334571515412482>";

	private long id;
	private JDA jda;
	private Status status;
	
	Discord(String token) {
		JDABuilder builder = JDABuilder.createLight(token);
		builder.enableIntents(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES);
		builder.addEventListeners(this);
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
	
	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		if(event.getMessage().getContentRaw().replace("<@!", "<@").startsWith(Monitoring.getInstance().getDiscord().getJDA().getSelfUser().getAsMention() + " ")) {
			String[] args = event.getMessage().getContentRaw().replace("<@!", "<@").replace(Monitoring.getInstance().getDiscord().getJDA().getSelfUser().getAsMention()+" ", "").split(Pattern.quote(" "));
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("sendembed") && event.getAuthor().getIdLong() == 444889694002741249L) {
					event.getChannel().sendMessage(new EmbedBuilder().setDescription("sent message").build()).queue();
				}else if(args[0].equalsIgnoreCase("uptime")) {
					String sb = new String(":satellite: **Uptime**");
					int color = 0x5958ff;
					DecimalFormat format = new DecimalFormat("#.##");
					format.setRoundingMode(RoundingMode.HALF_UP);
					for(MonitoredInstance mi : Monitoring.getInstance().getMonitoredInstances(event.getAuthor())) {
						String uptime = mi.getUptime()+"";
						if(uptime.split(Pattern.quote("."))[1].length() > 2) {
							uptime = format.format(Double.valueOf(uptime));
						}
						String downtime = mi.getDowntime()+"";
						if(downtime.split(Pattern.quote("."))[1].length() > 2) {
							downtime = format.format(Double.valueOf(downtime));
						}
						if((sb + "\n\n**"+mi.getName()+"**\n"+OK_SYMBOL+" "+uptime+"%\n"+FAIL_SYMBOL+" "+downtime+"%").length() > MessageEmbed.TEXT_MAX_LENGTH) {
							event.getChannel().sendMessage(new EmbedBuilder().setDescription(sb).setColor(0x5958ff).build()).queue();
							sb = "";
						}
						sb += "\n\n**"+mi.getName()+"**\n"+OK_SYMBOL+" "+uptime+"%\n"+FAIL_SYMBOL+" "+downtime+"%";
					}
					if(sb.equals(":satellite: **Uptime**")) {
						sb += "\n\n:no_entry_sign: Es existieren keine überwachten Systeme, die dir angezeigt werden können!";
						color = 0x8b0000;
					}
					event.getChannel().sendMessage(new EmbedBuilder().setDescription(sb).setColor(0x5958ff).build()).queue();
				}else if(args[0].equalsIgnoreCase("uptime") && args[1].equalsIgnoreCase("all") && event.getAuthor().getIdLong() == 444889694002741249L) {
					String sb = new String(":satellite: **Uptime**");
					DecimalFormat format = new DecimalFormat("#.##");
					format.setRoundingMode(RoundingMode.HALF_UP);
					for(MonitoredInstance mi : Monitoring.getInstance().getMonitoredInstances()) {
						String uptime = mi.getUptime()+"";
						if(uptime.split(Pattern.quote("."))[1].length() > 2) {
							uptime = format.format(Double.valueOf(uptime));
						}
						String downtime = mi.getDowntime()+"";
						if(downtime.split(Pattern.quote("."))[1].length() > 2) {
							downtime = format.format(Double.valueOf(downtime));
						}
						if((sb + "\n\n**"+mi.getName()+"**\n"+OK_SYMBOL+" "+uptime+"%\n"+FAIL_SYMBOL+" "+downtime+"%").length() > MessageEmbed.TEXT_MAX_LENGTH) {
							event.getChannel().sendMessage(new EmbedBuilder().setDescription(sb).setColor(0x5958ff).build()).queue();
							sb = "";
						}
						sb += "\n\n**"+mi.getName()+"**\n"+OK_SYMBOL+" "+uptime+"%\n"+FAIL_SYMBOL+" "+downtime+"%";
					}
					event.getChannel().sendMessage(new EmbedBuilder().setDescription(sb).setColor(0x5958ff).build()).queue();
				}
			}
		}
	}
	
	public void onStatusChange(@Nonnull StatusChangeEvent event) {
		status = event.getNewStatus();
		if(status.equals(Status.CONNECTED)) {
			for(MonitoredInstance mi : Monitoring.getInstance().getMonitoredInstances()){
				mi.resetStatus();
			}
		}
	}
}