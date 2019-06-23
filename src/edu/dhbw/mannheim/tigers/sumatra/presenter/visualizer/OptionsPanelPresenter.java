/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 1, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer;

import java.util.Map;

import javax.swing.JCheckBox;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.EVisualizerOptions;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IOptionsPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.OptionsPanel;


/**
 * Presenter for controlling the optionsPanel in the visualizer
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class OptionsPanelPresenter implements IOptionsPanelObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IFieldPanel	fieldPanel;
	private final OptionsPanel	optionsPanel;
	private boolean				saveOptions	= true;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param fieldPanel
	 * @param optionsPanel
	 */
	public OptionsPanelPresenter(IFieldPanel fieldPanel, OptionsPanel optionsPanel)
	{
		this.fieldPanel = fieldPanel;
		this.optionsPanel = optionsPanel;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Options checkboxes-handling
	 */
	@Override
	public void onCheckboxClick(String actionCommand, boolean isSelected)
	{
		Map<String, JCheckBox> cbs = optionsPanel.getCheckBoxes();
		
		JCheckBox selCb = cbs.get(actionCommand);
		if (selCb == null)
		{
			throw new IllegalStateException("Checkbox not found for actionCommand " + actionCommand);
		}
		
		if (saveOptions)
		{
			SumatraModel.getInstance().setUserProperty(OptionsPanel.class.getCanonicalName() + "." + actionCommand,
					String.valueOf(isSelected));
		}
		
		reactOnActionCommand(actionCommand, isSelected);
	}
	
	
	@Override
	public void onActionFired(EVisualizerOptions option, boolean state)
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
		EVisualizerOptions option = EVisualizerOptions.valueOf(actionCommand);
		if (option == null)
		{
			throw new IllegalArgumentException("action command should be something from EVisualizerOptions");
		}
		
		fieldPanel.onOptionChanged(option, isSelected);
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
	public final void setSaveOptions(boolean saveOptions)
	{
		this.saveOptions = saveOptions;
	}
}
