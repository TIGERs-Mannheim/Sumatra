/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;


/**
 * data holder for a set of plays which were chosen at the same time
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
@Entity
public class PlayCombinations
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private List<PlayStats>	offensive	= new ArrayList<PlayStats>(0);
	private List<PlayStats>	deffensive	= new ArrayList<PlayStats>(0);
	private List<PlayStats>	supports		= new ArrayList<PlayStats>(0);
	private List<PlayStats>	others		= new ArrayList<PlayStats>(0);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param offensive
	 * @param deffensive
	 * @param supports
	 * @param others
	 */
	public PlayCombinations(List<PlayStats> offensive, List<PlayStats> deffensive, List<PlayStats> supports,
			List<PlayStats> others)
	{
		super();
		this.offensive = offensive;
		this.deffensive = deffensive;
		this.supports = supports;
		this.others = others;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the offensive
	 */
	public List<PlayStats> getOffensive()
	{
		return offensive;
	}
	
	
	/**
	 * @return the deffensive
	 */
	public List<PlayStats> getDeffensive()
	{
		return deffensive;
	}
	
	
	/**
	 * @return the supports
	 */
	public List<PlayStats> getSupports()
	{
		return supports;
	}
	
	
	/**
	 * @return A deffense-, offense-, others- or supportsplay that is not null
	 */
	public PlayStats getFirstPlayStats()
	{
		if (getDeffensive().size() > 0)
		{
			return getDeffensive().get(0);
		}
		if (getOffensive().size() > 0)
		{
			return getOffensive().get(0);
		}
		if (getOthers().size() > 0)
		{
			return getOthers().get(0);
		}
		if (getSupports().size() > 0)
		{
			return getSupports().get(0);
		}
		return null;
	}
	
	
	/**
	 * @param offensive the offensive to set
	 */
	public void setOffensive(List<PlayStats> offensive)
	{
		this.offensive = offensive;
	}
	
	
	/**
	 * @param deffensive the deffensive to set
	 */
	public void setDeffensive(List<PlayStats> deffensive)
	{
		this.deffensive = deffensive;
	}
	
	
	/**
	 * @param supports the supports to set
	 */
	public void setSupports(List<PlayStats> supports)
	{
		this.supports = supports;
	}
	
	
	/**
	 * @return the others
	 */
	public List<PlayStats> getOthers()
	{
		return others;
	}
	
	
	/**
	 * @param others the others to set
	 */
	public void setOthers(List<PlayStats> others)
	{
		this.others = others;
	}
	
	
}
