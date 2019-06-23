/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 27, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.helper.ShooterMemory;


/**
 * ShooterMemory calculates the best target in the goal for the bot that has the ball atm.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class ShooterMemoryCalc extends ACalculator
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private ShooterMemory	mem;
	private final Vector2	uniqueBestPoint	= new Vector2();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public ShooterMemoryCalc()
	{
		mem = new ShooterMemory();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		// BotID ballCarrier = curFrame.tacticalInfo.getBallPossession().getTigersId();
		// if ((ballCarrier != null) && ballCarrier.isBot())
		// {
		// if (!ballCarrier.equals(mem.getBotID()))
		// {
		// mem.setBotID(ballCarrier);
		// }
		// } else
		// {
		// if (mem.getBotID() != null)
		// {
		// mem = new ShooterMemory();
		// }
		// curFrame.tacticalInfo.setBestDirectShootTarget(null);
		// }
		
		// if ((mem.getBotID() != null) && mem.getBotID().isBot())
		{
			mem.update(curFrame);
			uniqueBestPoint.set(mem.getBestPoint());
			curFrame.tacticalInfo.setBestDirectShootTarget(uniqueBestPoint);
			curFrame.tacticalInfo.setValueOfBestDirectShootTarget(mem.getBestPointValue());
		}
	}
	
	
	@Override
	public void fallbackCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
