package com.api.BetStrat.entity.football;

import com.api.BetStrat.entity.StatsBySeasonInfo;
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
@Table(name = "DrawSeasonInfo", uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamDH", columnNames = { "teamID", "season", "competition" }) })
public class DrawSeasonInfo extends StatsBySeasonInfo {

    @Column(name = "drawRate")
    private double drawRate;

    @Column(name = "num_draws")
    private int numDraws;

}
