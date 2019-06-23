/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.04.2013
 * Author(s): Philipp
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;


/**
 * Represented a DefenePoint with his target to protect and the kind of shot.
 * 
 * @see ValuePoint
 * @author PhilippP
 */
@Persistent(version = 2)
@Deprecated
public class DefensePoint extends ValuePoint
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private TrackedBot					protectAgainst;
	// Threat value direct from the calculators
	private Map<EThreatKind, Float>	threatList				= new HashMap<EThreatKind, Float>();
	// Normalized and Quantifiyd values of the threat
	private Map<EThreatKind, Float>	threatListQuantifyd	= new HashMap<EThreatKind, Float>();
	/**  */
	private List<EThreatKind>			threatKinds				= new ArrayList<EThreatKind>();
	
	
	/**
	 * Describes simple all kinds of threats this point is affectedF
	 * 
	 * @author PhilppP
	 */
	public enum EThreatKind
	{
		/** for indirect shoots */
		INDIRECT,
		/** for direct shoots */
		DIRECT,
		/** for critcal angels */
		CRITICAL_ANGLE,
		/** no specific threat kind */
		DEFAULT
	}
	
	/**
	 * compatibility for Berkeley DB
	 */
	@Deprecated
	public enum EShootKind
	{
		/** for indirect shoots */
		INDIRECT,
		/** for direct shoots */
		DIRECT,
		/**  */
		BALL,
		/** no specific threat kind */
		DEFAULT;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	@SuppressWarnings("unused")
	private DefensePoint()
	{
		super(0, 0);
	}
	
	
	/**
	 * @param x
	 * @param y
	 */
	public DefensePoint(final float x, final float y)
	{
		super(x, y);
	}
	
	
	/**
	 * @param vec
	 * @param value
	 * @param passingBot
	 */
	public DefensePoint(final IVector2 vec, final float value, final TrackedTigerBot passingBot)
	{
		super(vec, value);
		setProtectAgainst(passingBot);
	}
	
	
	/**
	 * @param vec
	 */
	public DefensePoint(final IVector2 vec)
	{
		super(vec);
	}
	
	
	/**
	 * @param vec
	 * @param value
	 */
	public DefensePoint(final IVector2 vec, final float value)
	{
		super(vec);
		this.value = value;
	}
	
	
	/**
	 * @param x
	 * @param y
	 * @param value
	 */
	public DefensePoint(final float x, final float y, final float value)
	{
		super(x, y, value);
	}
	
	
	/**
	 * @param copy
	 */
	public DefensePoint(final DefensePoint copy)
	{
		super(copy);
		setProtectAgainst(copy.getProtectAgainst());
		for (EThreatKind kind : copy.getKindOfThreats())
		{
			threatKinds.add(kind);
		}
		
		// TODO Copy hashMap
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return a copy of the {@link EThreatKind} list
	 */
	public List<EThreatKind> getKindOfThreats()
	{
		List<EThreatKind> tempList = new ArrayList<>();
		
		for (EThreatKind kind : threatKinds)
		{
			tempList.add(kind);
		}
		
		return tempList;
	}
	
	
	@Override
	public String toString()
	{
		return "Vector2 (" + x + "," + y + ") Value (" + value + ") Threatkind(" + getThreadKindsAsString()
				+ ") ProtectAgainsBotID(" + (getProtectAgainst() == null ? "null" : getProtectAgainst().getId()) + ")";
	}
	
	
	/**
	 * @return the list of threads with , as separator
	 *         E.g. Threat1, Threat2...
	 */
	private String getThreadKindsAsString()
	{
		StringBuilder temp = new StringBuilder();
		for (int i = 0, size = threatKinds.size(); i < size; i++)
		{
			if (i < size)
			{
				temp.append(threatKinds.get(i) + ",");
			} else
			{
				temp.append(threatKinds.get(i));
			}
		}
		return temp.toString();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the protectAgainst
	 */
	public TrackedTigerBot getProtectAgainst()
	{
		return (TrackedTigerBot) protectAgainst;
	}
	
	
	/**
	 * @param protectAgainst the protectAgainst to set
	 */
	public void setProtectAgainst(final TrackedTigerBot protectAgainst)
	{
		this.protectAgainst = protectAgainst;
	}
	
	
	/**
	 * Check if a {@link EThreatKind} is contained in the list
	 * 
	 * @param kindToCheck - {@link EThreatKind} to check
	 * @return if the threat is contains in the list
	 */
	public boolean containsThreat(final EThreatKind kindToCheck)
	{
		for (EThreatKind kind : threatKinds)
		{
			if (kind == kindToCheck)
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Check if a {@link EThreatKind} is not contained in the list
	 * 
	 * @param kindToCheck - {@link EThreatKind} to check
	 * @return if the threat is contains in the list
	 */
	public boolean notContainsThreat(final EThreatKind kindToCheck)
	{
		boolean contains = true;
		for (EThreatKind kind : threatKinds)
		{
			if (kind == kindToCheck)
			{
				contains = false;
			}
		}
		return contains;
	}
	
	
	@Override
	/**
	 * Calls add value!
	 */
	public void setValue(final float value)
	{
		this.value = value;
	}
	
	
	/**
	 * Adds a value with a special threat kind to the defense point.
	 * 
	 * @param value
	 * @param kindOfThreat
	 */
	public void addThreatKind(final EThreatKind kindOfThreat, final float value)
	{
		threatList.put(kindOfThreat, value);
		threatKinds.add(kindOfThreat);
	}
	
	
	/**
	 * Adds a value with a special threat kind to the defense point.
	 * 
	 * @param value
	 * @param kindOfThreat
	 */
	public void addThreatKindQuantifyd(final EThreatKind kindOfThreat, final float value)
	{
		threatListQuantifyd.put(kindOfThreat, value);
	}
	
	
	/**
	 * Returns the value of a specific threat
	 * 
	 * @param kindOfThreat
	 * @return
	 */
	public float getValueOfThreat(final EThreatKind kindOfThreat)
	{
		return threatList.get(kindOfThreat);
	}
	
	
	/**
	 * Calculate the final the final threat value
	 */
	public void calcualteFinalThreat()
	{
		for (EThreatKind kind : threatKinds)
		{
			value += threatListQuantifyd.get(kind);
		}
		
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((protectAgainst == null) ? 0 : protectAgainst.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		DefensePoint other = (DefensePoint) obj;
		if (protectAgainst == null)
		{
			if (other.protectAgainst != null)
			{
				return false;
			}
		} else if (!protectAgainst.equals(other.protectAgainst))
		{
			return false;
		}
		return true;
	}
}
