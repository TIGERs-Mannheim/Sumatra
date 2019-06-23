/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.data.ballpossession;

import java.io.Serializable;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Contains the EBallPossession and the id of the bot, who has got the ball
 * 
 * @author DirkK
 */
@Persistent
public class BallPossession implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long serialVersionUID = -1819701506143270823L;
	
	private EBallPossession eBallPossession;
	private BotID opponentsId;
	private BotID tigersId;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Default
	 */
	public BallPossession()
	{
		eBallPossession = EBallPossession.NO_ONE;
		opponentsId = BotID.noBot();
		tigersId = BotID.noBot();
	}
	
	
	/**
	 * Deep copy constructor.
	 * 
	 * @param copy
	 */
	public BallPossession(final BallPossession copy)
	{
		eBallPossession = copy.eBallPossession;
		opponentsId = copy.opponentsId;
		tigersId = copy.tigersId;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Is the ball possessed by a bot from the other subtract team?
	 * This is only useful for mixed team mode
	 * 
	 * @param wFrame
	 * @return
	 */
	public boolean isPossessedByOtherSubTeam(final WorldFrame wFrame)
	{
		return tigersId.isBot() && wFrame.tigerBotsVisible.containsKey(tigersId)
				&& !wFrame.tigerBotsAvailable.containsKey(tigersId);
	}
	
	
	/**
	 * @param obj
	 * @return
	 */
	public boolean isEqual(final BallPossession obj)
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
	public void setEBallPossession(final EBallPossession eBallPossession)
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
	public void setOpponentsId(final BotID opponentsId)
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
	public void setTigersId(final BotID tigersId)
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
	public boolean equals(final Object obj)
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
