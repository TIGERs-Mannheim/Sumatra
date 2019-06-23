/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 27, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.FieldInformation;


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
	public float getDstOrient()
	{
		return moveCon.getAngleCon().getTargetAngle();
	}
	
	
	/**
	 * @return the target
	 */
	public IVector2 getDestination()
	{
		return moveCon.getDestCon().getDestination();
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