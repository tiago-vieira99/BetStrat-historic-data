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
@Table(name = "ComebackSeasonInfo", uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamComebacks", columnNames = { "teamID", "season", "competition" }) })
public class ComebackSeasonInfo extends StatsBySeasonInfo {

    @Column(name = "comebacksRate")
    private double comebacksRate;

    @Column(name = "winsRate")
    private double winsRate;

    @Column(name = "num_comebacks")
    private int numComebacks;

    @Column(name = "num_wins")
    private int numWins;

}
