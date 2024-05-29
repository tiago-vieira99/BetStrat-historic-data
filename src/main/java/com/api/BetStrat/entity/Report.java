package com.api.BetStrat.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Report",  uniqueConstraints = { @UniqueConstraint(name = "UniqueReport", columnNames = { "strategy", "season" }) })
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Report implements Serializable {
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

    @ApiModelProperty(example = "dd-MM-yyyy hh:mm:ss")
    @JsonFormat(pattern = "dd-MM-yyyy hh:mm:ss")
    @Temporal(TemporalType.DATE)
    @UpdateTimestamp
    @Column(name = "updated_date")
    private Date updated_date;

    @Column(name = "season")
    private String season;

    @Column(name = "strategy")
    private String strategy;

    @Type(type = "jsonb")
    @Column(name = "report_map", columnDefinition = "jsonb")
    private Map<String, Object> reportMap = new HashMap<>();

}
