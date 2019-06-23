/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.10.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.FieldRasterConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.EVisualizerOptions;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IFieldPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.MultiFieldLayerUI;


/**
 * Interface a fieldpanel has to implement for controlling purpose.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public interface IFieldPanel
{
	
	/**
	 * Sets the visibility of this {@link IFieldPanel}.
	 * @param visible
	 */
	void setPanelVisible(boolean visible);
	
	
	/**
	 * Draws information stored within {@link IRecordFrame}.
	 * @param aiFrame
	 */
	void drawAIFrame(AIInfoFrame aiFrame);
	
	
	/**
	 * Draws information stored within {@link IRecordFrame}.
	 * @param recFrame
	 */
	void drawRecordFrame(IRecordFrame recFrame);
	
	
	/**
	 * Sets the {@link FieldRasterConfig}-configuration to the visualization panel.
	 * @param fieldRasterconfig
	 */
	void setNewFieldRaster(FieldRasterConfig fieldRasterconfig);
	
	
	/**
	 * Sets the actual paths of the bots which can be drawn.
	 * @param paths
	 */
	void setPaths(List<Path> paths);
	
	
	/**
	 * 
	 * @param newObserver
	 */
	void addObserver(IFieldPanelObserver newObserver);
	
	
	/**
	 * 
	 * @param oldObserver
	 */
	void removeObserver(IFieldPanelObserver oldObserver);
	
	
	/**
	 * Will be triggered, if an option on the GUI changed
	 * 
	 * @param option
	 * @param isSelected
	 */
	void onOptionChanged(EVisualizerOptions option, boolean isSelected);
	
	
	/**
	 * 
	 * @return
	 */
	MultiFieldLayerUI getMultiLayer();
	
}
