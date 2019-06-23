/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 1, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.statistics;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class PositionData
{
	private int					resolutionX;
	private int					resolutionY;
	
	private List<Integer>	detectedPositions	= new ArrayList<Integer>();
	
	
	/**
	 * @param resolutionX
	 * @param resolutionY
	 */
	public PositionData(final int resolutionX, final int resolutionY)
	{
		this.resolutionX = resolutionX;
		this.resolutionY = resolutionY;
		
		for (int i = 0; i < (resolutionX * resolutionY); i++)
		{
			detectedPositions.add(0);
		}
	}
	
	
	/**
	 * @return
	 */
	public int getResolutionX()
	{
		return resolutionX;
	}
	
	
	/**
	 * @return
	 */
	public int getResolutionY()
	{
		return resolutionY;
	}
	
	
	/**
	 * Will signal that there was a position detected at the given position
	 * 
	 * @param position The position that was detected
	 */
	public void signalPositionAt(final IVector2 position)
	{
		int indexToIncrease = getIndexForPosition(position);
		
		detectedPositions.set(indexToIncrease, detectedPositions.get(indexToIncrease) + 1);
	}
	
	
	/**
	 * Will get the count of detected positions at this position
	 * 
	 * @param position The requested positions
	 * @return The count of events at this position
	 */
	public int getEntryForPosition(final IVector2 position)
	{
		return detectedPositions.get(getIndexForPosition(position));
	}
	
	
	private int getIndexForPosition(final IVector2 position)
	{
		double stepSizeX = Geometry.getFieldWidth() / resolutionX;
		int indexX = (int) ((position.get(0) + (Geometry.getFieldLength() / 2)) / stepSizeX);
		
		double stepSizeY = Geometry.getFieldLength() / resolutionY;
		int indexY = (int) ((position.get(1) + (Geometry.getFieldWidth() / 2)) / stepSizeY);
		
		return (indexX * resolutionY) + indexY;
	}
}
