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
@Table(name = "WinBothHalvesSeasonStats",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamWBH", columnNames = { "teamID", "season", "competition" }) })
public class WinBothHalvesSeasonStats extends StrategySeasonStats {

    @Column(name = "win_both_halves_rate")
    private double winBothHalvesRate;

    @Column(name = "num_wins_both_halves")
    private int numWinsBothHalves;

    @Column(name = "winsRate")
    private double winsRate;

    @Column(name = "num_wins")
    private int numWins;

    public WinBothHalvesSeasonStats() {
        maxSeqScale();
    }

    @Override
    public void maxSeqScale() {
        // avg odds : 2.3 - 5
        super.setMaxSeqScale(StrategyDurationScaleEnum.LONG.getValue());
    }

}
