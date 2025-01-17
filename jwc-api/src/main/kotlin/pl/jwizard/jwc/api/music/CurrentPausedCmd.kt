/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwc.api.music

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import pl.jwizard.jwc.api.CommandEnvironmentBean
import pl.jwizard.jwc.api.MusicCommandBase
import pl.jwizard.jwc.audio.manager.GuildMusicManager
import pl.jwizard.jwc.command.context.GuildCommandContext
import pl.jwizard.jwc.command.interaction.component.RefreshableContent
import pl.jwizard.jwc.command.reflect.JdaCommand
import pl.jwizard.jwc.core.i18n.source.I18nAudioSource
import pl.jwizard.jwc.core.jda.command.CommandResponse
import pl.jwizard.jwc.core.jda.command.TFutureResponse
import pl.jwizard.jwc.exception.UnexpectedException
import pl.jwizard.jwl.command.Command

/**
 * Command responsible for displaying the currently paused track information.
 *
 * This command is part of the music control system and is used when the bot's playback is paused. It generates a
 * detailed message displaying the paused track information, which can be refreshed by the user through interaction.
 *
 * @param commandEnvironment The environment context for executing the command.
 * @author Miłosz Gilga
 */
@JdaCommand(Command.PAUSED)
class CurrentPausedCmd(
	commandEnvironment: CommandEnvironmentBean,
) : MusicCommandBase(commandEnvironment), RefreshableContent<Pair<GuildCommandContext, GuildMusicManager>> {

	override val shouldPaused = true

	/**
	 * Executes the command logic for displaying the paused track.
	 *
	 * This method retrieves the currently paused track from the music manager and generates a detailed message
	 * with track information. The message is sent to the user and contains a refreshable component to allow
	 * updating the message with the latest track information.
	 *
	 * @param context The context of the command, containing user interaction details.
	 * @param manager The guild music manager responsible for handling the audio queue and playback.
	 * @param response The future response object used to send the result of the command execution.
	 * @throws UnexpectedException If the paused track is not found.
	 */
	override fun executeMusic(context: GuildCommandContext, manager: GuildMusicManager, response: TFutureResponse) {
		val pausedTrack = manager.cachedPlayer?.track ?: throw UnexpectedException(context, "Paused track is NULL.")

		val message = createDetailedTrackMessage(
			context,
			manager,
			i18nTitle = I18nAudioSource.CURRENT_PAUSED_TRACK,
			i18nPosition = I18nAudioSource.CURRENT_PAUSED_TIMESTAMP,
			track = pausedTrack,
		)
		val refreshableComponent = createRefreshable(this, Pair(context, manager))
		refreshableComponent.initEvent()

		val commandResponse = CommandResponse.Builder()
			.addEmbedMessages(message)
			.addActionRows(refreshableComponent.createRefreshButtonRow(context))
			.build()

		response.complete(commandResponse)
	}

	/**
	 * Handles the refresh action when the user interacts with the refresh button. This method updates the paused track
	 * information in the message when the user clicks the refresh button.
	 *
	 * @param event The button interaction event triggered by the user.
	 * @param response The list of message embeds to be updated with refreshed track information.
	 * @param payload The pair containing the command context and guild music manager used for retrieving the updated
	 *        track information.
	 */
	override fun onRefresh(
		event: ButtonInteractionEvent,
		response: MutableList<MessageEmbed>,
		payload: Pair<GuildCommandContext, GuildMusicManager>,
	) {
		val (context, manager) = payload
		val pausedTrack = manager.cachedPlayer?.track ?: return
		val message = createDetailedTrackMessage(
			context,
			manager,
			i18nTitle = I18nAudioSource.CURRENT_PAUSED_TRACK,
			i18nPosition = I18nAudioSource.CURRENT_PAUSED_TIMESTAMP,
			pausedTrack,
		)
		response.add(message)
	}
}
