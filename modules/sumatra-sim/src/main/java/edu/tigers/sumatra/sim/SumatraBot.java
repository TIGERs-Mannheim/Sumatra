/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 27, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.sim;

import java.util.Optional;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.sleepycat.persist.model.Persistent;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.bots.ASimBot;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * Bot for internal Sumatra simulation
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class SumatraBot extends ASimBot implements ISimulatedObject
{
	private static final Logger	log						= Logger.getLogger(SumatraBot.class.getName());
	
	private static final int		ACC_MAX					= 10;
	
	private transient Vector3		pos						= new Vector3();
	private transient Vector3		vel						= new Vector3();
	
	private transient double		delay						= 0;
	
	@Configurable
	private static double			center2DribblerDist	= 75;
	
	@Configurable
	private double						mass						= 2.5f;
	
	@Configurable
	private static double			maxChipSpeed			= 4;
	
	
	static
	{
		ConfigRegistration.registerClass("botmgr", SumatraBot.class);
	}
	
	
	@SuppressWarnings("unused")
	private SumatraBot()
	{
		super();
	}
	
	
	/**
	 * @param id
	 * @param bs
	 */
	public SumatraBot(final BotID id, final SumatraBaseStation bs)
	{
		super(EBotType.SUMATRA, id, bs);
		init();
	}
	
	
	private void init()
	{
		double y = 1000;
		double inv = 1;
		if (getBotId().getTeamColor() == ETeamColor.YELLOW)
		{
			y *= -1;
			inv *= -1;
		}
		
		switch (getBotId().getNumber())
		{
			case 0:
				pos = new Vector3(-3500 * inv, 0, 0);
				break;
			case 1:
				pos = new Vector3(-2500 * inv, -300, 0);
				break;
			case 2:
				pos = new Vector3(-2500 * inv, 300, 0);
				break;
			case 3:
				pos = new Vector3(-500 * inv, -1000, 0);
				break;
			case 4:
				pos = new Vector3(-1000 * inv, 800, 0);
				break;
			case 5:
				pos = new Vector3(-500 * inv, 0, 0);
				break;
			default:
				pos = new Vector3(-1000 + (300 * getBotId().getNumber()), y, 0);
		}
		
		pos = new Vector3(-3800 * inv, -2700 + (getBotId().getNumber() * 200), 0);
		
		vel = new Vector3();
	}
	
	
	@Override
	public void update(final double dt)
	{
		delay = 0;
		IVector3 localVel = executeBotSkill(getMatchCtrl().getSkill(), pos, vel, dt);
		Vector3 action = new Vector3(localVel);
		// action = action.multiplyNew(new Vector3(0.9, 0.7, 1));
		// action.set(2, action.get(2) + (0.5 * localVel.get(1) * localVel.get(1)));
		IVector2 globVel = GeoMath.convertLocalBotVector2Global(action.getXYVector(), pos.z());
		IVector3 targetVel = new Vector3(globVel, action.z());
		
		IVector3 diff = targetVel.subtractNew(vel);
		
		// diff = diff.multiplyNew(new Vector3(0.3, 0.3, 1));
		
		IVector3 acc = diff.multiplyNew(1 / dt);
		IVector2 accxy = acc.getXYVector();
		if (accxy.getLength() > ACC_MAX)
		{
			accxy = accxy.scaleToNew(ACC_MAX);
		}
		IVector3 limAcc = new Vector3(accxy, acc.get(2));
		vel.add(limAcc.multiplyNew(dt));
	}
	
	
	@Override
	public void step(final double dt, final MotionContext context)
	{
		pos.set(0, pos.x() + ((vel.x() * dt) * 1000));
		pos.set(1, pos.y() + ((vel.y() * dt) * 1000));
		pos.set(2, AngleMath.normalizeAngle(pos.z() + (vel.z() * dt)));
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param timestamp
	 * @return
	 */
	public double getKickSpeed(final long timestamp)
	{
		if ((getMatchCtrl().getMode() == EKickerMode.DISARM))
		{
			return 0;
		}
		
		if ((getMatchCtrl().getMode() == EKickerMode.ARM_AIM) && !isTargetAngleReached())
		{
			return 0;
		}
		
		double timeSinceLastKick = (timestamp - getLastKickTime()) / 1e9;
		if (timeSinceLastKick > 0.5)
		{
			setLastKickTime(timestamp);
		}
		
		if (getMatchCtrl().getDevice() == EKickerDevice.CHIP)
		{
			return Math.min(getMatchCtrl().getKickSpeed(), maxChipSpeed);
		}
		return getMatchCtrl().getKickSpeed();
	}
	
	
	@Override
	public void start()
	{
		try
		{
			SumatraCam sc = (SumatraCam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			sc.registerBot(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find module ACam", err);
		} catch (ClassCastException err)
		{
			log.error("You try to use a SumatraBot with wrong moduli");
		}
		super.start();
	}
	
	
	@Override
	public void stop()
	{
		super.stop();
		try
		{
			SumatraCam sc = (SumatraCam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			sc.unregisterBot(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find module ACam", err);
		} catch (ClassCastException err)
		{
			log.error("You try to use a SumatraBot with wrong moduli");
		}
	}
	
	
	/**
	 * @return the pos [x,y,w]
	 */
	@Override
	public IVector3 getPos()
	{
		return new Vector3(pos.getXYVector(), AngleMath.normalizeAngle(pos.z()));
	}
	
	
	/**
	 * @return the vel [x,y,w]
	 */
	@Override
	public IVector3 getVel()
	{
		return new Vector3(vel);
	}
	
	
	/**
	 * @return the pos [x,y,w]
	 */
	@Override
	public Optional<IVector3> getSensoryPos()
	{
		return Optional.of(getPos());
	}
	
	
	/**
	 * @return the vel [x,y,w]
	 */
	@Override
	public Optional<IVector3> getSensoryVel()
	{
		return Optional.of(getVel());
	}
	
	
	/**
	 * @param pos the pos to set
	 */
	@Override
	public void setPos(final IVector3 pos)
	{
		this.pos = new Vector3(pos);
	}
	
	
	/**
	 * @param vel the vel to set
	 */
	@Override
	public void setVel(final IVector3 vel)
	{
		this.vel = new Vector3(vel);
	}
	
	
	@Override
	public void addVel(final IVector3 vector3)
	{
		vel.add(vector3);
	}
	
	
	/**
	 * @return
	 */
	@Override
	public double getCenter2DribblerDist()
	{
		return center2DribblerDist;
	}
	
	
	@Override
	protected double getFeedbackDelay()
	{
		return delay;
	}
	
	
	/**
	 * @return
	 */
	public double getMass()
	{
		return mass;
	}
}
