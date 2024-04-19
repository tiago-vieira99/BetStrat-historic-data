package com.api.BetStrat.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class SimulatedMatchDto {

    private String homeTeam;

    private String awayTeam;

    private String season;

    private String matchDate;

    private String ftResult;

    private String htResult;

    private String competition;

    private String matchNumber;

    private Boolean isGreen;

}
