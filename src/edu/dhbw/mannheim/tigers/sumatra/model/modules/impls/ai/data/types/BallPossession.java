/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.05.2011
 * Author(s): DirkK
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types;

/**
 * Contains the EBallPossession and the id of the bot, who has got the ball
 * 
 * 
 * @author DirkK
 * 
 */
public class BallPossession
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private EBallPossession	eBallPossession;
	private int					opponentsId;
	private int					tigersId;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public BallPossession()
	{
		eBallPossession = EBallPossession.UNKNOWN;
		opponentsId = -1;
		tigersId = -1;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public boolean isEqual(BallPossession obj)
	{
		return eBallPossession != obj.getEBallPossession() && opponentsId != obj.getOpponentsId()
				&& tigersId != obj.getTigersId();
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
	public int getOpponentsId()
	{
		return opponentsId;
	}
	

	/**
	 * @param opponentsId the opponentsId to set
	 */
	public void setOpponentsId(int opponentsId)
	{
		this.opponentsId = opponentsId;
	}
	

	/**
	 * @return the tigersId
	 */
	public int getTigersId()
	{
		return tigersId;
	}
	

	/**
	 * @param tigersId the tigersId to set
	 */
	public void setTigersId(int tigersId)
	{
		this.tigersId = tigersId;
	}
}
