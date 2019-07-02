/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.dribbling;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.loganalysis.ELogAnalysisShapesLayer;
import edu.tigers.sumatra.loganalysis.eventtypes.IEventTypeDetection;
import edu.tigers.sumatra.loganalysis.eventtypes.TypeDetectionFrame;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

public class DribblingDetection implements IEventTypeDetection<Dribbling>
{

    @Configurable(comment = "max distance between ball and bot kicker to detect dribbling", defValue = "35.0")
    private double dribblingDistance = 35d;

    private boolean dribblingDistanceHysteresis = true;
    private double additionalDistanceForHysteresis = 20d;


    private List<IDrawableShape> dribblingHistoryDraw = new LinkedList<>();

    private boolean lastFrameWasDribbling = false;
    private Dribbling detectedDribbling = null;

    @Override
    public void nextFrameForDetection(TypeDetectionFrame frame)
    {
        ITrackedBot nextBotToBall = frame.getNextBotToBall();
        IVector2 botKicker = nextBotToBall.getBotKickerPos();
        IVector2 ball = frame.getWorldFrameWrapper().getSimpleWorldFrame().getBall().getPos();

        double distanceBallKicker = botKicker.distanceTo(ball);
        double currentDribblingDistance = dribblingDistance;

        if(dribblingDistanceHysteresis && lastFrameWasDribbling)
        {
            currentDribblingDistance += additionalDistanceForHysteresis;
        }

        frame.getShapeMap().get(ELogAnalysisShapesLayer.DRIBBLING).add(new DrawableCircle(Circle.createCircle(botKicker, currentDribblingDistance), Color.GRAY));

        GameState gameState = frame.getWorldFrameWrapper().getGameState();

        if(!gameState.isRunning())
        {
            detectedDribbling = new Dribbling(false, null, ETeamColor.NEUTRAL);
        }
        else if(distanceBallKicker < currentDribblingDistance)
        {
            detectedDribbling = new Dribbling(true, nextBotToBall, nextBotToBall.getTeamColor());
        }
        else
        {
            detectedDribbling = new Dribbling(false, null, ETeamColor.NEUTRAL);
        }

        dribblingHistoryDraw.addAll(detectedDribbling.getDrawableShape());

        // Draw last passes in the visualizer
        for (IDrawableShape dribbling : dribblingHistoryDraw)
        {
            frame.getShapeMap().get(ELogAnalysisShapesLayer.DRIBBLING).add(dribbling);
        }

        lastFrameWasDribbling = detectedDribbling.isDribbling();
    }

    @Override
    public void resetDetection()
    {
        dribblingHistoryDraw.clear();
        lastFrameWasDribbling = false;
    }

    @Override
    public Dribbling getDetectedEventType() {
        return detectedDribbling;
    }


}
