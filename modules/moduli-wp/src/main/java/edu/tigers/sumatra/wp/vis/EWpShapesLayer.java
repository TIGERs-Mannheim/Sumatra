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
	private static final String BALL_INFO = "Ball Info";
	private static final String BOT_STATES = "Bot States";
	private static final String BASIC = "Basic";

	public static final IShapeLayerIdentifier FIELD_BORDERS = F.create(F.layer("Borders")
			.category(FIELD)
			.category(BASIC)
			.visibleByDefault(true)
			.orderId(-100));
	public static final IShapeLayerIdentifier FIELD_BORDERS_ADDITIONAL = F.create(F.layer("Additional Borders")
			.category(FIELD)
			.orderId(-90)
			.persistenceType(DEBUG_PERSIST));
	public static final IShapeLayerIdentifier REFEREE = F.create(F.layer("Referee")
			.category("Annotations")
			.visibleByDefault(true));
	public static final IShapeLayerIdentifier BALL_BUFFER = F.create(F.layer("Ball Buffer")
			.category(FIELD)
			.category("Buffers"));
	public static final IShapeLayerIdentifier BOT_BUFFER = F.create(F.layer("Bot Buffers")
			.category(FIELD)
			.category("Buffers"));
	public static final IShapeLayerIdentifier BOTS = F.create(F.layer("Bots")
			.category(FIELD)
			.category(BASIC)
			.visibleByDefault(true));
	public static final IShapeLayerIdentifier BOT_FEEDBACK = F.create(F.layer("Feedback")
			.category(FIELD)
			.category(BOT_STATES));
	public static final IShapeLayerIdentifier BOT_FILTER = F.create(F.layer("Filtered")
			.category(FIELD)
			.category(BOT_STATES));
	public static final IShapeLayerIdentifier BOT_PATTERNS = F.create(F.layer("Pattern")
			.category(FIELD)
			.category(BOT_STATES));
	public static final IShapeLayerIdentifier BALL = F.create(F.layer("Ball")
			.category(FIELD)
			.category(BASIC)
			.visibleByDefault(true));
	public static final IShapeLayerIdentifier BALL_HIGHLIGHTER = F.create(F.layer("Ball Highlighter")
			.category(FIELD)
			.category(BALL_INFO));
	public static final IShapeLayerIdentifier BALL_PREDICTION = F.create(F.layer("Prediction")
			.category(FIELD)
			.category(BALL_INFO));
	public static final IShapeLayerIdentifier VELOCITY = F.create(F.layer("Velocities")
			.category(FIELD));
	public static final IShapeLayerIdentifier VISION = F.create(F.layer("Vision")
			.category(FIELD));
}
