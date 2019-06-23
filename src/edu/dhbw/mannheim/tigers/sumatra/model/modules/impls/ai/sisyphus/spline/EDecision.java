/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 22, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline;

/**
 * the decision of every deciscion maker
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public enum EDecision
{
	/**  */
	NO_VIOLATION(1),
	/** */
	OPTIMIZATION_FOUND(2),
	/**  */
	VIOLATION(3),
	/** the bot will crash into an obstacle if it does not change the spline */
	COLLISION_AHEAD(4),
	/**  */
	ENFORCE(5);
	
	private final int	severity;
	
	
	private EDecision(int severity)
	{
		this.severity = severity;
	}
	
	
	/**
	 * 
	 * @param otherDecision
	 * @return the decision with the higher severity
	 */
	public EDecision max(EDecision otherDecision)
	{
		if (otherDecision.getSeverity() > severity)
		{
			return otherDecision;
		}
		return this;
	}
	
	
	/**
	 * @return the severity
	 */
	public int getSeverity()
	{
		return severity;
	}
	
	
}
