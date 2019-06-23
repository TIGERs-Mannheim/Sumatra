/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 24, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;


/**
 * The type of a play may be either:
 * <ul>
 * <li> {@link EPlayType#STANDARD},</li>
 * <li> {@link EPlayType#OFFENSIVE} or</li>
 * <li> {@link EPlayType#DEFENSIVE} or</li>
 * <li> {@link EPlayType#SUPPORT} or</li>
 * <li> {@link EPlayType#TEST} or</li>
 * <li> {@link EPlayType#DEPRECATED}.</li>
 * <li> {@link EPlayType#HELPER}.</li>
 * <li> {@link EPlayType#DEFECT}.</li>
 * </ul>
 * 
 * @author Gero, DanielAl
 */
public enum EPlayType
{
	/** Each play that is meant to handle a "standard"-situation */
	STANDARD(0),
	/** Offensive plays */
	OFFENSIVE(3),
	/** Defensive plays */
	DEFENSIVE(2),
	/** Keeper plays */
	KEEPER(1),
	/** Other plays like middle field, etc. */
	SUPPORT(4),
	/** Plays that are meant for testing-purposes only */
	TEST(5),
	/** Plays that are meant for calibrating-purposes only */
	CALIBRATE(6),
	/** Plays that are replaced by newer once */
	DEPRECATED(7),
	/** Not a real play */
	HELPER(8),
	/** defect plays */
	DEFECT(9),
	/** play is disabled temporarily */
	DISABLED(10),
	/**  */
	CHALLENGE(11);
	
	private final int	order;
	
	
	private EPlayType(int order)
	{
		this.order = order;
	}
	
	
	/**
	 * Order number for sorting the list.
	 * @return
	 */
	public int getOrder()
	{
		return order;
	}
}
