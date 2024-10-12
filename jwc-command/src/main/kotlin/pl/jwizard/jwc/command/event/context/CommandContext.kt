/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwc.command.event.context

import net.dv8tion.jda.api.Permission
import pl.jwizard.jwc.command.GuildCommandProperties
import pl.jwizard.jwc.command.event.arg.CommandArgumentParsingData
import pl.jwizard.jwc.command.event.exception.CommandParserException
import pl.jwizard.jwc.command.refer.CommandArgument
import pl.jwizard.jwc.core.jda.command.CommandBaseContext

/**
 * Abstract class representing the context of a command execution within a guild. It provides access to command
 * arguments and guild-specific properties needed for command processing.
 *
 * @property guildCommandProperties Properties related to the guild where the command is executed.
 * @author Miłosz Gilga
 */
abstract class CommandContext(private val guildCommandProperties: GuildCommandProperties) : CommandBaseContext {

	val musicTextChannelId = guildCommandProperties.musicTextChannelId
	val djRoleName = guildCommandProperties.djRoleName

	override val guildLanguage = guildCommandProperties.lang
	override val guildDbId = guildCommandProperties.guildDbId

	/**
	 * A boolean indicating whether the command is executed as a slash command event. This affects how the command is
	 * processed and interpreted.
	 */
	abstract val isSlashEvent: Boolean

	/**
	 * A mutable map that stores the parsed command arguments, with their corresponding [CommandArgument] as keys and
	 * their parsed data as values.
	 */
	val commandArguments: MutableMap<CommandArgument, CommandArgumentParsingData> = mutableMapOf()

	/**
	 * Retrieves and casts the argument value for the specified [CommandArgument].
	 *
	 * @param argument The [CommandArgument] whose value is to be retrieved.
	 * @param T The type to cast the argument value to.
	 * @return The cast value of the argument.
	 * @throws CommandParserException If the argument is not found or cannot be cast to the desired type.
	 */
	inline fun <reified T : Any> getArg(argument: CommandArgument) = getNullableArg<T>(argument)
		?: throw CommandParserException()

	/**
	 * Retrieves the argument value for the specified [CommandArgument], returning null if not found.
	 *
	 * @param argument The [CommandArgument] whose value is to be retrieved.
	 * @param T The type to cast the argument value to.
	 * @return The cast value of the argument, or null if not found.
	 * @throws CommandParserException If the argument cannot be cast to the desired type.
	 */
	inline fun <reified T : Any> getNullableArg(argument: CommandArgument) = try {
		val (value, type) = commandArguments[argument] ?: throw NumberFormatException()
		if (value == null) null else type.castTo(value) as T?
	} catch (ex: NumberFormatException) {
		throw CommandParserException()
	}

	/**
	 * Checks if the command author has the specified permissions.
	 *
	 * @param permissions The list of permission strings to check against the author's permissions.
	 * @return True if the author has at least one of the specified permissions; otherwise, false.
	 */
	fun checkIfAuthorHasPermissions(vararg permissions: String) = permissions.any {
		author.hasPermission(Permission.valueOf(it))
	}

	/**
	 * Checks if the command author has any of the specified roles.
	 *
	 * @param roles The list of role names to check against the author's roles.
	 * @return True if the author has at least one of the specified roles; otherwise, false.
	 */
	fun checkIfAuthorHasRoles(vararg roles: String) = author.roles.any { roles.contains(it.name) }
}
