/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 28, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.BangBangTrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.ITrajectory1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.TrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawableTrajectory;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.DebugShapeHacker;
import edu.dhbw.mannheim.tigers.sumatra.util.PIDController;
import edu.dhbw.mannheim.tigers.sumatra.util.config.ConfigRegistration;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.config.EConfigurableCat;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 2)
public abstract class SimBot extends ABot implements IConfigObserver
{
	@SuppressWarnings("unused")
	private static final Logger				log					= Logger.getLogger(SimBot.class.getName());
	private ENetworkState						networkState		= ENetworkState.ONLINE;
	
	private IVector2								lastDest				= null;
	private float									lastTargetAngle	= 0;
	
	@Configurable
	private static float							pidXYp				= 0.005f;
	@Configurable
	private static float							pidXYd				= 0.0f;
	@Configurable
	private static float							pidXYi				= 0.0f;
	@Configurable
	private static float							pidWp					= 4f;
	@Configurable
	private static float							pidWd					= 0.0f;
	@Configurable
	private static float							pidWi					= 0.0f;
	
	private transient BangBangTrajectory2D	trajXY				= null;
	private transient ITrajectory1D			trajW					= null;
	private transient long						trajXYTime			= 0;
	private transient float						lastTransTime		= 0;
	
	
	private transient PIDController			pidX					= new PIDController(pidXYp, pidXYi, pidXYd);
	private transient PIDController			pidY					= new PIDController(pidXYp, pidXYi, pidXYd);
	private transient PIDController			pidW					= new PIDController(pidWp, pidWi, pidWd, true);
	
	
	private transient FileWriter				debugFileWriter;
	
	
	protected SimBot()
	{
		super();
	}
	
	
	/**
	 * @param botConfig
	 */
	public SimBot(final SubnodeConfiguration botConfig)
	{
		super(botConfig);
		networkState = ENetworkState.valueOf(botConfig.getString("networkState", "OFFLINE"));
		onReload(null);
	}
	
	
	/**
	 * @param type
	 * @param id
	 * @param baseStationKey
	 * @param mcastDelegateKey
	 */
	public SimBot(final EBotType type, final BotID id, final int baseStationKey, final int mcastDelegateKey)
	{
		super(type, id, baseStationKey, mcastDelegateKey);
		networkState = ENetworkState.OFFLINE;
		onReload(null);
	}
	
	
	@Override
	public HierarchicalConfiguration getConfiguration()
	{
		HierarchicalConfiguration config = super.getConfiguration();
		config.addProperty("bot.networkState", networkState);
		return config;
	}
	
	
	/**
	 * @param networkState the networkState to set
	 */
	public void setNetworkState(final ENetworkState networkState)
	{
		this.networkState = networkState;
		notifyNetworkStateChanged(networkState);
	}
	
	
	@Override
	public ENetworkState getNetworkState()
	{
		return networkState;
	}
	
	
	protected IVector3 handleTrajectoryMove(final IVector2 dest, final float targetAngle, final float transTime,
			final IVector3 botPos,
			final IVector3 botVel, final float lookahead, final boolean local)
	{
		Vector3 action = new Vector3();
		
		if (lastDest == null)
		{
			lastDest = dest;
		}
		
		float t = (System.nanoTime() - trajXYTime) / 1e9f;
		boolean destChanged = (dest.x() != lastDest.x()) || (dest.y() != lastDest.y());
		boolean angleChanged = targetAngle != lastTargetAngle;
		// float distDest = GeoMath.distancePP(dest, lastDest);
		// float angleDiff = Math.abs(AngleMath.getShortestRotation(lastTargetAngle, targetAngle));
		boolean reset = (transTime < 0) && (!SumatraMath.isEqual(transTime, lastTransTime));
		
		// if (!SumatraMath.isEqual(transTime, lastTransTime))
		// {
		// log.debug("transTime " + transTime);
		// }
		
		lastTransTime = transTime;
		
		if ((trajXY == null) || reset || (((destChanged) || angleChanged) && ((t >= transTime))))
		{
			// log.info("New traj: " + reset + " " + destChanged + " " + angleChanged + " " + t + " " + transTime + " "
			// + dest
			// + " " + targetAngle);
			final IVector2 vel;
			final IVector2 pos;
			final float orient;
			final float aVel;
			if ((trajXY != null) && (t < trajXY.getTotalTime()) && (transTime >= 0))
			{
				vel = trajXY.getVelocity(transTime);
				pos = trajXY.getPosition(transTime);
				orient = trajW.getPosition(transTime);
				aVel = trajW.getVelocity(transTime);
			} else
			{
				vel = botVel.getXYVector();
				pos = botPos.getXYVector().addNew(vel.multiplyNew(lookahead));
				orient = botPos.z();
				aVel = botVel.z();
			}
			
			trajXY = TrajectoryGenerator.generatePositionTrajectory(this, pos, vel, dest);
			trajXYTime = System.nanoTime();
			lastDest = dest;
			lastTargetAngle = targetAngle;
			trajW = TrajectoryGenerator.generateRotationTrajectory(this, orient, aVel, targetAngle, trajXY);
			t = 0;
		}
		
		IVector2 curDest = trajXY.getPosition(t);
		IVector2 pastDest = trajXY.getPosition(t - lookahead);
		IVector2 curVel = trajXY.getVelocity(t);
		
		float curAngle = trajW.getPosition(t);
		float pastAngle = trajW.getPosition(t - lookahead);
		float curAVel = trajW.getVelocity(t);
		
		DebugShapeHacker.addDebugShape(new DrawableBot(trajXY.getPosition(0), trajW.getPosition(0), Color.green,
				0.5f));
		DebugShapeHacker.addDebugShape(new DrawableBot(trajXY.getPosition(trajXY.getTotalTime()),
				trajW.getPosition(trajW
						.getTotalTime()), Color.red, 0.5f));
		DebugShapeHacker.addDebugShape(new DrawableBot(curDest, curAngle, Color.blue, 0.5f));
		DebugShapeHacker.addDebugShape(new DrawableTrajectory(trajXY, 1));
		if (transTime > 0)
		{
			DebugShapeHacker.addDebugShape(new DrawableCircle(trajXY.getPosition(transTime), 30, Color.gray));
		}
		
		pidX.setSetpoint(curDest.x());
		pidY.setSetpoint(curDest.y());
		pidW.setSetpoint(curAngle);
		pidX.update((botPos.getXYVector().x()) + (curDest.x() - pastDest.x()));
		pidY.update((botPos.getXYVector().y()) + (curDest.y() - pastDest.y()));
		pidW.update(botPos.z() + (curAngle - pastAngle));
		
		
		IVector2 error = new Vector2(pidX.getResult(), pidY.getResult());
		IVector2 outVel = curVel.addNew(error);
		
		if (local)
		{
			float futureAngle = curAngle;
			IVector2 localVel = AiMath.convertGlobalBotVector2Local(outVel, futureAngle);
			action.set(0, localVel.y());
			action.set(1, -localVel.x());
			action.set(2, curAVel + pidW.getResult());
		} else
		{
			action.set(outVel, curAVel + pidW.getResult());
		}
		
		// try
		// {
		// debugFileWriter.write(String.format(
		// "%d %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %n",
		// System.currentTimeMillis(), t, transTime,
		// dest.x(), dest.y(), targetAngle,
		// pidX.getSetpoint(), pidY.getSetpoint(), pidW.getSetpoint(),
		// botPos.getXYVector().x(), botPos.getXYVector().y(), botPos.z(),
		// pidX.getError(), pidY.getError(), pidW.getError(),
		// pidX.getResult(), pidY.getResult(), pidW.getResult(),
		// pidX.getInput(), pidY.getInput(), pidW.getInput(),
		// curVel.x(), curVel.y(), curAVel,
		// botVel.getXYVector().x(), botVel.getXYVector().y(), botVel.z(),
		// outVel.x(), outVel.y(), curAVel + pidW.getResult(),
		// curAngle));
		// } catch (IOException err)
		// {
		// log.error("Could not write debug", err);
		// }
		
		if (action.isFinite())
		{
			return action;
		}
		
		log.error("vel not finite!!! Report the following lines to Andre please! :) \n" + trajW);
		return new Vector3();
	}
	
	
	@Override
	public void onLoad(final HierarchicalConfiguration newConfig)
	{
		onReload(newConfig);
	}
	
	
	@Override
	public void onReload(final HierarchicalConfiguration freshConfig)
	{
		pidX = new PIDController(pidXYp, pidXYi, pidXYd);
		pidY = new PIDController(pidXYp, pidXYi, pidXYd);
		pidW = new PIDController(pidWp, pidWi, pidWd, true);
		pidW.setInputRange(-AngleMath.PI, AngleMath.PI);
		pidX.setOutputRange(-3, 3);
		pidY.setOutputRange(-3, 3);
		ConfigRegistration.applySpezis(getPerformance(), EConfigurableCat.BOTMGR, getType().name());
	}
	
	
	@Override
	public void start()
	{
		ConfigRegistration.registerConfigurableCallback(EConfigurableCat.BOTMGR, this);
		try
		{
			debugFileWriter = new FileWriter("data/simBot" + getBotId().getNumberWithColorOffset() + ".csv");
		} catch (IOException err)
		{
			log.error("Could not open debugging file.", err);
		}
	}
	
	
	@Override
	public void stop()
	{
		ConfigRegistration.unregisterConfigurableCallback(EConfigurableCat.BOTMGR, this);
		try
		{
			debugFileWriter.close();
		} catch (IOException err)
		{
			log.error("Could not close file writer.", err);
		}
	}
	
	
	@Override
	public int getHardwareId()
	{
		return getBotId().getNumberWithColorOffset();
	}
}
