/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.11.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.DestinationCon;


/**
 * Role base class with a {@link DestinationCon} and implementations for {@link #initDestination(Vector2f)} and
 * {@link #getDestination()}!
 * 
 * @author Gero
 * 
 */
public abstract class ABaseRole extends ARole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long			serialVersionUID	= 8046760741028468715L;
	

	/** The {@link ABaseRole}s {@link DestinationCon}. <b>Already added, do not add it again!!!</b> */
	protected final DestinationCon	destCon;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param type Simply passed to {@link ARole#ARole(ERole)}
	 */
	public ABaseRole(ERole type)
	{
		super(type);
		
		this.destCon = new DestinationCon();
		addCondition(destCon);
	}
	

	/**
	 * @param type Simply passed to {@link ARole#ARole(ERole)}
	 * @param tolerance The {@link DestinationCon}s ({@link #destCon}) tolerance!
	 */
	public ABaseRole(ERole type, float tolerance)
	{
		super(type);
		
		this.destCon = new DestinationCon(tolerance);
		addCondition(destCon);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void initDestination(IVector2 destination)
	{
		destCon.updateDestination(destination);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public IVector2 getDestination()
	{
		return destCon.getDestination();
	}
}
