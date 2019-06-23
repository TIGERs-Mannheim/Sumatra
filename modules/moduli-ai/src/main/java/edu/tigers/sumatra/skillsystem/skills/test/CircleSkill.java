/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 15, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills.test;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.driver.ABaseDriver;
import edu.tigers.sumatra.skillsystem.driver.EPathDriver;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CircleSkill extends AMoveSkill
{
	/**
	 * @param duration
	 * @param speed
	 */
	public CircleSkill(final double duration, final double speed)
	{
		super(ESkill.CIRCLE);
		setPathDriver(new CircleDriver(duration, speed));
	}
	
	
	private static class CircleDriver extends ABaseDriver
	{
		private long			tStart	= 0;
		private final double	duration;
		private final double	speed;
									
									
		/**
		 * @param duration [s]
		 * @param speed
		 */
		public CircleDriver(final double duration, final double speed)
		{
			this.duration = duration;
			this.speed = speed;
			addSupportedCommand(EBotSkill.LOCAL_VELOCITY);
		}
		
		
		@Override
		public IVector3 getNextDestination(final ITrackedBot bot, final WorldFrame wFrame)
		{
			throw new IllegalStateException();
		}
		
		
		@Override
		public IVector3 getNextVelocity(final ITrackedBot bot, final WorldFrame wFrame)
		{
			if (tStart == 0)
			{
				tStart = wFrame.getTimestamp();
			}
			double relTime = ((wFrame.getTimestamp() - tStart)) / 1e9 / duration;
			if (relTime > 1)
			{
				setDone(true);
				return AVector3.ZERO_VECTOR;
			}
			IVector2 vel = new Vector2(relTime * AngleMath.PI_TWO).scaleTo(speed);
			return new Vector3(vel, 0);
		}
		
		
		@Override
		public EPathDriver getType()
		{
			return EPathDriver.CIRCLE;
		}
		
	}
}
