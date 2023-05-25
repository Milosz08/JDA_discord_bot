/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 *
 * File name: CacheableModuleData.java
 * Last modified: 28/04/2023, 23:45
 * Project name: jwizard-discord-bot
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 *     <http://www.apache.org/license/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the license.
 */

package pl.miloszgilga.cacheable;

import lombok.Builder;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.BiConsumer;

import pl.miloszgilga.dto.CommandEventWrapper;
import pl.miloszgilga.core.configuration.BotProperty;

import pl.miloszgilga.domain.guild_modules.GuildModulesEntity;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@Builder
public record CacheableModuleData(
    CommandEventWrapper event,
    BotProperty settingModule,
    Function<GuildModulesEntity, Boolean> modulePropGetter,
    BiConsumer<GuildModulesEntity, Boolean> modulePropSetter,
    Consumer<GuildModulesEntity> moduleRemoteSetter
) {
}