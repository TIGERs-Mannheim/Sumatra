/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.passtarget;

import org.apache.commons.lang.Validate;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * An implementation of a pass target.
 */
@Persistent
public class PassTarget implements IPassTarget
{
	private final DynamicPosition targetPos;
	private final BotID botID;
	
	
	protected PassTarget()
	{
		targetPos = null;
		botID = null;
	}
	
	
	/**
	 * New Pass targets with required values
	 * 
	 * @param targetPos
	 * @param botID
	 */
	public PassTarget(final DynamicPosition targetPos, final BotID botID)
	{
		Validate.notNull(targetPos);
		Validate.notNull(botID);
		this.targetPos = targetPos;
		this.botID = botID;
	}
	
	
	@Override
	public IVector2 getPos()
	{
		return targetPos.getPos();
	}
	
	
	@Override
	public DynamicPosition getDynamicPos()
	{
		return targetPos;
	}
	
	
	@Override
	public BotID getBotId()
	{
		return botID;
	}
	
	
	@Override
	public String toString()
	{
		return "PassTarget{" +
				"targetPos=" + targetPos +
				", botID=" + botID +
				'}';
	}
}
