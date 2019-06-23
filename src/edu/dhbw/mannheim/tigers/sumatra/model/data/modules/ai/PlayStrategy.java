/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.01.2011
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EMatchBehavior;


/**
 * This stores all tactical information.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
@Entity
public class PlayStrategy implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log					= Logger.getLogger(PlayStrategy.class.getName());
	
	/**  */
	private static final long		serialVersionUID	= 3929719828068763160L;
	
	private transient List<APlay>	activePlays;
	
	/** Contains all finished plays of the last cycle */
	private transient List<APlay>	finishedPlays;
	
	private boolean					changedPlay;
	/** Still allowed to write {@link #changedPlay}? ({@link #setChangedPlay()}) */
	private boolean					changedPlayLock;
	
	private EMatchBehavior			matchBehavior;
	/** Still allowed to write {@link #matchBehavior}? ({@link #setMatchBehavior(EMatchBehavior)}) */
	private boolean					matchBehaviorLock;
	
	
	private boolean					forceNewDecision;
	
	private boolean					stateChanged;
	
	private BotConnection			botConnection;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public PlayStrategy()
	{
		activePlays = new ArrayList<APlay>();
		finishedPlays = new ArrayList<APlay>();
		
		changedPlay = false;
		changedPlayLock = true;
		
		forceNewDecision = false;
		setStateChanged(false);
		
		matchBehavior = EMatchBehavior.NOT_DEFINED;
		matchBehaviorLock = true;
		
		botConnection = new BotConnection(false, false, false, false, false, false);
	}
	
	
	/**
	 * Providing a <strong>shallow</strong> copy of original (Thus collections are created, but filled with the same
	 * values
	 * @param original
	 */
	public PlayStrategy(PlayStrategy original)
	{
		activePlays = new ArrayList<APlay>(original.activePlays);
		finishedPlays = new ArrayList<APlay>(original.finishedPlays);
		
		changedPlay = original.changedPlay;
		changedPlayLock = original.changedPlayLock;
		
		forceNewDecision = original.forceNewDecision;
		
		matchBehavior = original.matchBehavior;
		matchBehaviorLock = original.matchBehaviorLock;
		
		botConnection = original.botConnection;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param b
	 */
	public void setBotConnection(BotConnection b)
	{
		botConnection = b;
	}
	
	
	/**
	 * @return
	 */
	public List<APlay> getActivePlays()
	{
		return activePlays;
	}
	
	
	/**
	 * @return if play has changed
	 */
	public boolean hasPlayChanged()
	{
		return changedPlay;
	}
	
	
	/**
	 * Sets the indicator for a change in the selected plays.
	 * By calling this indicator is set to true. <strong>Default:</strong> false.
	 * <p>
	 * <strong>NOTE:</strong> Only first call of this method has an effect!!!
	 * </p>
	 */
	public void setChangedPlay()
	{
		if (changedPlayLock)
		{
			changedPlay = true;
			changedPlayLock = false;
		} else
		{
			log.debug("setChangedPlay called more then once!");
		}
	}
	
	
	/**
	 * 
	 * Set play behavior for this frame. <strong>Default:</strong> NOT_DEFINED.
	 * <p>
	 * <strong>NOTE:</strong> Only first call of this method has an effect!!!
	 * </p>
	 * 
	 * @param matchBehavior the matchBehavior to set
	 */
	public void setMatchBehavior(EMatchBehavior matchBehavior)
	{
		if (matchBehaviorLock)
		{
			this.matchBehavior = matchBehavior;
			matchBehaviorLock = false;
		} else
		{
			log.warn("Change of match-behavior in AIInfoFrame " + this + " denied!");
		}
	}
	
	
	/**
	 * @return
	 */
	public EMatchBehavior getMatchBehavior()
	{
		return matchBehavior;
	}
	
	
	/**
	 * @return
	 */
	public List<APlay> getFinishedPlays()
	{
		return finishedPlays;
	}
	
	
	/**
	 * Sets a flag which causes a new play-decision.
	 * By calling this indicator is set to true
	 */
	public void setForceNewDecision()
	{
		forceNewDecision = true;
	}
	
	
	/**
	 * @return
	 */
	public boolean isForceNewDecision()
	{
		return forceNewDecision;
	}
	
	
	/**
	 * @param stateChanged the stateChanged to set
	 */
	public void setStateChanged(boolean stateChanged)
	{
		this.stateChanged = stateChanged;
	}
	
	
	/**
	 * @return the stateChanged
	 */
	public boolean isStateChanged()
	{
		return stateChanged;
	}
	
	
	/**
	 * @return
	 */
	public BotConnection getBotConnection()
	{
		return botConnection;
	}
}
