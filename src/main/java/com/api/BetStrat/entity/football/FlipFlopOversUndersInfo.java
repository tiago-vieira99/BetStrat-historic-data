package com.api.BetStrat.entity.football;

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
@Table(name = "FlipFlopOversUndersInfo", uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamFF", columnNames = { "teamID", "season", "competition" }) })
public class FlipFlopOversUndersInfo extends StatsBySeasonInfo {

    @Column(name = "oversRate")
    private double oversRate;

    @Column(name = "undersRate")
    private double undersRate;

    @Column(name = "num_overs")
    private int numOvers;

    @Column(name = "num_unders")
    private int numUnders;

}
