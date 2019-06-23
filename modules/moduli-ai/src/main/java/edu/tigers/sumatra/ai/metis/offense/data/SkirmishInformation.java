/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.10.2016
 * Author(s): MarkG
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.offense.data;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author MarkG
 */
public class SkirmishInformation
{
	
	
	public enum ESkirmishStrategy
	{
		BLOCKING,
		FREE_BALL,
		SUPPORTIVE_PASS,
		NONE
	}
	
	private boolean				skirmishDetected				= false;
	
	private double					skirmishIntensity				= 0;
	
	private DynamicPosition		correctedTarget				= null;
	
	private ESkirmishStrategy	strategy							= ESkirmishStrategy.NONE;
	
	private DynamicPosition		enemyPos							= null;
	
	private IVector2				supportiveCircleCatchPos	= null;
	
	private boolean				startCircleMove				= false;
	
	
	/**
	 * @return the skirmishDetected
	 */
	public boolean isSkirmishDetected()
	{
		return skirmishDetected;
	}
	
	
	/**
	 * @param skirmishDetected the skirmishDetected to set
	 */
	public void setSkirmishDetected(final boolean skirmishDetected)
	{
		this.skirmishDetected = skirmishDetected;
	}
	
	
	/**
	 * @return the skirmishIntensity
	 */
	public double getSkirmishIntensity()
	{
		return skirmishIntensity;
	}
	
	
	/**
	 * @param skirmishIntensity the skirmishIntensity to set
	 */
	public void setSkirmishIntensity(final double skirmishIntensity)
	{
		this.skirmishIntensity = skirmishIntensity;
	}
	
	
	/**
	 * @return the correctedTarget
	 */
	public DynamicPosition getCorrectedTarget()
	{
		return correctedTarget;
	}
	
	
	/**
	 * @param correctedTarget the correctedTarget to set
	 */
	public void setCorrectedTarget(final DynamicPosition correctedTarget)
	{
		this.correctedTarget = correctedTarget;
	}
	
	
	/**
	 * @return the strategy
	 */
	public ESkirmishStrategy getStrategy()
	{
		return strategy;
	}
	
	
	/**
	 * @param strategy the strategy to set
	 */
	public void setStrategy(final ESkirmishStrategy strategy)
	{
		this.strategy = strategy;
	}
	
	
	/**
	 * @return the enemyPos
	 */
	public DynamicPosition getEnemyPos()
	{
		return enemyPos;
	}
	
	
	/**
	 * @param enemyPos the enemyPos to set
	 */
	public void setEnemyPos(DynamicPosition enemyPos)
	{
		this.enemyPos = enemyPos;
	}
	
	
	public IVector2 getSupportiveCircleCatchPos()
	{
		return supportiveCircleCatchPos;
	}
	
	
	public void setSupportiveCircleCatchPos(final IVector2 supportiveCircleCatchPos)
	{
		this.supportiveCircleCatchPos = supportiveCircleCatchPos;
	}
	
	
	public boolean isStartCircleMove()
	{
		return startCircleMove;
	}
	
	
	public void setStartCircleMove(final boolean startCircleMove)
	{
		this.startCircleMove = startCircleMove;
	}
}
