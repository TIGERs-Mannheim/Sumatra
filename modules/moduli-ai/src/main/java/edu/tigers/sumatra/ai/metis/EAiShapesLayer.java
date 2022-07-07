/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis;

import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeLayerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.ALWAYS_PERSIST;
import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.DEBUG_PERSIST;
import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.NEVER_PERSIST;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EAiShapesLayer
{
	private static final ShapeLayerFactory F = new ShapeLayerFactory(EAiShapesLayer.class, 50);
	private static final String SUPPORT = "Support";
	private static final String DEFENSE = "Defense";
	private static final String OFFENSIVE = "Offensive";

	public static final IShapeLayerIdentifier AI_ROLE_COLOR = F.create(
			F.layer("Colored Roles").category("AI").persistenceType(ALWAYS_PERSIST).orderId(1));
	public static final IShapeLayerIdentifier AI_ROLE_NAMES = F.create(
			F.layer("Role names").category("AI"));

	public static final IShapeLayerIdentifier SUPPORT_MOVE_FREE = F.create(
			F.layer("Possible Free Positions").category(SUPPORT));
	public static final IShapeLayerIdentifier SUPPORT_FORCE_FIELD = F.create(
			F.layer("force field").category(SUPPORT).persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier SUPPORT_FORCE_FIELD_FLOW = F.create(
			F.layer("force field flow").category(SUPPORT));
	public static final IShapeLayerIdentifier SUPPORT_ACTIVE_ROLES = F.create(
			F.layer("Role shapes").category(SUPPORT));
	public static final IShapeLayerIdentifier SUPPORT_AGGRESSIVE_MAN_MARKER = F.create(
			F.layer("Aggressive Man2Man Markers").category(SUPPORT));
	public static final IShapeLayerIdentifier SUPPORT_ANGLE_RANGE = F.create(
			F.layer("Angle Range ").category(SUPPORT).persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier SUPPORT_BREAK_THROUGH_DEFENSE = F.create(
			F.layer("Break Through Defense").category(SUPPORT).persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier SUPPORT_PENALTY_AREA_ATTACKER = F.create(
			F.layer("Penalty Area Attacker").category(SUPPORT).persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier SUPPORT_MIDFIELD = F.create(
			F.layer("MidfieldRepulsiveBehavior").category(SUPPORT).persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier SUPPORT_KICKOFF = F.create(
			F.layer("Kickoff Behavior").category(SUPPORT).persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier SUPPORT_FAKE_PASS_RECEIVER = F.create(
			F.layer("Fake Pass Receiver").category(SUPPORT).persistenceType(NEVER_PERSIST));

	public static final IShapeLayerIdentifier AI_BEST_GOAL_KICK = F.create(
			F.layer("Best Goal Kick").category("AI"));
	public static final IShapeLayerIdentifier AI_BALL_POSSESSION = F.create(
			F.layer("Ball Possession").category("AI"));
	public static final IShapeLayerIdentifier AI_BALL_CONTACT = F.create(
			F.layer("Ball Contact").category("AI"));
	public static final IShapeLayerIdentifier AI_BALL_LEAVING_FIELD = F.create(
			F.layer("Ball Leaving Field").category("AI"));
	public static final IShapeLayerIdentifier AI_KEEPER = F.create(
			F.layer("Keeper").category("AI"));
	public static final IShapeLayerIdentifier AI_BALL_PLACEMENT = F.create(
			F.layer("Ball Placement").category("AI"));
	public static final IShapeLayerIdentifier AI_SKIRMISH_DETECTOR = F.create(
			F.layer("Skirmish detection").category("AI"));
	public static final IShapeLayerIdentifier AI_WEAK_BOT = F.create(
			F.layer("Weak bots").category("AI"));
	public static final IShapeLayerIdentifier AI_DIRECT_SHOT_DETECTION = F.create(
			F.layer("Direct Shot Detection").category("AI"));
	public static final IShapeLayerIdentifier AI_BALL_LEFT_FIELD = F.create(
			F.layer("Ball left field").category("AI"));
	public static final IShapeLayerIdentifier AI_POSSIBLE_GOAL = F.create(
			F.layer("Possible goal").category("AI"));
	public static final IShapeLayerIdentifier AI_BALL_RESPONSIBILITY = F.create(
			F.layer("Ball responsibility").category("AI"));
	public static final IShapeLayerIdentifier AI_REDIRECTOR_DETECTION = F.create(
			F.layer("Redirector detection").category("AI"));
	public static final IShapeLayerIdentifier AI_MAINTENANCE = F.create(
			F.layer("Maintenance").category("AI"));

	public static final IShapeLayerIdentifier KICK_ORIGIN = F.create(
			F.layer("Kick Origin").category("Pass"));
	public static final IShapeLayerIdentifier PASS_GENERATION = F.create(
			F.layer("Generation").category("Pass"));
	public static final IShapeLayerIdentifier PASS_GENERATION_PASS_DIR = F.create(
			F.layer("Generation (pass dir)").category("Pass"));
	public static final IShapeLayerIdentifier PASS_GENERATION_FORBIDDEN = F.create(
			F.layer("Generation (forbidden)").category("Pass"));
	public static final IShapeLayerIdentifier PASS_GENERATION_REDIRECT = F.create(
			F.layer("Generation (redirect)").category("Pass"));
	public static final IShapeLayerIdentifier PASS_RATING = F.create(
			F.layer("Rating").category("Pass"));
	public static final IShapeLayerIdentifier PASS_SELECTION = F.create(
			F.layer("Selection").category("Pass"));

	public static final IShapeLayerIdentifier DEFENSE_CRUCIAL_DEFENDERS = F.create(
			F.layer("Crucial Defenders").category(DEFENSE));
	public static final IShapeLayerIdentifier DEFENSE_BOT_THREATS = F.create(
			F.layer("Bot Threats").category(DEFENSE));
	public static final IShapeLayerIdentifier DEFENSE_THREATS = F.create(
			F.layer("Threats").category(DEFENSE));
	public static final IShapeLayerIdentifier DEFENSE_THREAT_ASSIGNMENT = F.create(
			F.layer("Threat Assignment").category(DEFENSE));
	public static final IShapeLayerIdentifier DEFENSE_BALL_THREAT = F.create(
			F.layer("Ball Threat").category(DEFENSE));
	public static final IShapeLayerIdentifier DEFENSE_CENTER_BACK = F.create(
			F.layer("CenterBack").category(DEFENSE));
	public static final IShapeLayerIdentifier DEFENSE_PASS_RECEIVER = F.create(
			F.layer("Pass Receiver").category(DEFENSE));
	public static final IShapeLayerIdentifier DEFENSE_COVERAGE = F.create(
			F.layer("Coverage").category(DEFENSE));

	public static final IShapeLayerIdentifier DEFENSE_PENALTY_AREA_GROUP_FINDER = F.create(
			F.layer("Penalty Area Group Finder").category(DEFENSE));
	public static final IShapeLayerIdentifier DEFENSE_PENALTY_AREA_GROUP_ASSIGNMENT = F.create(
			F.layer("Penalty Area Group Assignment").category(DEFENSE));
	public static final IShapeLayerIdentifier DEFENSE_PENALTY_AREA_ROLE = F.create(
			F.layer("Penalty Area Role").category(DEFENSE));

	public static final IShapeLayerIdentifier OFFENSIVE_ONGOING_PASS = F.create(
			F.layer("Ongoing Passes").category(OFFENSIVE));
	public static final IShapeLayerIdentifier OFFENSIVE_ATTACKER = F.create(
			F.layer("Attacker").category(OFFENSIVE));
	public static final IShapeLayerIdentifier OFFENSIVE_FINISHER = F.create(
			F.layer("Finisher").category(OFFENSIVE));
	public static final IShapeLayerIdentifier OFFENSIVE_ACCESSIBILITY = F.create(
			F.layer("Accessibility").category(OFFENSIVE).persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier OFFENSIVE_SITUATION = F.create(
			F.layer("Situation").category(OFFENSIVE));
	public static final IShapeLayerIdentifier OFFENSIVE_SITUATION_GRID = F.create(
			F.layer("Situation Grid").category(OFFENSIVE).persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier OFFENSIVE_CLEARING_KICK = F.create(
			F.layer("Clearing kick").category(OFFENSIVE));
	public static final IShapeLayerIdentifier OFFENSIVE_PASSING = F.create(
			F.layer("Passing").category(OFFENSIVE));
	public static final IShapeLayerIdentifier OFFENSIVE_KICK_INS_BLAUE = F.create(
			F.layer("Kick ins Blaue").category(OFFENSIVE).persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier OFFENSIVE_BALL_INTERCEPTION = F.create(
			F.layer("Ball Interception").category(OFFENSIVE));
	public static final IShapeLayerIdentifier OFFENSIVE_BALL_INTERCEPTION_DEBUG = F.create(
			F.layer("Ball Interception Debug").category(OFFENSIVE).persistenceType(DEBUG_PERSIST));
	public static final IShapeLayerIdentifier OFFENSIVE_ACTION = F.create(
			F.layer("Action").category(OFFENSIVE));
	public static final IShapeLayerIdentifier OFFENSIVE_ACTION_DEBUG = F.create(
			F.layer("Action Debug").category(OFFENSIVE));
	public static final IShapeLayerIdentifier OFFENSIVE_DRIBBLE = F.create(
			F.layer("Dribble").category(OFFENSIVE));
	public static final IShapeLayerIdentifier OFFENSIVE_STRATEGY_DEBUG = F.create(
			F.layer("Strategy Debug").category(OFFENSIVE));
	public static final IShapeLayerIdentifier OFFENSIVE_OPPONENT_INTERCEPTION = F.create(
			F.layer("Opponent Interception").category(OFFENSIVE));

	public static final IShapeLayerIdentifier PENALTY_ONE_ON_ONE = F.create(
			F.layer("One On One Shooter").category("Penalty"));

	public static final IShapeLayerIdentifier TEST_MULTIMEDIA = F.create(
			F.layer("Multimedia").category("Test"));
	public static final IShapeLayerIdentifier TEST_KICK = F.create(
			F.layer("Kick").category("Test"));
	public static final IShapeLayerIdentifier TEST_ANGLE_RANGE_RATER = F.create(
			F.layer("Angle Range Rater").category("Test"));
	public static final IShapeLayerIdentifier TEST_DEBUG_GRID = F.create(
			F.layer("Debug Grid").category("Test").persistenceType(NEVER_PERSIST).orderId(0));
	public static final IShapeLayerIdentifier TEST_BALL_PLACEMENT = F.create(
			F.layer("Ball Placement").category("Test"));
	public static final IShapeLayerIdentifier TEST_DRIBBLE_CHALLENGE = F.create(
			F.layer("Dribble Challenge").category("Test"));
	public static final IShapeLayerIdentifier TEST_CONTESTED_POSSESSION_CHALLENGE = F.create(
			F.layer("Contested Possession").category("Test"));

	public static final IShapeLayerIdentifier STATS_DEBUG_BALL_DEFENDER_IN_TIME = F.create(
			F.layer("BallDefenderInTimeStatsCalc - Debug").category("StatisticsDebug").persistenceType(DEBUG_PERSIST));
	public static final IShapeLayerIdentifier STATS_DEBUG_DIRECT_SHOTS = F.create(
			F.layer("DirectShotStatsCalc - Debug").category("StatisticsDebug").persistenceType(DEBUG_PERSIST));
}
