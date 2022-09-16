import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.time.Instant;

public class IsClubRoomOpen implements EventListener {
    private static final long CLUB_ROOM_CHANNEL_ID = 1019242278100930702L;
    private static long TRACKING_MESSAGE_ID = Long.MIN_VALUE;

    private static JDA jda;

    public static void main(String[] args) throws InterruptedException {
        // Note: It is important to register your ReadyListener before building
        jda = JDABuilder.createDefault("token")
                .addEventListeners(new IsClubRoomOpen())
                .build();

        // optionally block until JDA is ready
        jda.awaitReady();

        setUpdateMessageTo("The club room is closed");
    }

    private static void setUpdateMessageTo(String string) {
        MessageChannelUnion clubRoomChannel = jda.getChannelById(MessageChannelUnion.class, CLUB_ROOM_CHANNEL_ID);
        Message message = clubRoomChannel.getHistory().getMessageById(clubRoomChannel.asTextChannel().getLatestMessageId());
        TRACKING_MESSAGE_ID = message.getIdLong();

        if (message.getMember().getIdLong() == jda.getSelfUser().getIdLong()) {
            message.editMessage(string);
            return;
        }

        clubRoomChannel.sendMessage(string);
    }

    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof MessageReactionAddEvent) {
            GenericMessageReactionEvent messageReactionEvent = (GenericMessageReactionEvent) event;
            if (messageReactionEvent.getMessageIdLong() == TRACKING_MESSAGE_ID &&
                    messageReactionEvent.getMember().getRoles().stream().filter(role -> role.getName().equals("Officer")).findFirst().orElse(null) != null) {
                setUpdateMessageTo("The club room is **open!** Last updated by " + ((MessageReactionAddEvent) event).getUser().getName() + " at <" + Instant.now().getEpochSecond() + ">");
            }
        }

        if (event instanceof MessageReactionRemoveEvent) {
            MessageReactionRemoveEvent messageReactionRemoveEvent = (MessageReactionRemoveEvent) event;
            if (messageReactionRemoveEvent.getMessageIdLong() == TRACKING_MESSAGE_ID &&
                    messageReactionRemoveEvent.getMember().getRoles().stream().filter(role -> role.getName().equals("Officer")).findFirst().orElse(null) != null) {
                setUpdateMessageTo("The club room is closed");
            }
        }
    }
}
