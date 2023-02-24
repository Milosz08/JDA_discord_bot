/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 *
 * File name: JWizard.java
 * Last modified: 22/02/2023, 16:55
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

package pl.miloszgilga;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class Bootloader {

    private static final String SPRING_CFG = "spring/spring-context.cfg.xml";
    public static final ApplicationContext APP_CONTEXT = new ClassPathXmlApplicationContext(SPRING_CFG);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
        final JWizardBot jWizardBot = APP_CONTEXT.getBean(JWizardBot.class);
        jWizardBot.run(args);
        ((ConfigurableApplicationContext)APP_CONTEXT).close();
    }
}
