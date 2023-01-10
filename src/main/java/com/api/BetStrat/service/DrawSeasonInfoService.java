package com.api.BetStrat.service;

import com.api.BetStrat.entity.DrawSeasonInfo;
import com.api.BetStrat.entity.Team;
import com.api.BetStrat.exception.ForbiddenException;
import com.api.BetStrat.exception.NotFoundException;
import com.api.BetStrat.repository.DrawSeasonInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DrawSeasonInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrawSeasonInfoService.class);

    @Autowired
    private DrawSeasonInfoRepository drawSeasonInfoRepository;

    public DrawSeasonInfo insertDrawInfo(DrawSeasonInfo drawSeasonInfo) {
        return drawSeasonInfoRepository.save(drawSeasonInfo);
    }

}
