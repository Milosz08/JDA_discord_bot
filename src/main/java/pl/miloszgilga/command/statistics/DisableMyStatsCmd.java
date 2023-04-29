/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 *
 * File name: DisableMyStatsCmd.java
 * Last modified: 09/04/2023, 21:51
 * Project name: jwizard-discord-bot
 *
 * Licensed under the MIT license; you may not use this file except in compliance with the License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * THE ABOVE COPYRIGHT NOTICE AND THIS PERMISSION NOTICE SHALL BE INCLUDED IN ALL COPIES OR
 * SUBSTANTIAL PORTIONS OF THE SOFTWARE.
 *
 * The software is provided "as is", without warranty of any kind, express or implied, including but not limited
 * to the warranties of merchantability, fitness for a particular purpose and noninfringement. In no event
 * shall the authors or copyright holders be liable for any claim, damages or other liability, whether in an
 * action of contract, tort or otherwise, arising from, out of or in connection with the software or the use
 * or other dealings in the software.
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
import pl.miloszgilga.cacheable.CacheableMemberSettingsDao;
import pl.miloszgilga.core.remote.RemotePropertyHandler;
import pl.miloszgilga.core.configuration.BotConfiguration;
import pl.miloszgilga.core.loader.JDAInjectableCommandLazyService;

import pl.miloszgilga.domain.member_stats.IMemberStatsRepository;
import pl.miloszgilga.domain.member_settings.IMemberSettingsRepository;

import static pl.miloszgilga.exception.StatsException.StatsAlreadyDisabledException;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@Slf4j
@JDAInjectableCommandLazyService
public class DisableMyStatsCmd extends AbstractMyStatsCommand {

    private final CacheableMemberSettingsDao cacheableMemberSettingsDao;
    private final IMemberSettingsRepository settingsRepository;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    DisableMyStatsCmd(
        BotConfiguration config, EmbedMessageBuilder embedBuilder, CacheableMemberSettingsDao cacheableMemberSettingsDao,
        IMemberStatsRepository statsRepository, IMemberSettingsRepository settingsRepository, RemotePropertyHandler handler
    ) {
        super(BotCommand.DISABLE_STATS, config, embedBuilder, statsRepository, handler);
        this.cacheableMemberSettingsDao = cacheableMemberSettingsDao;
        this.settingsRepository = settingsRepository;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void doExecuteMyStatsCommand(CommandEventWrapper event) {
        final var updatedSettings = cacheableMemberSettingsDao.toggleMemberStatsDisabled(event, true, isDisabled -> {
            if (isDisabled) throw new StatsAlreadyDisabledException(config, event);
        });
        settingsRepository.save(updatedSettings);
        final String userTag = event.getMember().getUser().getAsTag();
        final MessageEmbed messageEmbed = embedBuilder.createMessage(ResLocaleSet.STATS_COLLECTOR_DISABLED_MESS, Map.of(
            "enableStatsCmd", BotCommand.ENABLE_STATS.parseWithPrefix(config)
        ));
        JDALog.info(log, event, "Stats for selected memeber '%s' was successfully disabled", userTag);
        event.sendEmbedMessage(messageEmbed);
    }
}
