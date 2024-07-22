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
			F.category(SKILLS).layerName("Skill names"));
	public static final IShapeLayerIdentifier KICK_SKILL = F.create(
			F.category(SKILLS).layerName("Kick"));
	public static final IShapeLayerIdentifier KICK_SKILL_COMP = F.create(
			F.category(SKILLS).layerName("Kick Comp"));
	public static final IShapeLayerIdentifier KICK_SKILL_DEBUG = F.create(
			F.category(SKILLS).layerName("Kick Debug"));
	public static final IShapeLayerIdentifier BALL_ARRIVAL_SKILL = F.create(
			F.category(SKILLS).layerName("Ball Arrival"));
	public static final IShapeLayerIdentifier APPROACH_AND_STOP_BALL_SKILL = F.create(
			F.category(SKILLS).layerName("Approach + Stop"));
	public static final IShapeLayerIdentifier APPROACH_BALL_LINE_SKILL = F.create(
			F.category(SKILLS).layerName("Approach Ball"));
	public static final IShapeLayerIdentifier MOVE_WITH_BALL = F.create(
			F.category(SKILLS).layerName("Move With Ball"));
	public static final IShapeLayerIdentifier DRIBBLING_KICK = F.create(
			F.category(SKILLS).layerName("Dribbling Kick"));
	public static final IShapeLayerIdentifier KEEPER = F.create(
			F.category(SKILLS).layerName("Keeper"));
	public static final IShapeLayerIdentifier KEEPER_POSITIONING_CALCULATORS = F.create(
			F.category(SKILLS).layerName("Keeper Positioning").persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier KEEPER_DEFLECTION_ADAPTION = F.create(
			F.category(SKILLS).layerName("Keeper Deflection Adaption").persistenceType(NEVER_PERSIST));
	public static final IShapeLayerIdentifier GET_BALL_CONTACT = F.create(
			F.category(SKILLS).layerName("Get Ball Contact"));
	public static final IShapeLayerIdentifier CALIBRATION = F.create(
			F.category(SKILLS).layerName("Calibration"));
	public static final IShapeLayerIdentifier MOVE_ON_PENALTY_AREA_SKILL = F.create(
			F.category(SKILLS).layerName("Move on PenaltyArea").persistenceType(NEVER_PERSIST));

	public static final IShapeLayerIdentifier MOVE_TO_DEST = F.create(
			F.category(MOVEMENT).layerName("MoveTo destination"));
	public static final IShapeLayerIdentifier PATH = F.create(
			F.category(MOVEMENT).layerName("Path"));
	public static final IShapeLayerIdentifier PATH_DEBUG = F.create(
			F.category(MOVEMENT).layerName("Path Debug"));
	public static final IShapeLayerIdentifier PATH_LIMITED_VEL = F.create(
			F.category(MOVEMENT).layerName("Limited Vel"));
}
