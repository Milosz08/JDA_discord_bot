/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.core.command.embed

import java.awt.Color

enum class EmbedColor(private val hex: Int) {
	TINT(0x505050), // gray
	DOMINATE(0x000000), // black
	WHITE(0xFFFFFF), // white
	ERROR(0xEF4444), // red
	;

	fun color(): Color = Color(hex)
}
