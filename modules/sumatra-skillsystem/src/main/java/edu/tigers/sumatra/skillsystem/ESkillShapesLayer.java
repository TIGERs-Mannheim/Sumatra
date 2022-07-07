/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeLayerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.DEBUG_PERSIST;
import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.NEVER_PERSIST;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ESkillShapesLayer
{
	private static final ShapeLayerFactory F = new ShapeLayerFactory(ESkillShapesLayer.class, 50);
	private static final String SKILLS = "Skills";
	private static final String MOVEMENT = "Movement";

	public static final IShapeLayerIdentifier SKILL_NAMES = F.create(
			F.layer("Skill names").category(SKILLS));

	public static final IShapeLayerIdentifier KICK_SKILL = F.create(
			F.layer("Kick").category(SKILLS));
	public static final IShapeLayerIdentifier KICK_SKILL_DEBUG = F.create(F.layer("Kick Debug")
			.category(SKILLS).persistenceType(DEBUG_PERSIST));

	public static final IShapeLayerIdentifier DRIBBLE_SKILL = F.create(
			F.layer("Dribble").category(SKILLS));
	public static final IShapeLayerIdentifier PROTECT_AND_MOVE_WITH_BALL_SKILL = F.create(
			F.layer("Protect + Move").category(SKILLS));

	public static final IShapeLayerIdentifier BALL_ARRIVAL_SKILL = F.create(
			F.layer("Ball Arrival").category(SKILLS));
	public static final IShapeLayerIdentifier APPROACH_AND_STOP_BALL_SKILL = F.create(
			F.layer("Approach + Stop").category(SKILLS));
	public static final IShapeLayerIdentifier PUSH_AROUND_OBSTACLE_SKILL = F.create(
			F.layer("Push").category(SKILLS));
	public static final IShapeLayerIdentifier APPROACH_BALL_LINE_SKILL = F.create(
			F.layer("Approach Ball").category(SKILLS));
	public static final IShapeLayerIdentifier MOVE_WITH_BALL = F.create(
			F.layer("Move With Ball").category(SKILLS));

	public static final IShapeLayerIdentifier KEEPER = F.create(
			F.layer("Keeper").category(SKILLS));
	public static final IShapeLayerIdentifier KEEPER_DEFLECTION_ADAPTION = F.create(
			F.layer("Keeper Deflection Adaption").category(SKILLS).persistenceType(NEVER_PERSIST));

	public static final IShapeLayerIdentifier PATH = F.create(
			F.layer("Path").category(MOVEMENT));
	public static final IShapeLayerIdentifier PATH_DEBUG = F.create(
			F.layer("Path Debug").category(MOVEMENT));
	public static final IShapeLayerIdentifier CRITICAL_COLLISION = F.create(
			F.layer("Critical Collision").category(MOVEMENT));
	public static final IShapeLayerIdentifier DEBUG = F.create(
			F.layer("Debug").category(MOVEMENT));
	public static final IShapeLayerIdentifier PATH_FINDER_DEBUG = F.create(
			F.layer("PathFinder Debug").category(MOVEMENT).persistenceType(DEBUG_PERSIST));
	public static final IShapeLayerIdentifier TRAJ_PATH_OBSTACLES = F.create(
			F.layer("Path Obstacles").category(MOVEMENT).persistenceType(DEBUG_PERSIST));
	public static final IShapeLayerIdentifier BUFFERED_TRAJECTORY = F.create(
			F.layer("Buffered Trajectory").category(MOVEMENT).persistenceType(DEBUG_PERSIST));
	public static final IShapeLayerIdentifier DRIBBLING_DEBUG = F.create(
			F.layer("Dribbling Debug").category(MOVEMENT).persistenceType(NEVER_PERSIST));

	public static final IShapeLayerIdentifier CALIBRATION = F.create(
			F.layer("Calibration").category(SKILLS));
}
