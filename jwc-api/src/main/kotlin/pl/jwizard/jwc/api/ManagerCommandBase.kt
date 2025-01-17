/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwc.api

import pl.jwizard.jwc.command.context.GuildCommandContext
import pl.jwizard.jwc.core.jda.command.TFutureResponse
import pl.jwizard.jwc.exception.command.UnauthorizedManagerException

/**
 * Base class for commands that require managerial permissions.
 *
 * This class provides a framework for commands that should only be executed by users with managerial privileges. It
 * enforces permission checks and delegates the actual command execution to subclasses that implement specific
 * manager functionalities.
 *
 * @param commandEnvironment The environment context for executing the command.
 * @author Miłosz Gilga
 */
abstract class ManagerCommandBase(commandEnvironment: CommandEnvironmentBean) : CommandBase(commandEnvironment) {

	/**
	 * Executes the command after checking for managerial permissions.
	 *
	 * This method checks whether the user invoking the command has the required permissions to perform managerial
	 * actions. If the user does not have the necessary permissions, an UnauthorizedManagerException is thrown.
	 *
	 * @param context The context of the command, containing user interaction details.
	 * @param response The future response object used to send the result of the command execution.
	 * @throws UnauthorizedManagerException If the user does not have managerial permissions.
	 */
	final override fun execute(context: GuildCommandContext, response: TFutureResponse) {
		if (!context.checkIfAuthorHasPermissions(*(superuserPermissions.toTypedArray()))) {
			throw UnauthorizedManagerException(context)
		}
		executeManager(context, response)
	}

	/**
	 * Executes the specific managerial command functionality.
	 *
	 * This method must be implemented by subclasses to define the specific behavior of the managerial command. It is
	 * called only after the necessary permission checks have passed.
	 *
	 * @param context The context of the command, containing user interaction details.
	 * @param response The future response object used to send the result of the command execution.
	 */
	protected abstract fun executeManager(context: GuildCommandContext, response: TFutureResponse)
}
