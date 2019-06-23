/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 1, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;


/**
 * Interface for a receiver or redirecter role
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public abstract class AReceiverRole extends ARole
{
	/**
	 * @param type
	 */
	public AReceiverRole(ERole type)
	{
		super(type);
	}
	
	
	/**
	 * Indicate that the passer has shot.
	 * The receiver should be ready to receive the ball now and may do move corrections
	 */
	public abstract void setReady();
	
	
	/**
	 * @return if this receiver is ready to receive
	 */
	public abstract boolean isReady();
	
	
	/**
	 * @param passUsesChipper the passUsesChipper to set
	 */
	public abstract void setPassUsesChipper(boolean passUsesChipper);
	
	
	/**
	 * Updates the internal init position.<br>
	 * note, that the initPositio may be changed by this role as soon as it is in receive state.
	 * Calling this method during receive state will most probably have no effect
	 * 
	 * @param pos
	 */
	public abstract void setInitPosition(IVector2 pos);
}
