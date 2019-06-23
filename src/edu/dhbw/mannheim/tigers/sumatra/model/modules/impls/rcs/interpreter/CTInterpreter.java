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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.CtBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTSetSpeed;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcs.ARCCommandInterpreter;

/**
 * @see ARCCommandInterpreter
 * 
 * @author Gero
 * 
 */
public class CTInterpreter extends ARCCommandInterpreter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final float				COMPASS_THRESHOLD	= 0.05f;
	
	
	private final CtBot bot;
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public CTInterpreter(ABot bot)
	{
		super(EBotType.CT);
		
		this.bot = (CtBot) bot;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void interpret(ActionCommand command)
	{
		
		final Vector2 vector = new Vector2();
		float angularVelocityBoost = 0;
		
		// First analog stick
		float rotate = (float) command.rotate;
		float translate = (float) command.translateY;
		

		if (translate < COMPASS_THRESHOLD && translate > -COMPASS_THRESHOLD)
		{
			vector.y = 0;
		} else
		{
			vector.y = (float) (Math.pow(translate * -1.0f, 3) * bot.getMaxSpeed(0)) * (1.5f - Math.abs(rotate));
		}
		
		// --- send vector to bot ---
		bot.execute(new CTSetSpeed(vector));
		
		
		if (rotate < COMPASS_THRESHOLD && rotate > -COMPASS_THRESHOLD)
		{
			angularVelocityBoost = 0;
		} else
		{
			angularVelocityBoost = rotate * bot.getMaxAngularVelocity() / 5;
		}
		
		
		// --- send turn to bot ---
		bot.execute(new CTSetSpeed(angularVelocityBoost));
	}
}
