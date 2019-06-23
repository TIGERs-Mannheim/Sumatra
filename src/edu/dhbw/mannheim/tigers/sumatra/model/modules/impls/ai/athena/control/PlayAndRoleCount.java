/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 28, 2012
 * Author(s): andres
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.ESelectionReason;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;


/**
 * This class provides the information of how many roles a Play gets.
 * 
 * @author Daniel Andres
 * 
 */
@Embeddable
public class PlayAndRoleCount
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@Enumerated(EnumType.STRING)
	private EPlay					ePlay;
	private int						numRolesToAssign;
	@Enumerated(EnumType.STRING)
	private ESelectionReason	selectionReason	= ESelectionReason.UNKNOWN;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param ePlay
	 * @param numRolesToAssign
	 * @param selReason
	 */
	public PlayAndRoleCount(EPlay ePlay, int numRolesToAssign, ESelectionReason selReason)
	{
		this.ePlay = ePlay;
		this.numRolesToAssign = numRolesToAssign;
		selectionReason = selReason;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the ePlay
	 */
	public EPlay getePlay()
	{
		return ePlay;
	}
	
	
	/**
	 * @param ePlay the ePlay to set
	 */
	public void setePlay(EPlay ePlay)
	{
		this.ePlay = ePlay;
	}
	
	
	/**
	 * @return the numRolesToAssign
	 */
	public int getNumRolesToAssign()
	{
		return numRolesToAssign;
	}
	
	
	/**
	 * @param numRolesToAssign the numRolesToAssign to set
	 */
	public void setNumRolesToAssign(int numRolesToAssign)
	{
		this.numRolesToAssign = numRolesToAssign;
	}
	
	
	@Override
	public String toString()
	{
		return "[" + ePlay + "," + numRolesToAssign + "]";
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((ePlay == null) ? 0 : ePlay.hashCode());
		return (prime * result) + numRolesToAssign;
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
		PlayAndRoleCount other = (PlayAndRoleCount) obj;
		if (!ePlay.equals(other.ePlay))
		{
			return false;
		}
		if (numRolesToAssign != other.numRolesToAssign)
		{
			return false;
		}
		return true;
	}
	
	
	/**
	 * @return the selectionReason
	 */
	public final ESelectionReason getSelectionReason()
	{
		return selectionReason;
	}
	
	
	/**
	 * @param selectionReason the selectionReason to set
	 */
	public final void setSelectionReason(ESelectionReason selectionReason)
	{
		this.selectionReason = selectionReason;
	}
}
