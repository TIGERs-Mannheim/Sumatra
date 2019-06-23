/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 9, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;
import java.util.Random;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.ABaseDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EMovingSpeed;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.TrajPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Prepare for redirect
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class DelayedKickSkill extends AMoveSkill
{
	private DynamicPosition			target;
	
	@Configurable
	private static float				shootSpeed		= 4.0f;
	
	private EMoveState				state				= EMoveState.PREPARE;
	
	private int							numberOfTurns	= 0;
	private ETurnState				turnState		= ETurnState.LEFT;
	private int							counter			= 0;
	private TurnAroundBallDriver	driver;
	protected TrajPathDriver		driverTraj		= null;
	protected TrajPathFinderInput	finderInput		= new TrajPathFinderInput();
	
	private enum EMoveState
	{
		PREPARE,
		TURN,
		LASTTURN,
		SHOOT;
	}
	
	private enum ETurnState
	{
		LEFT,
		RIGHT;
	}
	
	
	/**
	 * @param target
	 */
	public DelayedKickSkill(final DynamicPosition target)
	{
		super(ESkillName.DELAYED_KICK);
		this.target = target;
		
		Random rn = new Random();
		int i = (rn.nextInt() % 4);
		numberOfTurns = Math.abs(i) + 2;
		driver = new TurnAroundBallDriver();
		setPathDriver(driver);
	}
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		IVector2 dest = null;
		float orient = 0;
		IVector2 targetToBall = getWorldFrame().getBall().getPos().subtractNew(target);
		float toBallDist = (AIConfig.getGeometry().getBallRadius() + getBot().getCenter2DribblerDist()) - 10;
		
		switch (state)
		{
			case PREPARE:
				dest = getWorldFrame().getBall().getPos().addNew(targetToBall.normalizeNew().multiplyNew(500f));
				orient = target.subtractNew(getPos()).getAngle();
				dest = GeoMath.stepAlongLine(dest, target, -toBallDist);
				if (GeoMath.distancePP(getPos(), dest) < 50)
				{
					state = EMoveState.TURN;
				}
				break;
			case TURN:
				switch (turnState)
				{
					case LEFT:
						dest = getWorldFrame().getBall().getPos().addNew(targetToBall.getNormalVector().multiplyNew(300f));
						orient = target.subtractNew(getPos()).getAngle();
						dest = GeoMath.stepAlongLine(dest, target, -toBallDist);
						break;
					case RIGHT:
						dest = getWorldFrame().getBall().getPos().addNew(targetToBall.getNormalVector().multiplyNew(-300f));
						orient = target.subtractNew(getPos()).getAngle();
						dest = GeoMath.stepAlongLine(dest, target, -toBallDist);
						break;
				}
				if (GeoMath.distancePP(getPos(), dest) < 70)
				{
					if (turnState == ETurnState.RIGHT)
					{
						turnState = ETurnState.LEFT;
					} else
					{
						turnState = ETurnState.RIGHT;
					}
					counter++;
				}
				if ((counter > numberOfTurns))
				{
					state = EMoveState.LASTTURN;
				}
				break;
			case LASTTURN:
				dest = getWorldFrame().getBall().getPos().addNew(targetToBall.normalizeNew().multiplyNew(300f));
				orient = target.subtractNew(getPos()).getAngle();
				dest = GeoMath.stepAlongLine(dest, target, -toBallDist);
				if ((GeoMath.distancePP(getPos(), dest) < 50))
				{
					state = EMoveState.SHOOT;
				}
				break;
			case SHOOT:
				if (driverTraj == null)
				{
					driverTraj = new TrajPathDriver();
					setPathDriver(driverTraj);
				}
				dest = getWorldFrame().getBall().getPos();
				orient = target.subtractNew(getPos()).getAngle();
				dest = GeoMath.stepAlongLine(dest, target, -toBallDist);
				
				finderInput.setTrackedBot(getTBot());
				finderInput.setDest(dest);
				finderInput.setTargetAngle(orient);
				final TrajPathFinderInput localInput = new TrajPathFinderInput(finderInput);
				getBot().getPathFinder().calcPath(localInput);
				TrajPath path = getBot().getPathFinder().getCurPath();
				driverTraj.setPath(path);
				break;
		}
		
		driver.setDest(dest);
		driver.setOrient(-orient);
		
		getDevices().kickGeneralDuration(cmds, EKickerMode.ARM, EKickerDevice.STRAIGHT, 5000, 0);
	}
	
	
	@Override
	public void doCalcEntryActions(final List<ACommand> cmds)
	{
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
	
	
	private class TurnAroundBallDriver extends ABaseDriver
	{
		
		private IVector2	dest;
		private float		orient;
		
		
		/**
		 * 
		 */
		private TurnAroundBallDriver()
		{
			addSupportedCommand(ECommandType.VEL);
		}
		
		
		@Override
		public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
		{
			return null;
		}
		
		
		@Override
		public IVector3 getNextVelocity(final TrackedTigerBot bot, final WorldFrame wFrame)
		{
			Circle circle = new Circle(wFrame.getBall().getPos(), 150f);
			Line botDestLine = Line.newLine(getPos(), dest);
			
			if (!circle.isLineIntersectingShape(botDestLine))
			{
				IVector2 velocity = dest.subtractNew(getPos()).multiplyNew(0.001f);
				if (velocity.getLength2() < 0.5f)
				{
					velocity = velocity.normalizeNew().multiplyNew(0.5f);
				} else if (velocity.getLength2() > 2.0)
				{
					velocity = velocity.normalizeNew().multiplyNew(2.0f);
				}
				return new Vector3(velocity, 0);
			}
			IVector2 normal = botDestLine.getOrthogonalLine().directionVector();
			normal = normal.addNew(botDestLine.directionVector().normalizeNew().multiplyNew(0.3f)).normalizeNew();
			float orientation = orient - getAngle();
			return new Vector3(normal, orientation);
		}
		
		
		@Override
		public void setMovingSpeed(final EMovingSpeed speed)
		{
		}
		
		
		@Override
		public void decoratePath(final TrackedTigerBot bot, final List<IDrawableShape> shapes)
		{
			shapes.add(new DrawableCircle(new Circle(dest, 100f)));
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
		
		
		/**
		 * @param dest the dest to set
		 */
		public void setDest(final IVector2 dest)
		{
			this.dest = dest;
		}
		
		
		/**
		 * @param orient the orient to set
		 */
		public void setOrient(final float orient)
		{
			this.orient = orient;
		}
	}
}
