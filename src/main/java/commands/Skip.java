package commands;

import music.PlayerManager;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Skip extends Command {

    public Skip() {
        super("skip");
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run(MessageReceivedEvent event) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        String actor = event.getMember().getEffectiveName();
        if (connectedChannel == null) {
            event.getChannel().sendMessage("You are not connected to a voice channel. `" + actor + "`").queue();
            return;
        }

        VoiceChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (botChannel != connectedChannel && !event.getMember().isOwner()) {
            event.getChannel().sendMessage("You have to be in the same voice channel as me to the skip song. `"
                    + actor + "`")
                    .queue();
            return;
        }

        PlayerManager manager = PlayerManager.getInstance();
        double winPercentage = 0.45;
        manager.getGuildMusicManager(event.getGuild()).scheduler.voteHolder
                .modifyAttributes(connectedChannel.getMembers().size() - 1, winPercentage);
        if (event.getMember().isOwner()) {
            manager.getGuildMusicManager(event.getGuild()).scheduler.nextTrack();
            event.getChannel().sendMessage("Owner of the server skipped the song.").queue();
            return;
        }

        boolean voted = manager.getGuildMusicManager(event.getGuild()).scheduler.voteHolder.addVoter(event.getMember());
        int votes = manager.getGuildMusicManager(event.getGuild()).scheduler.voteHolder.getVotes();
        int voteCap = manager.getGuildMusicManager(event.getGuild()).scheduler.voteHolder.getVoteCap();
        if (!voted) {
            event.getChannel().sendMessage("You have already voted to skip the song. `" +
                    actor + "`").queue();
        } else {
            event.getChannel().sendMessage("`" + actor + "` has voted to skip. (" +
                    votes + "/" + this.calculateThreshold(voteCap, winPercentage)+ ")").queue();
        }

        boolean pass = manager.getGuildMusicManager(event.getGuild()).scheduler.voteHolder.runVoteCheck();
        if (pass) {
            manager.getGuildMusicManager(event.getGuild()).scheduler.nextTrack();
            event.getChannel().sendMessage("Enough votes. I am skipping the song.").queue();
        }
    }

    private int calculateThreshold(int voteCap, double winPercentage) {
        double threshold = (double) voteCap * winPercentage;
        return (int) Math.ceil(threshold);
    }
}
