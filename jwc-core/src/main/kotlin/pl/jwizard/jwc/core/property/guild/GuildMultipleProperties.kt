/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwc.core.property.guild

/**
 * A specialized map that holds multiple guild-specific properties.
 *
 * This class extends [HashMap] and is used to store and manage guild properties. The map size is initialized based
 * on the provided size parameter to optimize memory allocation.
 *
 * @property mapSize The initial size of the map used to store guild properties.
 * @author Miłosz Gilga
 */
class GuildMultipleProperties(private val mapSize: Int) : HashMap<GuildProperty, Any>(mapSize) {

	/**
	 * Retrieves a property of type [T] associated with the given [GuildProperty] key.
	 *
	 * This method casts the value associated with the key to the specified type [T].
	 *
	 * @param T The type to which the property value should be cast.
	 * @param key The [GuildProperty] key for which to retrieve the property value.
	 * @return The property value cast to type [T].
	 */
	inline fun <reified T> getProperty(key: GuildProperty) = get(key) as T

	/**
	 * Retrieves a property of type [T] associated with the given [GuildProperty] key, returning null if the property
	 * is not found or if it is null.
	 *
	 * This method attempts to cast the value associated with the key to the specified type [T]. If the key does not
	 * exist in the map or the value is null, it returns null without throwing an exception.
	 *
	 * @param T The type to which the property value should be cast.
	 * @param key The [GuildProperty] key for which to retrieve the property value.
	 * @return The property value cast to type [T], or null if the property is not found or is null.
	 */
	inline fun <reified T> getNullableProperty(key: GuildProperty) = get(key) as T?
}
