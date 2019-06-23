/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 27, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.sisyphus;

import edu.tigers.sumatra.ai.sisyphus.finder.FieldInformation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.skillsystem.MovementCon;


/**
 * data holder for the input data passed to the path planning
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public final class PathFinderInput
{
	private final BotID					botId;
	private final MovementCon			moveCon;
	/** Information about the Field, calculates whether a way is free */
	private final FieldInformation	fieldInfo;
	
	
	/**
	 * @param botId
	 * @param moveCon
	 */
	public PathFinderInput(final BotID botId, final MovementCon moveCon)
	{
		this.botId = botId;
		this.moveCon = moveCon;
		fieldInfo = new FieldInformation(botId, moveCon);
	}
	
	
	/**
	 * @return the botId
	 */
	public BotID getBotId()
	{
		return botId;
	}
	
	
	/**
	 * @return the dstOrient
	 */
	public double getDstOrient()
	{
		return moveCon.getTargetAngle();
	}
	
	
	/**
	 * @return the target
	 */
	public IVector2 getDestination()
	{
		return moveCon.getDestination();
	}
	
	
	/**
	 * @return the moveCon
	 */
	public MovementCon getMoveCon()
	{
		return moveCon;
	}
	
	
	/**
	 * @return the fieldInfo
	 */
	public FieldInformation getFieldInfo()
	{
		return fieldInfo;
	}
}