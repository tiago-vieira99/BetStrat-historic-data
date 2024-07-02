package com.api.BetStrat.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HistoricMatches",  uniqueConstraints = { @UniqueConstraint(name = "UniqueHistoricMatch", columnNames = { "teamId", "matchDate", "homeTeam", "awayTeam", "season", "competition", "ftResult" }) })
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

    @ApiModelProperty(example = "dd-MM-yyyy hh:mm:ss")
    @JsonFormat(pattern = "dd-MM-yyyy hh:mm:ss")
    @Temporal(TemporalType.DATE)
    @UpdateTimestamp
    @Column(name = "updated_date")
    private Date updated_date;

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

    public static Comparator<HistoricMatch> matchDateComparator = new Comparator<HistoricMatch>() {
        private SimpleDateFormat[] dateFormats = {
            new SimpleDateFormat("dd/MM/yyyy"),
            new SimpleDateFormat("yyyy-MM-dd")
        };

        @Override
        public int compare(HistoricMatch obj1, HistoricMatch obj2) {
            Date date1 = parseDate(obj1.getMatchDate());
            Date date2 = parseDate(obj2.getMatchDate());

            if (date1 != null && date2 != null) {
                return date1.compareTo(date2);
            }

            // Handle cases where parsing fails by treating them as greater
            return 1;
        }

        private Date parseDate(String dateString) {
            for (SimpleDateFormat dateFormat : dateFormats) {
                try {
                    return dateFormat.parse(dateString);
                } catch (ParseException e) {
                    // Parsing failed, try the next format
                }
            }
            return null; // Parsing failed for all formats
        }
    };

}
