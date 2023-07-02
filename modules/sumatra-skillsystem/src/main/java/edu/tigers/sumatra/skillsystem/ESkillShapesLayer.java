/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeLayerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
			.category(SKILLS));

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

	public static final IShapeLayerIdentifier DRIBBLING_KICK = F.create(
			F.layer("Dribbling Kick").category(SKILLS));

	public static final IShapeLayerIdentifier KEEPER = F.create(
			F.layer("Keeper").category(SKILLS));

	public static final IShapeLayerIdentifier KEEPER_DEFLECTION_ADAPTION = F.create(
			F.layer("Keeper Deflection Adaption").category(SKILLS).persistenceType(NEVER_PERSIST));

	public static final IShapeLayerIdentifier MOVE_TO_DEST = F.create(
			F.layer("MoveTo destination").category(MOVEMENT));

	public static final IShapeLayerIdentifier PATH = F.create(
			F.layer("Path").category(MOVEMENT));

	public static final IShapeLayerIdentifier PATH_DEBUG = F.create(
			F.layer("Path Debug").category(MOVEMENT));

	public static final IShapeLayerIdentifier GET_BALL_CONTACT = F.create(
			F.layer("Get Ball Contact").category(SKILLS));

	public static final IShapeLayerIdentifier CALIBRATION = F.create(
			F.layer("Calibration").category(SKILLS));

	public static final IShapeLayerIdentifier MOVE_ON_PENALTY_AREA_SKILL = F.create(
			F.layer("Move on PenaltyArea").category(SKILLS).persistenceType(NEVER_PERSIST));
}
