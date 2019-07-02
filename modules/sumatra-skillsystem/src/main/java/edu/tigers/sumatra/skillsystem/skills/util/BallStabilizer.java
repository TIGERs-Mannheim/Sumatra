/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;


public class BallStabilizer
{
	private static final double ON_CAM_HORIZON = 0.3;
	
	private ITrackedBall ball;
	private ITrackedBot bot;
	private double botBrakeAcc = 0;
	
	private IVector2 ballPos;
	private IVector2 ballDir;
	
	
	public void update(final ITrackedBall ball, final ITrackedBot bot)
	{
		this.ball = ball;
		this.bot = bot;
		
		if (ballPos != null && bot.hadBallContact(1.0))
		{
			ballPos = bot.getBotShape().withMargin(Geometry.getBallRadius()).getKickerLine()
					.closestPointOnLine(ballPos);
		} else if (ballPos == null || ball.isOnCam(ON_CAM_HORIZON))
		{
			ballPos = ball.getPos();
			ballDir = ball.getVel().normalizeNew();
		} else
		{
			ballPos = bot.getBotShape().withMargin(Geometry.getBallRadius()).nearestPointOutside(ballPos);
		}
		
		if (botBrakeAcc <= 0)
		{
			botBrakeAcc = bot.getMoveConstraints().getAccMax();
		}
	}
	
	
	public void setBotBrakeAcc(final double botBrakeAcc)
	{
		this.botBrakeAcc = botBrakeAcc;
	}
	
	
	public IVector2 getBallPos()
	{
		return ballPos;
	}
	
	
	public IVector2 getBallPos(final double lookahead)
	{
		if (ball.isOnCam(ON_CAM_HORIZON) && !bot.hasBallContact())
		{
			return ball.getTrajectory().getPosByTime(lookahead).getXYVector();
		}
		double botVel = bot.getVel().getLength2();
		double brake = bot.getMoveConstraints().getAccMax() * lookahead;
		double avgVel = Math.max(0, botVel - brake / 2);
		return ballPos.addNew(ballDir.multiplyNew(avgVel * lookahead * 1000));
	}
}
