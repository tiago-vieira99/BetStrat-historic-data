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
@Table(name = "EuroHandicapSeasonStats",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamEH", columnNames = { "teamID", "season", "competition" }) })
public class EuroHandicapSeasonStats extends StrategySeasonStats {
    private static final long serialVersionUID = 1L;

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
        // avg odds :  TODO
        // odd tight match: 3.17 - 4.64
        // odd in favour match:
        // odd underdog match:
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM_LONG.getValue());
    }

}
