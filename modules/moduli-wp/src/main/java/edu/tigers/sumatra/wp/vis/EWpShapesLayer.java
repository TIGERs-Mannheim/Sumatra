/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeLayerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.DEBUG_PERSIST;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EWpShapesLayer
{
	private static final ShapeLayerFactory F = new ShapeLayerFactory(EWpShapesLayer.class, 10);
	private static final String FIELD = "Field";
	private static final String REFEREE = "Referee";

	private static final String FIELD_LINES = "Field Lines";
	private static final String VISION = "Vision";
	private static final String CAT_BOTS = "Bots";
	private static final String CAT_BALL = "Ball";
	private static final String BOT_STATES = "States";

	public static final IShapeLayerIdentifier FIELD_LINES_REGULAR = F.create(
			F.category(FIELD).category(FIELD_LINES).layerName("Regular").visibleByDefault(true).orderId(-100));
	public static final IShapeLayerIdentifier FIELD_LINES_ADDITIONAL = F.create(
			F.category(FIELD).category(FIELD_LINES).layerName("Additional").orderId(-90).persistenceType(DEBUG_PERSIST));

	public static final IShapeLayerIdentifier REFEREE_HEADER = F.create(
			F.category(REFEREE).layerName("Header").visibleByDefault(true));

	public static final IShapeLayerIdentifier RAW_VISION = F.create(
			F.category(FIELD).category(VISION).layerName("Raw Vision"));
	public static final IShapeLayerIdentifier CAM_OBJECT_FILTER = F.create(
			F.category(FIELD).category(VISION).layerName("CamObject Filter").visibleByDefault(true));

	public static final IShapeLayerIdentifier BOTS = F.create(
			F.category(FIELD).category(CAT_BOTS).layerName("Bots").visibleByDefault(true));
	public static final IShapeLayerIdentifier BOT_VELOCITIES = F.create(
			F.category(FIELD).category(CAT_BOTS).layerName("Velocities"));
	public static final IShapeLayerIdentifier BOT_BUFFER = F.create(
			F.category(FIELD).category(CAT_BOTS).layerName("Buffers"));
	public static final IShapeLayerIdentifier BOT_PATTERNS = F.create(
			F.category(FIELD).category(CAT_BOTS).layerName("Pattern"));

	public static final IShapeLayerIdentifier BOT_FEEDBACK = F.create(
			F.category(FIELD).category(CAT_BOTS).category(BOT_STATES).layerName("Feedback"));
	public static final IShapeLayerIdentifier BOT_FILTER = F.create(
			F.category(FIELD).category(CAT_BOTS).category(BOT_STATES).layerName("Filtered"));

	public static final IShapeLayerIdentifier BALL = F.create(
			F.category(FIELD).category(CAT_BALL).layerName("Ball").visibleByDefault(true));
	public static final IShapeLayerIdentifier BALL_VELOCITY = F.create(
			F.category(FIELD).category(CAT_BALL).layerName("Velocity"));
	public static final IShapeLayerIdentifier BALL_BUFFER = F.create(
			F.category(FIELD).category(CAT_BALL).layerName("Buffer"));

	public static final IShapeLayerIdentifier BALL_HIGHLIGHTER = F.create(
			F.category(FIELD).category(CAT_BALL).layerName("Highlighter").visibleByDefault(true));
	public static final IShapeLayerIdentifier BALL_PREDICTION = F.create(
			F.category(FIELD).category(CAT_BALL).layerName("Prediction"));
	public static final IShapeLayerIdentifier BALL_MODELS = F.create(
			F.category(FIELD).category(CAT_BALL).layerName("Models"));
}
