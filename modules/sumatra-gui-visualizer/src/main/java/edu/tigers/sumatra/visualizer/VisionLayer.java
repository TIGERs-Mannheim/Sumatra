/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.drawable.IShapeLayer;


class VisionLayer implements IShapeLayer
{
	private final String id = VisionLayer.class.getCanonicalName() + getLayerName();
	
	
	@Override
	public String getCategory()
	{
		return "Field";
	}
	
	
	@Override
	public String getLayerName()
	{
		return "vision";
	}
	
	
	@Override
	public int hashCode()
	{
		return getId().hashCode();
	}
	
	
	@Override
	public String getId()
	{
		return id;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		return (obj != null) && obj.getClass().equals(this.getClass()) && ((VisionLayer) obj).getId().equals(getId());
	}
}
