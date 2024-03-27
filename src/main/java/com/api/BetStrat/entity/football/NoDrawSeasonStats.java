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
@Table(name = "NoDrawSeasonStats", uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamNoDH", columnNames = { "teamID", "season", "competition" }) })
public class NoDrawSeasonStats extends StrategySeasonStats {

    @Column(name = "no_draw_rate")
    private double noDrawRate;

    @Column(name = "num_no_draws")
    private int numNoDraws;

    @Override
    public void maxSeqScale() {
        // avg odds : 3 - 3.3
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM_LONG.getValue());
    }
}
