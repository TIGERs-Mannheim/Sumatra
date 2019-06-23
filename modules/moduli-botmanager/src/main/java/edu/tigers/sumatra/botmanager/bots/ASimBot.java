/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.bots;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;
import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;
import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalPosition;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalVelXyPosW;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.control.PIDController;
import edu.tigers.sumatra.control.motor.EMotorModel;
import edu.tigers.sumatra.control.motor.IMotorModel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.IVectorN;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.math.VectorN;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1D;
import edu.tigers.sumatra.trajectory.BangBangTrajectory1DOrient;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public abstract class ASimBot extends ABot implements IConfigObserver
{
	
	@SuppressWarnings("unused")
	private static final Logger		log						= Logger
			.getLogger(ASimBot.class.getName());
	
	@Configurable
	private static EMotorModel			motorModel				= EMotorModel.MATRIX;
	
	@Configurable(comment = "Add Gaussian noise to motor speeds", defValue = "0.0;0.0;0.0;0.0")
	private static IVectorN				motorNoise				= new VectorN(4);
	
	@Configurable(comment = "Add Gaussian noise to xyw speeds", defValue = "0.0;0.0;0.0")
	private static IVector3				xywNoise					= new Vector3();
	
	@Configurable(spezis = { "GRSIM", "SUMATRA" }, defValueSpezis = { "3.5;0.0;0.4", "8.0;0.0;0.15" })
	private Double[]						pid_x						= { 3.5, 0.0, 0.4 };
	@Configurable(spezis = { "GRSIM", "SUMATRA" }, defValueSpezis = { "3.5;0.0;0.4", "8.0;0.0;0.05" })
	private Double[]						pid_w						= { 8.0, 0.0, 0.15 };
	
	@Configurable(comment = "Distance to destination to not generate any Trajectory")
	private double							trajectoryThreshold	= 10;
	
	
	private transient PIDController	pidX						= new PIDController(1, 0, 0);
	private transient PIDController	pidW						= new PIDController(1, 0, 0);
	private transient IVector3			targetVel				= Vector3.ZERO_VECTOR;
	private transient boolean			targetAngleReached	= false;
	
	private static IMotorModel			mm;
	
	
	static
	{
		ConfigRegistration.registerClass("botmgr", ASimBot.class);
	}
	
	
	protected ASimBot(final EBotType botType, final BotID botId, final IBaseStation baseStation)
	{
		super(botType, botId, baseStation);
		afterApply(null);
		setRelBattery(1);
		setKickerLevel(getKickerLevelMax());
	}
	
	
	protected ASimBot(final ABot aBot, final EBotType botType)
	{
		super(aBot, botType);
	}
	
	
	@Override
	public double getDribblerSpeed()
	{
		return getMatchCtrl().getDribbleSpeed();
	}
	
	
	protected ASimBot()
	{
		super();
	}
	
	
	/**
	 * @param skill
	 * @param pos
	 * @param vel
	 * @param dt
	 * @return
	 */
	public IVector3 executeBotSkill(final ABotSkill skill, final IVector3 pos, final IVector3 vel, final double dt)
	{
		final IVector3 action;
		switch (skill.getType())
		{
			case LOCAL_VELOCITY:
			{
				BotSkillLocalVelocity velCmd = (BotSkillLocalVelocity) skill;
				action = new Vector3(velCmd.getX(), velCmd.getY(), velCmd.getW());
				break;
			}
			case MOTORS_OFF:
			{
				action = new Vector3();
				targetVel = AVector3.ZERO_VECTOR;
				break;
			}
			case GLOBAL_POSITION:
			{
				BotSkillGlobalPosition ctrl = (BotSkillGlobalPosition) skill;
				targetVel = new Vector3(
						handleTrajCtrlXy(ctrl, pos, vel, dt),
						handleTrajCtrlW(ctrl.getOrientation(), ctrl.getAccMaxW(), ctrl.getVelMaxW(), pos, vel, dt));
				IVector2 localVel = GeoMath.convertGlobalBotVector2Local(targetVel.getXYVector(), pos.z());
				action = new Vector3(localVel, targetVel.z());
				targetAngleReached = (Math.abs(AngleMath.difference(ctrl.getOrientation(), pos.z())) < 0.01)
						&& (Math.abs(vel.z()) < 0.1);
				break;
			}
			case WHEEL_VELOCITY:
			{
				BotSkillWheelVelocity wv = (BotSkillWheelVelocity) skill;
				action = mm.getXywSpeed(new VectorN(wv.getVelocities()));
				break;
			}
			case GLOBAL_VEL_XY_POS_W:
			{
				BotSkillGlobalVelXyPosW ctrl = (BotSkillGlobalVelXyPosW) skill;
				targetVel = new Vector3(
						ctrl.getVel(),
						handleTrajCtrlW(ctrl.getTargetAngle(), ctrl.getAccMaxW(), ctrl.getVelMaxW(), pos, vel, dt));
				IVector2 localVel = GeoMath.convertGlobalBotVector2Local(targetVel.getXYVector(), pos.z());
				action = new Vector3(localVel, targetVel.z());
				targetAngleReached = (Math.abs(AngleMath.difference(ctrl.getTargetAngle(), pos.z())) < 0.01)
						&& (Math.abs(vel.z()) < 0.1);
				break;
			}
			default:
			{
				log.warn("Unhandled bot skill: " + skill.getType());
				action = new Vector3();
				break;
			}
		}
		return action;
	}
	
	
	private IVector2 handleTrajCtrlXy(final BotSkillGlobalPosition skill, final IVector3 pos, final IVector3 vel,
			final double dt)
	{
		double delay = getFeedbackDelay();
		IVector3 curVel = vel;
		IVector3 curPos = pos;
		IVector2 dest = skill.getPos();
		
		BangBangTrajectory2D trajXy = new BangBangTrajectory2D(
				curPos.getXYVector().multiplyNew(1e-3),
				dest.multiplyNew(1e-3),
				curVel.getXYVector(),
				skill.getAccMax(),
				skill.getAccMax(),
				skill.getVelMax());
		
		double trajOffset = 0.01;
		double remTime = Math.max(trajOffset, trajXy.getTotalTime() - trajOffset);
		double ddt = Math.min(dt + delay, remTime);
		Vector2 targetVelXy = trajXy.getVelocity(ddt);
		
		return targetVelXy;
	}
	
	
	private double handleTrajCtrlW(final double targetAngle, final double accMax, final double velMax,
			final IVector3 pos, final IVector3 vel,
			final double dt)
	{
		double delay = getFeedbackDelay();
		IVector3 curVel = vel;
		IVector3 curPos = pos;
		
		BangBangTrajectory1D trajW = new BangBangTrajectory1DOrient(
				curPos.z(),
				targetAngle,
				curVel.z(),
				accMax,
				accMax,
				velMax);
		
		
		double trajOffset = 0.01;
		double remTime = Math.max(trajOffset, trajW.getTotalTime() - trajOffset);
		double ddt = Math.min(dt + delay, remTime);
		double targetVelW = trajW.getVelocity(ddt);
		
		return targetVelW;
	}
	
	
	/**
	 * @param dest
	 * @param orient
	 * @param pos
	 * @param vel
	 * @param dt
	 * @return global velocity
	 */
	@Deprecated
	public IVector3 handlePositioning(final IVector2 dest, final double orient, final IVector3 pos, final IVector3 vel,
			final double dt)
	{
		IVector2 error = dest.subtractNew(pos.getXYVector()).multiply(0.001f);
		double errorW = AngleMath.getShortestRotation(orient, pos.z());
		
		pidX.setP(pid_x[0]);
		pidX.setI(pid_x[1]);
		pidX.setD(pid_x[2]);
		double xyLimit = 0;
		pidX.setOutputRange(-xyLimit, xyLimit);
		pidX.update(error.getLength());
		pidW.setP(pid_w[0]);
		pidW.setI(pid_w[1]);
		pidW.setD(pid_w[2]);
		double wLimit = 0;
		pidW.setOutputRange(-wLimit, wLimit);
		pidW.update(errorW);
		
		IVector2 xyOut = error.scaleToNew(-pidX.getResult());
		double w = pidW.getResult();
		return new Vector3(xyOut, w);
	}
	
	
	protected double getFeedbackDelay()
	{
		return 0;
	}
	
	
	@Override
	public void onIncommingBotCommand(final BotID id, final ACommand cmd)
	{
	}
	
	
	@Override
	public void afterApply(final IConfigClient configClient)
	{
		ConfigRegistration.applySpezis(this, "botmgr", getType().name());
		
		try
		{
			mm = (IMotorModel) motorModel.getInstanceableClass().newDefaultInstance();
			mm.setMotorNoise(motorNoise);
			mm.setXywNoise(xywNoise);
		} catch (NotCreateableException err)
		{
			log.error("Could not create motor model", err);
		}
	}
	
	
	@Override
	public void start()
	{
		ConfigRegistration.registerConfigurableCallback("botmgr", this);
	}
	
	
	@Override
	public void stop()
	{
		ConfigRegistration.unregisterConfigurableCallback("botmgr", this);
	}
	
	
	@Override
	public int getHardwareId()
	{
		return getBotId().getNumberWithColorOffset();
	}
	
	
	@Override
	public boolean isBarrierInterrupted()
	{
		return false;
	}
	
	
	/**
	 * @return the minUpdateRate
	 */
	@Override
	public double getMinUpdateRate()
	{
		return 0;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public IVector3 getGlobalTargetVelocity(final long timestamp)
	{
		return targetVel;
	}
	
	
	/**
	 * @return the targetAngleReached
	 */
	public boolean isTargetAngleReached()
	{
		return targetAngleReached;
	}
	
}
