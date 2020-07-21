package de.taucher.monitoring;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

public class MonitoringMessage {
	
	private static final String OK_SYMBOL = "<a:vrox_yes:716334571708481578>";
	private static final String BAD_SYMBOL = OK_SYMBOL; //Symbol coming soon
	private static final String FAIL_SYMBOL = "<a:vrox_no:716334571515412482>";
	
	private String category;
	private Message message;
	private MonitoredInstance[] instances;
	private EmbedBuilder builder;
	
	MonitoringMessage(String category, Message message, MonitoredInstance... instances) {
		this.category = category;
		this.message = message;
		this.instances = instances;
		this.builder = new EmbedBuilder();
	}
	
	void update() {
		StringBuilder desc = new StringBuilder(category != null ? ":satellite: **" + category + "**\n\n" : ":satellite: **Statusübersicht**\n\n");
		int color = 0x15d11c;
		
		for(MonitoredInstance mi : instances) {
			String emoji = mi.getStatus() == 1 ? OK_SYMBOL : mi.getStatus() == 2 ? BAD_SYMBOL : FAIL_SYMBOL;
			desc.append(emoji + " " + mi.getDisplayName() + "\n\n");
		}
//		if(desc.toString().contains(BAD_SYMBOL)) {
//			color = 0xec6602;
//		}
		if(desc.toString().contains(FAIL_SYMBOL)) {
			color = 0xd11a15;
		}
		
		message.editMessage(builder.setDescription(desc.toString()).setColor(color).build()).queue();
	}
	
	public Message getMessage() {
		return message;
	}
	
	public MonitoredInstance[] getInstances() {
		return instances;
	}
}
