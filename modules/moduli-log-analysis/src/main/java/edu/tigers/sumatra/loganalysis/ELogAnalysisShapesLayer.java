/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.loganalysis;


import edu.tigers.sumatra.drawable.IShapeLayer;


/**
 * @author "Stefan Schneyer"
 */
@SuppressWarnings("squid:S1192") // duplicated strings not avoidable here
public enum ELogAnalysisShapesLayer implements IShapeLayer
{
	LOG_ANALYSIS("Log_Analysis", "LogAnalysis", true),
	BALL_POSSESSION("Ball_Possession", "LogAnalysis", true),
	DRIBBLING("DribblingDetection", "LogAnalysis", true),
	GOAL_SHOT("Goal_Shot", "LogAnalysis", true),
	PASSING("PassingDetection", "LogAnalysis", true),
	GAME_MEMORY("Game_Memory", "LogAnalysis", true);
	
	private final String id;
	private final String name;
	private final String category;
	private final boolean visible;
	
	
	ELogAnalysisShapesLayer(final String name, final String category, final boolean visible)
	{
		this.name = name;
		this.category = category;
		this.visible = visible;
		id = ELogAnalysisShapesLayer.class.getCanonicalName() + name();
	}
	
	
	@Override
	public String getCategory()
	{
		return category;
	}
	
	
	@Override
	public String getLayerName()
	{
		return name;
	}
	
	
	@Override
	public String getId()
	{
		return id;
	}
	
	
	@Override
	public boolean isVisibleByDefault()
	{
		return visible;
	}
	
}
