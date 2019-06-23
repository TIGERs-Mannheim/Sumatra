/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 23, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;


/**
 * Possible game states which depend on incoming referee messages and some other constraints
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public enum EGameState
{
	/**  */
	UNKNOWN,
	
	/**  */
	HALTED,
	/**  */
	STOPPED,
	/**  */
	RUNNING,
	
	/**  */
	TIMEOUT_WE,
	/**  */
	TIMEOUT_THEY,
	
	/**  */
	PREPARE_KICKOFF_WE,
	/**  */
	PREPARE_KICKOFF_THEY,
	
	/**  */
	PREPARE_PENALTY_WE,
	/**  */
	PREPARE_PENALTY_THEY,
	
	/**  */
	THROW_IN_WE,
	/**  */
	THROW_IN_THEY,
	
	/**  */
	CORNER_KICK_WE,
	/**  */
	CORNER_KICK_THEY,
	
	/**  */
	GOAL_KICK_WE,
	/**  */
	GOAL_KICK_THEY,
	
	/** A direkt free kick within the field */
	DIRECT_KICK_WE,
	/** A direkt free kick within the field */
	DIRECT_KICK_THEY;
	
	/**
	 * Get the required ball position for this game state
	 * 
	 * @param frame
	 * @return
	 */
	public IVector2 getRequiredBallPos(IRecordFrame frame)
	{
		IVector2 marker = null;
		float fLength = AIConfig.getGeometry().getFieldLength();
		float fWidth = AIConfig.getGeometry().getFieldWidth();
		int ballSide = frame.getWorldFrame().getBall().getPos().y() > 0 ? 1 : -1;
		switch (this)
		{
			case CORNER_KICK_THEY:
				marker = new Vector2(-((fLength / 2) - 100), ballSide * ((fWidth / 2) - 100));
				break;
			case CORNER_KICK_WE:
				marker = new Vector2((fLength / 2) - 100, ballSide * ((fWidth / 2) - 100));
				break;
			case GOAL_KICK_THEY:
				marker = new Vector2(((fLength / 2) - 500), ballSide * ((fWidth / 2) - 100));
				break;
			case GOAL_KICK_WE:
				marker = new Vector2(-((fLength / 2) - 500), ballSide * ((fWidth / 2) - 100));
				break;
			case DIRECT_KICK_THEY:
			case DIRECT_KICK_WE:
				marker = frame.getWorldFrame().getBall().getPos();
				break;
			case THROW_IN_THEY:
			case THROW_IN_WE:
				if (frame.getTacticalField().getBallLeftFieldPos() != null)
				{
					marker = frame.getTacticalField().getBallLeftFieldPos().addNew(new Vector2(0, -ballSide * 100));
				}
				break;
			case PREPARE_KICKOFF_THEY:
			case PREPARE_KICKOFF_WE:
				marker = AIConfig.getGeometry().getCenter();
				break;
			case STOPPED:
				marker = frame.getWorldFrame().getBall().getPos();
				break;
			default:
				break;
		}
		return marker;
	}
}
