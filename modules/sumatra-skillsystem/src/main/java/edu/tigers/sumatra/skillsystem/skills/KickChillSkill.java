/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.AroundBallCalc;
import edu.tigers.sumatra.skillsystem.skills.util.MinMarginChargeValue;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickChillSkill extends AKickSkill
{
	
	@Configurable(comment = "Speed in chill mode")
	private static double chillVel = 1;
	
	@Configurable(comment = "Acceleration in chill model")
	private static double chillAcc = 2;
	
	
	@Configurable
	private static double maxAroundBallMargin = 120;
	
	
	/**
	 * @param receiver
	 * @param kickMode
	 * @param kickerDevice
	 * @param kickSpeed
	 */
	public KickChillSkill(final DynamicPosition receiver, EKickMode kickMode, EKickerDevice kickerDevice,
			double kickSpeed)
	{
		this(receiver);
		setKickSpeed(kickSpeed);
		setKickMode(kickMode);
		setDevice(kickerDevice);
	}
	
	
	/**
	 * @param receiver
	 */
	public KickChillSkill(final DynamicPosition receiver)
	{
		super(ESkill.KICK_CHILL, receiver);
		
		setInitialState(new KickState());
	}
	
	
	/**
	 * @param receiver
	 * @param skill
	 */
	public KickChillSkill(final DynamicPosition receiver, ESkill skill)
	{
		super(skill, receiver);
	}
	
	
	@Override
	protected void updateKickerParams(final KickerParams kickerParams)
	{
		super.updateKickerParams(kickerParams);
		
		if (!isReadyAndFocussed())
		{
			kickerParams.setMode(EKickerMode.DISARM);
		}
	}
	
	
	protected class KickState extends MoveToState
	{
		private double dist2Ball = 0;
		
		
		protected KickState()
		{
			super(KickChillSkill.this);
		}
		
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			dist2Ball = 20;
			getMoveCon().getMoveConstraints().setVelMax(chillVel);
			getMoveCon().getMoveConstraints().setAccMax(chillAcc);
			getMoveCon().setBallObstacle(false);
			getMoveCon().setIgnoreGameStateObstacles(true);
			
			minMarginChargeValue = MinMarginChargeValue.aMinMargin()
					.withDefaultValue(10)
					.withChargeRate(-50)
					.withLowerThreshold(50)
					.withUpperThreshold(70)
					.withLimit(-100)
					.build();
		}
		
		
		@Override
		public void doUpdate()
		{
			double curBot2BallDist = getBall().getPos().distanceTo(getPos());
			getMoveCon().setBotsObstacle(curBot2BallDist > 1000);
			IVector2 dest = AroundBallCalc
					.aroundBall()
					.withBallPos(getBall().getPos())
					.withTBot(getTBot())
					.withDestination(getDestination(0))
					.withMaxMargin(maxAroundBallMargin)
					.withMinMargin(dist2Ball)
					.build()
					.getAroundBallDest();
			dist2Ball = getMinMargin(dest);
			
			getMoveCon().updateDestination(dest);
			double targetOrientation = getTargetOrientation();
			getMoveCon().updateTargetAngle(targetOrientation);
			super.doUpdate();
			
			getShapes().get(ESkillShapesLayer.KICK_SKILL_DEBUG).add(new DrawableLine(
					Line.fromDirection(dest, Vector2.fromAngle(targetOrientation).scaleTo(5000)), Color.RED));
		}
		
		
		private IVector2 getDestination(double margin)
		{
			return LineMath.stepAlongLine(getBall().getPos(), getReceiver(), -getDistance(margin));
		}
		
		
		private double getDistance(double margin)
		{
			return getTBot().getCenter2DribblerDist() + Geometry.getBallRadius() + dist2Ball + margin;
		}
	}
}
