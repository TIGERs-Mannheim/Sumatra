/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.11.2011
 * Author(s): Manuel
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction.interpreter;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.Device;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.Mode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.ActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction.ARCCommandInterpreter;


/**
 * This interpreter converts ActionCommands to specific tiger bot instructions.
 * 
 * @author Manuel, AndreR
 * 
 */
public class TigerInterpreter extends ARCCommandInterpreter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log						= Logger.getLogger(TigerInterpreter.class.getName());
	
	private static final float		COMPASS_THRESHOLD		= 0.33f;
	
	private static final double	PRESSED_THRESHOLD		= 0.5f;
	
	/** Maximum speed for dribble roll: 8000 [rotations per minute] */
	// 8000
	private static final float		MAX_DRIBBLE_SPEED		= 25000f;
	
	/** Maximum discharge time for kick: 10000 [us] */
	private static final int		MAX_KICK_SPEED			= 10000;
	private static final int		MAX_CHIP_SPEED			= 10000;
	
	private boolean					dribblerOn				= false;
	private final TigerBot			bot;
	
	private long						lastCall					= 0;
	private Vector2					lastVel					= new Vector2(0, 0);
	private float						lastRotateVel			= 0;
	
	private long						lastForceKick			= 0;
	private long						lastKick					= 0;
	
	
	private static final float		SPEED_HIGH				= 6.0f;
	private static final float		SPEED_LOW				= 3.0f;
	private static final float		SPEED_ROTATE_HIGH		= 30f;
	private static final float		SPEED_ROTATE_LOW		= 20f;
	private float						maxSpeed					= 3.0f;
	private float						maxRotateSpeed			= 20;
	private boolean					highSpeedMode			= false;
	private long						timeLastSwitchedMode	= System.nanoTime();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param bot
	 */
	public TigerInterpreter(ABot bot)
	{
		super(EBotType.TIGER);
		
		this.bot = (TigerBot) bot;
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
					maxSpeed = SPEED_LOW;
					maxRotateSpeed = SPEED_ROTATE_LOW;
					log.info("Switched to low speed mode");
				} else
				{
					maxSpeed = SPEED_HIGH;
					maxRotateSpeed = SPEED_ROTATE_HIGH;
					log.info("Switched to high speed mode");
				}
				highSpeedMode = !highSpeedMode;
				timeLastSwitchedMode = System.nanoTime();
			}
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
			forceCmd.setMode(Mode.FORCE);
			forceCmd.setDevice(Device.STRAIGHT);
			forceCmd.setFiringDuration(MAX_KICK_SPEED);
			bot.execute(forceCmd);
		}
		
		
		/*
		 * send Pass Command to bot (Force with half power)
		 */
		if (command.pass > 0.5f)
		{
			TigerKickerKickV2 forceCmd = new TigerKickerKickV2();
			forceCmd.setMode(Mode.ARM);
			forceCmd.setDevice(Device.STRAIGHT);
			forceCmd.setFiringDuration(MAX_KICK_SPEED / 2);
			bot.execute(forceCmd);
		}
		
		/*
		 * send Dribble Command to bot
		 */
		if (command.dribble > 0.25)
		{
			dribblerOn = true;
			final TigerDribble dribbleCmd = new TigerDribble();
			final int rpm = (int) (command.dribble * MAX_DRIBBLE_SPEED);
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
			armCmd.setMode(Mode.ARM);
			armCmd.setDevice(Device.STRAIGHT);
			armCmd.setFiringDuration(MAX_KICK_SPEED);
			bot.execute(armCmd);
		}
		
		
		/*
		 * send Disarm Command to bot
		 */
		if ((command.disarm > 0.5))
		{
			final TigerKickerKickV2 armCmd = new TigerKickerKickV2();
			armCmd.setMode(Mode.DISARM);
			armCmd.setDevice(Device.STRAIGHT);
			bot.execute(armCmd);
		}
		
		if ((bot.getType() == EBotType.GRSIM) && (command.chipKick > 0.5) && ((System.nanoTime() - lastKick) > 2e9))
		{
			lastKick = System.nanoTime();
			
			final TigerKickerKickV2 armCmd = new TigerKickerKickV2();
			armCmd.setMode(Mode.ARM);
			armCmd.setDevice(Device.CHIP);
			armCmd.setFiringDuration(MAX_CHIP_SPEED / 2);
			bot.execute(armCmd);
		}
	}
	
	
	TigerMotorMoveV2 getMove(ActionCommand command)
	{
		// Time measurement
		// Get delta T in [s]
		float dt = (System.nanoTime() - lastCall) * 1.0e-9f;
		if (dt > 1.0f)
		{
			// assume 50ms since last call because dt > 1s doesnt make sense
			dt = 0.05f;
		}
		
		lastCall = System.nanoTime();
		
		/*
		 * X-Y-Translation
		 */
		final Vector2 acc = new Vector2();
		
		acc.x = (float) command.translateX;
		acc.y = (float) command.translateY;
		
		// respect controller dead zone
		if ((acc.y < COMPASS_THRESHOLD) && (acc.y > -COMPASS_THRESHOLD))
		{
			acc.y = 0;
		}
		
		if ((acc.x < COMPASS_THRESHOLD) && (acc.x > -COMPASS_THRESHOLD))
		{
			acc.x = 0;
		}
		
		// substract COMPASS_THRESHOLD to start at 0 value after dead zone
		if (acc.y < 0)
		{
			acc.y += COMPASS_THRESHOLD;
		}
		
		if (acc.y > 0)
		{
			acc.y -= COMPASS_THRESHOLD;
		}
		
		if (acc.x < 0)
		{
			acc.x += COMPASS_THRESHOLD;
		}
		
		if (acc.x > 0)
		{
			acc.x -= COMPASS_THRESHOLD;
		}
		
		// scale back to full range (-1.0 - 1.0)
		acc.multiply(1.0f / (1.0f - COMPASS_THRESHOLD));
		
		acc.multiply(maxSpeed);
		
		// calc new velocity
		lastVel.add(acc.multiplyNew(dt));
		
		// simulate stopping force in opposite direction
		final Vector2 friction = new Vector2(0, 0);
		friction.x = (lastVel.x * -2.0f) + (lastVel.x > 0 ? -0.5f : 0.5f);
		friction.y = (lastVel.y * -2.0f) + (lastVel.y > 0 ? -0.5f : 0.5f);
		
		final Vector2 outVel = lastVel.addNew(friction.multiplyNew(dt));
		
		// prevent friction acceleration in opposite direction
		if (((lastVel.x >= 0) && (outVel.x <= 0)) || ((lastVel.x <= 0) && (outVel.x >= 0)))
		{
			outVel.x = 0;
		}
		
		if (((lastVel.y >= 0) && (outVel.y <= 0)) || ((lastVel.y <= 0) && (outVel.y >= 0)))
		{
			outVel.y = 0;
		}
		
		lastVel = outVel;
		
		/*
		 * Left-Right-Rotation
		 */
		float rotate = (float) command.rotate;
		
		if ((rotate < COMPASS_THRESHOLD) && (rotate > -COMPASS_THRESHOLD))
		{
			rotate = 0;
		}
		
		// substract COMPASS_THRESHOLD to start at 0 value after dead zone
		if (rotate < 0)
		{
			rotate += COMPASS_THRESHOLD;
		}
		
		if (rotate > 0)
		{
			rotate -= COMPASS_THRESHOLD;
		}
		
		// scale back to full range (-1.0 - 1.0)
		rotate *= 1.0f / (1.0f - COMPASS_THRESHOLD);
		
		rotate *= maxRotateSpeed;
		
		// calc new velocity
		lastRotateVel += rotate * dt;
		
		// simulate stopping force in opposite direction
		final float rotFriction = (lastRotateVel * -4.0f) + (lastRotateVel > 0 ? -0.5f : 0.5f);
		
		float outRotate = lastRotateVel + (rotFriction * dt);
		
		// prevent friction acceleration in opposite direction
		if (((lastRotateVel >= 0) && (outRotate <= 0)) || ((lastRotateVel <= 0) && (outRotate >= 0)))
		{
			outRotate = 0;
		}
		
		lastRotateVel = outRotate;
		
		/*
		 * send vector and turn to bot
		 */
		final TigerMotorMoveV2 cmd = new TigerMotorMoveV2();
		cmd.setXY(outVel);
		cmd.setW(outRotate);
		
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
		final TigerKickerKickV2 kickerCmd = new TigerKickerKickV2(0, TigerKickerKickV2.Mode.DISARM);
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
	
	
	/**
	 * @return the maxSpeed
	 */
	public final float getMaxSpeed()
	{
		return maxSpeed;
	}
	
	
	/**
	 * @param maxSpeed the maxSpeed to set
	 */
	public final void setMaxSpeed(float maxSpeed)
	{
		this.maxSpeed = maxSpeed;
	}
	
	
	/**
	 * @return the maxRotateSpeed
	 */
	public final float getMaxRotateSpeed()
	{
		return maxRotateSpeed;
	}
	
	
	/**
	 * @param maxRotateSpeed the maxRotateSpeed to set
	 */
	public final void setMaxRotateSpeed(float maxRotateSpeed)
	{
		this.maxRotateSpeed = maxRotateSpeed;
	}
}
