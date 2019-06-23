/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.08.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcs.interpreter;

import edu.dhbw.mannheim.tigers.robotcontrolutility.model.ActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.Device;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.Mode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcs.ARCCommandInterpreter;


/**
 * @see ARCCommandInterpreter
 * 
 * @author Gero
 */
public class TigerInterpreter extends ARCCommandInterpreter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final float	COMPASS_THRESHOLD		= 0.33f;
	
	/** Maximum speed the bot can drive (yeah, no replacement for a correct motion model) */
	private static final float	MAX_STRAIGHT_SPEED	= 1.2f;
	
	/** AndreR said: 7 turns per second, that is 2pi*7 [rad/s] */
	private static final float	MAX_TURN_SPEED			= 1.5f * AIMath.PI;	// 1 rotation/s (old: 43.98f)
																							
	/** Maximum speed for dribble roll: 8000 [rotations per minute] */
	private static final float	MAX_DRIBBLE_SPEED		= 25000f;			// 8000
																							
	/** Maximum discharge time for kick: 25000 [us] */
	private static final int	MAX_KICK_SPEED			= 1000;
	

	private boolean				dribblerOn				= false;
	private final TigerBot		bot;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
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
		/*
		 * X-Y-Translation
		 */
		final Vector2 vec = new Vector2();
		
		vec.x = (float) command.translateX;
		vec.y = (float) command.translateY;
		

		if (vec.y < COMPASS_THRESHOLD && vec.y > -COMPASS_THRESHOLD)
		{
			vec.y = 0;
		}
		
		if (vec.x < COMPASS_THRESHOLD && vec.x > -COMPASS_THRESHOLD)
		{
			vec.x = 0;
		}
		
		// Scale to robots speed
		vec.x *= MAX_STRAIGHT_SPEED - COMPASS_THRESHOLD;
		vec.y *= MAX_STRAIGHT_SPEED - COMPASS_THRESHOLD;
		
		/*
		 * Left-Right-Rotation
		 */
		float rotate = (float) command.rotate;
		
		if (rotate < COMPASS_THRESHOLD && rotate > -COMPASS_THRESHOLD)
		{
			rotate = 0;
		} else
		{
			rotate *= MAX_TURN_SPEED;
		}
		
		// System.out.println("Vec: " + vec + ", W: " + rotate + ", W1000: " + (int)rotate*1000);
		

		/*
		 * send vector and turn to bot
		 */
		TigerMotorMoveV2 cmd = new TigerMotorMoveV2();
		cmd.setXY(vec);
		cmd.setW(rotate);
		bot.execute(cmd);
		

		/*
		 * send Force Command to bot
		 */
		if (command.force > 0.5f)
		{
			TigerKickerKickV2 forceCmd = new TigerKickerKickV2();
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
			TigerDribble dribbleCmd = new TigerDribble();
			int rpm = (int) (command.dribble * MAX_DRIBBLE_SPEED);
			dribbleCmd.setSpeed(rpm);
			bot.execute(dribbleCmd);
			
		} else if (dribblerOn)
		{
			dribblerOn = false;
			TigerDribble dribbleCmd = new TigerDribble();
			int rpm = 0;
			dribbleCmd.setSpeed(rpm);
			bot.execute(dribbleCmd);
		}
		

		/*
		 * send Arm Command to bot
		 */
		if (command.arm > 0.5)
		{
			TigerKickerKickV2 armCmd = new TigerKickerKickV2();
			armCmd.setMode(Mode.ARM);
			armCmd.setDevice(Device.STRAIGHT);
			armCmd.setFiringDuration(MAX_KICK_SPEED);
			bot.execute(armCmd);
		}
		

		/*
		 * send Chipkick Command to bot
		 */
		if (command.chipKick > 0.5)
		{
			TigerKickerKickV2 chipCmd = new TigerKickerKickV2();
			chipCmd.setMode(Mode.FORCE);
			chipCmd.setDevice(Device.CHIP);
			chipCmd.setFiringDuration(MAX_KICK_SPEED / 2);
			bot.execute(chipCmd);
		}
	}
}
