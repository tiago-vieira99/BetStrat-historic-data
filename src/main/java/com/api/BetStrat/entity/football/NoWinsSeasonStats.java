package com.api.BetStrat.entity.football;

import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.enums.StrategyDurationScaleEnum;
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
@Table(name = "NoWinsSeasonStats",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamNoW", columnNames = { "teamID", "season", "competition" }) })
public class NoWinsSeasonStats extends StrategySeasonStats {

    @Column(name = "no_wins_rate")
    private double noWinsRate;

    @Column(name = "num_no_wins")
    private int numNoWins;

    @Override
    public void maxSeqScale() {
        // avg odds : 3 - 3.3 TODO
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM_LONG.getValue());
    }

}
