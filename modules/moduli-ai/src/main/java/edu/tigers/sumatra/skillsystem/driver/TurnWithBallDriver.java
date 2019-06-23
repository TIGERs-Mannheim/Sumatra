/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 20, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TurnWithBallDriver extends PositionDriver implements IKickPathDriver
{
	
	private final DynamicPosition	receiver;
	private boolean					shoot						= false;
	
	@Configurable
	private static double			orthDirTurnPercent	= 1;
	@Configurable
	private static double			orientAheadPercent	= 0.5;
	@Configurable
	private static double			destDist					= 100;
	
	
	private double						lastRotation			= 0;
	
	
	static
	{
		ConfigRegistration.registerClass("skills", TurnWithBallDriver.class);
	}
	
	
	/**
	 * @param receiver
	 */
	public TurnWithBallDriver(final DynamicPosition receiver)
	{
		this.receiver = receiver;
		
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.TURN_WITH_BALL;
	}
	
	
	@Override
	public boolean isEnableDribbler()
	{
		return true;
	}
	
	
	@Override
	public void update(final ITrackedBot bot, final ABot aBot, final WorldFrame wFrame)
	{
		double dist2Ball = GeoMath.distancePP(wFrame.getBall().getPos(), bot.getPos());
		if (dist2Ball > 200)
		{
			setDone(true);
		} else
		{
			IVector2 isDir = wFrame.getBall().getPos().subtractNew(bot.getPos())
					.scaleToNew(bot.getBot().getCenter2DribblerDist());
			IVector2 targetDir = receiver.subtractNew(wFrame.getBall().getPos());
			
			double rotation = AngleMath.getShortestRotation(isDir.getAngle(), targetDir.getAngle());
			if ((Math.abs(rotation) < 0.05)
					|| ((lastRotation != 0) && (Math.signum(lastRotation) != Math.signum(rotation))))
			{
				shoot = true;
			} else
			{
				double rel = Math.signum(rotation)
						* Math.min(1, (Math.abs(rotation) * orthDirTurnPercent) / AngleMath.PI_HALF);
				IVector2 orthDir = isDir.turnNew(AngleMath.PI_HALF * -rel).normalize();
				IVector2 dest = bot.getPos().addNew(orthDir.multiplyNew((destDist * Math.abs(rotation))))
						.add(targetDir.scaleToNew(50));
				double orient = isDir.getAngle() + (rotation * orientAheadPercent);
				setDestination(dest);
				setOrientation(orient);
			}
			lastRotation = rotation;
		}
	}
	
	
	@Override
	public boolean armKicker()
	{
		return shoot;
	}
}
