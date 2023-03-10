package com.api.BetStrat.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.context.annotation.Primary;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Teams", uniqueConstraints={@UniqueConstraint(columnNames={"name"})})
public class Team implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private long id;

    @Column(name = "name")
    private String name;

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

    @Column(name = "hockey_draws_hunter_score")
    private String hockeyDrawsHunterScore;

    @Column(name = "sport")
    private String sport;

    @SneakyThrows
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

}
