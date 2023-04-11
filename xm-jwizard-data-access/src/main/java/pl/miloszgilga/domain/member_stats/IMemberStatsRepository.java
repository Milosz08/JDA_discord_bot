/*
 * Copyright (c) 2023 by MILOSZ GILGA <http://miloszgilga.pl>
 *
 * File name: IMemberStatsRepository.java
 * Last modified: 07/04/2023, 01:25
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

package pl.miloszgilga.domain.member_stats;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import pl.miloszgilga.dto.GuildMembersStatsDto;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@Repository
public interface IMemberStatsRepository extends JpaRepository<MemberStatsEntity, Long> {
    Optional<MemberStatsEntity> findByMember_DiscordIdAndGuild_DiscordId(String memberDiscordId, String guildDiscordId);
    boolean existsByMember_DiscordIdAndGuild_DiscordId(String memberDiscordId, String guildDiscordId);
    void deleteAllByGuild_DiscordId(String guildDiscordId);
    void deleteByMember_DiscordIdAndGuild_DiscordId(String memberDiscordId, String guildDiscordId);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Query(value = """
        select new pl.miloszgilga.dto.GuildMembersStatsDto(
            sum(e.messagesSended), sum(e.messagesUpdated), sum(e.reactionsAdded), sum(e.slashInteractions)
        ) from MemberStatsEntity e join e.guild g where g.discordId = :guildDiscordId group by g
    """)
    GuildMembersStatsDto getAllMemberStats(@Param("guildDiscordId") String guildDiscordId);
}
