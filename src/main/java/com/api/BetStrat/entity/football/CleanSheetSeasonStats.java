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
@Table(name = "CleanSheetSeasonStats", uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamCleanSheet", columnNames = { "teamID", "season", "competition" }) })
public class CleanSheetSeasonStats extends StrategySeasonStats {

    @Column(name = "clean_sheet_rate")
    private double cleanSheetRate;

    @Column(name = "num_clean_sheets")
    private int numCleanSheets;

    @Override
    public void maxSeqScale() {
        // avg odds : 3 - 3.3
        super.setMaxSeqScale(StrategyDurationScaleEnum.MEDIUM_LONG.getValue());
    }
}
