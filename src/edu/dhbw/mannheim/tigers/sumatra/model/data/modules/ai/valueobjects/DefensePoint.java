/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.04.2013
 * Author(s): Philipp
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects;

import javax.persistence.Embeddable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;


/**
 * Represented a DefenePoint with his target to protect and the kind of shot.
 * 
 * @see ValuePoint
 * 
 * @author PhilippP
 * 
 */
@Embeddable
public class DefensePoint extends ValuePoint
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long	serialVersionUID	= -3397056222207062545L;
	/**  */
	private TrackedBot			protectAgainst;
	/**  */
	public EShootKind				kindOfshoot			= EShootKind.DEFAULT;
	
	
	/**
	 * To clasify the kind of shoot.
	 * Direct or Indirect
	 * 
	 * @author PhilippP {ph.posovszky@gmail.com}
	 * 
	 */
	public enum EShootKind
	{
		
		/** for indirect shoots */
		INDIRECT,
		/** for direct shoots */
		DIRECT,
		/** for default points */
		DEFAULT,
		/**  */
		BALL;
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * @param x
	 * @param y
	 */
	public DefensePoint(float x, float y)
	{
		super(x, y);
	}
	
	
	/**
	 * 
	 * @param vec
	 * @param value
	 * @param passingBot
	 */
	public DefensePoint(IVector2 vec, float value, TrackedBot passingBot)
	{
		super(vec, value);
		setProtectAgainst(passingBot);
	}
	
	
	/**
	 * 
	 * @param vec
	 * @param value
	 * @param passingBot
	 * @param kind - 0 for indirect, 1 for direct
	 */
	public DefensePoint(IVector2 vec, float value, TrackedBot passingBot, EShootKind kind)
	{
		super(vec, value);
		setProtectAgainst(passingBot);
		setKindOfshoot(kind);
	}
	
	
	/**
	 * 
	 * @param vec
	 */
	public DefensePoint(IVector2 vec)
	{
		super(vec);
	}
	
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param value
	 */
	public DefensePoint(float x, float y, float value)
	{
		super(x, y, value);
	}
	
	
	/**
	 * 
	 * @param copy
	 */
	public DefensePoint(DefensePoint copy)
	{
		super(copy);
		setProtectAgainst(copy.getProtectAgainst());
		setKindOfshoot(copy.getKindOfshoot());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public String toString()
	{
		return "Vector2 (" + x + "," + y + ") Value (" + value + ") ShootKind(" + getKindOfshoot()
				+ ") ProtectAgainsBotID(" + (getProtectAgainst() == null ? "null" : getProtectAgainst().getId()) + ")";
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param kind to seth kindOfShoot
	 */
	public void setKindOfshoot(EShootKind kind)
	{
		kindOfshoot = kind;
	}
	
	
	/**
	 * @return the protectAgainst
	 */
	public TrackedBot getProtectAgainst()
	{
		return protectAgainst;
	}
	
	
	/**
	 * @param protectAgainst the protectAgainst to set
	 */
	public void setProtectAgainst(TrackedBot protectAgainst)
	{
		this.protectAgainst = protectAgainst;
	}
	
	
	/**
	 * @return the kindOfshoot
	 */
	public EShootKind getKindOfshoot()
	{
		return kindOfshoot;
	}
}
