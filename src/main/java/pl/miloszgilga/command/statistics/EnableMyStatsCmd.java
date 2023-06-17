/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 *
 * File name: EnableMyStatsCmd.java
 * Last modified: 16/05/2023, 18:53
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

package pl.miloszgilga.command.statistics;

import lombok.extern.slf4j.Slf4j;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Map;

import pl.miloszgilga.BotCommand;
import pl.miloszgilga.misc.JDALog;
import pl.miloszgilga.locale.ResLocaleSet;
import pl.miloszgilga.dto.CommandEventWrapper;
import pl.miloszgilga.embed.EmbedMessageBuilder;
import pl.miloszgilga.command.AbstractMyStatsCommand;
import pl.miloszgilga.cacheable.CacheableCommandStateDao;
import pl.miloszgilga.cacheable.CacheableMemberSettingsDao;
import pl.miloszgilga.core.remote.RemotePropertyHandler;
import pl.miloszgilga.core.configuration.BotConfiguration;
import pl.miloszgilga.core.loader.JDAInjectableCommandLazyService;

import pl.miloszgilga.domain.member_stats.IMemberStatsRepository;
import pl.miloszgilga.domain.member_settings.IMemberSettingsRepository;

import static pl.miloszgilga.exception.StatsException.StatsAlreadyEnabledException;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@Slf4j
@JDAInjectableCommandLazyService
public class EnableMyStatsCmd extends AbstractMyStatsCommand {

    private final CacheableMemberSettingsDao cacheableMemberSettingsDao;
    private final IMemberSettingsRepository settingsRepository;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    EnableMyStatsCmd(
        BotConfiguration config, EmbedMessageBuilder embedBuilder, CacheableMemberSettingsDao cacheableMemberSettingsDao,
        IMemberStatsRepository statsRepository, IMemberSettingsRepository settingsRepository, RemotePropertyHandler handler,
        CacheableCommandStateDao cacheableCommandStateDao
    ) {
        super(BotCommand.ENABLE_STATS, config, embedBuilder, statsRepository, handler, cacheableCommandStateDao);
        this.cacheableMemberSettingsDao = cacheableMemberSettingsDao;
        this.settingsRepository = settingsRepository;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doExecuteMyStatsCommand(CommandEventWrapper event) {
        final var updatedSettings = cacheableMemberSettingsDao.toggleMemberStatsDisabled(event, false, isDisabled -> {
            if (!isDisabled) throw new StatsAlreadyEnabledException(config, event);
        });
        settingsRepository.save(updatedSettings);
        final String userTag = event.getMember().getUser().getAsTag();
        final MessageEmbed messageEmbed = embedBuilder.createMessage(ResLocaleSet.STATS_COLLECTOR_ENABLED_MESS, Map.of(
            "disableStatsCmd", BotCommand.DISABLE_STATS.parseWithPrefix(config)
        ), event.getGuild());
        JDALog.info(log, event, "Stats for selected memeber '%s' was successfully enabled", userTag);
        event.sendEmbedMessage(messageEmbed);
    }
}
