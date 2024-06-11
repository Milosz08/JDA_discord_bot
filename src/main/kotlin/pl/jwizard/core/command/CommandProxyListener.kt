/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.core.command

import java.util.*
import pl.jwizard.core.bot.BotConfiguration
import pl.jwizard.core.command.arg.CommandArgument
import pl.jwizard.core.command.arg.CommandArgumentData
import pl.jwizard.core.command.embed.CustomEmbedBuilder
import pl.jwizard.core.command.reflect.CommandDetailsDto
import pl.jwizard.core.command.reflect.CommandLoader
import pl.jwizard.core.exception.AbstractBotException
import pl.jwizard.core.exception.CommandException
import pl.jwizard.core.log.AbstractLoggingBean
import pl.jwizard.core.settings.GuildSettings
import pl.jwizard.core.util.Formatter
import org.springframework.stereotype.Component
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.requests.RestAction

@Component
class CommandProxyListener(
	private val commandLoader: CommandLoader,
	private val guildSettings: GuildSettings,
	private val botConfiguration: BotConfiguration,
) : AbstractLoggingBean(CommandProxyListener::class) {

	fun onRegularCommand(event: GuildMessageReceivedEvent) {
		if (event.author.isBot) {
			return // skipping bot messages
		}
		try {
			val guildProps = guildSettings.getGuildProperties(event.guild.id)

			val messageContentWithPrefix = event.message.contentRaw
			if (!messageContentWithPrefix.startsWith(guildProps.legacyPrefix)) {
				return // skipping non-command message
			}
			val cmdWithArguments = messageContentWithPrefix.substring(1)
			val endPosition = messageContentWithPrefix.indexOf(' ')

			val commandName = if (endPosition > -1) {
				cmdWithArguments.substring(0, cmdWithArguments.indexOf(' '))
			} else {
				cmdWithArguments
			}
			if (!BotCommand.checkIfCommandExist(commandName)) {
				return // skipping, non existing command
			}
			val compoundCommandEvent = CompoundCommandEvent(event)
			if (!guildProps.enabledCommands.contains(commandName)) {
				throw CommandException.CommandIsTurnedOffException(compoundCommandEvent, commandName)
			}
			val commandDetails = commandLoader.getCommandBaseLang(commandName, guildProps.locale)
				?: return // skipping, not existing command

			val commandOptions: Queue<String> = LinkedList(cmdWithArguments
				.substring(commandName.length)
				.trim()
				.split("\\|")
				.filter { it.isNotEmpty() })
			if (commandOptions.size < commandDetails.args.filter { it.req }.size) {
				throwSyntaxException(compoundCommandEvent, commandName, commandDetails)
			}
			for (arg in commandDetails.args) {
				val optionMapping = commandOptions.poll()
				val argKey = CommandArgument.getInstation(arg.id)
				if (argKey == null || (optionMapping == null && arg.req)) {
					throwSyntaxException(compoundCommandEvent, commandName, commandDetails)
				}
				compoundCommandEvent.commandArgs[argKey!!] = CommandArgumentData(optionMapping, arg.type)
			}
			var interactiveMessage = InteractiveMessage()
			try {
				val command = commandLoader.commandsProxyContainer[commandName]
				interactiveMessage = command?.performCommand(compoundCommandEvent) ?: return
			} catch (ex: NumberFormatException) {
				throwSyntaxException(compoundCommandEvent, commandName, commandDetails)
			}
			val (messageEmbeds) = interactiveMessage
			if (messageEmbeds.isNotEmpty()) {
				val defferedSender = event.channel.sendMessageEmbeds(messageEmbeds)
				sendEmbeds(defferedSender, compoundCommandEvent)
			}
		} catch (ex: AbstractBotException) {
			val embedMessage = CustomEmbedBuilder(botConfiguration, guildProps.lang).buildErrorMessage(ex)
			event.channel.sendMessageEmbeds(embedMessage).queue()
		}
	}

	fun onSlashCommand(event: SlashCommandEvent) {
		val commandName = event.commandPath
		val guildId = event.guild?.id ?: return
		try {
			val guildProps = guildSettings.getGuildProperties(guildId)

			val compoundCommandEvent = CompoundCommandEvent(event)
			if (!guildProps.enabledSlashCommands.contains(commandName)) {
				throw CommandException.CommandIsTurnedOffException(compoundCommandEvent, commandName)
			}
			val commandDetails = commandLoader.getCommandBaseLang(commandName, guildProps.locale) ?: return
			val commandOptions: Queue<OptionMapping> = LinkedList(event.options)
			if (commandOptions.size < commandDetails.args.filter { it.req }.size) {
				throwSyntaxException(compoundCommandEvent, commandName, commandDetails)
			}
			for (arg in commandDetails.args) {
				val optionMapping = commandOptions.poll()
				val argKey = CommandArgument.getInstation(arg.id)
				if (argKey == null || (optionMapping == null && arg.req)) {
					throwSyntaxException(compoundCommandEvent, commandName, commandDetails)
				}
				compoundCommandEvent.commandArgs[argKey!!] = CommandArgumentData(optionMapping.asString, arg.type)
			}
			var interactiveMessage = InteractiveMessage()
			try {
				val command = commandLoader.commandsProxyContainer[commandName] ?: return
				interactiveMessage = command.performCommand(compoundCommandEvent)
			} catch (ex: NumberFormatException) {
				throwSyntaxException(compoundCommandEvent, commandName, commandDetails)
			}
			event.deferReply().queue()

			val (messageEmbeds, actionComponents) = interactiveMessage
			if (compoundCommandEvent.interactiveMessage.messageEmbeds.isNotEmpty()) {
				val defferedSender = if (!event.hook.isExpired) {
					val message = event.hook.sendMessageEmbeds(messageEmbeds)
					if (actionComponents.isNotEmpty()) {
						message.addActionRow(actionComponents)
					}
					message
				} else {
					event.channel.sendMessageEmbeds(messageEmbeds)
				}
				sendEmbeds(defferedSender, compoundCommandEvent)
			}
		} catch (ex: AbstractBotException) {
			val embedMessage = CustomEmbedBuilder(botConfiguration, guildProps.lang).buildErrorMessage(ex)
			event.channel.sendMessageEmbeds(embedMessage).queue()
		}
	}

	private fun sendEmbeds(deferredMessages: RestAction<Message>, event: CompoundCommandEvent) {
		if (event.delay.isDefault()) { // without delay, default behaviour
			if (event.appendAfterEmbeds == null) { // without post-send action
				deferredMessages.queue()
			} else {  // append post-send embed
				deferredMessages.queue { event.appendAfterEmbeds?.let { it() } }
			}
		} else { // with delay
			val (duration, unit) = event.delay
			if (event.appendAfterEmbeds == null) { // without post-send action
				deferredMessages.queueAfter(duration, unit)
			} else { // append post-send embed
				val scheduledFuture = deferredMessages.queueAfter(duration, unit)
				if (scheduledFuture.isDone) {
					event.appendAfterEmbeds?.let { it() }
				}
			}
		}
	}

	private fun throwSyntaxException(
		event: CompoundCommandEvent,
		commandName: String,
		commandDetails: CommandDetailsDto,
	) {
		throw CommandException.MismatchCommandArgumentsException(
			event,
			commandName,
			syntax = Formatter.createCommandSyntax(
				commandName,
				commandDetails,
				legacyPrefix = event.legacyPrefix,
				lang = event.lang,
			),
		)
	}
}
