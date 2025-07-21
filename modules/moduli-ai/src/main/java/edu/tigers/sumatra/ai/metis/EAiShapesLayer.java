/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
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
	private static final String AI = "AI";
	private static final String SUPPORT = "Support";
	private static final String PASS = "Pass";
	private static final String DEFENSE = "Defense";
	private static final String DEFENSE_OFFENSE_COORDINATION = "D/O Coordination";
	private static final String OFFENSE = "Offense";
	private static final String KEEPER = "Keeper";
	private static final String TEST = "Test";
	private static final String STATISTICS = "Statistics";

	public static final IShapeLayerIdentifier AI_ROLE_COLOR = F.create(
			F.category(AI).layerName("Colored Roles").visibleByDefault(true).persistenceType(ALWAYS_PERSIST).orderId(1));
	public static final IShapeLayerIdentifier AI_ROLE_NAMES = F.create(
			F.category(AI).layerName("Role names"));
	public static final IShapeLayerIdentifier AI_PATH_FINDER_PRIORITIES = F.create(
			F.category(AI).layerName("Path finder priorities"));
	public static final IShapeLayerIdentifier AI_BEST_GOAL_KICK = F.create(
			F.category(AI).layerName("Best Goal Kick"));
	public static final IShapeLayerIdentifier AI_BALL_POSSESSION = F.create(
			F.category(AI).layerName("Ball Possession"));
	public static final IShapeLayerIdentifier AI_BALL_CONTACT = F.create(
			F.category(AI).layerName("Ball Contact"));
	public static final IShapeLayerIdentifier AI_BALL_LEAVING_FIELD = F.create(
			F.category(AI).layerName("Ball Leaving Field"));
	public static final IShapeLayerIdentifier AI_BALL_PLACEMENT = F.create(
			F.category(AI).layerName("Ball Placement"));
	public static final IShapeLayerIdentifier AI_SKIRMISH_DETECTOR = F.create(
			F.category(AI).layerName("Skirmish detection"));
	public static final IShapeLayerIdentifier AI_SKIRMISH_STRATEGY = F.create(
			F.category(AI).layerName("Skirmish strategy"));
	public static final IShapeLayerIdentifier AI_WEAK_BOT = F.create(
			F.category(AI).layerName("Weak bots"));
	public static final IShapeLayerIdentifier AI_DIRECT_SHOT_DETECTION = F.create(
			F.category(AI).layerName("Direct Shot Detection"));
	public static final IShapeLayerIdentifier AI_BALL_LEFT_FIELD = F.create(
			F.category(AI).layerName("Ball left field"));
	public static final IShapeLayerIdentifier AI_POSSIBLE_GOAL = F.create(
			F.category(AI).layerName("Possible goal"));
	public static final IShapeLayerIdentifier AI_BALL_RESPONSIBILITY = F.create(
			F.category(AI).layerName("Ball responsibility"));
	public static final IShapeLayerIdentifier AI_REDIRECTOR_DETECTION = F.create(
			F.category(AI).layerName("Redirector detection"));
	public static final IShapeLayerIdentifier AI_MAINTENANCE = F.create(
			F.category(AI).layerName("Maintenance"));
	public static final IShapeLayerIdentifier AI_MULTIMEDIA = F.create(
			F.category(AI).layerName("Multimedia"));
	public static final IShapeLayerIdentifier AI_OPPONENT_CLASSIFIER = F.create(
			F.category(AI).layerName("Opponent Classifier"));
	public static final IShapeLayerIdentifier PENALTY_ONE_ON_ONE = F.create(
			F.category(AI).layerName("One On One Shooter"));

	public static final IShapeLayerIdentifier SUPPORT_PASS_RECEIVER = F.create(
			F.category(SUPPORT).layerName("pass receiver"));
	public static final IShapeLayerIdentifier SUPPORT_FORCE_FIELD = F.create(
			F.category(SUPPORT).layerName("force field").persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier SUPPORT_FORCE_FIELD_FLOW = F.create(
			F.category(SUPPORT).layerName("force field flow"));
	public static final IShapeLayerIdentifier SUPPORT_ACTIVE_ROLES = F.create(
			F.category(SUPPORT).layerName("Role shapes"));
	public static final IShapeLayerIdentifier SUPPORT_AGGRESSIVE_MAN_MARKER = F.create(
			F.category(SUPPORT).layerName("Aggressive Man2Man Markers"));
	public static final IShapeLayerIdentifier SUPPORT_ANGLE_RANGE = F.create(
			F.category(SUPPORT).layerName("Angle Range ").persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier SUPPORT_BREAK_THROUGH_DEFENSE = F.create(
			F.category(SUPPORT).layerName("Break Through Defense").persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier SUPPORT_PENALTY_AREA_ATTACKER = F.create(
			F.category(SUPPORT).layerName("Penalty Area Attacker").persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier SUPPORT_KICKOFF = F.create(
			F.category(SUPPORT).layerName("Kickoff Behavior").persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier SUPPORT_FAKE_PASS_RECEIVER = F.create(
			F.category(SUPPORT).layerName("Fake Pass Receiver").persistenceType(NEVER_PERSIST));

	public static final IShapeLayerIdentifier PASS_KICK_ORIGIN = F.create(
			F.category(PASS).layerName("Kick Origin"));
	public static final IShapeLayerIdentifier PASS_STATS = F.create(
			F.category(PASS).layerName("Pass Stats"));
	public static final IShapeLayerIdentifier PASS_GENERATION = F.create(
			F.category(PASS).layerName("Generation"));
	public static final IShapeLayerIdentifier PASS_GENERATION_FORBIDDEN = F.create(
			F.category(PASS).layerName("Generation (forbidden)"));
	public static final IShapeLayerIdentifier PASS_GENERATION_REDIRECT = F.create(
			F.category(PASS).layerName("Generation (redirect)"));
	public static final IShapeLayerIdentifier PASS_RATING = F.create(
			F.category(PASS).layerName("Rating"));
	public static final IShapeLayerIdentifier PASS_RATING_DEBUG = F.create(
			F.category(PASS).layerName("Rating Debug"));
	public static final IShapeLayerIdentifier PASS_SELECTION = F.create(
			F.category(PASS).layerName("Selection"));

	public static final IShapeLayerIdentifier DEFENSE_BOT_THREATS = F.create(
			F.category(DEFENSE).layerName("Bot Threats"));
	public static final IShapeLayerIdentifier DEFENSE_PASS_DISRUPTION = F.create(
			F.category(DEFENSE).layerName("Pass Disruption"));
	public static final IShapeLayerIdentifier DEFENSE_PASS_DISRUPTION_DEBUG = F.create(
			F.category(DEFENSE).layerName("Pass Disruption Debug").persistenceType(DEBUG_PERSIST));
	public static final IShapeLayerIdentifier DEFENSE_THREATS = F.create(
			F.category(DEFENSE).layerName("Threats"));
	public static final IShapeLayerIdentifier DEFENSE_THREAT_RATING = F.create(
			F.category(DEFENSE).layerName("Threat Ratings"));
	public static final IShapeLayerIdentifier DEFENSE_THREAT_RATING_REDUCTION = F.create(
			F.category(DEFENSE).layerName("Threat Rating Reduction"));
	public static final IShapeLayerIdentifier DEFENSE_THREAT_ASSIGNMENT = F.create(
			F.category(DEFENSE).layerName("Threat Assignment"));
	public static final IShapeLayerIdentifier DEFENSE_BALL_THREAT = F.create(
			F.category(DEFENSE).layerName("Ball Threat"));
	public static final IShapeLayerIdentifier DEFENSE_NUM_DEFENDER_FOR_BALL_DEBUG = F.create(
			F.category(DEFENSE).layerName("NumDefenderForBall Debug").persistenceType(DEBUG_PERSIST));
	public static final IShapeLayerIdentifier DEFENSE_CENTER_BACK = F.create(
			F.category(DEFENSE).layerName("CenterBack"));
	public static final IShapeLayerIdentifier DEFENSE_PASS_RECEIVER = F.create(
			F.category(DEFENSE).layerName("Pass Receiver"));
	public static final IShapeLayerIdentifier DEFENSE_COVERAGE = F.create(
			F.category(DEFENSE).layerName("Coverage"));
	public static final IShapeLayerIdentifier DEFENSE_DRIBBLER_KICKER = F.create(
			F.category(DEFENSE).layerName("DribblerKicker"));
	public static final IShapeLayerIdentifier DEFENSE_PENALTY_AREA_GROUP_FINDER = F.create(
			F.category(DEFENSE).layerName("Penalty Area Group Finder"));
	public static final IShapeLayerIdentifier DEFENSE_PENALTY_AREA_GROUP_ASSIGNMENT = F.create(
			F.category(DEFENSE).layerName("Penalty Area Group Assignment"));
	public static final IShapeLayerIdentifier DEFENSE_PENALTY_AREA_ROLE = F.create(
			F.category(DEFENSE).layerName("Penalty Area Role"));

	public static final IShapeLayerIdentifier DO_COORD_CRUCIAL_OFFENDERS = F.create(
			F.category(DEFENSE_OFFENSE_COORDINATION).layerName("Crucial Offenders"));
	public static final IShapeLayerIdentifier DO_COORD_CRUCIAL_DEFENDERS = F.create(
			F.category(DEFENSE_OFFENSE_COORDINATION).layerName("Crucial Defenders"));
	public static final IShapeLayerIdentifier DO_COORD_BEST_BALL_DEFENDER_CANDIDATES = F.create(
			F.category(DEFENSE_OFFENSE_COORDINATION).layerName("Ball Defender Candidates"));
	public static final IShapeLayerIdentifier DO_COORD_BALL_DEFENSE_READY = F.create(
			F.category(DEFENSE_OFFENSE_COORDINATION).layerName("Ball Defense Ready"));
	public static final IShapeLayerIdentifier DO_COORD_DEFENSE_SUPPORTIVE_BALL_RECEPTION = F.create(
			F.category(DEFENSE_OFFENSE_COORDINATION).layerName("Defense supportive reception"));

	public static final IShapeLayerIdentifier OFFENSE_ONGOING_PASS = F.create(
			F.category(OFFENSE).layerName("Ongoing Passes"));
	public static final IShapeLayerIdentifier OFFENSE_SUPPORTIVE_BLOCK = F.create(
			F.category(OFFENSE).layerName("Supportive Block"));
	public static final IShapeLayerIdentifier OFFENSE_KICK_MOVEMENT = F.create(
			F.category(OFFENSE).layerName("Kick Movement"));
	public static final IShapeLayerIdentifier OFFENSE_SUPPORTIVE_FINISHER_BLOCK = F.create(
			F.category(OFFENSE).layerName("Supportive Finisher Block"));
	public static final IShapeLayerIdentifier OFFENSE_SUPPORTIVE_ATTACKER = F.create(
			F.category(OFFENSE).layerName("Supportive Attacker"));
	public static final IShapeLayerIdentifier OFFENSE_ATTACKER = F.create(
			F.category(OFFENSE).layerName("Attacker"));
	public static final IShapeLayerIdentifier OFFENSE_FINISHER = F.create(
			F.category(OFFENSE).layerName("Finisher"));
	public static final IShapeLayerIdentifier OFFENSE_ACCESSIBILITY = F.create(
			F.category(OFFENSE).layerName("Accessibility").persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier OFFENSE_SITUATION = F.create(
			F.category(OFFENSE).layerName("Situation"));
	public static final IShapeLayerIdentifier OFFENSE_SITUATION_GRID = F.create(
			F.category(OFFENSE).layerName("Situation Grid").persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier OFFENSE_PASSING = F.create(
			F.category(OFFENSE).layerName("Passing"));
	public static final IShapeLayerIdentifier OFFENSE_BALL_INTERCEPTION = F.create(
			F.category(OFFENSE).layerName("Ball Interception"));
	public static final IShapeLayerIdentifier OFFENSE_ACTION = F.create(
			F.category(OFFENSE).layerName("Action"));
	public static final IShapeLayerIdentifier OFFENSE_ACTION_DEBUG = F.create(
			F.category(OFFENSE).layerName("Action Debug"));
	public static final IShapeLayerIdentifier OFFENSE_DRIBBLE = F.create(
			F.category(OFFENSE).layerName("Dribble"));
	public static final IShapeLayerIdentifier OFFENSE_PROTECT_KICK = F.create(
			F.category(OFFENSE).layerName("Protect Kick"));
	public static final IShapeLayerIdentifier OFFENSE_STRATEGY_DEBUG = F.create(
			F.category(OFFENSE).layerName("Strategy Debug").persistenceType(DEBUG_PERSIST));
	public static final IShapeLayerIdentifier OFFENSE_OPPONENT_INTERCEPTION = F.create(
			F.category(OFFENSE).layerName("Opponent Interception"));
	public static final IShapeLayerIdentifier OFFENSE_PASS_OBSTACLES = F.create(
			F.category(OFFENSE).layerName("Pass Obstacles"));

	public static final IShapeLayerIdentifier KEEPER_BEHAVIOR = F.create(
			F.category(KEEPER).layerName("Behavior"));
	public static final IShapeLayerIdentifier KEEPER_BEHAVIOR_DEBUG = F.create(
			F.category(KEEPER).layerName("Behavior Debug").persistenceType(DEBUG_PERSIST));
	public static final IShapeLayerIdentifier KEEPER_INTERCEPT = F.create(
			F.category(KEEPER).layerName("Intercept"));

	public static final IShapeLayerIdentifier TEST_PASSING = F.create(
			F.category(TEST).layerName("Passing").visibleByDefault(true));
	public static final IShapeLayerIdentifier TEST_ANGLE_RANGE_RATER = F.create(
			F.category(TEST).layerName("Angle Range Rater").visibleByDefault(true));
	public static final IShapeLayerIdentifier TEST_GRID_DEBUG = F.create(
			F.category(TEST).layerName("Debug Grid").visibleByDefault(true).persistenceType(NEVER_PERSIST).orderId(-200));
	public static final IShapeLayerIdentifier TEST_GRID_ADDITIONAL = F.create(
			F.category(TEST).layerName("Debug Grid Additional").visibleByDefault(true));
	public static final IShapeLayerIdentifier TEST_DRIBBLE = F.create(
			F.category(TEST).layerName("Test Dribbling Role").visibleByDefault(true));
	public static final IShapeLayerIdentifier TEST_BALL_CALIBRATION = F.create(
			F.category(TEST).layerName("Ball Calibration").visibleByDefault(true));
	public static final IShapeLayerIdentifier TEST_BALL_PLACEMENT = F.create(
			F.category(TEST).layerName("Ball Placement").visibleByDefault(true));
	public static final IShapeLayerIdentifier TEST_MOVE_AROUND_PEN_AREA = F.create(
			F.category(TEST).layerName("Move Around Penalty Area").visibleByDefault(true));
	public static final IShapeLayerIdentifier TEST_CHEERING_PLAY_DEBUG = F.create(
			F.category(TEST).layerName("Cheering Play").persistenceType(DEBUG_PERSIST));

	public static final IShapeLayerIdentifier STATS_BALL_DEFENDER_IN_TIME_DEBUG = F.create(
			F.category(STATISTICS).layerName("BallDefenderInTimeStatsCalc - Debug").persistenceType(DEBUG_PERSIST));
	public static final IShapeLayerIdentifier STATS_DIRECT_SHOTS_DEBUG = F.create(
			F.category(STATISTICS).layerName("DirectShotStatsCalc - Debug").persistenceType(DEBUG_PERSIST));
}
