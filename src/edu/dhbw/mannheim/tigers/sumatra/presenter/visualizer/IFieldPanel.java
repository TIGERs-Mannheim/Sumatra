/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.10.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer;

import java.awt.Graphics2D;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordWfFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.EVisualizerOptions;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IFieldPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel.EFieldTurn;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.MultiFieldLayerUI;


/**
 * Interface a fieldpanel has to implement for controlling purpose.
 * 
 * @author Oliver Steinbrecher
 */
public interface IFieldPanel
{
	
	/**
	 * Sets the visibility of this {@link IFieldPanel}.
	 * 
	 * @param visible
	 */
	void setPanelVisible(boolean visible);
	
	
	/**
	 * Draws information stored within {@link IRecordFrame}.
	 * 
	 * @param aiFrame
	 */
	void updateAiFrame(IRecordFrame aiFrame);
	
	
	/**
	 * Draws information stored within {@link IRecordWfFrame}.
	 * 
	 * @param frame
	 */
	void updateWFrame(SimpleWorldFrame frame);
	
	
	/**
	 * Draws referee msg
	 * 
	 * @param msg
	 */
	void updateRefereeMsg(final RefereeMsg msg);
	
	
	/**
	 * @param newObserver
	 */
	void addObserver(IFieldPanelObserver newObserver);
	
	
	/**
	 * @param oldObserver
	 */
	void removeObserver(IFieldPanelObserver oldObserver);
	
	
	/**
	 * @param dsLayer
	 * @param visible
	 */
	void setLayerVisiblility(final EDrawableShapesLayer dsLayer, final boolean visible);
	
	
	/**
	 * Will be triggered, if an option on the GUI changed
	 * 
	 * @param option
	 * @param isSelected
	 */
	void onOptionChanged(EVisualizerOptions option, boolean isSelected);
	
	
	/**
	 * @return
	 */
	MultiFieldLayerUI getMultiLayer();
	
	
	/**
	 * Transforms a gui position into a global(field)position.
	 * 
	 * @param guiPosition
	 * @return globalPosition
	 */
	IVector2 transformToGlobalCoordinates(IVector2 guiPosition);
	
	
	/**
	 * Transform a gui position into a global position with respect to the team color
	 * 
	 * @param globalPosition
	 * @param invert
	 * @return
	 */
	IVector2 transformToGlobalCoordinates(IVector2 globalPosition, boolean invert);
	
	
	/**
	 * Transform a global point to a GUI point. The color is important to mirror points correctly for the blue AI
	 * 
	 * @param globalPosition
	 * @param invert
	 * @return
	 */
	IVector2 transformToGuiCoordinates(IVector2 globalPosition, boolean invert);
	
	
	/**
	 * Transforms a global(field)position into a gui position.
	 * 
	 * @param globalPosition
	 * @return guiPosition
	 */
	IVector2 transformToGuiCoordinates(IVector2 globalPosition);
	
	
	/**
	 * Scales a global x length to a gui x length.
	 * 
	 * @param length length on field
	 * @return length in gui
	 */
	int scaleXLength(float length);
	
	
	/**
	 * Scales a global y length to a gui y length.
	 * 
	 * @param length length on field
	 * @return length in gui
	 */
	int scaleYLength(float length);
	
	
	/**
	 * width with margins
	 * 
	 * @return
	 */
	int getFieldTotalWidth();
	
	
	/**
	 * height width margins
	 * 
	 * @return
	 */
	int getFieldTotalHeight();
	
	
	/**
	 * Turn the field in desired angle
	 * 
	 * @param fieldTurn
	 * @param angle [rad]
	 * @param g2
	 */
	void turnField(EFieldTurn fieldTurn, double angle, Graphics2D g2);
	
	
	/**
	 */
	void clearField();
	
	
	/**
	 * @return
	 */
	EFieldTurn getFieldTurn();
	
	
	/**
	 * @return
	 */
	int getFieldHeight();
	
	
	/**
	 * @return
	 */
	int getFieldWidth();
	
	
	/**
	 * @return the scaleFactor
	 */
	float getScaleFactor();
	
	
	/**
	 * @return the fieldOriginY
	 */
	float getFieldOriginY();
	
	
	/**
	 * @return the fieldOriginX
	 */
	float getFieldOriginX();
	
	
	/**
	 * @return
	 */
	int getWidth();
	
	
	/**
	 * @return
	 */
	int getHeight();
	
	
	/**
	 * 
	 */
	void repaint();
}
