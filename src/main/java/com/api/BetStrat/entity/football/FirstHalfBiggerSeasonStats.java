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
@Table(name = "FirstHalfBiggerSeasonStats",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeam1HB", columnNames = { "teamID", "season", "competition" }) })
public class FirstHalfBiggerSeasonStats extends StrategySeasonStats {

    @Column(name = "first_half_bigger_rate")
    private double firstHalfBiggerRate;

    @Column(name = "num_first_half_bigger")
    private int numFirstHalfBigger;

    public FirstHalfBiggerSeasonStats() {
        maxSeqScale();
    }

    @Override
    public void maxSeqScale() {
        // avg odds : 2.9 - 3.2
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM.getValue());
    }

}
