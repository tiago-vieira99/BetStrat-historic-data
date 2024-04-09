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
@Table(name = "NoScoreBothHalvesSeasonStats",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamNoSBH", columnNames = { "teamID", "season", "competition" }) })
public class NoScoreBothHalvesSeasonStats extends StrategySeasonStats {

    @Column(name = "no_score_both_halves_rate")
    private double noScoreBothHalvesRate;

    @Column(name = "num_no_score_both_halves")
    private int numNoScoreBothHalves;

    @Override
    public void maxSeqScale() {
        // avg odds : 3 - 3.3 TODO
        // odd tight match:
        // odd in favour match:
        // odd underdog match:
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM_LONG.getValue());
    }

}
