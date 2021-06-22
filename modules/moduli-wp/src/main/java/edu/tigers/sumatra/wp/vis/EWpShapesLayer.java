/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import edu.tigers.sumatra.drawable.IShapeLayer;
import edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType;

import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.ALWAYS_PERSIST;
import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.DEBUG_PERSIST;
import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.NEVER_PERSIST;


/**
 * Shape layers for WP
 */
@SuppressWarnings("squid:S1192") // duplicated strings
public enum EWpShapesLayer implements IShapeLayer
{
	FIELD_BORDERS("Field Borders", "Field", true, -100, ALWAYS_PERSIST),
	FIELD_BORDERS_ADDITIONAL("Additional Field Borders", "Field", false, -90, DEBUG_PERSIST),
	REFEREE("Referee", "Field", true, 1000, ALWAYS_PERSIST),
	BALL_BUFFER("Ball buffer", "Field", false, NEVER_PERSIST),
	BOT_BUFFER("Bot buffer", "Field", false, NEVER_PERSIST),
	BOTS("Bots", "Field", true),
	BOT_FEEDBACK("Bot Feedback", "Field", false),
	BOT_FILTER("Bot Filter", "Field", false),
	BOT_PATTERNS("Bot Patterns", "Field", false, DEBUG_PERSIST),
	BALL("Ball", "Field", true),
	BALL_HIGHLIGHTER("Ball highlighter", "Field", true),
	BALL_PREDICTION("Ball prediction", "Field", false),
	VELOCITY("Velocities", "Field"),

	;


	private final String name;
	private final String category;
	private final boolean visible;
	private final int orderId;
	private final String id;
	private final EShapeLayerPersistenceType persistenceType;


	/**
	 *
	 */
	EWpShapesLayer(final String name, final String category)
	{
		this(name, category, false);
	}


	EWpShapesLayer(final String name, final String category, final boolean visible)
	{
		this.name = name;
		this.category = category;
		this.visible = visible;
		this.persistenceType = ALWAYS_PERSIST;
		orderId = 10 + ordinal();
		id = EWpShapesLayer.class.getCanonicalName() + name;
	}


	/**
	 *
	 */
	EWpShapesLayer(final String name, final String category, final boolean visible,
			final EShapeLayerPersistenceType persistenceType)
	{
		this.name = name;
		this.category = category;
		this.visible = visible;
		this.persistenceType = persistenceType;
		this.orderId = 10 + ordinal();
		id = EWpShapesLayer.class.getCanonicalName() + name;
	}


	EWpShapesLayer(final String name, final String category, final boolean visible, final int orderId,
			final EShapeLayerPersistenceType persistenceType)
	{
		this.name = name;
		this.category = category;
		this.visible = visible;
		this.orderId = orderId;
		id = EWpShapesLayer.class.getCanonicalName() + name;
		this.persistenceType = persistenceType;
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


	@Override
	public boolean isVisibleByDefault()
	{
		return visible;
	}
}
