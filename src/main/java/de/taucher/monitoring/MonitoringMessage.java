package de.taucher.monitoring;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

public class MonitoringMessage {
	
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
			String emoji = mi.getStatus() == 1 ? "<a:vrox_yes:716334571708481578>" : mi.getStatus() == 2 ? "<a:vrox_yes:716334571708481578>" : "<a:vrox_no:716334571515412482>";
			desc.append(emoji + " " + mi.getDisplayName() + "\n\n");
		}
//		if(color == 0x15d11c && desc.toString().contains("")) {
//			color = 0xec6602;
//		}
		if(desc.toString().contains("<a:vrox_no:716334571515412482>")) {
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
