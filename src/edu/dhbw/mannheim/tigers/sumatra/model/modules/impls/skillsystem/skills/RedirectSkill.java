/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 9, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test.PositionSkill;
import edu.dhbw.mannheim.tigers.sumatra.util.DebugShapeHacker;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Prepare for redirect
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectSkill extends PositionSkill
{
	private DynamicPosition	target;
	
	@Configurable
	private static float		shootSpeed				= 4.0f;
	
	private IVector2			desiredDestination	= null;
	private int					desiredDuration		= 0;
	private IVector2			initBallPos				= null;
	
	
	/**
	 * @param target
	 */
	public RedirectSkill(final DynamicPosition target)
	{
		super(ESkillName.REDIRECT);
		this.target = target;
	}
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		if (getWorldFrame().getBall().getVel().getLength2() > 0.2f)
		{
			float dist2BallTravelLine = GeoMath.distancePL(initBallPos, new Line(getWorldFrame().getBall().getPos(),
					getWorldFrame().getBall().getVel()));
			if (dist2BallTravelLine > 500)
			{
				initBallPos = getWorldFrame().getBall().getPos();
			}
		}
		
		final IVector2 dest;
		if (desiredDestination == null)
		{
			dest = getPos();
			desiredDestination = dest;
		}
		else
		{
			dest = desiredDestination;
		}
		target.update(getWorldFrame());
		final float angle;
		if (target.equals(dest, 1f))
		{
			angle = getAngle();
		} else
		{
			angle = target.subtractNew(dest).getAngle();
		}
		
		float kickSpeed = 8.0f;
		if (desiredDuration == 0)
		{
			getDevices().kickGeneralSpeed(cmds, EKickerMode.ARM, EKickerDevice.STRAIGHT, kickSpeed, 0);
		} else
		{
			kickSpeed = getDevices().calcKickSpeed(desiredDuration);
			getDevices().kickGeneralDuration(cmds, EKickerMode.ARM, EKickerDevice.STRAIGHT, desiredDuration, 0);
		}
		
		IVector3 poss = AiMath.calcRedirectPositions(getTBot(), dest, angle, getWorldFrame().getBall(), target,
				kickSpeed);
		setDestination(poss.getXYVector());
		setOrientation(poss.z());
		
		DebugShapeHacker.addDebugShape(new DrawableLine(Line.newLine(getPos(), target), Color.blue));
		DebugShapeHacker.addDebugShape(new DrawableLine(new Line(getPos(), new Vector2(poss.z()).scaleTo(200)),
				Color.red));
		if (getWorldFrame().getBall().getVel().getLength2() > 0.1f)
		{
			DebugShapeHacker.addDebugShape(new DrawableLine(new Line(getWorldFrame().getBall().getPos(), getWorldFrame()
					.getBall().getVel().scaleToNew(2000)),
					Color.magenta));
		}
		
		if (!initBallPos.equals(getWorldFrame().getBall().getPos()))
		{
			DebugShapeHacker.addDebugShape(new DrawableLine(Line.newLine(initBallPos, getWorldFrame().getBall().getPos()),
					Color.yellow));
		}
	}
	
	
	@Override
	public void doCalcEntryActions(final List<ACommand> cmds)
	{
		initBallPos = getWorldFrame().getBall().getPos();
	}
	
	
	/**
	 * @param desiredDestination the desiredDestination to set
	 */
	public final void setDesiredDestination(final IVector2 desiredDestination)
	{
		this.desiredDestination = desiredDestination;
	}
	
	
	/**
	 * @param duration
	 */
	public final void setDesiredDuration(final int duration)
	{
		desiredDuration = duration;
	}
	
	
	/**
	 * @return the target
	 */
	public final DynamicPosition getTarget()
	{
		return target;
	}
	
	
	/**
	 * @param target the target to set
	 */
	public final void setTarget(final DynamicPosition target)
	{
		this.target = target;
	}
}
