/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerSystemPerformance;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class Performance
{
	@Configurable(spezis = { "GRSIM" }, defValue = "2")
	private float	accMax			= 2f;
	@Configurable(spezis = { "GRSIM" }, defValue = "2")
	private float	brkMax			= 2f;
	
	@Configurable(spezis = { "GRSIM" }, defValue = "2")
	private float	velMax			= 2.0f;
	
	@Configurable(spezis = { "GRSIM" }, defValue = "30")
	private float	accMaxW			= 30f;
	@Configurable(spezis = { "GRSIM" }, defValue = "30")
	private float	brkMaxW			= 30f;
	
	@Configurable(spezis = { "GRSIM" }, defValue = "10")
	private float	velMaxW			= 10f;
	
	private float	velMaxOverride	= -1;
	
	
	/**
	 * 
	 */
	public Performance()
	{
	}
	
	
	/**
	 * @param perfCmd
	 */
	public Performance(final TigerSystemPerformance perfCmd)
	{
		accMax = perfCmd.getAccMax();
		accMaxW = perfCmd.getAccMaxW();
		brkMax = perfCmd.getBrkMax();
		brkMaxW = perfCmd.getBrkMaxW();
		velMax = perfCmd.getVelMax();
		velMaxW = perfCmd.getVelMaxW();
	}
	
	
	/**
	 * @return the accMax
	 */
	public final float getAccMax()
	{
		return accMax;
	}
	
	
	/**
	 * @return the accMaxW
	 */
	public final float getAccMaxW()
	{
		return accMaxW;
	}
	
	
	/**
	 * @return the brkMax
	 */
	public final float getBrkMax()
	{
		return brkMax;
	}
	
	
	/**
	 * @return the brkMaxW
	 */
	public final float getBrkMaxW()
	{
		return brkMaxW;
	}
	
	
	/**
	 * @return the velMax
	 */
	public final float getVelMax()
	{
		if (velMaxOverride > 0)
		{
			return velMaxOverride;
		}
		return velMax;
	}
	
	
	/**
	 * @return the velMaxW
	 */
	public final float getVelMaxW()
	{
		return velMaxW;
	}
	
	
	/**
	 * @param accMax the accMax to set
	 */
	public final void setAccMax(final float accMax)
	{
		this.accMax = accMax;
	}
	
	
	/**
	 * @param accMaxW the accMaxW to set
	 */
	public final void setAccMaxW(final float accMaxW)
	{
		this.accMaxW = accMaxW;
	}
	
	
	/**
	 * @param brkMax the brkMax to set
	 */
	public final void setBrkMax(final float brkMax)
	{
		this.brkMax = brkMax;
	}
	
	
	/**
	 * @param brkMaxW the brkMaxW to set
	 */
	public final void setBrkMaxW(final float brkMaxW)
	{
		this.brkMaxW = brkMaxW;
	}
	
	
	/**
	 * @param velMax the velMax to set
	 */
	public final void setVelMax(final float velMax)
	{
		this.velMax = velMax;
	}
	
	
	/**
	 * @param velMaxW the velMaxW to set
	 */
	public final void setVelMaxW(final float velMaxW)
	{
		this.velMaxW = velMaxW;
	}
	
	
	/**
	 * @return the velMaxOverride
	 */
	public final float getVelMaxOverride()
	{
		return velMaxOverride;
	}
	
	
	/**
	 * @param velMaxOverride the velMaxOverride to set
	 */
	public final void setVelMaxOverride(final float velMaxOverride)
	{
		this.velMaxOverride = velMaxOverride;
	}
}
