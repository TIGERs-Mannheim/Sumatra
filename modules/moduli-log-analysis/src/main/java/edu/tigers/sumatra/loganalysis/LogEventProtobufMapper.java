/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis;


import edu.tigers.sumatra.gamelog.proto.LogLabels;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.loganalysis.eventtypes.ballpossession.BallPossessionEventType;
import edu.tigers.sumatra.loganalysis.eventtypes.dribbling.Dribbling;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.GoalShot;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.Passing;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class LogEventProtobufMapper
{

	private static final Logger log = LogManager.getLogger(LogEventProtobufMapper.class.getName());

    public LogLabels.DribblingLabel mapDribbling(Dribbling dribbling)
    {
        LogLabels.DribblingLabel.Builder dribblingLabelBuilder = LogLabels.DribblingLabel.newBuilder();

        dribblingLabelBuilder.setIsDribbling(dribbling.isDribbling());

        if(dribbling.getRobot().isPresent()) {
            dribblingLabelBuilder.setRobotId(dribbling.getRobot().get().getBotId().getNumber());
        }

        LogLabels.Team dribblingTeam = mapTeamColor(dribbling.getRobotTeam());
        if(dribblingTeam != LogLabels.Team.UNRECOGNIZED) {
            dribblingLabelBuilder.setTeam(mapTeamColor(dribbling.getRobotTeam()));
        }

        return dribblingLabelBuilder.build();
    }

    public LogLabels.BallPossessionLabel mapBallPossession(BallPossessionEventType ballPossession)
    {
        LogLabels.BallPossessionLabel.Builder ballPossessionLabelBuilder = LogLabels.BallPossessionLabel.newBuilder();

        ballPossessionLabelBuilder.setState(mapColorToState(ballPossession.getPossessionState()));

        boolean bluePosses = ballPossession.getPossessionState() == ETeamColor.BLUE;
        boolean yellowPosses = ballPossession.getPossessionState() == ETeamColor.YELLOW;

        if((bluePosses || yellowPosses) && !ballPossession.getRobot().isPresent())
        {
            log.error("Inconsistent data: There is a ball possession but no bot given, which is in possession ");
        }

        if(ballPossession.getRobot().isPresent())
        {
            ballPossessionLabelBuilder.setRobotId(ballPossession.getRobot().get().getBotId().getNumber());
        }

        return ballPossessionLabelBuilder.build();
    }

    public LogLabels.PassingLabel mapPassing(Passing passing)
    {
        LogLabels.PassingLabel.Builder passingLabelBuilder = LogLabels.PassingLabel.newBuilder();

        passingLabelBuilder.setStartFrame(passing.getStartTimestamp());
        passingLabelBuilder.setEndFrame(passing.getEndTimestamp());
        passingLabelBuilder.setSuccessful(passing.isSuccessful());
        passingLabelBuilder.setPasserId(passing.getPasserBot().getBotId().getNumber());
        passingLabelBuilder.setPasserTeam(mapTeamColor(passing.getPasserBot().getTeamColor()));

        Optional<ITrackedBot> receiverBot = passing.getReceiverBot();
        if(receiverBot.isPresent()) {
            passingLabelBuilder.setReceiverId(receiverBot.get().getBotId().getNumber());
        }

        return passingLabelBuilder.build();
    }

    public LogLabels.GoalShotLabel mapGoalShot(GoalShot goalShot)
    {
        LogLabels.GoalShotLabel.Builder goalShotLabelBuilder = LogLabels.GoalShotLabel.newBuilder();

        goalShotLabelBuilder.setStartFrame(goalShot.getStartTimestamp());
        goalShotLabelBuilder.setEndFrame(goalShot.getEndTimestamp());
        goalShotLabelBuilder.setSuccessful(goalShot.isSuccessful());
        goalShotLabelBuilder.setShooterId(goalShot.getShooterBot().getBotId().getNumber());
        goalShotLabelBuilder.setShooterTeam(mapTeamColor(goalShot.getShooterBot().getTeamColor()));

        return goalShotLabelBuilder.build();
    }

    private LogLabels.BallPossessionLabel.State mapColorToState(ETeamColor teamColor)
    {
        switch (teamColor)
        {
            case YELLOW:
                return LogLabels.BallPossessionLabel.State.YELLOW_POSSES;
            case BLUE:
                return LogLabels.BallPossessionLabel.State.BLUE_POSSES;
            default:
            case NEUTRAL:
                return LogLabels.BallPossessionLabel.State.NONE;
        }
    }

    private LogLabels.Team mapTeamColor(ETeamColor teamColor)
    {
        switch (teamColor)
        {
            case YELLOW:
                return LogLabels.Team.YELLOW;
            case BLUE:
                return LogLabels.Team.BLUE;
            default:
            case NEUTRAL:
                return LogLabels.Team.UNRECOGNIZED;
        }
    }
}
