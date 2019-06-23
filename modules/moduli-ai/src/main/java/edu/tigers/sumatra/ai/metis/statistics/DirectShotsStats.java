/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import static edu.tigers.sumatra.ai.data.MatchStats.EMatchStatistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.statistics.Percentage;
import edu.tigers.sumatra.vision.data.IKickEvent;

import edu.tigers.sumatra.ai.data.EPossibleGoal;
import edu.tigers.sumatra.ai.data.MatchStats;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ids.BotID;


/**
 * Statistics for direct Shots to goal
 *
 * @author Stefan Schneyer
 */
public class DirectShotsStats extends AStats
{
    //Number of Direct Shots
    private final Map<BotID, Integer> directBotShots = new HashMap<>();

    //Success Percentage for direct Shots
    private final Percentage successGeneral = new Percentage(0,0);
    private final Map<BotID, Percentage> successPerBot = new HashMap<>();

    //Buffer Variables for detection of direct shots
    private boolean lastIsDirectShot = false;
    Optional<BotID> shooter = Optional.empty();

    @Configurable(comment = "maximalDetectionDistanceToGoal", defValue = "9000")
    private double maximalDetectionDistanceToGoal = 9000;

    static
    {
        ConfigRegistration.registerClass("DirectShotsStats", DirectShotsStats.class);
    }

    @Override
    public void saveStatsToMatchStatistics(final MatchStats matchStatistics)
    {
        //Direct Shots
        int directShotsSum = successPerBot.values().stream().mapToInt(Percentage::getAll).sum();
        StatisticData directShotTigersStats = new StatisticData(directBotShots, directShotsSum);
        matchStatistics.putStatisticData(EMatchStatistics.DIRECT_SHOTS, directShotTigersStats);

        //Direct Shots Success rate
        StatisticData ballPossessionTigers = new StatisticData(successPerBot, successGeneral);
        matchStatistics.putStatisticData(EMatchStatistics.DIRECT_SHOTS_SUCCESS_RATE, ballPossessionTigers);
    }

    @Override
    public void onStatisticUpdate(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
    {
        Optional<IKickEvent> directShot = newTacticalField.getDirectShot();
        boolean isDirectShot = directShot.isPresent();

        final IVector2 ballPosition = baseAiFrame.getWorldFrame().getBall().getPos();

        if(Geometry.getGoalTheir().getCenter().distanceToSqr(ballPosition) <= Math.pow(maximalDetectionDistanceToGoal, 2)) {
            if (!lastIsDirectShot && isDirectShot)
                directShotDetected(directShot.get().getKickingBot());
            else
                watchingForGoal(newTacticalField);
        }

        lastIsDirectShot = isDirectShot;
    }

    private void directShotDetected(BotID shooter)
    {
        successPerBot.computeIfAbsent(shooter, sb -> new Percentage(0,0)).incAll();
        successGeneral.incAll();

        directBotShots.putIfAbsent(shooter, 0);
        directBotShots.compute(shooter, (id, shot) -> shot + 1);

        this.shooter = Optional.of(shooter);
    }

    private void watchingForGoal(final TacticalField newTacticalField)
    {
        if(this.shooter.isPresent() && newTacticalField.getPossibleGoal() == EPossibleGoal.WE)
        {
            successPerBot.computeIfAbsent(this.shooter.get(), sb -> new Percentage(0,0)).inc();
            this.shooter = Optional.empty();

            successGeneral.inc();
        }
        if(newTacticalField.getKicking().isPresent())
            this.shooter = Optional.empty();
    }
}
