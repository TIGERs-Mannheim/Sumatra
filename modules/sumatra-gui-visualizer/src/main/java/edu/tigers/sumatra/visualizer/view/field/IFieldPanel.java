/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer.view.field;

import edu.tigers.sumatra.drawable.EFieldTurn;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.visualizer.view.EVisualizerOptions;
import edu.tigers.sumatra.visualizer.view.IFieldPanelObserver;


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
	 * Sets the visibility of this {@link IFieldPanel}.
	 * 
	 * @param visible
	 */
	void setPanelVisible(boolean visible);
	
	
	/**
	 * @param source
	 * @param shapeMap
	 * @param inverted
	 */
	void setShapeMap(EShapeLayerSource source, ShapeMap shapeMap, boolean inverted);
	
	
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
	void setLayerVisiblility(final String layerId, final boolean visible);
	
	
	/**
	 * @param option
	 * @param isSelected
	 */
	void onOptionChanged(final EVisualizerOptions option, final boolean isSelected);
	
	
	/**
	 */
	void clearField();
	
	
	/**
	 * @param source
	 */
	void clearField(final EShapeLayerSource source);
	
	
	/**
	 * 
	 */
	void repaint();
	
	
	/**
	 * @param fieldTurn
	 */
	public void setFieldTurn(EFieldTurn fieldTurn);
	
	
	/**
	 * @return
	 */
	public boolean isFancyPainting();
	
	
	/**
	 * @param fancy
	 */
	public void setFancyPainting(boolean fancy);
	
	
	/**
	 * @return
	 */
	public boolean isPaintCoordinates();
	
	
	/**
	 * @param paint
	 */
	public void setPaintCoordinates(boolean paint);
}
