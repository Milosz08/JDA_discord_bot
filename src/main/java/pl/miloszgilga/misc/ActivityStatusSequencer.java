/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 *
 * File name: StatusRoundRobin.java
 * Last modified: 24/02/2023, 00:18
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

package pl.miloszgilga.misc;

import lombok.extern.slf4j.Slf4j;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import pl.miloszgilga.BotCommand;
import pl.miloszgilga.core.configuration.BotConfiguration;

import static java.util.Objects.isNull;
import static java.util.Collections.shuffle;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import static pl.miloszgilga.core.configuration.BotProperty.*;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@Slf4j
@Component
public class ActivityStatusSequencer {

    private final BotConfiguration jConfig;

    private int position = 0;
    private JDA jda;

    private final List<BotCommand> botCommands = Arrays.stream(BotCommand.values()).toList();
    private final Deque<String> cachedTextActivities = new LinkedList<>();

    private final ScheduledExecutorService threadPool = newSingleThreadScheduledExecutor(r -> {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ActivityStatusSequencer(BotConfiguration jConfig) {
        this.jConfig = jConfig;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void loadConfiguration(JDA jda) {
        if (!jConfig.getProperty(J_RR_ACTIVITY_ENABLED, Boolean.class)) return;
        this.jda = jda;

        if (jConfig.getProperty(J_RR_EXTERNAL_FILE_ENABLED, Boolean.class)) {
            try {
                final InputStream fileStream = new FileInputStream(jConfig.getProperty(J_RR_EXTERNAL_FILE_PATH));
                try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileStream))) {
                    String line;
                    while (!isNull(line = bufferedReader.readLine())) {
                        cachedTextActivities.add(line);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
        if (!jConfig.getProperty(J_RR_COMMANDS_ENABLED, Boolean.class)) {
            botCommands.stream()
                .map(c -> jConfig.getProperty(J_PREFIX) + c.getName())
                .forEach(cachedTextActivities::addLast);
        }
        if (jConfig.getProperty(J_RR_RANDOMIZED, Boolean.class)) {
            shuffle((List<?>) cachedTextActivities);
        }
        log.info("Successfully loaded activity sequencer with parameters");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void invoke() {
        if (!jConfig.getProperty(J_RR_ACTIVITY_ENABLED, Boolean.class)) return;

        final int interval = jConfig.getProperty(J_RR_INTERVAL, Integer.class);
        log.info("Invoke activity sequencer thread. Inverval: {} sec per activity", interval);

        threadPool.scheduleWithFixedDelay(() -> {
            if (isNull(jda)) return;
            final String selectedActivity = ((LinkedList<String>) cachedTextActivities).get(position);
            jda.getPresence().setActivity(Activity.listening(selectedActivity));
            position = (position + 1) % cachedTextActivities.size();
        }, 0, interval, SECONDS);
    }
}
