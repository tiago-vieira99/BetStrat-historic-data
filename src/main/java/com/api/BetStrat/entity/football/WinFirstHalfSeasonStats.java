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
@Table(name = "WinFirstHalfSeasonStats",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamWFH", columnNames = { "teamID", "season", "competition" }) })
public class WinFirstHalfSeasonStats extends StrategySeasonStats {

    @Column(name = "win_first_half_rate")
    private double winFirstHalfRate;

    @Column(name = "num_wins_first_half")
    private int numWinsFirstHalf;

    public WinFirstHalfSeasonStats() {
        maxSeqScale();
    }

    @Override
    public void maxSeqScale() {
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM_SHORT.getValue());
    }

}
