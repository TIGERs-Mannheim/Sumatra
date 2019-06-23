/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 15, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.ABaseDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class CircleSkill extends AMoveSkill
{
	/**
	 * @param duration
	 * @param speed
	 */
	public CircleSkill(final float duration, final float speed)
	{
		super(ESkillName.CIRCLE);
		setPathDriver(new CircleDriver(duration, speed));
	}
	
	
	@Override
	public void doCalcActions(final List<ACommand> cmds)
	{
		super.doCalcActions(cmds);
		if (getPathDriver().isDone())
		{
			complete();
		}
	}
	
	private static class CircleDriver extends ABaseDriver
	{
		private final long	tStart	= System.nanoTime();
		private final float	duration;
		private final float	speed;
		
		
		/**
		 * @param duration [s]
		 * @param speed
		 */
		public CircleDriver(final float duration, final float speed)
		{
			this.duration = duration;
			this.speed = speed;
			addSupportedCommand(ECommandType.VEL);
		}
		
		
		@Override
		public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
		{
			throw new IllegalStateException();
		}
		
		
		@Override
		public IVector3 getNextVelocity(final TrackedTigerBot bot, final WorldFrame wFrame)
		{
			float relTime = ((System.nanoTime() - tStart)) / 1e9f / duration;
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
