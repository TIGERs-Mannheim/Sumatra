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

import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.AthenaControl;
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
	private final Logger						log	= Logger.getLogger(getClass());
	
	protected final AICenterPresenter	presenter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param panel
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
	public void addRole(ERole role, int botId)
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
	public void addPlay(EPlay play)
	{
		log.warn("This behavior is not intended by the AICenterPresenter!");
	}
	

	@Override
	public void removePlay(List<EPlay> oddPlays)
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
