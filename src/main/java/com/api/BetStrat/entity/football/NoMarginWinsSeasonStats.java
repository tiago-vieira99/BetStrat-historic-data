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
@Table(name = "NoMarginWinsSeasonStats",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamNoWM", columnNames = { "teamID", "season", "competition" }) })
public class NoMarginWinsSeasonStats extends StrategySeasonStats {

    @Column(name = "wins_rate")
    private double winsRate;

    @Column(name = "no_margin_wins_rate")
    private double noMarginWinsRate;

    @Column(name = "num_wins")
    private int numWins;

    @Column(name = "num_no_margin_wins")
    private int numNoMarginWins;

    @Override
    public void maxSeqScale() {
        // avg odds : 3 - 3.3 TODO
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM_LONG.getValue());
    }

}
