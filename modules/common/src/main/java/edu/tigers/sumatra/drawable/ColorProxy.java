/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 29, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.drawable;

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
