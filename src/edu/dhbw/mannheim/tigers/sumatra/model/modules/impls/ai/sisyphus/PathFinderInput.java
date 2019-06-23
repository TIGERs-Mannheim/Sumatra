/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 27, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus;

import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;


/**
 * data holder for the input data passed to the path planning
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class PathFinderInput
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final BotID					botId;
	private final MovementCon			moveCon;
	private final Map<BotID, Path>	existingPathes;
	private final FieldInformation	fieldInfo;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param botId
	 * @param existingPathes a list of the pathes from the last frame (all pathes are null at the beginning)
	 * @param currentTimeOnSpline
	 * @param moveCon
	 */
	public PathFinderInput(BotID botId, Map<BotID, Path> existingPathes, float currentTimeOnSpline, MovementCon moveCon)
	{
		this.botId = botId;
		this.existingPathes = existingPathes;
		this.moveCon = moveCon;
		
		fieldInfo = new FieldInformation(botId, moveCon);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * 
	 * @param swf
	 */
	public void update(SimpleWorldFrame swf)
	{
		fieldInfo.updateWorldFrame(swf);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
	 * @return the existingPathes
	 */
	public Map<BotID, Path> getExistingPathes()
	{
		return existingPathes;
	}
	
	
	/**
	 * @return the fieldInfo
	 */
	public FieldInformation getFieldInfo()
	{
		return fieldInfo;
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
	public final MovementCon getMoveCon()
	{
		return moveCon;
	}
	
}