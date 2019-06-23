/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 2, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test;

import java.awt.Color;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.DrawableLine;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.ABaseDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EMovingSpeed;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.util.DebugShapeHacker;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Turn with ball on dribbler
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TurnWithBallSkill extends AMoveSkill
{
	private static final float	g					= 9.81f;
	
	@Configurable
	private static int			dribbleSpeed	= 10000;
	
	@Configurable
	private static float			friction			= 0.2f;
	
	private static float			radius			= 90;
	
	private static float			speed				= 1;
	
	
	private final IVector2		lookAtTarget;
	
	private float					step				= 0;
	
	
	/**
	 * @param lookAtTarget
	 */
	public TurnWithBallSkill(final IVector2 lookAtTarget)
	{
		super(ESkillName.TURN_WITH_BALL);
		this.lookAtTarget = lookAtTarget;
	}
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		
	}
	
	
	@Override
	protected void doCalcExitActions(final List<ACommand> cmds)
	{
		getDevices().dribble(cmds, false);
	}
	
	
	@Override
	protected void doCalcEntryActions(final List<ACommand> cmds)
	{
		// getDevices().dribble(cmds, dribbleSpeed);
		setPathDriver(new TouchBallDriver());
	}
	
	private class TouchBallDriver extends ABaseDriver
	{
		/**
		 * 
		 */
		private TouchBallDriver()
		{
			setCommandType(ECommandType.POS);
		}
		
		
		@Override
		public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
		{
			if (bot.hasBallContact())
			{
				setPathDriver(new MyPathDriver());
			}
			
			IVector2 ball = getWorldFrame().getBall().getPos();
			float ori = ball.subtractNew(getPos()).getAngle();
			return new Vector3(GeoMath.stepAlongLine(ball, getPos(), 80), ori);
		}
		
		
		@Override
		public IVector3 getNextVelocity(final TrackedTigerBot bot, final WorldFrame wFrame)
		{
			return AVector3.ZERO_VECTOR;
		}
		
		
		@Override
		public void setMovingSpeed(final EMovingSpeed speed)
		{
		}
		
		
		@Override
		public void decoratePath(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
		{
		}
		
		
		@Override
		public void decoratePathDebug(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
		{
		}
		
		
		@Override
		public EPathDriver getType()
		{
			return EPathDriver.CUSTOM;
		}
	}
	
	private class MyPathDriver extends ABaseDriver
	{
		
		private MyPathDriver()
		{
			setCommandType(ECommandType.POS);
		}
		
		
		@Override
		public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
		{
			IVector2 ball = getWorldFrame().getBall().getPos();
			IVector2 desPos = GeoMath.stepAlongLine(ball, getPos(), 120);
			
			IVector2 pos2ball = ball.subtractNew(desPos);
			IVector2 ball2Target = lookAtTarget.subtractNew(ball);
			float rot = AngleMath.getShortestRotation(pos2ball.getAngle(), ball2Target.getAngle());
			
			float speed = Math.signum(rot) * Math.min(Math.abs(rot), 0.7f);
			
			IVector2 dest = GeoMath.stepAlongCircle(desPos, ball, speed);
			DebugShapeHacker.addDebugShape(new DrawableCircle(new Circle(ball, GeoMath.distancePP(ball, desPos)),
					Color.magenta));
			DebugShapeHacker.addDebugShape(new DrawablePoint(dest, Color.cyan));
			
			if (Math.abs(rot) < 0.01f)
			{
				complete();
			}
			
			float ori = ball.subtractNew(desPos).getAngle() + speed;
			
			return new Vector3(dest, ori);
		}
		
		
		@Override
		public IVector3 getNextVelocity(final TrackedTigerBot bot, final WorldFrame wFrame)
		{
			// speed = omega * radius
			float radiusM = radius / 1000f;
			float omega = speed / radiusM;
			
			float phi = (float) Math.atan(friction * g * omega * omega * radiusM);
			
			IVector2 ball = getWorldFrame().getBall().getPos();
			// IVector2 p = GeoMath.stepAlongCircle(getPos(), ball, step);
			IVector2 desPos = GeoMath.stepAlongLine(ball, getPos(), 120);
			IVector2 p = desPos.subtractNew(ball);
			step += 0.01f;
			IVector2 dir = p.turnNew(AngleMath.PI_HALF);
			
			if (step > AngleMath.PI)
			{
				complete();
			}
			
			float orientation = AngleMath.normalizeAngle(p.getAngle() + phi);
			float aVel = (AngleMath.normalizeAngle(getAngle() - orientation)) * -6;
			DebugShapeHacker.addDebugShape(new DrawableLine(new Line(getPos(), new Vector2(orientation).scaleTo(100)),
					Color.magenta,
					true));
			
			IVector3 vel = new Vector3(dir.scaleToNew(0.5f), aVel);
			return vel;
		}
		
		
		@Override
		public void setMovingSpeed(final EMovingSpeed speed)
		{
		}
		
		
		@Override
		public void decoratePath(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
		{
		}
		
		
		@Override
		public void decoratePathDebug(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
		{
		}
		
		
		@Override
		public EPathDriver getType()
		{
			return EPathDriver.CUSTOM;
		}
	}
}
