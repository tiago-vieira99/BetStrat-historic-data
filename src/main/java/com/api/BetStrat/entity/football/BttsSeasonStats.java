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
@Table(name = "BttsSeasonStats",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamBtts", columnNames = { "teamID", "season", "competition" }) })
public class BttsSeasonStats extends StrategySeasonStats {

    @Column(name = "btts_rate")
    private double bttsRate;

    @Column(name = "num_btts")
    private int numBtts;

    @Override
    public void maxSeqScale() {
        // avg odds : 1.5 - 2.2
        super.setMaxSeqScale(StrategyDurationScaleEnum.SHORT.getValue());
    }

}
