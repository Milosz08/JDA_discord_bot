/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.core.api.dj

import pl.jwizard.core.api.AbstractDjCmd
import pl.jwizard.core.audio.player.PlayerManagerFacade
import pl.jwizard.core.bot.BotConfiguration
import pl.jwizard.core.command.BotCommand
import pl.jwizard.core.command.CompoundCommandEvent
import pl.jwizard.core.command.embed.CustomEmbedBuilder
import pl.jwizard.core.command.reflect.CommandListenerBean
import pl.jwizard.core.exception.AudioPlayerException
import pl.jwizard.core.i18n.I18nResLocale

@CommandListenerBean(id = BotCommand.SHUFFLE)
class ShuffleQueueCmd(
	botConfiguration: BotConfiguration,
	playerManagerFacade: PlayerManagerFacade
) : AbstractDjCmd(
	botConfiguration,
	playerManagerFacade
) {
	init {
		onSameChannelWithBot = true
	}

	override fun executeDjCmd(event: CompoundCommandEvent) {
		val musicManager = playerManagerFacade.findMusicManager(event)
		if (musicManager.queue.isEmpty()) {
			throw AudioPlayerException.TrackQueueIsEmptyException(event)
		}
		playerManagerFacade.shuffleQueue(event)
		val embedMessage = CustomEmbedBuilder(event, botConfiguration).buildBaseMessage(
			placeholder = I18nResLocale.QUEUE_WAS_SHUFFLED,
			params = mapOf(
				"showQueueCmd" to BotCommand.QUEUE.parseWithPrefix(botConfiguration, event),
			),
		)
		event.appendEmbedMessage(embedMessage)
	}
}
