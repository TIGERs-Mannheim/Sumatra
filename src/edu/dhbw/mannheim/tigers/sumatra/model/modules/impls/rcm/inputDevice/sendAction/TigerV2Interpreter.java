/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.05.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.Device;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.ActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Interpreter for TigerBotV2.
 * 
 * @author AndreR
 * 
 */
public class TigerV2Interpreter extends ARCCommandInterpreter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log						= Logger.getLogger(TigerV2Interpreter.class.getName());
	
	@Configurable
	private static float				compassThreshold		= 0.05f;
	private static final double	PRESSED_THRESHOLD		= 0.5f;
	
	@Configurable(speziType = EBotType.class, spezis = { "GRSIM" }, comment = "Maximum speed for dribble roll [rotations per minute]")
	private static float				maxDribbleSpeed		= 8000f;
	@Configurable(comment = "Maximum discharge time for kick: 10000 [us]")
	private static int				maxKickSpeed			= 10000;
	@Configurable(comment = "Maximum discharge time for chip: 10000 [us]")
	private static int				maxChipSpeed			= 10000;
	
	private boolean					dribblerOn				= false;
	private final ABot				bot;
	
	private long						lastForceKick			= 0;
	private long						lastKick					= 0;
	
	
	@Configurable(speziType = EBotType.class, spezis = { "GRSIM" })
	private static float				speedHigh				= 3.0f;
	@Configurable(speziType = EBotType.class, spezis = { "GRSIM" })
	private static float				speedLow					= 1.0f;
	@Configurable(speziType = EBotType.class, spezis = { "GRSIM" })
	private static float				speedRotateHigh		= 10f;
	@Configurable(speziType = EBotType.class, spezis = { "GRSIM" })
	private static float				speedRotateLow			= 5f;
	private float						maxSpeed					= speedLow;
	private float						maxRotateSpeed			= speedRotateLow;
	private boolean					highSpeedMode			= false;
	private long						timeLastSwitchedMode	= System.nanoTime();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param bot
	 */
	public TigerV2Interpreter(ABot bot)
	{
		this.bot = bot;
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void interpret(ActionCommand command)
	{
		if ((command.kickArm > PRESSED_THRESHOLD) && (command.chipKick > PRESSED_THRESHOLD)
				&& (command.kick > PRESSED_THRESHOLD) && (command.dribble > PRESSED_THRESHOLD))
		{
			if ((System.nanoTime() - timeLastSwitchedMode) > TimeUnit.SECONDS.toNanos(2))
			{
				if (highSpeedMode)
				{
					log.info("Switched to low speed mode");
				} else
				{
					log.info("Switched to high speed mode");
				}
				highSpeedMode = !highSpeedMode;
				timeLastSwitchedMode = System.nanoTime();
			}
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
		
		/*
		 * send Force Command to bot
		 * at max every 2s
		 */
		if ((command.kick > 0.5f) && ((System.nanoTime() - lastForceKick) > 2e9))
		{
			lastForceKick = System.nanoTime();
			
			final TigerKickerKickV2 forceCmd = new TigerKickerKickV2();
			forceCmd.setMode(EKickerMode.FORCE);
			forceCmd.setDevice(Device.STRAIGHT);
			forceCmd.setFiringDuration(maxKickSpeed);
			bot.execute(forceCmd);
		}
		
		
		/*
		 * send Pass Command to bot (Force with half power)
		 */
		if (command.pass > 0.5f)
		{
			TigerKickerKickV2 forceCmd = new TigerKickerKickV2();
			forceCmd.setMode(EKickerMode.ARM);
			forceCmd.setDevice(Device.STRAIGHT);
			forceCmd.setFiringDuration(maxKickSpeed / 2);
			bot.execute(forceCmd);
		}
		
		/*
		 * send Dribble Command to bot
		 */
		if (command.dribble > 0.25)
		{
			dribblerOn = true;
			final TigerDribble dribbleCmd = new TigerDribble();
			final int rpm = (int) (command.dribble * maxDribbleSpeed);
			dribbleCmd.setSpeed(rpm);
			bot.execute(dribbleCmd);
			
		} else if (dribblerOn)
		{
			dribblerOn = false;
			final TigerDribble dribbleCmd = new TigerDribble();
			final int rpm = 0;
			dribbleCmd.setSpeed(rpm);
			bot.execute(dribbleCmd);
		}
		
		
		/*
		 * send Arm Command to bot
		 * only arm every 2s at max to prevent arm after kick when barrier is still interrupted
		 */
		if ((command.kickArm > 0.5) && ((System.nanoTime() - lastKick) > 2e9))
		{
			lastKick = System.nanoTime();
			
			final TigerKickerKickV2 armCmd = new TigerKickerKickV2();
			armCmd.setMode(EKickerMode.ARM);
			armCmd.setDevice(Device.STRAIGHT);
			armCmd.setFiringDuration(maxKickSpeed);
			bot.execute(armCmd);
		}
		
		
		/*
		 * send Disarm Command to bot
		 */
		if ((command.disarm > 0.5))
		{
			final TigerKickerKickV2 armCmd = new TigerKickerKickV2();
			armCmd.setMode(EKickerMode.DISARM);
			armCmd.setDevice(Device.STRAIGHT);
			bot.execute(armCmd);
		}
		
		
		/*
		 * send Chipkick Command to bot
		 */
		if ((command.chipKick > 0.5) && ((System.nanoTime() - lastForceKick) > 2e9))
		{
			lastForceKick = System.nanoTime();
			
			TigerKickerKickV2 chipCmd = new TigerKickerKickV2();
			chipCmd.setMode(EKickerMode.FORCE);
			chipCmd.setDevice(Device.CHIP);
			chipCmd.setFiringDuration(maxKickSpeed);
			bot.execute(chipCmd);
		}
	}
	
	
	private TigerMotorMoveV2 getMove(ActionCommand command)
	{
		/*
		 * X-Y-Translation
		 */
		final Vector2 vel = new Vector2();
		
		vel.x = (float) command.translateX;
		vel.y = (float) command.translateY;
		
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
		
		/*
		 * Left-Right-Rotation
		 */
		float rotate = (float) command.rotate;
		
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
		final TigerMotorMoveV2 cmd = new TigerMotorMoveV2();
		cmd.setXY(vel);
		cmd.setW(rotate);
		
		return cmd;
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
	@Override
	public ABot getBot()
	{
		return bot;
	}
}
