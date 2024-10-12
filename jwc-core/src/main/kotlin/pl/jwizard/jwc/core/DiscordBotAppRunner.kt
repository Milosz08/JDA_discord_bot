/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwc.core

import org.springframework.context.annotation.ComponentScan
import pl.jwizard.jwc.core.audio.spi.DistributedAudioClientSupplier
import pl.jwizard.jwc.core.exception.spi.ExceptionTrackerStore
import pl.jwizard.jwc.core.jda.ActivitySplashesBean
import pl.jwizard.jwc.core.jda.JdaInstanceBean
import pl.jwizard.jwc.core.jda.spi.ChannelListenerGuard
import pl.jwizard.jwc.core.jda.spi.CommandsLoader
import pl.jwizard.jwc.core.printer.AbstractPrinter
import pl.jwizard.jwc.core.printer.ConsolePrinter
import pl.jwizard.jwc.core.printer.FancyFramePrinter
import pl.jwizard.jwc.core.printer.FancyTitlePrinter
import pl.jwizard.jwc.core.radio.spi.RadioPlaybackMappersCache
import pl.jwizard.jwc.core.util.logger
import kotlin.reflect.KClass

/**
 * The main class that loads resources, configuration and runs the bot instance.
 * Use this class with [DiscordBotApp] annotation. Singleton instance.
 *
 * @author Miłosz Gilga
 */
object DiscordBotAppRunner {
	private val log = logger<DiscordBotAppRunner>()

	/**
	 * Base application package. Used for Spring Context [ComponentScan] annotation. All classes related with
	 * Spring IoC containers will be loaded into Spring Context.
	 */
	const val BASE_PACKAGE = "pl.jwizard"

	/**
	 * Fancy title banner classpath location in `resources` directory.
	 */
	private const val BANNER_CLASSPATH_LOCATION = "util/banner.txt"

	/**
	 * Fancy frame classpath location in `resources` directory.
	 */
	private const val FRAME_CLASSPATH_LOCATION = "util/frame.txt"

	/**
	 * Spring Kotlin Context instance.
	 */
	private lateinit var context: SpringKtContextFactory

	/**
	 * Static method which starts loading configuration, resources and a new JDA instance of the bot being created.
	 *
	 * @param clazz main type of class that runs the bot.
	 */
	fun run(clazz: KClass<*>) {
		val startTimestamp = System.currentTimeMillis()
		try {
			val printer = ConsolePrinter()
			val printers = arrayOf(
				FancyTitlePrinter(BANNER_CLASSPATH_LOCATION, printer),
				FancyFramePrinter(FRAME_CLASSPATH_LOCATION, printer),
			)
			AbstractPrinter.printContent(printers)
			try {
				log.info("Init Spring Context with base class: {}. Init packages tree: {}.", clazz.qualifiedName, BASE_PACKAGE)
				context = SpringKtContextFactory(clazz)

				val jdaInstance = context.getBean(JdaInstanceBean::class)
				val activitySplashes = context.getBean(ActivitySplashesBean::class)

				val radioPlaybackMappersCache = context.getBean(RadioPlaybackMappersCache::class)
				val exceptionTrackerStore = context.getBean(ExceptionTrackerStore::class)
				val commandLoader = context.getBean(CommandsLoader::class)
				val audioClientSupplier = context.getBean(DistributedAudioClientSupplier::class)
				val channelListenerGuard = context.getBean(ChannelListenerGuard::class)

				radioPlaybackMappersCache.loadRadioPlaybackClasses()
				exceptionTrackerStore.initTrackers()

				commandLoader.loadMetadata()
				commandLoader.loadClassesViaReflectionApi()

				audioClientSupplier.initClientNodes()

				jdaInstance.createJdaWrapper(audioClientSupplier)
				jdaInstance.configureMetadata()

				activitySplashes.initSplashesSequence()
				channelListenerGuard.initThreadPool()

				val endTimestamp = System.currentTimeMillis()
				val elapsedTime = endTimestamp - startTimestamp

				log.info("Load in: {}s. Start listening incoming requests...", elapsedTime / 1000.0)
			} catch (ex: Throwable) {
				throw IrreparableException(ex)
			}
		} catch (ex: IrreparableException) {
			ex.printLogStatement()
			ex.killProcess()
		}
	}
}
