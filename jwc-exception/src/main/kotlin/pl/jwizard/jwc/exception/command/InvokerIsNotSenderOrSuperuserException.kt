/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwc.exception.command

import pl.jwizard.jwc.core.i18n.source.I18nExceptionSource
import pl.jwizard.jwc.core.jda.command.CommandBaseContext
import pl.jwizard.jwc.exception.CommandPipelineExceptionHandler

/**
 * Exception thrown when an action is invoked by a user who is neither the sender nor a superuser (moderator, owner,
 * or DJ).
 *
 * @param commandBaseContext The context of the command that triggered this exception.
 * @author Miłosz Gilga
 */
class InvokerIsNotSenderOrSuperuserException(
	commandBaseContext: CommandBaseContext,
) : CommandPipelineExceptionHandler(
	commandBaseContext,
	i18nExceptionSource = I18nExceptionSource.INVOKER_IS_NOT_SENDER_OR_SUPERUSER,
	logMessage = "Attempt to invoke action while invoker is not sender or super-user (moderator, owner or dj).",
)
