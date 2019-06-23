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

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ETeam;
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
	
	private int			wish;
	private ETeam		teamWish;
	private EVariant	type;
	private Vector2	ulCorner;
	private Vector2	brCorner;
	
	public enum EVariant
	{
		DOMINANCE,
		MORE_OR_EQUAL_TIGERS,
		LESS_OR_EQUAL_TIGERS,
		MORE_OR_EQUAL_OPPONENTS,
		LESS_OR_EQUAL_OPPONENTS
	};
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public DynamicFieldRasterCrit(int wish, float penaltyFactor, Vector2 ulCorner, Vector2 brCorner, EVariant type)
	{
		super(ECriterion.DYNAMIC_FIELD_RASTER, penaltyFactor);
		
		this.wish = wish;
		this.ulCorner = ulCorner;
		this.brCorner = brCorner;
		this.type = type;
	}
	

	public DynamicFieldRasterCrit(ETeam teamWish, float penaltyFactor, Vector2 ulCorner, Vector2 brCorner)
	{
		super(ECriterion.DYNAMIC_FIELD_RASTER, penaltyFactor);
		
		this.teamWish = teamWish;
		this.ulCorner = ulCorner;
		this.brCorner = brCorner;
		this.type = EVariant.DOMINANCE;
	}
	

	public DynamicFieldRasterCrit(int wish, Vector2 ulCorner, Vector2 brCorner, EVariant type)
	{
		super(ECriterion.DYNAMIC_FIELD_RASTER);
		
		this.wish = wish;
		this.ulCorner = ulCorner;
		this.brCorner = brCorner;
		this.type = type;
	}
	

	public DynamicFieldRasterCrit(ETeam teamWish, Vector2 ulCorner, Vector2 brCorner)
	{
		super(ECriterion.DYNAMIC_FIELD_RASTER);
		
		this.teamWish = teamWish;
		this.ulCorner = ulCorner;
		this.brCorner = brCorner;
		this.type = EVariant.DOMINANCE;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		int numberOfTigerBots = 0;
		int numberOfOpponentBots = 0;
		ETeam dominance;
		
		// find number of Tigers inside the raster field
		for (TrackedBot currentBot : currentFrame.worldFrame.tigerBots.values())
		{
			if (currentBot.pos.x >= ulCorner.x && currentBot.pos.x <= brCorner.x && currentBot.pos.y <= ulCorner.y
					&& currentBot.pos.y >= brCorner.y)
			{
				numberOfTigerBots++;
			}
		}
		
		// find number of opponents inside the raster field
		for (TrackedBot currentBot : currentFrame.worldFrame.foeBots.values())
		{
			if (currentBot.pos.x >= ulCorner.x && currentBot.pos.x <= brCorner.x && currentBot.pos.y <= ulCorner.y
					&& currentBot.pos.y >= brCorner.y)
			{
				numberOfOpponentBots++;
			}
		}
		
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
		

		switch (type)
		{
			case MORE_OR_EQUAL_TIGERS:
				if (numberOfTigerBots >= wish)
				{
					return 1.0f;
				} else
				{
					return penaltyFactor;
				}
			case LESS_OR_EQUAL_TIGERS:
				if (numberOfTigerBots <= wish)
				{
					return 1.0f;
				} else
				{
					return penaltyFactor;
				}
			case MORE_OR_EQUAL_OPPONENTS:
				if (numberOfOpponentBots >= wish)
				{
					return 1.0f;
				} else
				{
					return penaltyFactor;
				}
			case LESS_OR_EQUAL_OPPONENTS:
				if (numberOfOpponentBots <= wish)
				{
					return 1.0f;
				} else
				{
					return penaltyFactor;
				}
			case DOMINANCE:
				if (dominance == teamWish)
				{
					return 1.0f;
				} else
				{
					return penaltyFactor;
				}
			default:
				return 1.0f;
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
