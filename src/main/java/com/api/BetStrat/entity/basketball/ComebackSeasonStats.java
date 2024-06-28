package com.api.BetStrat.entity.basketball;

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
@Table(name = "ComebackSeasonStats", uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamComebacks", columnNames = { "teamID", "season", "competition" }) })
public class ComebackSeasonStats extends StrategySeasonStats {

    @Column(name = "comebacksRate")
    private double comebacksRate;

    @Column(name = "winsRate")
    private double winsRate;

    @Column(name = "num_comebacks")
    private int numComebacks;

    @Column(name = "num_wins")
    private int numWins;

    @Override
    public void maxSeqScale() {
        // avg odds : 3 - 3.3 TODO
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM_LONG.getValue());
    }

}
