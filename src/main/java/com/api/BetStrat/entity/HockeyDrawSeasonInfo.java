package com.api.BetStrat.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HockeyDrawSeasonInfo", uniqueConstraints = { @UniqueConstraint(name = "UniqueSeasonAndCompetitionForTeam", columnNames = { "teamID", "season", "competition" }) })
public class HockeyDrawSeasonInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teamID", referencedColumnName = "ID")
    private Team teamId;

    @ApiModelProperty(example = "dd-MM-yyyy hh:mm:ss")
    @JsonFormat(pattern = "dd-MM-yyyy hh:mm:ss")
    @Temporal(TemporalType.DATE)
    @CreationTimestamp
    @Column(name = "created_date")
    private Date created_date;

    @Column(name = "description")
    private String description;

    @Column(name = "season")
    private String season;

    @Column(name = "competition")
    private String competition;

    @Column(name = "url")
    private String url;

    @Column(name = "num_matches")
    private int numMatches;

    @Column(name = "standard_deviation")
    private double stdDeviation;

    @Column(name = "coefficient_deviation")
    private double coefDeviation;

    @Column(name = "drawRate")
    private double drawRate;

    @Column(name = "no_draws_sequence")
    private String noDrawsSequence;

    @Column(name = "num_draws")
    private int numDraws;

    @SneakyThrows
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
