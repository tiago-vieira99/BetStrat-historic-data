package com.api.BetStrat.entity.hockey;

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
@Table(name = "WinsMargin3SeasonStats",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamWM", columnNames = { "teamID", "season", "competition" }) })
public class WinsMargin3SeasonStats extends StrategySeasonStats {

    @Column(name = "winsRate")
    private double winsRate;

    @Column(name = "marginWinsRate")
    private double marginWinsRate;

    @Column(name = "num_wins")
    private int numWins;

    @Column(name = "num_marginWins")
    private int numMarginWins;

    @Override
    public void maxSeqScale() {
        // avg odds : 3 - 3.3 TODO
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM_LONG.getValue());
    }

}
