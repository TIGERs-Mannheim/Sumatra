/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee;


import edu.tigers.sumatra.drawable.ShapeMap;


/**
 * @author "Lukas Magel"
 */
@SuppressWarnings("squid:S1192") // duplicated strings not avoidable here
public enum EAutoRefShapesLayer implements ShapeMap.IShapeLayer
{
	/**  */
	ENGINE("Engine", "AutoReferee", true),
	/**  */
	LAST_BALL_CONTACT("Ball Contact", "AutoReferee", true),
	/**  */
	LAST_BALL_CONTACT_EXT("Ball Contact ext", "AutoReferee", false),
	/**  */
	BALL_LEFT_FIELD("Ball Left Field", "AutoReferee", true);
	
	private final String name;
	private final String category;
	private final boolean visible;
	
	
	EAutoRefShapesLayer(final String name, final String category, final boolean visible)
	{
		this.name = name;
		this.category = category;
		this.visible = visible;
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
		return EAutoRefShapesLayer.class.getCanonicalName() + name();
	}
	
	
	@Override
	public boolean isVisibleByDefault()
	{
		return visible;
	}
	
}
