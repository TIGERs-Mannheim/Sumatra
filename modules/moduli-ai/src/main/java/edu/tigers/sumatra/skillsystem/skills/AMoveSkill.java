/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalPosition;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalVelXyPosW;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalVelocity;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.control.motor.EMotorModel;
import edu.tigers.sumatra.control.motor.IMotorModel;
import edu.tigers.sumatra.control.motor.MatrixMotorModel;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableAnnotation.ELocation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.IVectorN;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.driver.DoNothingDriver;
import edu.tigers.sumatra.skillsystem.driver.IPathDriver;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ShapeMap;


/**
 * The base class for all move-skills.
 * Just follows trajectories.
 * 
 * @author AndreR
 */
public abstract class AMoveSkill extends ASkill
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(AMoveSkill.class.getName());
	
	private IPathDriver				pathDriver			= new DoNothingDriver();
	private static IMotorModel		motorModel			= new MatrixMotorModel();
	
	@Configurable
	private static EBotSkill		defaultMoveSkill	= EBotSkill.GLOBAL_POSITION;
	
	@Configurable
	private static EMotorModel		motorModelType		= EMotorModel.MATRIX;
	
	@Configurable
	private static double			emergencyBreakAcc	= 6;
	
	
	protected AMoveSkill(final ESkill skillName)
	{
		super(skillName);
		getMoveCon().setPenaltyAreaAllowedOur(false);
	}
	
	
	private IVector3 getNextDestination()
	{
		IVector3 poss = pathDriver.getNextDestination(getTBot(), getWorldFrame());
		IVector2 dest = new Vector2(poss.getXYVector());
		double orient = poss.z();
		if (getWorldFrame().isInverted())
		{
			dest = dest.multiplyNew(-1);
			orient = AngleMath.normalizeAngle(orient + AngleMath.PI);
		}
		return new Vector3(dest, orient);
	}
	
	
	@Override
	protected final void doCalcActionsBeforeStateUpdate()
	{
		getMoveCon().update(getWorldFrame(), getTBot());
		
		beforeStateUpdate();
		
		List<IDrawableShape> shapes = new ArrayList<>(3);
		
		StringBuilder sb = new StringBuilder();
		sb.append(getType().name());
		sb.append('\n');
		sb.append(getCurrentState().getName());
		sb.append('\n');
		sb.append(getPathDriver().getType().name());
		String text = sb.toString();
		DrawableAnnotation dAnno = new DrawableAnnotation(getPos(), text);
		dAnno.setColor(Color.red);
		dAnno.setFontSize(8);
		dAnno.setLocation(ELocation.BOTTOM);
		dAnno.setMargin(130);
		shapes.add(dAnno);
		
		getPathDriver().setShapes(EShapesLayer.SKILL_NAMES, shapes);
	}
	
	
	@Override
	protected final void doCalcActionsAfterStateUpdate()
	{
		if (pathDriver.getSupportedCommands().isEmpty())
		{
			return;
		}
		
		EBotSkill cmdType = defaultMoveSkill;
		if (!pathDriver.getSupportedCommands().contains(cmdType))
		{
			cmdType = pathDriver.getSupportedCommands().iterator().next();
		}
		pathDriver.update(getTBot(), getBot(), getWorldFrame());
		
		afterDriverUpdate();
		
		double limitedVel = getMoveCon().getMoveConstraints().getVelMax();
		double limitedAcc = getMoveCon().getMoveConstraints().getAccMax();
		switch (getGameState())
		{
			case STOPPED:
				limitedVel = Geometry.getStopSpeed() - 0.2;
				break;
			default:
		}
		if (getMoveCon().isEmergencyBreak())
		{
			limitedAcc = emergencyBreakAcc;
		}
		
		final ABotSkill botSkill;
		switch (cmdType)
		{
			case GLOBAL_POSITION:
			{
				IVector3 poss = getNextDestination();
				BotSkillGlobalPosition skill = new BotSkillGlobalPosition(poss.getXYVector(), poss.z(),
						getBot().getMoveConstraints());
				
				skill.setVelMax(limitedVel);
				skill.setAccMax(limitedAcc);
				
				skill.setAccMaxW(getMoveCon().getMoveConstraints().getAccMaxW());
				skill.setVelMaxW(getMoveCon().getMoveConstraints().getVelMaxW());
				botSkill = skill;
				break;
			}
			case GLOBAL_VELOCITY:
			{
				IVector3 vel = pathDriver.getNextVelocity(getTBot(), getWorldFrame());
				BotSkillGlobalVelocity skill = new BotSkillGlobalVelocity(vel.getXYVector(), vel.z(),
						getBot().getMoveConstraints());
				
				// skill.setAccMaxW(getMoveCon().getMoveConstraints().getAccMaxW());
				
				botSkill = skill;
				break;
			}
			case LOCAL_VELOCITY:
			{
				IVector3 vel = pathDriver.getNextLocalVelocity(getTBot(), getWorldFrame(), getDt());
				BotSkillLocalVelocity skill = new BotSkillLocalVelocity(vel.getXYVector(), vel.z(),
						getBot().getMoveConstraints());
				
				skill.setAccMaxW(getMoveCon().getMoveConstraints().getAccMaxW());
				skill.setAccMax(limitedAcc);
				
				botSkill = skill;
				break;
			}
			case GLOBAL_VEL_XY_POS_W:
			{
				IVector3 poss = getNextDestination();
				IVector3 vel = pathDriver.getNextVelocity(getTBot(), getWorldFrame());
				BotSkillGlobalVelXyPosW skill = new BotSkillGlobalVelXyPosW(vel.getXYVector(), poss.z());
				
				skill.setAccMaxW(getMoveCon().getMoveConstraints().getAccMaxW());
				skill.setVelMaxW(getMoveCon().getMoveConstraints().getVelMaxW());
				
				botSkill = skill;
				break;
			}
			case WHEEL_VELOCITY:
			{
				IVector3 vel = pathDriver.getNextLocalVelocity(getTBot(), getWorldFrame(), getDt());
				if (motorModel.getType() != motorModelType)
				{
					try
					{
						motorModel = (IMotorModel) motorModelType.getInstanceableClass().newDefaultInstance();
					} catch (NotCreateableException err)
					{
						log.error("Could not create motor model", err);
					}
				}
				
				IVectorN motors = motorModel.getWheelSpeed(vel);
				botSkill = new BotSkillWheelVelocity(motors.toArray());
				break;
			}
			default:
			{
				log.error("Invalid botSkill type for movement: " + cmdType);
				botSkill = new BotSkillMotorsOff();
				break;
			}
		}
		getMatchCtrl().setSkill(botSkill);
		
		
	}
	
	
	protected void beforeStateUpdate()
	{
	}
	
	
	protected void afterDriverUpdate()
	{
	}
	
	
	/**
	 * Check if destination reached
	 * 
	 * @return
	 */
	public final boolean isDestinationReached()
	{
		if (getTBot() == null)
		{
			return false;
		}
		boolean moveConOk = GeoMath.distancePP(getTBot().getPos(), getMoveCon().getDestination()) < 70;
		boolean orientOk = AngleMath.getShortestRotation(getAngle(), getMoveCon().getTargetAngle()) < 0.5;
		return moveConOk && orientOk;
	}
	
	
	/**
	 * @return the pathDriver
	 */
	@Override
	public final IPathDriver getPathDriver()
	{
		return pathDriver;
	}
	
	
	/**
	 * @param pathDriver the pathDriver to set
	 */
	public final void setPathDriver(final IPathDriver pathDriver)
	{
		this.pathDriver = pathDriver;
		this.pathDriver.setMoveCon(getMoveCon());
	}
	
	
	@Override
	public ShapeMap getShapes()
	{
		return pathDriver.getShapes();
	}
}
