package com.api.BetStrat.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Teams",  uniqueConstraints = { @UniqueConstraint(name = "UniqueTeamCountrySport", columnNames = { "name", "country", "sport" }) })
public class Team implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "country")
    private String country;

    @ApiModelProperty(example = "dd-MM-yyyy hh:mm:ss")
    @JsonFormat(pattern = "dd-MM-yyyy hh:mm:ss")
    @Temporal(TemporalType.DATE)
    @CreationTimestamp
    @Column(name = "created_date")
    private Date created_date;

    @ApiModelProperty(example = "dd-MM-yyyy hh:mm:ss")
    @JsonFormat(pattern = "dd-MM-yyyy hh:mm:ss")
    @Temporal(TemporalType.DATE)
    @UpdateTimestamp
    @Column(name = "updated_date")
    private Date updated_date;

    @Column(name = "begin_season")
    private String beginSeason;

    @Column(name = "end_season")
    private String endSeason;

    @Column(name = "url")
    private String url;

    @Column(name = "draws_hunter_score")
    private String drawsHunterScore;

    @Column(name = "draws_hunter_max_red_run")
    private Integer drawsHunterMaxRedRun;

    @Column(name = "draws_hunter_avg_red_run")
    private Integer drawsHunterAvgRedRun;

    @Column(name = "euro_handicap_score")
    private String euroHandicapScore;

    @Column(name = "euro_handicap_max_red_run")
    private Integer euroHandicapMaxRedRun;

    @Column(name = "euro_handicap_avg_red_run")
    private Integer euroHandicapAvgRedRun;

    @Column(name = "margin_wins_score")
    private String marginWinsScore;

    @Column(name = "margin_wins_max_red_run")
    private Integer marginWinsMaxRedRun;

    @Column(name = "margin_wins_avg_red_run")
    private Integer marginWinsAvgRedRun;

    @Column(name = "flip_flop_score")
    private String flipFlopScore;

    @Column(name = "flip_flop_max_red_run")
    private Integer flipFlopMaxRedRun;

    @Column(name = "flip_flop_avg_red_run")
    private Integer flipFlopAvgRedRun;

    @Column(name = "wins_score")
    private String winsScore;

    @Column(name = "wins_max_red_run")
    private Integer winsMaxRedRun;

    @Column(name = "wins_avg_red_run")
    private Integer winsAvgRedRun;

    @Column(name = "no_wins_score")
    private String noWinsScore;

    @Column(name = "no_wins_max_red_run")
    private Integer noWinsMaxRedRun;

    @Column(name = "no_wins_avg_red_run")
    private Integer noWinsAvgRedRun;

    @Column(name = "clean_sheet_score")
    private String cleanSheetScore;

    @Column(name = "clean_sheet_max_red_run")
    private Integer cleanSheetMaxRedRun;

    @Column(name = "clean_sheet_avg_red_run")
    private Integer cleanSheetAvgRedRun;

    @Column(name = "hockey_margin_wins_any2_score")
    private String hockeyMarginWinsAny2Score;

    @Column(name = "hockey_margin_wins_any2_max_red_run")
    private Integer hockeyMarginWinsAny2MaxRedRun;

    @Column(name = "hockey_margin_wins_any2_avg_red_run")
    private Integer hockeyMarginWinsAny2AvgRedRun;

    @Column(name = "hockey_margin_wins_3_score")
    private String hockeyMarginWins3Score;

    @Column(name = "hockey_margin_wins_3_max_red_run")
    private Integer hockeyMarginWins3MaxRedRun;

    @Column(name = "hockey_margin_wins_3_avg_red_run")
    private Integer hockeyMarginWins3AvgRedRun;

    @Column(name = "goals_fest_score")
    private String goalsFestScore;

    @Column(name = "goals_fest_max_red_run")
    private Integer goalsFestMaxRedRun;

    @Column(name = "goals_fest_avg_red_run")
    private Integer goalsFestAvgRedRun;

    @Column(name = "btts_score")
    private String bttsScore;

    @Column(name = "btts_max_red_run")
    private Integer bttsMaxRedRun;

    @Column(name = "btts_avg_red_run")
    private Integer bttsAvgRedRun;

    @Column(name = "no_btts_score")
    private String noBttsScore;

    @Column(name = "no_btts_max_red_run")
    private Integer noBttsMaxRedRun;

    @Column(name = "no_btts_avg_red_run")
    private Integer noBttsAvgRedRun;

    @Column(name = "score_both_halves_score")
    private String scoreBothHalvesScore;

    @Column(name = "score_both_halves_max_red_run")
    private Integer scoreBothHalvesMaxRedRun;

    @Column(name = "score_both_halves_avg_red_run")
    private Integer scoreBothHalvesAvgRedRun;

    @Column(name = "no_goals_fest_score")
    private String noGoalsFestScore;

    @Column(name = "no_goals_fest_max_red_run")
    private Integer noGoalsFestMaxRedRun;

    @Column(name = "no_goals_fest_avg_red_run")
    private Integer noGoalsFestAvgRedRun;

    @Column(name = "concede_both_halves_score")
    private String concedeBothHalvesScore;

    @Column(name = "concede_both_halves_max_red_run")
    private Integer concedeBothHalvesMaxRedRun;

    @Column(name = "concede_both_halves_avg_red_run")
    private Integer concedeBothHalvesAvgRedRun;

    @Column(name = "win_both_halves_score")
    private String winBothHalvesScore;

    @Column(name = "win_both_halves_max_red_run")
    private Integer winBothHalvesMaxRedRun;

    @Column(name = "win_both_halves_avg_red_run")
    private Integer winBothHalvesAvgRedRun;

    @Column(name = "win_and_goals_score")
    private String winAndGoalsScore;

    @Column(name = "win_and_goals_max_red_run")
    private Integer winAndGoalsMaxRedRun;

    @Column(name = "win_and_goals_avg_red_run")
    private Integer winAndGoalsAvgRedRun;

    @Column(name = "hockey_draws_hunter_score")
    private String hockeyDrawsHunterScore;

    @Column(name = "hockey_draws_max_red_run")
    private Integer hockeyDrawsMaxRedRun;

    @Column(name = "hockey_draws_avg_red_run")
    private Integer hockeyDrawsAvgRedRun;

    @Column(name = "basket_comeback_score")
    private String basketComebackScore;

    @Column(name = "basket_comeback_max_red_run")
    private Integer basketComebackMaxRedRun;

    @Column(name = "basket_comeback_avg_red_run")
    private Integer basketComebackAvgRedRun;

    @Column(name = "basket_short_wins_score")
    private String basketShortWinsScore;

    @Column(name = "basket_short_wins_max_red_run")
    private Integer basketShortWinsMaxRedRun;

    @Column(name = "basket_short_wins_avg_red_run")
    private Integer basketShortWinsAvgRedRun;

    @Column(name = "basket_long_wins_score")
    private String basketLongWinsScore;

    @Column(name = "basket_long_wins_max_red_run")
    private Integer basketLongWinsMaxRedRun;

    @Column(name = "basket_long_wins_avg_red_run")
    private Integer basketLongWinsAvgRedRun;

    @Column(name = "handball_16margin_wins_score")
    private String handball16MarginWinsScore;

    @Column(name = "handball_16margin_wins_max_red_run")
    private Integer handball16MarginWinsMaxRedRun;

    @Column(name = "handball_16margin_wins_avg_red_run")
    private Integer handball16MarginWinsAvgRedRun;

    @Column(name = "handball_49margin_wins_score")
    private String handball49MarginWinsScore;

    @Column(name = "handball_49margin_wins_max_red_run")
    private Integer handball49MarginWinsMaxRedRun;

    @Column(name = "handball_49margin_wins_avg_red_run")
    private Integer handball49MarginWinsAvgRedRun;

    @Column(name = "handball_712margin_wins_score")
    private String handball712MarginWinsScore;

    @Column(name = "handball_712margin_wins_max_red_run")
    private Integer handball712MarginWinsMaxRedRun;

    @Column(name = "handball_712margin_wins_avg_red_run")
    private Integer handball712MarginWinsAvgRedRun;

    @Column(name = "sport")
    private String sport;

}
