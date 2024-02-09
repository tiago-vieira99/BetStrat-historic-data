package com.api.BetStrat.entity.hockey;

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
@Table(name = "HockeyDrawSeasonInfo", uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamHD", columnNames = { "teamID", "season", "competition" }) })
public class HockeyDrawSeasonInfo extends StatsBySeasonInfo {

    @Column(name = "drawRate")
    private double drawRate;

    @Column(name = "num_draws")
    private int numDraws;

}
