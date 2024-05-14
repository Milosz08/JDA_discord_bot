/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.core.bot

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.internal.managers.AccountManagerImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Primary
import org.springframework.core.io.ResourceLoader
import pl.jwizard.core.command.reflect.CommandLoader
import pl.jwizard.core.i18n.I18nService
import pl.jwizard.core.log.AbstractLoggingBean
import pl.jwizard.core.settings.GuildSettings
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import kotlin.system.exitProcess

@Primary
@Configuration
class BotConfiguration(
	@Lazy val i18nService: I18nService,
	@Lazy val guildSettings: GuildSettings,
	@Lazy val commandLoader: CommandLoader,
	@Lazy val botProperties: BotProperties,
	val resourceLoader: ResourceLoader,
) : AbstractLoggingBean(BotConfiguration::class) {

	val threadPool: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
	val eventWaiter: EventWaiter = EventWaiter()

	fun setTitleAndIcon(jda: JDA) {
		try {
			val iconResource = resourceLoader.getResource(botProperties.appIconPath)
			AccountManagerImpl(jda.selfUser)
				.setName(botProperties.appName)
				.setAvatar(iconResource.inputStream.let { Icon.from(it) })
				.queue(
					{
						log.info(
							"Successfully set application icon: {} and title: {}",
							botProperties.appIconPath,
							botProperties.appName,
						)
					},
					{ log.error("Unable to set title and/or application icon. Cause: {}", it.message) }
				)
		} catch (ex: IOException) {
			log.info("Unable to set title and/or application icon. Cause: {}", ex.message)
			exitProcess(-1)
		}
	}

	@Bean
	fun objectMapper(): ObjectMapper = ObjectMapper()
		.registerModule(ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
}
