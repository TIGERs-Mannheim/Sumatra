/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.10.2011
 * Author(s): Oliver Steinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Graphics2D;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel.EFieldTurn;


/**
 * Base class for field layers which should be drawn upon the field visualization.
 * On default the DEBUG information is not visible. To draw additional DEBUG
 * information the method {@link AFieldLayer#paintDebugInformation(Graphics2D)} has to
 * be override in sub classes.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public abstract class AFieldLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final EFieldLayer	type;
	private IRecordFrame			aiFrame						= null;
	
	/** if true this layer is visible on the field */
	private boolean				visible;
	private final boolean		initialVisibility;
	
	private boolean				debugInformationVisible	= false;
	
	private EFieldTurn			fieldTurn					= EFieldTurn.NORMAL;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Base class for a field layer. On default a layer is visible.
	 * @param name identifier for this layer
	 */
	public AFieldLayer(EFieldLayer name)
	{
		type = name;
		visible = true;
		initialVisibility = true;
	}
	
	
	/**
	 * @param name
	 * @param visible
	 */
	public AFieldLayer(EFieldLayer name, boolean visible)
	{
		type = name;
		this.visible = visible;
		initialVisibility = visible;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Updates this layer with the actual {@link AIInfoFrame}.
	 * 
	 * @param aiFrame
	 */
	public final void update(IRecordFrame aiFrame)
	{
		this.aiFrame = aiFrame;
	}
	
	
	/**
	 * 
	 * @param g
	 */
	public void paint(Graphics2D g)
	{
		if (visible && (getAiFrame() != null))
		{
			paintLayer(g);
			if (debugInformationVisible)
			{
				paintDebugInformation(g);
			}
		}
	}
	
	
	/**
	 * Do the painting of this layer.
	 * @param g to paint on
	 */
	protected abstract void paintLayer(Graphics2D g);
	
	
	/**
	 * Draws additional DEBUG information.
	 * 
	 * @param g
	 */
	protected void paintDebugInformation(Graphics2D g)
	{
		// override this in sub classes. On default no DEBUG information is available.
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		
		if (obj == this)
		{
			return true;
		}
		
		if (!obj.getClass().equals(getClass()))
		{
			return false;
		}
		
		final AFieldLayer fieldObj = (AFieldLayer) obj;
		return (type == fieldObj.getType());
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		return (prime * result) + ((type == null) ? 0 : type.hashCode());
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the name
	 */
	public EFieldLayer getType()
	{
		return type;
	}
	
	
	/**
	 * @return true when this layer visible
	 */
	public boolean isVisible()
	{
		return visible;
	}
	
	
	/**
	 * @param visible
	 */
	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}
	
	
	/**
	 * @return true when the layers DEBUG information is visible.
	 */
	public boolean isDebugInformationVisible()
	{
		return debugInformationVisible;
	}
	
	
	/**
	 * Draws special DEBUG information on this layer.
	 * 
	 * @param debugInformationVisible
	 */
	public void setDebugInformationVisible(boolean debugInformationVisible)
	{
		this.debugInformationVisible = debugInformationVisible;
	}
	
	
	/**
	 * Resets the visibility of this layer to the initial value.
	 */
	public void setInitialVisibility()
	{
		visible = initialVisibility;
		debugInformationVisible = false;
	}
	
	
	/**
	 * @return the aiFrame
	 */
	public final IRecordFrame getAiFrame()
	{
		return aiFrame;
	}
	
	
	/**
	 * @return the fieldTurn
	 */
	public final EFieldTurn getFieldTurn()
	{
		return fieldTurn;
	}
	
	
	/**
	 * @param fieldTurn the fieldTurn to set
	 */
	public final void setFieldTurn(EFieldTurn fieldTurn)
	{
		this.fieldTurn = fieldTurn;
	}
}
