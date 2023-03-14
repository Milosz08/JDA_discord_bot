/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 *
 * File name: SettingsManager.java
 * Last modified: 14/03/2023, 11:27
 * Project name: jwizard-discord-bot
 *
 * Licensed under the MIT license; you may not use this file except in compliance with the License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * THE ABOVE COPYRIGHT NOTICE AND THIS PERMISSION NOTICE SHALL BE INCLUDED IN ALL
 * COPIES OR SUBSTANTIAL PORTIONS OF THE SOFTWARE.
 */

package pl.miloszgilga.settings;

import net.dv8tion.jda.api.entities.Guild;
import com.jagrosh.jdautilities.command.GuildSettingsManager;

import java.util.Map;
import java.util.HashMap;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class SettingsManager implements GuildSettingsManager<GuildSettings> {

    private final Map<Long, GuildSettings> settingsForGuilds = new HashMap<>();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void init() {

    }

    @Override
    public GuildSettings getSettings(Guild guild) {
        return null;
    }
}
