package com.api.BetStrat.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "AIInsight",  uniqueConstraints = { @UniqueConstraint(name = "UniqueAIInsight", columnNames = { "match" }) })
public class AIInsight implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private long id;

    @ApiModelProperty(example = "dd-MM-yyyy hh:mm:ss")
    @JsonFormat(pattern = "dd-MM-yyyy hh:mm:ss")
    @Temporal(TemporalType.DATE)
    @CreationTimestamp
    @Column(name = "created_date")
    private Date created_date;

    @OneToOne
    @JoinColumn(name = "match", referencedColumnName = "ID")
    private HistoricMatch match;

    @Type(type = "jsonb")
    @Column(name = "insight", columnDefinition = "jsonb")
    private String insight;

    @Column(name = "isCorrectPrediction")
    private Boolean  isCorrectPrediction;

}
