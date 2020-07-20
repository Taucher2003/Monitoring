package de.taucher.monitoring;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageReceivedListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		if(event.getAuthor().getIdLong() == 444889694002741249L) {
			if(event.getMessage().getContentRaw().replace("<@!", "<@").startsWith(Monitoring.getInstance().getDiscord().getJDA().getSelfUser().getAsMention() + " sendembed")) {
				event.getChannel().sendMessage(new EmbedBuilder().setDescription("sent message").build()).queue();
			}
		}
	}
}
