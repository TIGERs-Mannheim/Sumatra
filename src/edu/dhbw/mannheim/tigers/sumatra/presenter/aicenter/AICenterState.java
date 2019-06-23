/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.AthenaControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;


/**
 * The basic implementation of {@link IAICenterState}. Provides supporting methods and an implementation for very method
 * for easy debugging.
 * 
 * @author Gero
 */
public abstract class AICenterState implements IAICenterState
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger			log	= Logger.getLogger(AICenterState.class.getName());
	
	protected final AICenterPresenter	presenter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param presenter
	 */
	public AICenterState(AICenterPresenter presenter)
	{
		this.presenter = presenter;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Prepare {@link AthenaControl} for the use with this {@link IAICenterState}
	 */
	public void init()
	{
		getControl().clear();
		sendControl();
	}
	
	
	// --------------------------------------------------------------------------
	// --- roles ----------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void addRole(ERole role, BotID botId)
	{
		log.warn("This behavior is not intended by the AICenterPresenter!");
	}
	
	
	@Override
	public void addRole(ERole role)
	{
		log.warn("This behavior is not intended by the AICenterPresenter!");
	}
	
	
	@Override
	public void removeRole(ERole role)
	{
		log.warn("This behavior is not intended by the AICenterPresenter!");
	}
	
	
	@Override
	public void clearRoles()
	{
		log.warn("This behavior is not intended by the AICenterPresenter!");
	}
	
	
	// --------------------------------------------------------------------------
	// --- plays ----------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void addNewPlay(EPlay play, int numRolesToAssign)
	{
		log.warn("This behavior is not intended by the AICenterPresenter!");
	}
	
	
	@Override
	public void removePlay(APlay play)
	{
		log.warn("This behavior is not intended by the AICenterPresenter!");
	}
	
	
	// --------------------------------------------------------------------------
	// --- match ----------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void forceNewDecision()
	{
		log.warn("This behavior is not intended by the AICenterPresenter!");
	}
	
	
	// --------------------------------------------------------------------------
	// --- ApollonControl - should always work-----------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	protected AthenaControl getControl()
	{
		return presenter.getControl();
	}
	
	
	protected void sendControl()
	{
		presenter.sendControl();
	}
}
