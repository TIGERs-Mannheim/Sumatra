/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.data;

import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.ALWAYS_PERSIST;
import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.DEBUG_PERSIST;
import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.NEVER_PERSIST;

import edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType;
import edu.tigers.sumatra.drawable.ShapeMap.IShapeLayer;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("squid:S1192") // duplicated strings
public enum EAiShapesLayer implements IShapeLayer
{
	/** */
	ROLE_COLOR("Colored Roles", "AI", ALWAYS_PERSIST, 1),
	/** */
	SUPPORTER_POSITION_SELECTION("Supporter GlobalPos", "Support"),
	/** */
	SUPPORTER_POSITION_SELECTION_DEBUG("Supporter GlobalPos DEBUG", "Support", DEBUG_PERSIST),
	/** */
	SUPPORTER_POSITION_FIELD_RATING("Supporter Passrating Field", "Support", NEVER_PERSIST, 0),
	/**  */
	ROLE_NAMES("Role names", "AI"),
	
	/**  */
	GOAL_POINTS("Goal points", "AI"),
	/** */
	BEST_DIRECT_SHOT("Best direct shot", "AI"),
	/**  */
	BALL_POSSESSION("Ball possession", "AI"),
	/** */
	ICING("Icing", "AI"),
	/** */
	KEEPER("Keeper", "AI"),
	/**  */
	AUTOMATED_THROW_IN("Automated placement", "AI"),
	/**  */
	SKIRMISH_DETECTOR("Skirmish detection", "AI"),
	/** */
	CHIP_KICK_TARGET("ChipKick target", "AI"),
	/**  */
	REDIRECT_ROLE("Redirect Role", "AI"),
	
	/**  */
	PASS_TARGETS("Pass targets", "Support"),
	/**  */
	PASS_TARGETS_DEBUG("Pass Targets Debug", "Support", DEBUG_PERSIST),
	/** */
	PASS_TARGETS_GRID("Pass Target Grid", "Support", NEVER_PERSIST, 0),
	
	/** */
	DEFENSE_CRUCIAL_DEFENDERS("Crucial Defenders", "Defense"),
	/** */
	DEFENSE_BOT_THREATS("Bot Threats", "Defense"),
	/** */
	BOT_THREADS_GRIT("Bot Threats Grid", "Defense", NEVER_PERSIST, 0),
	/** */
	DEFENSE_BALL_THREAT("Ball Threat", "Defense"),
	/** */
	ANGLE_DEFENSE("Angle defense", "Defense"),
	/** */
	CENTER_BACK("CenterBack", "Defense"),
	/** */
	MAN_MARKER("ManMarker", "Defense"),
	/** */
	INTERCEPT_STATE("InterceptState", "Defense"),
	/** */
	PASS_RECEIVER("Pass Receiver", "Defense"),
	
	/** */
	DEFENSE_PENALTY_AREA_GROUP("Penalty Area Group", "Defense"),
	/** */
	DEFENSE_PENALTY_AREA("Defense Penalty Area", "Defense", DEBUG_PERSIST),
	/** */
	DEFENSE_PENALTY_AREA_ROLE("Penalty Area Role", "Defense"),
	
	/**  */
	OFFENSIVE("Main", "Offensive"),
	/**  */
	OFFENSIVE_FINDER("Bot Finder", "Offensive"),
	/**  */
	OFFENSIVE_DOUBLE_PASS("Double Pass", "Offensive"),
	/**  */
	OFFENSIVE_ADDITIONAL("Hit-Chance", "Offensive"),
	/**  */
	OFFENSIVE_CLEARING_KICK("Clearing kick", "Offensive"),
	/**  */
	OFFENSIVE_PASSING("Passing", "Offensive"),
	/** */
	OFFENSIVE_PASSING_DEBUG("Passing Debug", "Offensive"),
	/** */
	OFFENSIVE_REDIRECT_INTERCEPT("Redirect Intercept", "Offensive"),
	/** */
	OFFENSIVE_REDIRECT_STATE("RedirectState debug", "Offensive", NEVER_PERSIST),
	/**  */
	OFFENSIVE_PROTECTION("Protection", "Offensive"),
	/**  */
	OFFENSIVE_DONT_CATCH_GOAL_SHOT("Dont catch goal", "Offensive"),
	/**  */
	OFFENSIVE_IS_CHIP_NEEDED("Chip kick needed", "Offensive"),
	/**  */
	OFFENSIVE_REDIRECT_PASSING("Redirect Passing", "Offensive"),
	/**  */
	OFFENSIVE_ROLE_STATUS("Role / State - Status", "Offensive"),
	/**	 */
	OFFENSIVE_KICK_INS_BLAUE("Kick ins Blaue", "Offensive"),
	/** */
	OFFENSIVE_TIME_ESTIMATION("Time Estimation", "Offensive"),
	/** */
	OFFENSIVE_MOVE_POSITIONS("Move Positions", "Offensive"),
	
	
	PENALTY_PLACEMENT_GROUPS("Penalty Placement Groups", "Penalty"),
	
	/** */
	LEARNING("Learning"),
	/**  */
	UNSORTED("Unsorted"),
	
	
	/** */
	@Deprecated
	DEFENSE_ADDITIONAL("Defense - additional", "Defense"),
	/**  */
	@Deprecated
	COORDINATE_SYSTEM("Coordinate System", "FIELD", ALWAYS_PERSIST, 11),
	/**  */
	@Deprecated
	KICK_SKILL_TIMING("KickSkill Timing", "AI"),
	/**  */
	@Deprecated
	INJECT("Inject"),
	/**  */
	@Deprecated
	BOTS_AVAILABLE("Bots available", "FIELD"),
	/**  */
	@Deprecated
	PASS_TARGETS_V2("Pass targets v2", "AI"),
	/**  */
	@Deprecated
	TOP_GPU_GRID("Top GPU grid pos", "AI"),
	/**  */
	@Deprecated
	GPU_GRID("GPU grid", "AI", NEVER_PERSIST, 0),
	/** */
	@Deprecated
	CPU_GRID("CPU grid", "AI", NEVER_PERSIST, 0),
	/** */
	@Deprecated
	BIG_DATA("Goal chance heatmap", "AI"),
	/** */
	@Deprecated
	MARKERS("Markers", "AI"),
	/** */
	@Deprecated
	DEFENSE("Defense", "Defense"),;
	
	
	private final String name;
	private final String category;
	private final EShapeLayerPersistenceType persistenceType;
	private final int orderId;
	
	
	EAiShapesLayer(final String name)
	{
		this(name, "UNCATEGORIZED");
	}
	
	
	EAiShapesLayer(final String name, final String category)
	{
		this(name, category, ALWAYS_PERSIST);
	}
	
	
	EAiShapesLayer(final String name, final String category, final EShapeLayerPersistenceType persistenceType)
	{
		this.name = name;
		this.category = category;
		this.persistenceType = persistenceType;
		orderId = 50 + ordinal();
	}
	
	
	EAiShapesLayer(final String name, final String category, final EShapeLayerPersistenceType persistenceType,
			final int orderId)
	{
		this.name = name;
		this.category = category;
		this.persistenceType = persistenceType;
		this.orderId = orderId;
	}
	
	
	@Override
	public String getLayerName()
	{
		return name;
	}
	
	
	@Override
	public final String getCategory()
	{
		return category;
	}
	
	
	@Override
	public int getOrderId()
	{
		return orderId;
	}
	
	
	@Override
	public String getId()
	{
		return EAiShapesLayer.class.getCanonicalName() + name();
	}
	
	
	@Override
	public EShapeLayerPersistenceType getPersistenceType()
	{
		return persistenceType;
	}
}
