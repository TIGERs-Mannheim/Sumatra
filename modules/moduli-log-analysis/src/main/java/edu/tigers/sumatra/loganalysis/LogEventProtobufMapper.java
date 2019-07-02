/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis;


import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.loganalysis.eventtypes.ballpossession.BallPossessionEventType;
import edu.tigers.sumatra.loganalysis.eventtypes.dribbling.Dribbling;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.GoalShot;
import edu.tigers.sumatra.loganalysis.eventtypes.shot.Passing;
import edu.tigers.sumatra.labeler.LogLabels;

public class LogEventProtobufMapper
{

    public LogLabels.DribblingLabel mapDribbling(Dribbling dribbling)
    {
        LogLabels.DribblingLabel.Builder dribblingLabelBuilder = LogLabels.DribblingLabel.newBuilder();

        dribblingLabelBuilder.setIsDribbling(dribbling.isDribbling());
        dribblingLabelBuilder.setRobotId(dribbling.getRobot().getBotId().getNumber());
        dribblingLabelBuilder.setTeam(mapTeamColor(dribbling.getRobotTeam()));

        return dribblingLabelBuilder.build();
    }

    public LogLabels.BallPossessionLabel mapBallPossession(BallPossessionEventType ballPossession) throws InconsistentProtobufMapperException
    {
        LogLabels.BallPossessionLabel.Builder ballPossessionLabelBuilder = LogLabels.BallPossessionLabel.newBuilder();

        ballPossessionLabelBuilder.setState(mapColorToState(ballPossession.getPossessionState()));

        boolean bluePosses = ballPossession.getPossessionState() == ETeamColor.BLUE;
        boolean yellowPosses = ballPossession.getPossessionState() == ETeamColor.YELLOW;

        if(bluePosses || yellowPosses)
        {
            if(ballPossession.getRobot().isPresent())
            {
                ballPossessionLabelBuilder.setRobotId(ballPossession.getRobot().get().getBotId().getNumber());
            }
            else
            {
                throw new InconsistentProtobufMapperException("mapBallPossession: blue or yellow team posses the ball, but robot is not given");
            }
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
        passingLabelBuilder.setReceiverId(passing.getReceiverBot().getBotId().getNumber());

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

    public class InconsistentProtobufMapperException extends Exception
    {
        public InconsistentProtobufMapperException(String msg)
        {
            super(msg);
        }
    }
}
