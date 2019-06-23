/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 24, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.sumatra.components;

import java.util.Set;

import edu.tigers.sumatra.components.IEnumPanel.IEnumPanelObserver;


/**
 * @author "Lukas Magel"
 * @param <T>
 */
public interface IEnumPanel<T extends Enum<T>> extends IBasePanel<IEnumPanelObserver<T>>
{
	/**
	 * The observer interface of the {@link EnumCheckBoxPanel} class
	 * 
	 * @author "Lukas Magel"
	 * @param <E>
	 */
	public interface IEnumPanelObserver<E>
	{
		
		/**
		 * @param type
		 * @param value
		 */
		public void onValueTicked(E type, boolean value);
	}
	
	
	/**
	 * Returns a set of all enum constants which are currently selected
	 * 
	 * @return modifiable non shared set instance
	 */
	public Set<T> getValues();
	
	
	/**
	 * Select the boxes of all enums which are contained in the {@code enabledBoxes} set and unselect all others.
	 * 
	 * @param enabledBoxes
	 */
	public void setSelectedBoxes(final Set<T> enabledBoxes);
}
