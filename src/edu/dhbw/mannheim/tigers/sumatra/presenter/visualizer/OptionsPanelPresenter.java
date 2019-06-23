/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 1, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer;

import java.util.Map;

import javax.swing.JCheckBoxMenuItem;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.EVisualizerOptions;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IOptionsPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.VisualizerOptionsMenu;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


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
	
	private final IFieldPanel				fieldPanel;
	private final VisualizerOptionsMenu	optionsMenu;
	private boolean							saveOptions	= true;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param fieldPanel
	 * @param optionsMenu
	 */
	public OptionsPanelPresenter(final IFieldPanel fieldPanel, final VisualizerOptionsMenu optionsMenu)
	{
		this.fieldPanel = fieldPanel;
		this.optionsMenu = optionsMenu;
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
		Map<String, JCheckBoxMenuItem> cbs = optionsMenu.getCheckBoxes();
		
		JCheckBoxMenuItem selCb = cbs.get(actionCommand);
		if (selCb == null)
		{
			throw new IllegalStateException("Checkbox not found for actionCommand " + actionCommand);
		}
		
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
	
	
	@Override
	public void onPosBufferChanged(final int value)
	{
		// PositionBufferLayer l = (PositionBufferLayer) fieldPanel.getMultiLayer().getFieldLayer(
		// EFieldLayer.POSITION_BUFFER);
		// l.setBotBufferSize(value);
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
			EDrawableShapesLayer layer = EDrawableShapesLayer.valueOf(actionCommand);
			fieldPanel.setLayerVisiblility(layer, isSelected);
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
