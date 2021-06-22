/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.model.SumatraModel;

import java.util.Arrays;


/**
 * Presenter for controlling the optionsPanel in the visualizer
 */
public class OptionsPanelPresenter implements IOptionsPanelObserver
{
	private final FieldPanel fieldPanel;
	private boolean saveOptions = true;


	/**
	 * @param fieldPanel
	 */
	public OptionsPanelPresenter(final FieldPanel fieldPanel)
	{
		this.fieldPanel = fieldPanel;
	}


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
	private void reactOnActionCommand(final String actionCommand, final boolean isSelected)
	{
		if (actionCommand.startsWith(VisualizerOptionsMenu.SOURCE_PREFIX))
		{
			String layer = actionCommand.replace(VisualizerOptionsMenu.SOURCE_PREFIX, "");
			fieldPanel.setSourceVisibility(layer, isSelected);
		} else if (actionCommand.startsWith(VisualizerOptionsMenu.CATEGORY_PREFIX))
		{
			String layer = actionCommand.replace(VisualizerOptionsMenu.CATEGORY_PREFIX, "");
			fieldPanel.setSourceCategoryVisibility(layer, isSelected);
		} else if (isVisualizerOption(actionCommand))
		{
			EVisualizerOptions option = EVisualizerOptions.valueOf(actionCommand);
			fieldPanel.onOptionChanged(option, isSelected);
		} else
		{
			fieldPanel.setShapeLayerVisibility(actionCommand, isSelected);
		}
	}


	private boolean isVisualizerOption(String actionCommand)
	{
		return Arrays.stream(EVisualizerOptions.values()).map(Enum::name)
				.anyMatch(actionCommand::equals);
	}


	/**
	 * @param saveOptions the saveOptions to set
	 */
	public final void setSaveOptions(final boolean saveOptions)
	{
		this.saveOptions = saveOptions;
	}
}
