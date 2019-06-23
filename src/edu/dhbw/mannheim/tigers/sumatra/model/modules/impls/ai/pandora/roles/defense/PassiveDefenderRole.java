/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.02.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.penalty.PenaltyThemPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;


/**
 * Passive Role. Gets a destination position and a lookAt position
 * by the controlling Play.
 * Used by:<li> {@link PenaltyThemPlay}</li>.
 * 
 * 
 * @author Malte
 * 
 */
public class PassiveDefenderRole extends ADefenseRole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long	serialVersionUID	= 1941091761486677388L;
	

	private Vector2f				destination;
	private Vector2f				target;
	private boolean				isKeeper				= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public PassiveDefenderRole()
	{
		super(ERole.PASSIVE_DEFENDER_THEM);
		destination = new Vector2f(0, -AIConfig.getGeometry().getFieldLength() / 4);
		target = new Vector2f(AIConfig.getGeometry().getCenter());
	}
	

	public PassiveDefenderRole(IVector2 dest, IVector2 target)
	{
		super(ERole.PASSIVE_DEFENDER_THEM);
		this.destination = new Vector2f(dest);
		this.target = new Vector2f(target);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setDest(IVector2 d)
	{
		this.destination = new Vector2f(d);
		destCon.updateDestination(destination);
	}
	

	public void setTarget(IVector2 target)
	{
		this.target = new Vector2f(target);
	}
	

	@Override
	public void update(AIInfoFrame currentFrame)
	{
		destCon.updateDestination(destination);
		lookAtCon.updateTarget(target);
	}
	

	public void setKeeper(boolean isKeeper)
	{
		this.isKeeper = isKeeper;
	}
	

	@Override
	public boolean isKeeper()
	{
		return this.isKeeper;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public void setDestTolerance(float newTolerance)
	{
		destCon.setTolerance(newTolerance);
	}
}
