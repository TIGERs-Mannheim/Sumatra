/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 1, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.visualizer.view.EVisualizerOptions;
import edu.tigers.sumatra.visualizer.view.IOptionsPanelObserver;
import edu.tigers.sumatra.visualizer.view.field.IFieldPanel;


/**
 * Presenter for controlling the optionsPanel in the visualizer
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class OptionsPanelPresenter implements IOptionsPanelObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IFieldPanel	fieldPanel;
	private boolean				saveOptions	= true;
														
														
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param fieldPanel
	 */
	public OptionsPanelPresenter(final IFieldPanel fieldPanel)
	{
		this.fieldPanel = fieldPanel;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Options checkboxes-handling
	 */
	@Override
	public void onCheckboxClick(final String actionCommand, final boolean isSelected)
	{
		if (saveOptions)
		{
			SumatraModel.getInstance().setUserProperty(
					OptionsPanelPresenter.class.getCanonicalName() + "." + actionCommand,
					String.valueOf(isSelected));
		}
		
		reactOnActionCommand(actionCommand, isSelected);
	}
	
	
	@Override
	public void onActionFired(final EVisualizerOptions option, final boolean state)
	{
		fieldPanel.onOptionChanged(option, state);
	}
	
	
	/**
	 * Do what has to be done for the specified action command
	 * 
	 * @param actionCommand
	 * @param isSelected
	 */
	public void reactOnActionCommand(final String actionCommand, final boolean isSelected)
	{
		try
		{
			EVisualizerOptions option = EVisualizerOptions.valueOf(actionCommand);
			fieldPanel.onOptionChanged(option, isSelected);
		} catch (IllegalArgumentException err)
		{
			fieldPanel.setLayerVisiblility(actionCommand, isSelected);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the saveOptions
	 */
	public final boolean isSaveOptions()
	{
		return saveOptions;
	}
	
	
	/**
	 * @param saveOptions the saveOptions to set
	 */
	public final void setSaveOptions(final boolean saveOptions)
	{
		this.saveOptions = saveOptions;
	}
	
	
}
