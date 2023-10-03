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
@Table(name = "HistoricMatches",  uniqueConstraints = { @UniqueConstraint(name = "UniqueHistoricMatch", columnNames = { "teamId", "homeTeam", "awayTeam", "season", "competition", "ftResult" }) })
public class HistoricMatch implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
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

    @Column(name = "homeTeam")
    private String homeTeam;

    @Column(name = "awayTeam")
    private String awayTeam;

    @Column(name = "season")
    private String season;

    @Column(name = "matchDate")
    private String matchDate;

    @Column(name = "ftResult")
    private String ftResult;

    @Column(name = "htResult")
    private String htResult;

    @Column(name = "competition")
    private String competition;

    @Column(name = "sport")
    private String sport;

    @SneakyThrows
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

}
