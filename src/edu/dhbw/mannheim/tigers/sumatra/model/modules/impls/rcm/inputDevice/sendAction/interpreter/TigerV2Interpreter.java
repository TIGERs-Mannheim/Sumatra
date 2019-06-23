/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.05.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction.interpreter;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.Device;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.Mode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.ActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.inputDevice.sendAction.ARCCommandInterpreter;


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
	private static final float	COMPASS_THRESHOLD	= 0.33f;
	
	/** Maximum speed for dribble roll: 8000 [rotations per minute] */
	// 8000
	private static final float	MAX_DRIBBLE_SPEED	= 10000f;
	
	/** Maximum discharge time for kick: 25000 [us] */
	private static final int	MAX_KICK_SPEED		= 10000;
	
	private boolean				dribblerOn			= false;
	private final TigerBotV2	bot;
	
	private long					lastForceKick		= 0;
	private long					lastKick				= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param bot
	 */
	public TigerV2Interpreter(ABot bot)
	{
		super(EBotType.TIGER_V2);
		
		this.bot = (TigerBotV2) bot;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void interpret(ActionCommand command)
	{
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
		
		
		/*
		 * send Chipkick Command to bot
		 */
		if ((command.chipKick > 0.5) && ((System.nanoTime() - lastForceKick) > 2e9))
		{
			lastForceKick = System.nanoTime();
			
			TigerKickerKickV2 chipCmd = new TigerKickerKickV2();
			chipCmd.setMode(Mode.FORCE);
			chipCmd.setDevice(Device.CHIP);
			chipCmd.setFiringDuration(MAX_KICK_SPEED);
			bot.execute(chipCmd);
		}
	}
	
	
	TigerMotorMoveV2 getMove(ActionCommand command)
	{
		/*
		 * X-Y-Translation
		 */
		final Vector2 vel = new Vector2();
		
		vel.x = (float) command.translateX;
		vel.y = (float) command.translateY;
		
		// respect controller dead zone
		if ((vel.y < COMPASS_THRESHOLD) && (vel.y > -COMPASS_THRESHOLD))
		{
			vel.y = 0;
		}
		
		if ((vel.x < COMPASS_THRESHOLD) && (vel.x > -COMPASS_THRESHOLD))
		{
			vel.x = 0;
		}
		
		// substract COMPASS_THRESHOLD to start at 0 value after dead zone
		if (vel.y < 0)
		{
			vel.y += COMPASS_THRESHOLD;
		}
		
		if (vel.y > 0)
		{
			vel.y -= COMPASS_THRESHOLD;
		}
		
		if (vel.x < 0)
		{
			vel.x += COMPASS_THRESHOLD;
		}
		
		if (vel.x > 0)
		{
			vel.x -= COMPASS_THRESHOLD;
		}
		
		// scale back to full range (-1.0 - 1.0)
		vel.multiply(1.0f / (1.0f - COMPASS_THRESHOLD));
		
		vel.multiply(3.0f);
		
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
		
		rotate *= 6;
		
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
}
