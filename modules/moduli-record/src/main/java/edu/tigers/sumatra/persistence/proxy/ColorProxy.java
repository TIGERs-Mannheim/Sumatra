/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.persistence.proxy;

import java.awt.Color;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PersistentProxy;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(proxyFor = Color.class)
public class ColorProxy implements PersistentProxy<Color>
{
	private ColorWrapper wrapper = null;
	
	
	@Override
	public Color convertProxy()
	{
		if (wrapper != null)
		{
			return wrapper.getColor();
		}
		return Color.BLACK;
	}
	
	
	@Override
	public void initializeProxy(final Color arg0)
	{
		wrapper = new ColorWrapper(arg0);
	}
	
}
