/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.bot;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This class defines what features are available for each single bot
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @note IDs correspond to TigerBotV3 match feedback bitfield, do not change!
 */
public enum EFeature
{
	/**
	 *
	 */
	MOVE("Move", 0x0001),
	/**
	 *
	 */
	DRIBBLER("Dribbler", 0x0002),
	/**
	 *
	 */
	STRAIGHT_KICKER("StraightK", 0x0004),
	/**
	 *
	 */
	CHIP_KICKER("Chipper", 0x0008),
	/**
	 *
	 */
	BARRIER("Barrier", 0x0010),
	/**
	 *
	 */
	V2016("v2016", 0x0020),
	/**
	 *
	 */
	EXT_BOARD("Pi", 0x0040),
	/**
	 *
	 */
	CHARGE_CAPS("Charge Caps", 0x0080),
	/**
	 *
	 */
	KICKER_V2017("Kicker v2017", 0x0100),
	/**
	 * Battery level is good
	 */
	ENERGETIC("Energetic", 0x0200);

	private final String name;
	private final int id;


	EFeature(final String name, final int id)
	{
		this.name = name;
		this.id = id;
	}


	/**
	 * Create new feature list with all available features
	 *
	 * @return
	 */
	public static Map<EFeature, EFeatureState> createFeatureList()
	{
		Map<EFeature, EFeatureState> map = new EnumMap<>(EFeature.class);
		for (EFeature f : EFeature.values())
		{
			map.put(f, EFeatureState.UNKNOWN);
		}
		return map;
	}

	/**
	 * Create a default set of features, suitable for most robots.
	 * @return
	 */
	public static Map<EFeature, EFeatureState> getDefaultFeatureStates()
	{
		Map<EFeature, EFeatureState> map = new EnumMap<>(EFeature.class);
		getReadyFeatureSet().forEach(f -> map.put(f, EFeatureState.WORKING));
		return map;
	}


	/**
	 * Create a set of features which are required for a robot to be ready.
	 * All features must be WORKING for the bot to be {@link ERobotHealthState#READY}.
	 *
	 * @return
	 */
	public static Set<EFeature> getReadyFeatureSet()
	{
		var result = new HashSet<EFeature>();
		result.add(EFeature.DRIBBLER);
		result.add(EFeature.CHIP_KICKER);
		result.add(EFeature.STRAIGHT_KICKER);
		result.add(EFeature.MOVE);
		result.add(EFeature.BARRIER);
		result.add(EFeature.CHARGE_CAPS);
		result.add(EFeature.ENERGETIC);
		return result;
	}


	/**
	 * Create a set of features: If ANY of those is != WORKING => the robot is {@link ERobotHealthState#UNUSABLE}.
	 *
	 * @return
	 */
	public static Set<EFeature> getUnusableFeatureSet()
	{
		var result = new HashSet<EFeature>();
		result.add(EFeature.MOVE);
		return result;
	}


	/**
	 * @return the name
	 */
	public final String getName()
	{
		return name;
	}


	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}


	/**
	 * Convert an id to an enum.
	 *
	 * @param id
	 * @return enum
	 */
	public static EFeature getFeatureConstant(final int id)
	{
		for (EFeature s : values())
		{
			if (s.getId() == id)
			{
				return s;
			}
		}

		return null;
	}

}
