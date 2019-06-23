/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.drawable.EFieldTurn;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.drawable.ShapeMap;


/**
 * Interface a fieldpanel has to implement for controlling purpose.
 * 
 * @author Oliver Steinbrecher
 */
public interface IFieldPanel extends IDrawableTool
{
	/**
	 * 
	 */
	void start();
	
	
	/**
	 * 
	 */
	void stop();
	
	
	/**
	 * 
	 */
	void paintOffline();
	
	
	/**
	 * Sets the visibility of this panel
	 * 
	 * @param visible
	 */
	void setPanelVisible(boolean visible);
	
	
	/**
	 * @param source
	 * @param shapeMap
	 */
	void setShapeMap(String source, ShapeMap shapeMap);
	
	
	/**
	 * @param newObserver
	 */
	void addObserver(IFieldPanelObserver newObserver);
	
	
	/**
	 * @param oldObserver
	 */
	void removeObserver(IFieldPanelObserver oldObserver);
	
	
	/**
	 * @param layerId
	 * @param visible
	 */
	void setShapeLayerVisibility(final String layerId, final boolean visible);
	
	
	/**
	 * @param option
	 * @param isSelected
	 */
	void onOptionChanged(final EVisualizerOptions option, final boolean isSelected);
	
	
	/**
	 */
	void clearField();
	
	
	/**
	 * Set source visibility
	 * 
	 * @param source
	 * @param visible
	 */
	void setSourceVisibility(String source, boolean visible);
	
	
	/**
	 * @param fieldTurn
	 */
	void setFieldTurn(EFieldTurn fieldTurn);
	
	
	/**
	 * @param fancy
	 */
	void setFancyPainting(boolean fancy);
}
