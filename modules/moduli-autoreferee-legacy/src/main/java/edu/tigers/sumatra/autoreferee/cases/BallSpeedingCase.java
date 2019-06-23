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


/**
 * @author lukas
 */
public class BallSpeedingCase extends ARefereeCase
{
	private static final double	MAX_VELOCITY	= 8;	// m/s
																		
																		
	@Override
	protected void checkCase(final MetisAiFrame frame, final List<RefereeCaseMsg> caseMsgs)
	{
		double velocity = frame.getWorldFrame().getBall().getVel().getLength2();
		if (velocity > MAX_VELOCITY)
		{
			RefereeCaseMsg msg = new RefereeCaseMsg(frame.getTacticalField().getBotLastTouchedBall().getTeamColor(),
					EMsgType.BALL_SPEED);
			msg.setAdditionalInfo("Ball Speed = " + velocity);
			caseMsgs.add(msg);
		}
	}
}
