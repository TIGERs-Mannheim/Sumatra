/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.sim;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class SumatraBotPair
{
	private SumatraBot	botOne;
	private SumatraBot	botTwo;
	
	
	/**
	 * @param botOne
	 * @param botTwo
	 */
	public SumatraBotPair(final SumatraBot botOne, final SumatraBot botTwo)
	{
		this.botOne = botOne;
		this.botTwo = botTwo;
	}
	
	
	/**
	 * @return
	 */
	public List<SumatraBot> getBotPair()
	{
		List<SumatraBot> botPair = new ArrayList<>();
		
		botPair.add(botOne);
		botPair.add(botTwo);
		
		return botPair;
	}
	
	
	/**
	 * @return The vector between the two bots, seen from the first bot
	 */
	public IVector2 getVectorBetweenBots()
	{
		IVector2 returnVector = botOne.getPos().getXYVector();
		
		returnVector = returnVector.subtractNew(botTwo.getPos().getXYVector());
		
		return returnVector;
	}
	
}
