/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 14, 2014
 * Author(s): lukas
 * *********************************************************
 */
package edu.tigers.sumatra.autoreferee.cases;

import java.util.List;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.autoreferee.RefereeCaseMsg;
import edu.tigers.sumatra.autoreferee.RefereeCaseMsg.EMsgType;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author lukas
 */
public class OutOfBoundsCase extends ARefereeCase
{
	/**
	 * 
	 */
	public OutOfBoundsCase()
	{
		
	}
	
	
	@Override
	protected void checkCase(final MetisAiFrame frame, final List<RefereeCaseMsg> caseMsgs)
	{
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		if (!Geometry.getField().isPointInShape(ballPos))
		{
			// Is the ball outside the goal
			if (Math.abs(ballPos.y()) >= (Geometry.getGoalSize() / 2.0))
			{
				caseMsgs.add(new RefereeCaseMsg(frame.getTacticalField().getBotLastTouchedBall().getTeamColor(),
						EMsgType.OUT_OF_BOUNDS));
			}
		}
	}
}
