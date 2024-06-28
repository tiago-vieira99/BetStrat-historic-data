package com.api.BetStrat.entity.football;

import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.enums.StrategyDurationScaleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DrawSeasonStats", uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamDH", columnNames = { "teamID", "season", "competition" }) })
public class DrawSeasonStats extends StrategySeasonStats {

    @Column(name = "drawRate")
    private double drawRate;

    @Column(name = "num_draws")
    private int numDraws;

    @Override
    public void maxSeqScale() {
        // avg odds : 3 - 3.3
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM_LONG.getValue());
    }
}
