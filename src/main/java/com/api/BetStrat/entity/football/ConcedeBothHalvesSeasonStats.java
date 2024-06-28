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
@Table(name = "ConcedeBothHalvesSeasonStats",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamCBH", columnNames = { "teamID", "season", "competition" }) })
public class ConcedeBothHalvesSeasonStats extends StrategySeasonStats {

    @Column(name = "concede_both_halves_rate")
    private double concedeBothHalvesRate;

    @Column(name = "num_concede_both_halves")
    private int numConcedeBothHalves;

    @Override
    public void maxSeqScale() {
        // avg odds : 1.6 - 2
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM_SHORT.getValue());
    }

}
