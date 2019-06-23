/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.04.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ECriterion;


/**
 * This class determines the number of bots inside a certain rectangle. The
 * rectangle can be defined by giving the coordinates of the upper left and the
 * bottom right corner. It can be chosen between number of Tiger bots, number
 * of opponent bots or the dominance of a team inside a rectangle. If the
 * current situation relates to the desired situation the value 1 will be
 * returned. Otherwise the parameter 'penaltyFactor' will be returned.
 * 
 * @author FlorianS
 * 
 */
public class DynamicFieldRasterCrit extends ACriterion
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final int			numBots;
	private final ETeam		teamWish;
	private final EVariant	type;
	private final Vector2	ulCorner;
	private final Vector2	brCorner;
	
	/**
	 */
	public enum EVariant
	{
		/** */
		DOMINANCE,
		/** */
		MORE_OR_EQUAL_TIGERS,
		/** */
		LESS_OR_EQUAL_TIGERS,
		/** */
		MORE_OR_EQUAL_OPPONENTS,
		/** */
		LESS_OR_EQUAL_OPPONENTS
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * 
	 * @param numBots number of bots (depends on type)
	 * @param team
	 * @param ulCorner upper left corner coordinate
	 * @param brCorner bottom right corner coordinate
	 * @param type
	 */
	public DynamicFieldRasterCrit(int numBots, ETeam team, Vector2 ulCorner, Vector2 brCorner, EVariant type)
	{
		super(ECriterion.DYNAMIC_FIELD_RASTER);
		
		this.numBots = numBots;
		teamWish = team;
		this.ulCorner = ulCorner;
		this.brCorner = brCorner;
		this.type = type;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		int numberOfTigerBots = 0;
		int numberOfOpponentBots = 0;
		
		// find number of Tigers inside the raster field
		for (final TrackedBot currentBot : currentFrame.worldFrame.tigerBotsVisible.values())
		{
			if ((currentBot.getPos().x() >= ulCorner.x) && (currentBot.getPos().x() <= brCorner.x)
					&& (currentBot.getPos().y() <= ulCorner.y) && (currentBot.getPos().y() >= brCorner.y))
			{
				numberOfTigerBots++;
			}
		}
		
		// find number of opponents inside the raster field
		for (final TrackedBot currentBot : currentFrame.worldFrame.foeBots.values())
		{
			if ((currentBot.getPos().x() >= ulCorner.x) && (currentBot.getPos().x() <= brCorner.x)
					&& (currentBot.getPos().y() <= ulCorner.y) && (currentBot.getPos().y() >= brCorner.y))
			{
				numberOfOpponentBots++;
			}
		}
		
		ETeam dominance = getDominance(numberOfTigerBots, numberOfOpponentBots);
		
		return getScoreForType(type, numberOfTigerBots, numberOfOpponentBots, dominance);
	}
	
	
	private ETeam getDominance(int numberOfTigerBots, int numberOfOpponentBots)
	{
		ETeam dominance = ETeam.UNKNOWN;
		// check the dominance of a team inside the raster field
		if (numberOfTigerBots > numberOfOpponentBots)
		{
			dominance = ETeam.TIGERS;
		} else if (numberOfTigerBots < numberOfOpponentBots)
		{
			dominance = ETeam.OPPONENTS;
		} else
		{
			dominance = ETeam.EQUAL;
		}
		return dominance;
	}
	
	
	private float getScoreForType(EVariant type, int numberOfTigerBots, int numberOfOpponentBots, ETeam dominance)
	{
		switch (type)
		{
			case MORE_OR_EQUAL_TIGERS:
				if (numberOfTigerBots >= numBots)
				{
					return MAX_SCORE;
				}
				return MIN_SCORE;
			case LESS_OR_EQUAL_TIGERS:
				if (numberOfTigerBots <= numBots)
				{
					return MAX_SCORE;
				}
				return MIN_SCORE;
			case MORE_OR_EQUAL_OPPONENTS:
				if (numberOfOpponentBots >= numBots)
				{
					return MAX_SCORE;
				}
				return MIN_SCORE;
			case LESS_OR_EQUAL_OPPONENTS:
				if (numberOfOpponentBots <= numBots)
				{
					return MAX_SCORE;
				}
				return MIN_SCORE;
			case DOMINANCE:
				if (dominance.equals(teamWish))
				{
					return MAX_SCORE;
				}
				return MIN_SCORE;
			default:
				return MAX_SCORE;
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
