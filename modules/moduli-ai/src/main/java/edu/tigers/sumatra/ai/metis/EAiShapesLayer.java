/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis;

import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.ALWAYS_PERSIST;
import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.DEBUG_PERSIST;
import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.NEVER_PERSIST;

import edu.tigers.sumatra.drawable.IShapeLayer;
import edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("squid:S1192") // duplicated strings
public enum EAiShapesLayer implements IShapeLayer
{
	AI_ROLE_COLOR("Colored Roles", "AI", ALWAYS_PERSIST, 1),
	AI_ROLE_NAMES("Role names", "AI"),
	
	SUPPORTER_POSITION_SELECTION("Supporter GlobalPos", "Support"),
	SUPPORTER_POSITION_SELECTION_DEBUG("Supporter GlobalPos DEBUG", "Support", DEBUG_PERSIST),
	SUPPORTER_POSITION_FIELD_RATING("Supporter Passrating Field", "Support", NEVER_PERSIST, 0),
	SUPPORT_MOVE_FREE("Possible Free Positions", "Support"),
	SUPPORT_FORCE_FIELD("FORCE field", "Support", NEVER_PERSIST),
	SUPPORT_ACTIVE_ROLES("Role shapes", "Support"),
	
	AI_BEST_DIRECT_SHOT("Best Direct Kick", "AI"),
	AI_BALL_POSSESSION("Ball Possession", "AI"),
	AI_BALL_CONTACT("Ball Contact", "AI"),
	AI_ICING("Icing", "AI"),
	AI_KEEPER("Keeper", "AI"),
	AI_BALL_PLACEMENT("Ball Placement", "AI"),
	AI_SKIRMISH_DETECTOR("Skirmish detection", "AI"),
	AI_WEAK_BOT("Weak bots", "AI"),
	AI_DIRECT_SHOT_DETECTION("Direct Shot Detection", "AI"),
	AI_BALL_LEFT_FIELD("Ball left field", "AI"),
	AI_POSSIBLE_GOAL("Possible goal", "AI"),
	AI_BALL_RESPONSIBILITY("Ball responsibility", "AI"),
	
	PASS_TARGETS("Pass targets", "Support"),
	PASS_TARGETS_DEBUG("Pass Targets Debug", "Support", DEBUG_PERSIST),
	PASS_TARGETS_GRID("Pass Target Grid", "Support", NEVER_PERSIST, 0),
	
	DEFENSE_CRUCIAL_DEFENDERS("Crucial Defenders", "Defense"),
	DEFENSE_BOT_THREATS("Bot Threats", "Defense"),
	DEFENSE_BOT_THREADS_GRIT("Bot Threats Grid", "Defense", NEVER_PERSIST, 0),
	DEFENSE_BALL_THREAT("Ball Threat", "Defense"),
	DEFENSE_CENTER_BACK("CenterBack", "Defense"),
	DEFENSE_MAN_MARKER("ManMarker", "Defense"),
	DEFENSE_INTERCEPT_STATE("InterceptState", "Defense"),
	DEFENSE_PASS_RECEIVER("Pass Receiver", "Defense"),
	
	DEFENSE_PENALTY_AREA_GROUP("Penalty Area Group", "Defense"),
	DEFENSE_PENALTY_AREA("Defense Penalty Area", "Defense", DEBUG_PERSIST),
	DEFENSE_PENALTY_AREA_ROLE("Penalty Area Role", "Defense"),
	
	OFFENSIVE_ATTACKER("Attacker", "Offensive"),
	OFFENSIVE("Main", "Offensive"),
	OFFENSIVE_SITUATION("Situation", "Offensive"),
	OFFENSIVE_FINDER("Bot Finder", "Offensive"),
	OFFENSIVE_DOUBLE_PASS("Double Pass", "Offensive"),
	OFFENSIVE_CLEARING_KICK("Clearing kick", "Offensive"),
	OFFENSIVE_PASSING("Passing", "Offensive"),
	OFFENSIVE_IS_CHIP_NEEDED("Chip kick needed", "Offensive"),
	OFFENSIVE_KICK_INS_BLAUE("Kick ins Blaue", "Offensive"),
	OFFENSIVE_BALL_INTERCEPTION("Ball Interception", "Offensive"),
	OFFENSIVE_ACTION("Action", "Offensive"),
	OFFENSIVE_ACTION_DEBUG("Action Debug", "Offensive"),
	OFFENSIVE_STRATEGY("Strategy", "Offensive"),
	OFFENSIVE_STRATEGY_DEBUG("Strategy Debug", "Offensive"),
	OFFENSIVE_OPPONENT_INTERCEPTION("Opponent Interception", "Offensive"),
	
	PENALTY_PLACEMENT_GROUPS("Penalty Placement Groups", "Penalty"),
	
	TEST_REDIRECT("Redirect", "Test"),
	TEST_ANGLE_RANGE_RATER("Angle Range Rater", "Test"),
	TEST_LEARNING("Learning", "Test"),
	TEST_DEBUG_GRID("Debug Grid", "Test", NEVER_PERSIST, 0),
	
	;
	
	private final String id;
	private final String name;
	private final String category;
	private final EShapeLayerPersistenceType persistenceType;
	private final int orderId;
	
	
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
		id = EAiShapesLayer.class.getCanonicalName() + name();
	}
	
	
	EAiShapesLayer(final String name, final String category, final EShapeLayerPersistenceType persistenceType,
			final int orderId)
	{
		this.name = name;
		this.category = category;
		this.persistenceType = persistenceType;
		this.orderId = orderId;
		id = EAiShapesLayer.class.getCanonicalName() + name();
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
		return id;
	}
	
	
	@Override
	public EShapeLayerPersistenceType getPersistenceType()
	{
		return persistenceType;
	}
}