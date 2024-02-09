package com.api.BetStrat.entity.basketball;

import com.api.BetStrat.entity.StatsBySeasonInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ShortBasketWinsSeasonInfo", uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamShortBasketWins", columnNames = { "teamID", "season", "competition" }) })
public class ShortBasketWinsSeasonInfo extends StatsBySeasonInfo {

    @Column(name = "shortWinsRate")
    private double shortWinsRate;

    @Column(name = "winsRate")
    private double winsRate;

    @Column(name = "num_shortWins")
    private int numShortWins;

    @Column(name = "num_wins")
    private int numWins;

}
