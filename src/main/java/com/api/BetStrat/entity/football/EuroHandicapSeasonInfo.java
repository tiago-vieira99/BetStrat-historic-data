package com.api.BetStrat.entity.football;

import com.api.BetStrat.entity.StatsBySeasonInfo;
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
@Table(name = "EuroHandicapSeasonInfo",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamEH", columnNames = { "teamID", "season", "competition" }) })
public class EuroHandicapSeasonInfo extends StatsBySeasonInfo {
    private static final long serialVersionUID = 1L;

    @Column(name = "winsRate")
    private double winsRate;

    @Column(name = "marginWinsRate")
    private double marginWinsRate;

    @Column(name = "num_wins")
    private int numWins;

    @Column(name = "num_marginWins")
    private int numMarginWins;

}
