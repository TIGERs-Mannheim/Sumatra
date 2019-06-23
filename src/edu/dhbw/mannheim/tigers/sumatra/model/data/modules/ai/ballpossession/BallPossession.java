/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.05.2011
 * Author(s): DirkK
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * Contains the EBallPossession and the id of the bot, who has got the ball
 * 
 * 
 * @author DirkK
 * 
 */
@Embeddable
public class BallPossession implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long	serialVersionUID	= -1819701506143270823L;
	
	@Enumerated(EnumType.STRING)
	private EBallPossession		eBallPossession;
	private BotID					opponentsId;
	private BotID					tigersId;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public BallPossession()
	{
		eBallPossession = EBallPossession.UNKNOWN;
		opponentsId = new BotID();
		tigersId = new BotID();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Is the ball possessed by a bot from the other sub team?
	 * This is only useful for mixed team mode
	 * 
	 * @param wFrame
	 * @return
	 */
	public boolean isPossessedByOtherSubTeam(WorldFrame wFrame)
	{
		if (tigersId.isBot() && wFrame.tigerBotsVisible.containsKey(tigersId)
				&& !wFrame.tigerBotsAvailable.containsKey(tigersId))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * @param obj
	 * @return
	 */
	public boolean isEqual(BallPossession obj)
	{
		return (eBallPossession != obj.getEBallPossession()) && !opponentsId.equals(obj.getOpponentsId())
				&& !tigersId.equals(obj.getTigersId());
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the eBallPossession
	 */
	public EBallPossession getEBallPossession()
	{
		return eBallPossession;
	}
	
	
	/**
	 * @param eBallPossession the eBallPossession to set
	 */
	public void setEBallPossession(EBallPossession eBallPossession)
	{
		this.eBallPossession = eBallPossession;
	}
	
	
	/**
	 * @return the opponentsId
	 */
	public BotID getOpponentsId()
	{
		return opponentsId;
	}
	
	
	/**
	 * @param opponentsId the opponentsId to set
	 */
	public void setOpponentsId(BotID opponentsId)
	{
		this.opponentsId = opponentsId;
	}
	
	
	/**
	 * @return the tigersId
	 */
	public BotID getTigersId()
	{
		return tigersId;
	}
	
	
	/**
	 * @param tigersId the tigersId to set
	 */
	public void setTigersId(BotID tigersId)
	{
		this.tigersId = tigersId;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((eBallPossession == null) ? 0 : eBallPossession.hashCode());
		result = (prime * result) + ((opponentsId == null) ? 0 : opponentsId.hashCode());
		result = (prime * result) + ((tigersId == null) ? 0 : tigersId.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final BallPossession other = (BallPossession) obj;
		if (eBallPossession != other.eBallPossession)
		{
			return false;
		}
		if (opponentsId == null)
		{
			if (other.opponentsId != null)
			{
				return false;
			}
		} else if (!opponentsId.equals(other.opponentsId))
		{
			return false;
		}
		if (tigersId == null)
		{
			if (other.tigersId != null)
			{
				return false;
			}
		} else if (!tigersId.equals(other.tigersId))
		{
			return false;
		}
		return true;
	}
}
