/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.05.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.Device;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.ConfigRegistration;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.config.EConfigurableCat;


/**
 * Interpreter for TigerBotV2.
 * 
 * @author AndreR
 */
public class CommandInterpreter implements IConfigObserver, ICommandInterpreter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log					= Logger.getLogger(CommandInterpreter.class.getName());
	
	private boolean					dribblerOn			= false;
	private final ABot				bot;
	private long						lastForceKick		= 0;
	
	@Configurable(spezis = { "", "GRSIM", "TIGER" }, comment = "Maximum speed for dribble roll [rotations per minute]", defValue = "8000")
	private int							maxDribbleSpeed	= 8000;
	@Configurable(spezis = { "", "GRSIM", "TIGER" }, comment = "Maximum discharge time for kick: 10000 [us]", defValue = "10000")
	private int							maxKickSpeed		= 10000;
	@Configurable(spezis = { "", "GRSIM", "TIGER" }, comment = "Maximum discharge time for chip: 10000 [us]", defValue = "10000")
	private int							maxChipSpeed		= 10000;
	
	
	@Configurable(spezis = { "", "GRSIM", "TIGER" }, defValue = "2.0")
	private float						speedHigh			= 2.0f;
	@Configurable(spezis = { "", "GRSIM", "TIGER" }, defValue = "1.0")
	private float						speedLow				= 1.0f;
	@Configurable(spezis = { "", "GRSIM", "TIGER" }, defValue = "7.0")
	private float						speedRotateHigh	= 7f;
	@Configurable(spezis = { "", "GRSIM", "TIGER" }, defValue = "3.0")
	private float						speedRotateLow		= 3f;
	
	private float						maxSpeed				= speedLow;
	private float						maxRotateSpeed		= speedRotateLow;
	
	private boolean					highSpeedMode		= false;
	private boolean					paused				= false;
	
	private IVector2					lastSpeed			= new Vector2(0, 0);
	
	
	private float						compassThreshold	= 0;
	private float						speedDamp			= 1.0f;
	private float						breakDamp			= 1.0f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param bot
	 */
	public CommandInterpreter(final ABot bot)
	{
		this.bot = bot;
		ConfigRegistration.registerConfigurableCallback(EConfigurableCat.RCM, this);
		ConfigRegistration.applySpezis(this, EConfigurableCat.RCM, bot.getType().name());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param command
	 */
	@Override
	public void interpret(final BotActionCommand command)
	{
		if (paused)
		{
			return;
		}
		if (highSpeedMode)
		{
			maxSpeed = speedHigh;
			maxRotateSpeed = speedRotateHigh;
		} else
		{
			maxSpeed = speedLow;
			maxRotateSpeed = speedRotateLow;
		}
		
		bot.execute(getMove(command));
		
		if (command.hasDribble() && (command.getDribble() > 0.25))
		{
			dribblerOn = true;
			final int rpm = (int) (command.getDribble() * maxDribbleSpeed);
			final TigerDribble dribbleCmd = new TigerDribble(rpm);
			bot.execute(dribbleCmd);
			
		} else if (dribblerOn)
		{
			// disable dribbler as soon has there is no dribble signal anymore
			dribblerOn = false;
			final TigerDribble dribbleCmd = new TigerDribble(0);
			bot.execute(dribbleCmd);
		}
		
		if (((SumatraClock.nanoTime() - lastForceKick) > 0e9))
		{
			if (command.hasKickForce())
			{
				lastForceKick = SumatraClock.nanoTime();
				bot.execute(new TigerKickerKickV2(Device.STRAIGHT, EKickerMode.FORCE,
						command.getKickForce() * maxKickSpeed));
			}
			if (command.hasKickArm())
			{
				lastForceKick = SumatraClock.nanoTime();
				bot.execute(new TigerKickerKickV2(Device.STRAIGHT, EKickerMode.ARM,
						command.getKickArm() * maxKickSpeed));
			}
			if ((command.hasChipForce()))
			{
				lastForceKick = SumatraClock.nanoTime();
				bot.execute(new TigerKickerKickV2(Device.CHIP, EKickerMode.FORCE, command.getChipForce() * maxChipSpeed));
			}
			if ((command.hasChipArm()))
			{
				lastForceKick = SumatraClock.nanoTime();
				bot.execute(new TigerKickerKickV2(Device.CHIP, EKickerMode.ARM, command.getChipArm() * maxChipSpeed));
			}
		}
		
		if ((command.hasDisarm() && command.getDisarm()))
		{
			bot.execute(new TigerKickerKickV2(Device.STRAIGHT, EKickerMode.DISARM));
		}
	}
	
	
	private TigerMotorMoveV2 getMove(final BotActionCommand command)
	{
		/*
		 * X-Y-Translation
		 */
		final Vector2 vel = new Vector2();
		
		vel.x = command.getTranslateX();
		vel.y = command.getTranslateY();
		
		// respect controller dead zone
		if ((vel.y < compassThreshold) && (vel.y > -compassThreshold))
		{
			vel.y = 0;
		}
		
		if ((vel.x < compassThreshold) && (vel.x > -compassThreshold))
		{
			vel.x = 0;
		}
		
		// substract COMPASS_THRESHOLD to start at 0 value after dead zone
		if (vel.y < 0)
		{
			vel.y += compassThreshold;
		}
		
		if (vel.y > 0)
		{
			vel.y -= compassThreshold;
		}
		
		if (vel.x < 0)
		{
			vel.x += compassThreshold;
		}
		
		if (vel.x > 0)
		{
			vel.x -= compassThreshold;
		}
		
		
		// scale back to full range (-1.0 - 1.0)
		vel.multiply(1.0f / (1.0f - compassThreshold));
		
		vel.multiply(maxSpeed);
		
		if (vel.getLength2() > lastSpeed.getLength2())
		{
			vel.set(lastSpeed.addNew(vel.subtractNew(lastSpeed).multiply(speedDamp)));
		} else
		{
			vel.set(lastSpeed.addNew(vel.subtractNew(lastSpeed).multiply(breakDamp)));
		}
		lastSpeed = new Vector2(vel);
		if (Float.isNaN(vel.x) || Float.isNaN(vel.y))
		{
			lastSpeed = new Vector2(0, 0);
		}
		
		/*
		 * Left-Right-Rotation
		 */
		float rotate = command.getRotate();
		
		if ((rotate < compassThreshold) && (rotate > -compassThreshold))
		{
			rotate = 0;
		}
		
		// substract COMPASS_THRESHOLD to start at 0 value after dead zone
		if (rotate < 0)
		{
			rotate += compassThreshold;
		}
		
		if (rotate > 0)
		{
			rotate -= compassThreshold;
		}
		
		// scale back to full range (-1.0 - 1.0)
		rotate *= 1.0f / (1.0f - compassThreshold);
		
		rotate *= maxRotateSpeed;
		
		/*
		 * send vector and turn to bot
		 */
		return new TigerMotorMoveV2(vel, rotate);
	}
	
	
	/**
	 * send a move, dribble and kick cmd to stop everything
	 */
	@Override
	public void stopAll()
	{
		final TigerMotorMoveV2 moveCmd = new TigerMotorMoveV2(new Vector2(0, 0), 0);
		final TigerDribble dribbleCmd = new TigerDribble(0);
		final TigerKickerKickV2 kickerCmd = new TigerKickerKickV2(0, EKickerMode.DISARM);
		bot.execute(moveCmd);
		bot.execute(dribbleCmd);
		bot.execute(kickerCmd);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	@Override
	public ABot getBot()
	{
		return bot;
	}
	
	
	@Override
	public void onLoad(final HierarchicalConfiguration newConfig)
	{
	}
	
	
	@Override
	public void onReload(final HierarchicalConfiguration freshConfig)
	{
		ConfigRegistration.applySpezis(this, EConfigurableCat.RCM, bot.getType().name());
	}
	
	
	/**
	 * @param compassThreshold the compassThreshold to set
	 */
	public void setCompassThreshold(final float compassThreshold)
	{
		this.compassThreshold = compassThreshold;
	}
	
	
	/**
	 * @param speedDamp the speedDamp to set
	 */
	public void setSpeedDamp(final float speedDamp)
	{
		this.speedDamp = speedDamp;
	}
	
	
	/**
	 * @param breakDamp the breakDamp to set
	 */
	public void setBreakDamp(final float breakDamp)
	{
		this.breakDamp = breakDamp;
	}
	
	
	/**
	 * @return the highSpeedMode
	 */
	@Override
	public boolean isHighSpeedMode()
	{
		return highSpeedMode;
	}
	
	
	/**
	 * @param highSpeedMode the highSpeedMode to set
	 */
	@Override
	public void setHighSpeedMode(final boolean highSpeedMode)
	{
		this.highSpeedMode = highSpeedMode;
		if (highSpeedMode)
		{
			log.info("High Speed Mode activated");
		} else
		{
			log.info("High Speed Mode deactivated");
		}
	}
	
	
	/**
	 * @return the paused
	 */
	@Override
	public boolean isPaused()
	{
		return paused;
	}
	
	
	/**
	 * @param paused the paused to set
	 */
	@Override
	public void setPaused(final boolean paused)
	{
		this.paused = paused;
	}
	
	
	/**
	 * @return the compassThreshold
	 */
	@Override
	public float getCompassThreshold()
	{
		return compassThreshold;
	}
}
