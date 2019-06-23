/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.10.2010
 * Author(s):
 * Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions;


import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;


/**
 * Implementation of {@link AVisibleCon} which checks if
 * there are obstacle between the start point and end point.
 * <strong>Note: </strong>if the end-point is a bot-position, do not forget to add that bot to the ignore-list (via
 * {@linkplain #addToIgnore(int)})
 * 
 * @author DanielW, Oliver Steinbrecher <OST1988@aol.com>
 */
public class VisibleCon extends AVisibleCon
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public VisibleCon()
	{
		super(ECondition.VISIBLE);
		
	}
	

	public VisibleCon(IVector2 start, IVector2 end)
	{
		super(ECondition.VISIBLE);
		
		updateStart(start);
		updateEnd(end);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public boolean doCheckCondition(WorldFrame worldFrame, int botID)
	{
		addToIgnore(botID);
		return checkVisibility(worldFrame);
	}
	

	@Override
	public void updateEnd(IVector2 end)
	{
		super.updateEnd(end);
	}
	

	@Override
	public void updateStart(IVector2 start)
	{
		super.updateStart(start);
	}
	

	// --------------------------------------------------------------------------
	// --- setter/getter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public IVector2 getEnd()
	{
		return super.getEnd();
	}
	

	@Override
	public IVector2 getStart()
	{
		return super.getStart();
	}
	

}
