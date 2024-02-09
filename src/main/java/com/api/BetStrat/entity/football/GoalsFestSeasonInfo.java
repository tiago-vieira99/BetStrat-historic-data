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
@Table(name = "GoalsFestSeasonInfo",  uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeamGF", columnNames = { "teamID", "season", "competition" }) })
public class GoalsFestSeasonInfo extends StatsBySeasonInfo {

    @Column(name = "goalsFestRate")
    private double goalsFestRate;

    @Column(name = "num_goalsFest")
    private int numGoalsFest;

}
