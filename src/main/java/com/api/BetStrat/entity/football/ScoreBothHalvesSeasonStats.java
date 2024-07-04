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
@Table(name = "ScoreBothHalvesSeasonStats",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamSBH", columnNames = { "teamID", "season", "competition" }) })
public class ScoreBothHalvesSeasonStats extends StrategySeasonStats {

    @Column(name = "score_both_halves_rate")
    private double scoreBothHalvesRate;

    @Column(name = "num_score_both_halves")
    private int numScoreBothHalves;

    public ScoreBothHalvesSeasonStats() {
        maxSeqScale();
    }

    @Override
    public void maxSeqScale() {
        // avg odds : 1.8 - 3.1
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM.getValue());
    }

}
