/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwc.api

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.channel.ChannelType
import pl.jwizard.jwc.audio.manager.GuildMusicManager
import pl.jwizard.jwc.command.async.AsyncUpdatableHandler
import pl.jwizard.jwc.command.async.AsyncUpdatableHook
import pl.jwizard.jwc.command.context.GuildCommandContext
import pl.jwizard.jwc.core.jda.command.TFutureResponse
import pl.jwizard.jwc.core.property.BotListProperty
import pl.jwizard.jwc.exception.audio.TemporaryHaltedBotException
import pl.jwizard.jwc.exception.command.ForbiddenChannelException
import pl.jwizard.jwc.exception.command.InvokerIsNotSenderOrSuperuserException
import pl.jwizard.jwc.exception.user.UserOnVoiceChannelNotFoundException
import pl.jwizard.jwc.exception.user.UserOnVoiceChannelWithBotNotFoundException

/**
 * Base class for audio-related commands in the music command system.
 *
 * This class provides essential functionality for commands that interact with audio playback and requires a music
 * manager instance.
 *
 * @param commandEnvironment The environment context for executing the command.
 * @author Miłosz Gilga
 */
abstract class AudioCommandBase(commandEnvironment: CommandEnvironmentBean) : CommandBase(commandEnvironment) {

	/**
	 * Executes the audio command.
	 *
	 * This method performs various checks, including permissions and voice state, before delegating the execution to
	 * the concrete implementation of the audio command.
	 *
	 * @param context The context of the command, containing user interaction details.
	 * @param response The future response object used to send the result of the command execution.
	 */
	final override fun execute(context: GuildCommandContext, response: TFutureResponse) {
		val musicManager = commandEnvironment.musicManagers
			.getOrCreateMusicManager(context, response, commandEnvironment.audioClient)

		val musicTextChannel = context.musicTextChannelId?.let { context.guild.getTextChannelById(it) }
		val musicTextChannelId = musicTextChannel?.idLong

		// check invoking channel id
		if (musicTextChannelId != null && context.textChannel.idLong != musicTextChannelId) {
			throw ForbiddenChannelException(context, context.textChannel, musicTextChannel)
		}
		// check, if bot (self member) is not currently muted
		if (context.selfMember.voiceState?.isMuted == true) {
			throw TemporaryHaltedBotException(context)
		}
		// check, if content sender is sender or superuser
		if (shouldBeContentSenderOrSuperuser) {
			val (isSender, isDj, isSuperUser) = checkPermissions(context, musicManager)
			if (!isSender && !isDj && !isSuperUser) {
				throw InvokerIsNotSenderOrSuperuserException(context)
			}
		}
		executeAudio(context, musicManager, response)
	}

	/**
	 * Checks the permissions of the user in the context of the command.
	 *
	 * @param context The context of the command, containing user interaction details.
	 * @param manager The guild music manager responsible for handling the audio queue and playback.
	 * @return A Triple indicating whether the user is the sender, a DJ, or a superuser.
	 */
	protected fun checkPermissions(
		context: GuildCommandContext,
		manager: GuildMusicManager,
	): Triple<Boolean, Boolean, Boolean> {
		val isSender = manager.cachedPlayer?.track?.audioSender?.authorId == context.author.idLong
		val isSuperUser = context.checkIfAuthorHasPermissions(*(superuserPermissions.toTypedArray()))
		val isDj = context.checkIfAuthorHasRoles(context.djRoleName)
		return Triple(isSender, isDj, isSuperUser)
	}

	/**
	 * Checks the user's voice state to ensure they are in a voice channel.
	 *
	 * @param context The context of the command, containing user interaction details.
	 * @return The voice state of the user in the guild.
	 * @throws UserOnVoiceChannelNotFoundException If the user is not in a voice channel.
	 * @throws ForbiddenChannelException If the user is in the AFK channel.
	 */
	protected fun checkUserVoiceState(context: GuildCommandContext): GuildVoiceState {
		val userVoiceState = context.author.voiceState
		if (userVoiceState?.channel?.type != ChannelType.VOICE) {
			throw UserOnVoiceChannelNotFoundException(context)
		}
		val afkChannel = context.guild.afkChannel
		if (userVoiceState.channel == afkChannel) {
			throw ForbiddenChannelException(context, afkChannel, context.textChannel)
		}
		return userVoiceState
	}

	/**
	 * Checks if the user is in the same voice channel as the bot.
	 *
	 * @param voiceState The voice state of the user.
	 * @param context The context of the command, containing user interaction details.
	 * @throws UserOnVoiceChannelWithBotNotFoundException If the user is not in the same channel as the bot.
	 */
	protected fun userIsWithBotOnAudioChannel(voiceState: GuildVoiceState, context: GuildCommandContext) {
		val botVoiceState = context.selfMember.voiceState
		val superuserPermissions = environment.getListProperty<String>(BotListProperty.JDA_SUPERUSER_PERMISSIONS)
		val isRegularUser = superuserPermissions.none { context.author.hasPermission(Permission.valueOf(it)) }

		// check, if regular user is on the same channel with bot (omit for admin and server moderator)
		if (shouldOnSameChannelWithBot && botVoiceState?.channel?.id != voiceState.channel?.id && isRegularUser) {
			throw UserOnVoiceChannelWithBotNotFoundException(context, voiceState.channel, botVoiceState?.channel)
		}
	}

	/**
	 * Creates an asynchronous handler for the audio player updates.
	 *
	 * This method sets up an async handler using a hook to manage the update flow, which can then be processed for
	 * either success or failure.
	 *
	 * @param P Additional payload object.
	 * @param context The context of the command, containing user interaction details.
	 * @param response The future response object used to send the result of the command execution.
	 * @param hook The hook to handle success and failure of the asynchronous operation.
	 * @return An instance of AsyncUpdatableHandler for processing the async updates.
	 */
	protected fun <P> createAsyncUpdatablePlayerHandler(
		context: GuildCommandContext,
		response: TFutureResponse,
		hook: AsyncUpdatableHook<GuildCommandContext, P>,
	) = AsyncUpdatableHandler(context, response, this::class, hook, exceptionTrackerHandler)

	/**
	 * Flag indication whether the command should be available for all at first action (ex. add track to empty queue).
	 */
	protected open val shouldEnabledOnFirstAction = false

	/**
	 * Flag indicating whether the command requires the user to be in the same channel as the bot.
	 */
	protected open val shouldOnSameChannelWithBot = false

	/**
	 * Flag indicating whether the command requires the content sender to be the sender or a superuser.
	 */
	protected open val shouldBeContentSenderOrSuperuser = false

	/**
	 * Executes the audio command.
	 *
	 * This method must be implemented by subclasses to define the specific functionality of the audio command.
	 *
	 * @param context The context of the command, containing user interaction details.
	 * @param manager The guild music manager responsible for handling the audio queue and playback.
	 * @param response The future response object used to send the result of the command execution.
	 */
	protected abstract fun executeAudio(
		context: GuildCommandContext,
		manager: GuildMusicManager,
		response: TFutureResponse,
	)
}
