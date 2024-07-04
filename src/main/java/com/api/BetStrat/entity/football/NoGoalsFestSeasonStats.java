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
@AllArgsConstructor
@Entity
@Table(name = "NoGoalsFestSeasonStats",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamNoGF", columnNames = { "teamID", "season", "competition" }) })
public class NoGoalsFestSeasonStats extends StrategySeasonStats {

    @Column(name = "no_goals_fest_rate")
    private double noGoalsFestRate;

    @Column(name = "num_no_goals_fest")
    private int numNoGoalsFest;

    public NoGoalsFestSeasonStats() {
        maxSeqScale();
    }

    @Override
    public void maxSeqScale() {
        // avg odds : 1.6 - 2.2
        super.setMaxSeqScale(StrategyDurationScaleEnum.SHORT.getValue());
    }

}
