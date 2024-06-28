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
@Table(name = "HockeyDrawSeasonStats", uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamHD", columnNames = { "teamID", "season", "competition" }) })
public class HockeyDrawSeasonStats extends StrategySeasonStats {

    @Column(name = "drawRate")
    private double drawRate;

    @Column(name = "num_draws")
    private int numDraws;

    @Override
    public void maxSeqScale() {
        // avg odds : 3 - 3.3 TODO
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM_LONG.getValue());
    }

}
