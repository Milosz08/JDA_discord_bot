/*
 * Copyright (c) 2022 by MILOSZ GILGA <https://miloszgilga.pl>
 *
 * File name: AudioPlayCommandExecutor.java
 * Last modified: 15/07/2022, 02:58
 * Project name: franek-bot
 *
 * Licensed under the MIT license; you may not use this file except in compliance with the License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * THE ABOVE COPYRIGHT NOTICE AND THIS PERMISSION NOTICE SHALL BE INCLUDED IN ALL
 * COPIES OR SUBSTANTIAL PORTIONS OF THE SOFTWARE.
 */

package pl.miloszgilga.franekbotapp.executors.audioplayer;

import jdk.jfr.Description;
import net.dv8tion.jda.api.entities.Member;
import com.jagrosh.jdautilities.command.Command;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.net.URISyntaxException;

import pl.miloszgilga.franekbotapp.logger.LoggerFactory;
import pl.miloszgilga.franekbotapp.audioplayer.PlayerManager;
import pl.miloszgilga.franekbotapp.exceptions.MusicBotIsInUseException;
import pl.miloszgilga.franekbotapp.exceptions.IllegalCommandArgumentsException;
import pl.miloszgilga.franekbotapp.exceptions.UserOnVoiceChannelNotFoundException;

import static pl.miloszgilga.franekbotapp.BotCommand.MUSIC_PLAY;
import static pl.miloszgilga.franekbotapp.configuration.ConfigurationLoader.config;


public class PlayTrackCommandExecutor extends Command {

    private final LoggerFactory logger = new LoggerFactory(PlayTrackCommandExecutor.class);
    private final PlayerManager playerManager = PlayerManager.getSingletonInstance();

    public PlayTrackCommandExecutor() {
        name = MUSIC_PLAY.getCommandName();
        help = MUSIC_PLAY.getCommandDescription();
    }

    @Override
    @Description("command: <[prefix]play [music link or description]>")
    protected void execute(CommandEvent event) {
        try {
            if (event.getArgs().split(" ").length < 1) {
                throw new IllegalCommandArgumentsException(event, MUSIC_PLAY, String.format(
                        "`%s [link lub nazwa piosenki]`", config.getPrefix() + MUSIC_PLAY.getCommandName()));
            }

            checkIfBotIsCurrentyUsedOnAnotherChannel(event);
            if (!Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).inAudioChannel()) {
                throw new UserOnVoiceChannelNotFoundException(event, "Aby możliwe było odtworzenie piosenki, " +
                        "musisz znajdować się na kanale głosowym.");
            }

            final AudioManager audioManager = event.getGuild().getAudioManager();
            final VoiceChannel memberChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();
            audioManager.openAudioConnection(memberChannel);

            String withoutPrefix = event.getArgs();
            boolean ifValidUri = isUrl(withoutPrefix);
            if (!ifValidUri && event.getArgs().split(" ").length > 1) {
                withoutPrefix = "ytsearch: " + withoutPrefix + " audio";
            } else {
                withoutPrefix = withoutPrefix.replaceAll(" ", "");
            }
            playerManager.loadAndPlay(event, withoutPrefix, ifValidUri);
        } catch (UserOnVoiceChannelNotFoundException | MusicBotIsInUseException | IllegalCommandArgumentsException ex) {
            logger.warn(ex.getMessage(), event.getGuild());
        }
    }

    private void checkIfBotIsCurrentyUsedOnAnotherChannel(CommandEvent event) {
        final String guildId = event.getGuild().getId();
        final Optional<VoiceChannel> findBotOnVoiceChannel = Objects.requireNonNull(event.getJDA().getGuildById(guildId))
                .getVoiceChannels().stream().filter(channel -> {
                    final Member botMember = event.getGuild().getMember(event.getJDA().getSelfUser());
                    final Member senderUserMember = event.getGuild().getMember(event.getAuthor());
                    return channel.getMembers().contains(botMember) && !channel.getMembers().contains(senderUserMember);
                })
                .findFirst();
        if (findBotOnVoiceChannel.isPresent()) {
            throw new MusicBotIsInUseException(event);
        }
    }

    private boolean isUrl(String url) {
        try {
            new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}