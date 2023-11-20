package com.waterball.LegendsOfTheThreeKingdoms.domain.unittest;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Round;
import com.waterball.LegendsOfTheThreeKingdoms.domain.RoundPhase;
import com.waterball.LegendsOfTheThreeKingdoms.domain.builders.PlayerBuilder;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.*;
import com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase.Normal;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Dodge;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Kill;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Peach;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.BloodCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Hand;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.HealthStatus;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.RoleCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayCard.*;
import static com.waterball.LegendsOfTheThreeKingdoms.presenter.ViewModel.getEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlayerDyingTest {

    @DisplayName("""
            Given 
            A 血量等於 1
            B C D 沒有死亡
                        
            When
            A 玩家被 B 玩家出殺且沒有閃
               
            Then
            A 玩家已瀕臨死亡且狀態是 Dying
                        
            Then
            系統向所有玩家發出 player-b PlayCardEvent
            系統向所有玩家發出 player-a PlayerDamagedEvent
            系統向所有玩家發出 PlayerDyingEvent, playerId = "player-a"
            系統向所有玩家發出 AskPeachEvent, playerId = "player-b"
            activePlayer = "player-b"
            currentRoundPlayer = "player-b"
            dyingPlayer = "player-a"
            RoundPhase(Action)
            GamePhase(GeneralDying)
            """)
    @Test
    public void playerAHas1HP_WhenPlayerAPlayCardSkip_ThenPlayerADyingAndAskPlayerBPeach() {
        //Given
        Game game = new Game();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard("SHU001", "劉備", 1))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard("SHU001", "劉備", 4))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard("SHU001", "劉備", 4))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard("SHU001", "劉備", 4))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();


        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerB));
        game.enterPhase(new Normal(game));

        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");

        //When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");
        //Then
        PlayCardEvent playCardEvent = getEvent(events, PlayCardEvent.class).orElseThrow(RuntimeException::new);
        PlayerDamagedEvent playerDamagedEvent = getEvent(events, PlayerDamagedEvent.class).orElseThrow(RuntimeException::new);
        PlayerDyingEvent playerDyingEvent = getEvent(events, PlayerDyingEvent.class).orElseThrow(RuntimeException::new);
        AskPeachEvent askPeachEvent = getEvent(events, AskPeachEvent.class).orElseThrow(RuntimeException::new);

        assertNotNull(playCardEvent);
        RoundEvent roundEvent = playCardEvent.getRound();
        assertEquals("player-a", playerDamagedEvent.getPlayerId());
        assertEquals("player-a", playerDyingEvent.getPlayerId());
        assertEquals("player-b", askPeachEvent.getPlayerId());
        assertEquals("player-a",roundEvent.getDyingPlayer());

        assertEquals("player-b", roundEvent.getCurrentRoundPlayer());
        assertEquals("player-b", roundEvent.getActivePlayer());
        assertEquals("GeneralDying", game.getGamePhase().getPhaseName());
    }

    @DisplayName("""
            Given
            A 玩家已瀕臨死亡且狀態是 Dying
            B 玩家被詢問要不要出桃救A
                        
            When
            B玩家不出桃救A
               
            Then
            C玩家被詢問要不要出桃救A
            系統向所有玩家發出 player-b PlayCardEvent
            系統向所有玩家發出 AskPeachEvent, playerId = "player-c"
            activePlayer = "player-c"
            currentRoundPlayer = "player-b"
            dyingPlayer = "player-a"
            RoundPhase(Action)
            GamePhase(Normal)
            """)
    @Test
    public void playerADying_WhenPlayerBPlayCardSkip_ThenAskPlayerCPeach() {
        //Given
        Game game = new Game();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(0))
                .withGeneralCard(new GeneralCard("SHU001", "劉備", 0))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard("SHU001", "劉備", 4))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard("SHU001", "劉備", 4))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard("SHU001", "劉備", 4))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();


        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerB));
        game.enterPhase(new Normal(game));

        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");

        //When
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        //Then
        PlayCardEvent playCardEvent = getEvent(events, PlayCardEvent.class).orElseThrow(RuntimeException::new);
        AskPeachEvent askPeachEvent = getEvent(events, AskPeachEvent.class).orElseThrow(RuntimeException::new);

        assertNotNull(playCardEvent);
        RoundEvent roundEvent = playCardEvent.getRound();
        assertEquals("player-c", askPeachEvent.getPlayerId());
        assertEquals("player-a",roundEvent.getDyingPlayer());

        assertEquals("player-b", roundEvent.getCurrentRoundPlayer());
        assertEquals("player-c", roundEvent.getActivePlayer());
        assertEquals("GeneralDying", game.getGamePhase().getPhaseName());
    }
}

