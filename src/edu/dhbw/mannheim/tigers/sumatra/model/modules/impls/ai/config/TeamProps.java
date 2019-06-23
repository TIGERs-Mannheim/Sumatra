/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 26.11.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.ETeamColors;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * The data-holder for team-specific things like our color, the direction of play and the keeper-id.
 * 
 * @author Gero
 */
@Embeddable
public class TeamProps implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long		serialVersionUID	= 4029974009206769185L;
	private static final Logger	log					= Logger.getLogger(TeamProps.class.getName());
	
	private boolean					tigersAreYellow;
	
	@Enumerated(EnumType.STRING)
	private ETeamColors				color;
	
	private BotID						keeperId;
	
	/** */
	private static final String	LEFT_TO_RIGHT		= "leftToRight";
	private boolean					leftToRight;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param configFile
	 */
	public TeamProps(Configuration configFile)
	{
		tigersAreYellow = configFile.getString("color").equalsIgnoreCase(ETeamColors.YELLOW.toString());
		color = tigersAreYellow ? ETeamColors.YELLOW : ETeamColors.BLUE;
		
		keeperId = new BotID(configFile.getInt("keeperId"));
		
		leftToRight = LEFT_TO_RIGHT.equals(configFile.getString("ourDirection"));
	}
	
	
	/**
	 * @param original
	 */
	public TeamProps(TeamProps original)
	{
		tigersAreYellow = original.tigersAreYellow;
		color = original.color;
		
		keeperId = original.keeperId;
		leftToRight = original.leftToRight;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the color
	 */
	public ETeamColors getTigersColor()
	{
		return color;
	}
	
	
	/**
	 * @return the keeperId
	 */
	public BotID getKeeperId()
	{
		return keeperId;
	}
	
	
	/**
	 * @return
	 */
	public boolean getTigersAreYellow()
	{
		return tigersAreYellow;
	}
	
	
	/**
	 * @return
	 */
	public boolean getPlayLeftToRight()
	{
		return leftToRight;
	}
	
	
	/**
	 * @param keeperId the keeperId to set
	 */
	public final void setKeeperId(BotID keeperId)
	{
		this.keeperId = keeperId;
	}
	
	
	/**
	 * @param keeperId the keeperId to set
	 */
	public final void setKeeperId(int keeperId)
	{
		try
		{
			this.keeperId = new BotID(keeperId);
		} catch (IllegalArgumentException e)
		{
			log.error("Could not set keeper id", e);
			keeperId = 0;
		}
	}
}
