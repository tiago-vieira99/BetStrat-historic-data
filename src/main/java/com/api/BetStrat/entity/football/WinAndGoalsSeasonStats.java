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
@Table(name = "WinAndGoalsSeasonStats",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamWAndG", columnNames = { "teamID", "season", "competition" }) })
public class WinAndGoalsSeasonStats extends StrategySeasonStats {

    @Column(name = "win_and_goals_rate")
    private double winAndGoalsRate;

    @Column(name = "winsRate")
    private double winsRate;

    @Column(name = "num_wins")
    private int numWins;

    @Column(name = "num_wins_and_goals")
    private int numWinsAndGoals;

    @Override
    public void maxSeqScale() {
        // avg odds : 3 - 3.3 TODO
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM_LONG.getValue());
    }

}
