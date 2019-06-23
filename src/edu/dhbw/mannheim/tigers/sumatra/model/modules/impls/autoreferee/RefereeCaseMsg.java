/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 13, 2014
 * Author(s): lukas
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.autoreferee;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * @author lukas
 */
public class RefereeCaseMsg
{
	/**
	 * @author lukas
	 */
	public enum EMsgType
	{
		/**
		 * The ball has exited the field
		 */
		OUT_OF_BOUNDS,
		/**
		 * The defense rule regarding more than one player in the defense area has been violated.
		 * A robot other than the keeper has partially entered the penalty area
		 */
		PENALTY_PARTIAL,
		/**
		 * The defense rule regarding more than one player in the defense area has been violated.
		 * A robot other than the keeper has fully entered the penalty area
		 */
		PENALTY_FULL,
		
		/**
		 * The ball was kicked above the speed limit
		 */
		BALL_SPEED,
		
		/**
		 * A bot was too fast during stop
		 */
		BOT_SPEED_STOP,
		
		/**  */
		TOO_NEAR_TO_BALL,
		
		/**  */
		KICKOFF_PLACEMENT
	}
	
	private final ETeamColor	teamAtFault;
	private final EMsgType		msgType;
	
	
	// optional
	private BotID					botAtFault		= BotID.createBotId();
	private String					additionalInfo	= "";
	
	
	/**
	 * @param teamAtFault
	 * @param msgType
	 */
	public RefereeCaseMsg(final ETeamColor teamAtFault, final EMsgType msgType)
	{
		this.teamAtFault = teamAtFault;
		this.msgType = msgType;
	}
	
	
	/**
	 * @return the teamAtFault
	 */
	public ETeamColor getTeamAtFault()
	{
		return teamAtFault;
	}
	
	
	/**
	 * @return the msgType
	 */
	public EMsgType getMsgType()
	{
		return msgType;
	}
	
	
	/**
	 * @return the botAtFault
	 */
	public final BotID getBotAtFault()
	{
		return botAtFault;
	}
	
	
	/**
	 * @return the additionalInfo
	 */
	public final String getAdditionalInfo()
	{
		return additionalInfo;
	}
	
	
	/**
	 * @param additionalInfo the additionalInfo to set
	 */
	public final void setAdditionalInfo(final String additionalInfo)
	{
		this.additionalInfo = additionalInfo;
	}
	
	
	/**
	 * @param botAtFault the botAtFault to set
	 */
	public final void setBotAtFault(final BotID botAtFault)
	{
		this.botAtFault = botAtFault;
	}
}
