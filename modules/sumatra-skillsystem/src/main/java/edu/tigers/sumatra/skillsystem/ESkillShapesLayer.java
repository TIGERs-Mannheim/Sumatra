/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import edu.tigers.sumatra.drawable.IShapeLayer;
import edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType;

import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.ALWAYS_PERSIST;
import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.DEBUG_PERSIST;


/**
 * Shape layers for Skills
 */
@SuppressWarnings("squid:S1192") // duplicated strings
public enum ESkillShapesLayer implements IShapeLayer
{
	SKILL_NAMES("Skill names", "Skills"),

	KICK_SKILL("Kick", "Skills"),
	KICK_SKILL_DEBUG("Kick Debug", "Skills", DEBUG_PERSIST),

	DRIBBLE_SKILL("Dribble", "Skills"),
	PROTECT_AND_MOVE_WITH_BALL_SKILL("Protect + Move", "Skills"),

	BALL_ARRIVAL_SKILL("Ball Arrival", "Skills"),
	APPROACH_AND_STOP_BALL_SKILL("Approach + Stop", "Skills"),
	PUSH_AROUND_OBSTACLE_SKILL("Push", "Skills"),
	APPROACH_BALL_LINE_SKILL("Approach Ball", "Skills"),
	MOVE_WITH_BALL("Move With Ball", "Skills"),

	KEEPER("Keeper", "Skills"),

	PATH("Path", "Movement"),
	PATH_DEBUG("Path Debug", "Movement"),
	DEBUG("Debug", "Movement"),
	PATH_FINDER_DEBUG("PathFinder Debug", "Movement", DEBUG_PERSIST),
	TRAJ_PATH_OBSTACLES("Path Obstacles", "Movement", DEBUG_PERSIST),
	BUFFERED_TRAJECTORY("Buffered Trajectory", "Movement", DEBUG_PERSIST),

	CALIBRATION("Calibration", "Skills"),

	;

	private final String id;
	private final String name;
	private final String category;
	private final EShapeLayerPersistenceType persistenceType;
	private final int orderId;


	ESkillShapesLayer(final String name, final String category)
	{
		this(name, category, ALWAYS_PERSIST);
	}


	ESkillShapesLayer(final String name, final String category,
			final EShapeLayerPersistenceType persistenceType)
	{
		this.name = name;
		this.category = category;
		this.persistenceType = persistenceType;
		orderId = 50 + ordinal();
		id = ESkillShapesLayer.class.getCanonicalName() + name();
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
