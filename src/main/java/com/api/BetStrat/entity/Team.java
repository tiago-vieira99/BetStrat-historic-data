package com.api.BetStrat.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.context.annotation.Primary;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
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
    private Integer drawsHunterScoreMaxRedRun;

    @Column(name = "draws_hunter_avg_red_run")
    private Integer drawsHunterScoreAvgRedRun;

    @Column(name = "euro_handicap_score")
    private String euroHandicapScore;

    @Column(name = "euro_handicap_max_red_run")
    private Integer euroHandicapScoreMaxRedRun;

    @Column(name = "euro_handicap_avg_red_run")
    private Integer euroHandicapScoreAvgRedRun;

    @Column(name = "margin_wins_score")
    private String marginWinsScore;

    @Column(name = "margin_wins_max_red_run")
    private Integer marginWinsScoreMaxRedRun;

    @Column(name = "margin_wins_avg_red_run")
    private Integer marginWinsScoreAvgRedRun;

    @Column(name = "flip_flop_score")
    private String flipFlopScore;

    @Column(name = "flip_flop_max_red_run")
    private Integer flipFlopScoreMaxRedRun;

    @Column(name = "flip_flop_avg_red_run")
    private Integer flipFlopScoreAvgRedRun;

    @Column(name = "wins_score")
    private String winsScore;

    @Column(name = "wins_max_red_run")
    private Integer winsScoreMaxRedRun;

    @Column(name = "wins_avg_red_run")
    private Integer winsScoreAvgRedRun;

    @Column(name = "no_wins_score")
    private String noWinsScore;

    @Column(name = "no_wins_max_red_run")
    private Integer noWinsScoreMaxRedRun;

    @Column(name = "no_wins_avg_red_run")
    private Integer noWinsScoreAvgRedRun;

    @Column(name = "no_draws_score")
    private String noDrawsScore;

    @Column(name = "no_draws_max_red_run")
    private Integer noDrawsScoreMaxRedRun;

    @Column(name = "no_draws_avg_red_run")
    private Integer noDrawsScoreAvgRedRun;

    @Column(name = "no_margin_wins_score")
    private String noMarginWinsScore;

    @Column(name = "no_margin_wins_max_red_run")
    private Integer noMarginWinsScoreMaxRedRun;

    @Column(name = "no_margin_wins_avg_red_run")
    private Integer noMarginWinsScoreAvgRedRun;

    @Column(name = "clean_sheet_score")
    private String cleanSheetScore;

    @Column(name = "clean_sheet_max_red_run")
    private Integer cleanSheetScoreMaxRedRun;

    @Column(name = "clean_sheet_avg_red_run")
    private Integer cleanSheetScoreAvgRedRun;

    @Column(name = "margin_wins_any2_score")
    private String marginWinsAny2Score;

    @Column(name = "margin_wins_any2_max_red_run")
    private Integer marginWinsAny2ScoreMaxRedRun;

    @Column(name = "margin_wins_any2_avg_red_run")
    private Integer marginWinsAny2ScoreAvgRedRun;

    @Column(name = "margin_wins_3_score")
    private String marginWins3Score;

    @Column(name = "margin_wins_3_max_red_run")
    private Integer marginWins3ScoreMaxRedRun;

    @Column(name = "margin_wins_3_avg_red_run")
    private Integer marginWins3ScoreAvgRedRun;

    @Column(name = "goals_fest_score")
    private String goalsFestScore;

    @Column(name = "goals_fest_max_red_run")
    private Integer goalsFestScoreMaxRedRun;

    @Column(name = "goals_fest_avg_red_run")
    private Integer goalsFestScoreAvgRedRun;

    @Column(name = "hockey_draws_hunter_score")
    private String hockeyDrawsHunterScore;

    @Column(name = "hockey_draws_max_red_run")
    private Integer hockeyDrawsScoreMaxRedRun;

    @Column(name = "hockey_draws_avg_red_run")
    private Integer hockeyDrawsScoreAvgRedRun;

    @Column(name = "basket_comeback_score")
    private String basketComebackScore;

    @Column(name = "basket_comeback_max_red_run")
    private Integer basketComebackScoreMaxRedRun;

    @Column(name = "basket_comeback_avg_red_run")
    private Integer basketComebackScoreAvgRedRun;

    @Column(name = "basket_short_wins_score")
    private String basketShortWinsScore;

    @Column(name = "basket_short_wins_max_red_run")
    private Integer basketShortWinsScoreMaxRedRun;

    @Column(name = "basket_short_wins_avg_red_run")
    private Integer basketShortWinsScoreAvgRedRun;

    @Column(name = "basket_long_wins_score")
    private String basketLongWinsScore;

    @Column(name = "basket_long_wins_max_red_run")
    private Integer basketLongWinsScoreMaxRedRun;

    @Column(name = "basket_long_wins_avg_red_run")
    private Integer basketLongWinsScoreAvgRedRun;

    @Column(name = "handball_16margin_wins_score")
    private String handball16MarginWinsScore;

    @Column(name = "handball_16margin_wins_max_red_run")
    private Integer handball16MarginWinsScoreMaxRedRun;

    @Column(name = "handball_16margin_wins_avg_red_run")
    private Integer handball16MarginWinsScoreAvgRedRun;

    @Column(name = "handball_49margin_wins_score")
    private String handball49MarginWinsScore;

    @Column(name = "handball_49margin_wins_max_red_run")
    private Integer handball49MarginWinsScoreMaxRedRun;

    @Column(name = "handball_49margin_wins_avg_red_run")
    private Integer handball49MarginWinsScoreAvgRedRun;

    @Column(name = "handball_712margin_wins_score")
    private String handball712MarginWinsScore;

    @Column(name = "handball_712margin_wins_max_red_run")
    private Integer handball712MarginWinsScoreMaxRedRun;

    @Column(name = "handball_712margin_wins_avg_red_run")
    private Integer handball712MarginWinsScoreAvgRedRun;

    @Column(name = "sport")
    private String sport;

}
