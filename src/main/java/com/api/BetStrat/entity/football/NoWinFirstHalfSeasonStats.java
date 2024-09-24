package com.api.BetStrat.entity.football;

import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.enums.StrategyDurationScaleEnum;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "NoWinFirstHalfSeasonStats",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamNoWFH", columnNames = { "teamID", "season", "competition" }) })
public class NoWinFirstHalfSeasonStats extends StrategySeasonStats {

    @Column(name = "no_win_first_half_rate")
    private double noWinFirstHalfRate;

    @Column(name = "num_no_wins_first_half")
    private int numNoWinsFirstHalf;

    public NoWinFirstHalfSeasonStats() {
        maxSeqScale();
    }

    @Override
    public void maxSeqScale() {
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM_SHORT.getValue());
    }

}
