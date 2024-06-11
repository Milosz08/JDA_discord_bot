/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.core.command

import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.Component
import pl.jwizard.core.audio.ExtendedAudioTrackInfo
import pl.jwizard.core.command.arg.CommandArgument
import pl.jwizard.core.command.arg.CommandArgumentData
import pl.jwizard.core.db.GuildCommandPropertiesDto
import pl.jwizard.core.util.BotUtils

data class CompoundCommandEvent(
	val guild: Guild?,
	val guildId: String,
	val guildName: String,
	val guildDbId: Long,
	val legacyPrefix: String,
	val isSlashEnabled: Boolean,
	val lang: String,
	val djRoleName: String,
	val authorTag: String,
	val authorAvatarUrl: String,
	val dataSender: Member?,
	val author: User,
	val member: Member,
	var delay: DefferedEmbed,
	val textChannel: TextChannel,
	val commandArgs: MutableMap<CommandArgument, CommandArgumentData>,
	val systemTextChannel: TextChannel,
	var interactiveMessage: InteractiveMessage,
	val slashCommandEvent: SlashCommandEvent?,
	var appendAfterEmbeds: (() -> Unit)?,
	var invokedBySender: Boolean,
) {
	val botMember = guild?.selfMember

	constructor(event: GuildMessageReceivedEvent, props: GuildCommandPropertiesDto) : this(
		guild = event.guild,
		guildId = event.guild.id,
		guildName = event.guild.name,
		guildDbId = props.id,
		legacyPrefix = props.prefix,
		lang = props.lang,
		isSlashEnabled = props.slashEnabled,
		djRoleName = props.djRoleName,
		authorTag = event.author.asTag,
		authorAvatarUrl = event.author.avatarUrl ?: event.author.defaultAvatarUrl,
		dataSender = event.guild.getMember(event.author),
		author = event.author,
		member = event.member!!,
		delay = DefferedEmbed(),
		textChannel = event.channel,
		commandArgs = mutableMapOf(),
		systemTextChannel = BotUtils.getSystemTextChannel(event.guild),
		interactiveMessage = InteractiveMessage(),
		slashCommandEvent = null,
		appendAfterEmbeds = null,
		invokedBySender = false,
	)

	constructor(event: SlashCommandEvent, props: GuildCommandPropertiesDto) : this(
		guild = event.guild,
		guildId = event.guild?.id ?: "",
		guildName = event.guild?.name ?: "unknow",
		guildDbId = props.id,
		legacyPrefix = props.prefix,
		lang = props.lang,
		isSlashEnabled = props.slashEnabled,
		djRoleName = props.djRoleName,
		authorTag = event.member?.user?.asTag ?: "user",
		authorAvatarUrl = event.member?.user?.avatarUrl ?: event.member?.user?.defaultAvatarUrl ?: "",
		dataSender = event.guild?.getMember(event.member?.user!!),
		author = event.member?.user!!,
		member = event.member!!,
		delay = DefferedEmbed(),
		textChannel = event.textChannel,
		commandArgs = mutableMapOf(),
		systemTextChannel = BotUtils.getSystemTextChannel(event.guild!!),
		interactiveMessage = InteractiveMessage(),
		slashCommandEvent = event,
		appendAfterEmbeds = null,
		invokedBySender = false,
	)

	fun addWebhookActionComponents(vararg components: Component) {
		components.forEach { interactiveMessage.actionComponents.add(it) }
	}

	fun appendEmbedMessage(messageEmbed: MessageEmbed) {
		val (embedMessages) = interactiveMessage
		if (embedMessages.size < 10) {
			embedMessages.add(messageEmbed)
		}
	}

	fun appendEmbedMessage(messageEmbed: MessageEmbed, appendAfterEmbeds: () -> Unit) {
		appendEmbedMessage(messageEmbed)
		this.appendAfterEmbeds = appendAfterEmbeds
	}

	fun instantlySendEmbedMessage(
		messageEmbed: MessageEmbed,
		actionComponents: List<Component> = emptyList(),
		delay: DefferedEmbed = DefferedEmbed(),
		legacyTransport: Boolean = false
	) {
		val (duration, unit) = delay
		val deferredMessage = if (slashCommandEvent?.hook?.isExpired == false && !legacyTransport) {
			val message = slashCommandEvent.hook.sendMessageEmbeds(messageEmbed)
			if (actionComponents.isNotEmpty()) {
				message.addActionRow(actionComponents)
			}
			message
		} else {
			textChannel.sendMessageEmbeds(messageEmbed)
		}
		deferredMessage.queueAfter(duration, unit)
	}

	fun checkIfInvokerIsNotSenderOrAdmin(track: ExtendedAudioTrackInfo): Boolean {
		val trackSender = (track.audioTrack.userData as Member).user
		val validatedUserDetails = BotUtils.validateUserDetails(this)
		return if (dataSender != null) {
			!(trackSender.asTag == author.asTag || validatedUserDetails.concatLogicOr())
		} else {
			true
		}
	}
}
