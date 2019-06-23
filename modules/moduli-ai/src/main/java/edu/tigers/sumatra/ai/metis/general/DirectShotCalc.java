/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.WorldFrame;

import java.util.Optional;


/**
 * This calculator detects directly shots to goal
 *
 * @author Stefan Schneyer
 */
public class DirectShotCalc extends ACalculator
{

    // --------------------------------------------------------------------------
    // --- method(s) ------------------------------------------------------------
    // --------------------------------------------------------------------------

    @Override
    public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
    {
        Optional<IKickEvent> kickEvent = newTacticalField.getKicking();
        if(kickEvent.isPresent())
        {
            Goal directShotGoal = Geometry.getGoalTheir();

            WorldFrame wFrame = baseAiFrame.getWorldFrame();
            final IVector2 ballPosition = wFrame.getBall().getPos();
            final IVector2 ballVelocity = wFrame.getBall().getVel();

            ILine ballLine = Line.fromDirection(ballPosition, ballVelocity);
            ILine goalLine = directShotGoal.getLine();
            Optional<IVector2> intersection = ballLine.intersectionWith(goalLine);

            if(intersection.isPresent())
            {
                boolean isDirectShot = directShotGoal.getLine().isPointOnLineSegment(intersection.get()) &&
                        ballLine.getStart().distanceToSqr(intersection.get()) > ballLine.getEnd().distanceToSqr(intersection.get()) &&
                        Geometry.getField().isPointInShape(ballPosition) &&
                        newTacticalField.getBotLastTouchedBall().getTeamColor() == baseAiFrame.getTeamColor();
                if(isDirectShot) {
                    newTacticalField.setDirectShot(kickEvent);
                }
            }
        }
    }
}
