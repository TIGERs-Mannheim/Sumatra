/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 14, 2014
 * Author(s): lukas
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.cases;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee.RefereeCaseMsg.EMsgType;


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
		if (!AIConfig.getGeometry().getField().isPointInShape(ballPos))
		{
			// Is the ball outside the goal
			if (Math.abs(ballPos.y()) >= (AIConfig.getGeometry().getGoalSize() / 2))
			{
				caseMsgs.add(new RefereeCaseMsg(frame.getTacticalField().getBotLastTouchedBall().getTeamColor(),
						EMsgType.OUT_OF_BOUNDS));
			}
		}
	}
}
