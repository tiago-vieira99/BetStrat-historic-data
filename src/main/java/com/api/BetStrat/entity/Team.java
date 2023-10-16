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

    @Column(name = "begin_season")
    private String beginSeason;

    @Column(name = "end_season")
    private String endSeason;

    @Column(name = "url")
    private String url;

    @Column(name = "draws_hunter_score")
    private String drawsHunterScore;

    @Column(name = "euro_handicap_score")
    private String euroHandicapScore;

    @Column(name = "margin_wins_score")
    private String marginWinsScore;

    @Column(name = "margin_wins_any2_score")
    private String marginWinsAny2Score;

    @Column(name = "margin_wins_3_score")
    private String marginWins3Score;

    @Column(name = "goals_fest_score")
    private String goalsFestScore;

    @Column(name = "hockey_draws_hunter_score")
    private String hockeyDrawsHunterScore;

    @Column(name = "basket_comeback_score")
    private String basketComebackScore;

    @Column(name = "basket_short_wins_score")
    private String basketShortWinsScore;

    @Column(name = "handball_16margin_wins_score")
    private String handball16MarginWinsScore;

    @Column(name = "handball_49margin_wins_score")
    private String handball49MarginWinsScore;

    @Column(name = "handball_712margin_wins_score")
    private String handball712MarginWinsScore;

    @Column(name = "sport")
    private String sport;

}
