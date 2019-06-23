/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 22, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import edu.tigers.sumatra.wp.data.ShapeMap.IShapeLayer;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EShapesLayer implements IShapeLayer
{
	/**  */
	GPU_GRID("GPU grid", "AI", false, 0),
	/** */
	CPU_GRID("CPU grid", "AI"),
	/** */
	SUPPORTER("CPU Supporter", "AI"),
	/**  */
	COORDINATE_SYSTEM("Coordinate System", "FIELD", true, 11),
	/**  */
	BOTS_AVAILABLE("Bots available", "FIELD"),
	
	/**  */
	SKILL_NAMES("Skill names", "SKILLS"),
	/**  */
	RECEIVER_SKILL("Receiver Skill", "SKILLS"),
	/**  */
	REDIRECT_SKILL("Redirect SKill", "SKILLS"),
	/**  */
	KICK_SKILL("Kick Skill", "SKILLS"),
	/**  */
	KICK_SKILL_DEBUG("Kick Skill Debug", "SKILLS"),
	
	/**  */
	PATH("Path", "MOVEMENT"),
	/**  */
	PATH_DEBUG("Path Debug", "MOVEMENT"),
	/**  */
	PATH_LATEST("Latest Path", "MOVEMENT"),
	/**  */
	SPLINES("Splines", "MOVEMENT"),
	/**  */
	POSITION_DRIVER("Position driver", "MOVEMENT"),
	/**  */
	TRAJ_PATH_DEBUG("TrajPath Debug", "MOVEMENT"),
	/**  */
	TRAJ_PATH_OBSTACLES("TrajPath Obstacles", "MOVEMENT"),
	
	/**  */
	ROLE_NAMES("Role names", "AI"),
	
	/**  */
	GOAL_POINTS("Goal points", "AI"),
	/**  */
	BALL_POSSESSION("Ball possession", "AI"),
	
	/** */
	DEFENSE("Defense", "AI"),
	/** */
	DEFENSE_ADDITIONAL("Defense - additional", "AI"),
	/** */
	ANGLE_DEFENSE("Angle defense", "AI"),
	/** */
	KEEPER("Keeper", "AI"),
	
	/**  */
	TOP_GPU_GRID("Top GPU grid pos", "AI"),
	/**  */
	PASS_TARGETS("Pass targets", "AI"),
	/** */
	BIG_DATA("Goal chance heatmap", "AI"),
	/**  */
	REDIRECT_ROLE("Redirect Role", "AI"),
	/** */
	MARKERS("Markers", "AI"),
	
	/**  */
	OFFENSIVE("Offensive", "AI"),
	/**  */
	KICK_SKILL_TIMING("KickSkill Timing", "AI"),
	
	/**  */
	OFFENSIVE_FINDER("Offensive - bot finder", "AI"),
	
	/**  */
	OFFENSIVE_ADDITIONAL("Offensive - Additional", "AI"),
	
	/**  */
	AUTOMATED_THROW_IN("Automated placement", "AI"),
	
	/** */
	LEARNING("Learning"),
	
	/**  */
	INJECT("Inject"),
	/**  */
	UNSORTED("Unsorted");
	
	
	private final String		name;
	private final String		category;
	private final boolean	persist;
	private final int			orderId;
	
	
	private EShapesLayer(final String name)
	{
		this(name, "UNCATEGORIZED");
	}
	
	
	/**
	 * 
	 */
	private EShapesLayer(final String name, final String category)
	{
		this(name, category, true);
	}
	
	
	/**
	 * 
	 */
	private EShapesLayer(final String name, final String category, final boolean persist)
	{
		this.name = name;
		this.category = category;
		this.persist = persist;
		orderId = 50 + ordinal();
	}
	
	
	/**
	 * 
	 */
	private EShapesLayer(final String name, final String category, final boolean persist, final int orderId)
	{
		this.name = name;
		this.category = category;
		this.persist = persist;
		this.orderId = orderId;
	}
	
	
	@Override
	public String getLayerName()
	{
		return name;
	}
	
	
	/**
	 * @return the category
	 */
	@Override
	public final String getCategory()
	{
		return category;
	}
	
	
	/**
	 * @return the persist
	 */
	@Override
	public final boolean persist()
	{
		return persist;
	}
	
	
	@Override
	public int getOrderId()
	{
		return orderId;
	}
	
	
	@Override
	public String getId()
	{
		return EShapesLayer.class.getCanonicalName() + name();
	}
}
