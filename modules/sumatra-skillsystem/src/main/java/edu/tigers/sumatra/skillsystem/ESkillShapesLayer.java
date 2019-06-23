/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.ALWAYS_PERSIST;
import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.DEBUG_PERSIST;

import edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType;
import edu.tigers.sumatra.drawable.ShapeMap.IShapeLayer;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("squid:S1192") // duplicated strings
public enum ESkillShapesLayer implements IShapeLayer
{
	/**  */
	SKILL_NAMES("Skill names", "Skills"),
	/**  */
	RECEIVER_SKILL("Receive/Redirect", "Skills"),
	/**  */
	KICK_SKILL("Kick", "Skills"),
	/**  */
	KICK_SKILL_DEBUG("Kick Debug", "Skills", DEBUG_PERSIST),
	/**  */
	PENALTY_AREA_DEFENSE("Penalty Area Defense", "Skills", DEBUG_PERSIST),
	
	/**  */
	PATH("Path", "Movement"),
	/**  */
	PATH_DEBUG("Path Debug", "Movement", DEBUG_PERSIST),
	/**  */
	TRAJ_PATH_OBSTACLES("TrajPath Obstacles", "Movement", DEBUG_PERSIST),
	
	
	/**  */
	@Deprecated
	REDIRECT_SKILL("Redirect SKill", "Skills"),
	/**  */
	@Deprecated
	TRAJ_PATH_DEBUG("TrajPath Debug", "Movement"),
	/**  */
	@Deprecated
	SPLINES("Splines", "Movement"),
	/**  */
	@Deprecated
	PATH_LATEST("Latest Path", "Movement"),
	/**  */
	@Deprecated
	POSITION_DRIVER("Position driver", "Movement"),;
	
	private final String								name;
	private final String								category;
	private final EShapeLayerPersistenceType	persistenceType;
	private final int									orderId;
	
	
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
		return ESkillShapesLayer.class.getCanonicalName() + name();
	}
	
	
	@Override
	public EShapeLayerPersistenceType getPersistenceType()
	{
		return persistenceType;
	}
}
