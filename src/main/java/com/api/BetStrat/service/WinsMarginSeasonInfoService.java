package com.api.BetStrat.service;

import com.api.BetStrat.entity.DrawSeasonInfo;
import com.api.BetStrat.entity.WinsMarginSeasonInfo;
import com.api.BetStrat.repository.DrawSeasonInfoRepository;
import com.api.BetStrat.repository.WinsMarginSeasonInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WinsMarginSeasonInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinsMarginSeasonInfoService.class);

    @Autowired
    private WinsMarginSeasonInfoRepository winsMarginSeasonInfoRepository;

    public WinsMarginSeasonInfo insertWinsMarginInfo(WinsMarginSeasonInfo winsMarginSeasonInfo) {
        return winsMarginSeasonInfoRepository.save(winsMarginSeasonInfo);
    }

}
