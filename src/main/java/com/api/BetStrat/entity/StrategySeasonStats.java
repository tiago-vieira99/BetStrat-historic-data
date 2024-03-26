package com.api.BetStrat.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@MappedSuperclass
public abstract class StrategySeasonStats implements Serializable {
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

    @ApiModelProperty(example = "dd-MM-yyyy hh:mm:ss")
    @JsonFormat(pattern = "dd-MM-yyyy hh:mm:ss")
    @Temporal(TemporalType.DATE)
    @UpdateTimestamp
    @Column(name = "updated_date")
    private Date updated_date;

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

    @Column(name = "negative_sequence")
    private String negativeSequence;

    @Column(name = "max_seq_scale")
    private int maxSeqScale;

    @SneakyThrows
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public abstract void maxSeqScale();

}
