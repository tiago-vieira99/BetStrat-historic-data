package com.api.BetStrat.service;

import static com.api.BetStrat.enums.StrategyDurationScaleEnum.LONG;
import static com.api.BetStrat.enums.StrategyDurationScaleEnum.MEDIUM;
import static com.api.BetStrat.enums.StrategyDurationScaleEnum.MEDIUM_LONG;
import static com.api.BetStrat.enums.StrategyDurationScaleEnum.MEDIUM_SHORT;
import static com.api.BetStrat.enums.StrategyDurationScaleEnum.SHORT;

import com.api.BetStrat.entity.StrategySeasonStats;
import com.api.BetStrat.enums.TeamScoreEnum;
import com.api.BetStrat.util.Utils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class StrategyScoreCalculator<T extends StrategySeasonStats> {

    public String calculateFinalRating(double score) {
        if (isBetween(score,85,150)) {
            return TeamScoreEnum.EXCELLENT.getValue() + " (" + score + ")";
        } else if(isBetween(score,70,85)) {
            return TeamScoreEnum.ACCEPTABLE.getValue() + " (" + score + ")";
        } else if(isBetween(score,50,70)) {
            return TeamScoreEnum.RISKY.getValue() + " (" + score + ")";
        } else if(isBetween(score,0,50)) {
            return TeamScoreEnum.INAPT.getValue() + " (" + score + ")";
        }
        return "";
    }

    public abstract int calculateHistoricMaxNegativeSeq(List<T> statsByTeam);

    public abstract double calculateHistoricAvgNegativeSeq(List<T> statsByTeam);

    public abstract int calculateLast3SeasonsRateScore(List<T> statsByTeam);

    public abstract int calculateAllSeasonsRateScore(List<T> statsByTeam);

    public abstract int calculateLast3SeasonsTotalWinsRateScore(List<T> statsByTeam);

    public abstract int calculateAllSeasonsTotalWinsRateScore(List<T> statsByTeam);

    public int calculateLast3SeasonsMaxSeqWOGreenScore(List<T> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<3; i++) {
            String sequenceStr = statsByTeam.get(i).getNegativeSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }

        int maxSeqStrategyScale = statsByTeam.get(0).getMaxSeqScale();

        if (maxSeqStrategyScale == SHORT.getValue()) {
            if (isBetween(maxValue,0,3)) {
                return 100;
            } else if(isBetween(maxValue,3,4)) {
                return 90;
            } else if(isBetween(maxValue,4,6)) {
                return 80;
            } else if(isBetween(maxValue,6,7)) {
                return 50;
            } else if(isBetween(maxValue,7,50)) {
                return 30;
            }
        } else if (maxSeqStrategyScale == MEDIUM_SHORT.getValue()) {
            if (isBetween(maxValue,0,4)) {
                return 100;
            } else if(isBetween(maxValue,4,5)) {
                return 90;
            } else if(isBetween(maxValue,5,7)) {
                return 80;
            } else if(isBetween(maxValue,7,8)) {
                return 50;
            } else if(isBetween(maxValue,8,50)) {
                return 30;
            }
        } else if (maxSeqStrategyScale == MEDIUM.getValue()) {
            if (isBetween(maxValue,0,5)) {
                return 100;
            } else if(isBetween(maxValue,5,6)) {
                return 90;
            } else if(isBetween(maxValue,6,8)) {
                return 80;
            } else if(isBetween(maxValue,8,9)) {
                return 50;
            } else if(isBetween(maxValue,9,50)) {
                return 30;
            }
        } else if (maxSeqStrategyScale == MEDIUM_LONG.getValue()) {
            if (isBetween(maxValue,0,6)) {
                return 100;
            } else if(isBetween(maxValue,6,7)) {
                return 90;
            } else if(isBetween(maxValue,7,9)) {
                return 80;
            } else if(isBetween(maxValue,9,10)) {
                return 50;
            } else if(isBetween(maxValue,10,50)) {
                return 30;
            }
        } else if (maxSeqStrategyScale == LONG.getValue()) {
            if (isBetween(maxValue,0,7)) {
                return 100;
            } else if(isBetween(maxValue,7,8)) {
                return 90;
            } else if(isBetween(maxValue,8,10)) {
                return 80;
            } else if(isBetween(maxValue,10,12)) {
                return 50;
            } else if(isBetween(maxValue,12,50)) {
                return 30;
            }
        } else {
            return -1;
        }
        return -1;
    }

    public int calculateAllSeasonsMaxSeqWOGreenScore(List<T> statsByTeam) {
        int maxValue = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            String sequenceStr = statsByTeam.get(i).getNegativeSequence().replaceAll("[\\[\\]\\s]", "");
            List<Integer> sequenceList = Arrays.asList(sequenceStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toList());
            if (Collections.max(sequenceList) > maxValue) {
                maxValue = Collections.max(sequenceList);
            }
        }

        int maxSeqStrategyScale = statsByTeam.get(0).getMaxSeqScale();

        if (maxSeqStrategyScale == SHORT.getValue()) {
            if (isBetween(maxValue,0,3)) {
                return 100;
            } else if(isBetween(maxValue,3,4)) {
                return 90;
            } else if(isBetween(maxValue,4,6)) {
                return 80;
            } else if(isBetween(maxValue,6,7)) {
                return 50;
            } else if(isBetween(maxValue,7,50)) {
                return 30;
            }
        } else if (maxSeqStrategyScale == MEDIUM_SHORT.getValue()) {
            if (isBetween(maxValue,0,4)) {
                return 100;
            } else if(isBetween(maxValue,4,5)) {
                return 90;
            } else if(isBetween(maxValue,5,7)) {
                return 80;
            } else if(isBetween(maxValue,7,8)) {
                return 50;
            } else if(isBetween(maxValue,8,50)) {
                return 30;
            }
        } else if (maxSeqStrategyScale == MEDIUM.getValue()) {
            if (isBetween(maxValue,0,5)) {
                return 100;
            } else if(isBetween(maxValue,5,6)) {
                return 90;
            } else if(isBetween(maxValue,6,8)) {
                return 80;
            } else if(isBetween(maxValue,8,9)) {
                return 50;
            } else if(isBetween(maxValue,9,50)) {
                return 30;
            }
        } else if (maxSeqStrategyScale == MEDIUM_LONG.getValue()) {
            if (isBetween(maxValue,0,6)) {
                return 100;
            } else if(isBetween(maxValue,6,7)) {
                return 90;
            } else if(isBetween(maxValue,7,9)) {
                return 80;
            } else if(isBetween(maxValue,9,10)) {
                return 50;
            } else if(isBetween(maxValue,10,50)) {
                return 30;
            }
        } else if (maxSeqStrategyScale == LONG.getValue()) {
            if (isBetween(maxValue,0,7)) {
                return 100;
            } else if(isBetween(maxValue,7,8)) {
                return 90;
            } else if(isBetween(maxValue,8,10)) {
                return 80;
            } else if(isBetween(maxValue,10,12)) {
                return 50;
            } else if(isBetween(maxValue,12,50)) {
                return 30;
            }
        } else {
            return -1;
        }
        return -1;
    }

    // standard deviation tells me how regular is the team hitting the strategy outcome. The lowest value, the best
    public int calculateLast3SeasonsStdDevScore(List<T> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<3; i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/3);

        if (isBetween(avgStdDev,0,1.7)) {
            return 100;
        } else if(isBetween(avgStdDev,1.7,2.0)) {
            return 80;
        } else if(isBetween(avgStdDev,2.0,2.2)) {
            return 70;
        } else if(isBetween(avgStdDev,2.2,2.4)) {
            return 50;
        } else if(isBetween(avgStdDev,2.4,25)) {
            return 30;
        }
        return 0;
    }

    public int calculateAllSeasonsStdDevScore(List<T> statsByTeam) {
        double sumStdDev = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumStdDev += statsByTeam.get(i).getStdDeviation();
        }

        double avgStdDev = Utils.beautifyDoubleValue(sumStdDev/statsByTeam.size());

        if (isBetween(avgStdDev,0,1.7)) {
            return 100;
        } else if(isBetween(avgStdDev,1.7,2.0)) {
            return 80;
        } else if(isBetween(avgStdDev,2.0,2.2)) {
            return 70;
        } else if(isBetween(avgStdDev,2.2,2.4)) {
            return 50;
        } else if(isBetween(avgStdDev,2.4,25)) {
            return 30;
        }
        return 0;
    }

    // coefficient deviation tells me how high are the values of the negative sequence. The lowest, the best
    public int calculateLast3SeasonsCoefDevScore(List<T> statsByTeam) {
        double sumCoefDev = 0;
        for (int i=0; i<3; i++) {
            sumCoefDev += statsByTeam.get(i).getCoefDeviation();
        }

        double avgCoefDev = Utils.beautifyDoubleValue(sumCoefDev/3);

        if (isBetween(avgCoefDev,0,70)) {
            return 100;
        } else if(isBetween(avgCoefDev,70,80)) {
            return 80;
        } else if(isBetween(avgCoefDev,80,100)) {
            return 70;
        } else if(isBetween(avgCoefDev,100,140)) {
            return 50;
        } else if(isBetween(avgCoefDev,140,9999)) {
            return 30;
        }
        return 0;
    }

    public int calculateAllSeasonsCoefDevScore(List<T> statsByTeam) {
        double sumCoefDev = 0;
        for (int i=0; i<statsByTeam.size(); i++) {
            sumCoefDev += statsByTeam.get(i).getCoefDeviation();
        }

        double avgCoefDev = Utils.beautifyDoubleValue(sumCoefDev/statsByTeam.size());

        if (isBetween(avgCoefDev,0,70)) {
            return 100;
        } else if(isBetween(avgCoefDev,70,80)) {
            return 80;
        } else if(isBetween(avgCoefDev,80,100)) {
            return 70;
        } else if(isBetween(avgCoefDev,100,140)) {
            return 50;
        } else if(isBetween(avgCoefDev,140,9999)) {
            return 30;
        }
        return 0;
    }

    public int calculateLeagueMatchesScore(int totalMatches) {
        if (isBetween(totalMatches,0,31)) {
            return 100;
        } else if(isBetween(totalMatches,31,33)) {
            return 90;
        } else if(isBetween(totalMatches,33,35)) {
            return 80;
        } else if(isBetween(totalMatches,35,41)) {
            return 60;
        } else if(isBetween(totalMatches,41,50)) {
            return 50;
        } else if (isBetween(totalMatches, 50, 100)) {
            return 30;
        }
        return 0;
    }

    public static boolean isBetween(double x, double lower, double upper) {
        return lower <= x && x < upper;
    }

}
