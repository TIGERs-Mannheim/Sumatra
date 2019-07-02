/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 24, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.sumatra.components;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import org.apache.log4j.Logger;


/**
 * Generic Panel class that accepts an enum type as argument and displays checkboxes for each constant in the enum.
 * The checkboxes can be laid out horizontally or vertically. Observers are notified when the selection of a checkbox
 * is changed.
 * 
 * @param <T> The enum class to use
 */
public class EnumCheckBoxPanel<T extends Enum<T>> extends BasePanel<EnumCheckBoxPanel.IEnumPanelObserver<T>>
{
	private static final long serialVersionUID = 5263861341015714105L;
	private static final Logger log = Logger.getLogger(EnumCheckBoxPanel.class);
	
	private Function<T, String> formatter;
	private Class<T> enumClass;
	private Map<T, JCheckBox> boxes;
	
	
	/**
	 * @see #EnumCheckBoxPanel(Class, String, int, Function)
	 * @param enumClass
	 * @param title
	 * @param orientation
	 */
	public EnumCheckBoxPanel(final Class<T> enumClass, final String title, final int orientation)
	{
		this(enumClass, title, orientation, null);
	}
	
	/**
	 * @param enumClass The class of the enum
	 * @param title The title of the panel - if set to null no border will be drawn
	 * @param orientation {@link BoxLayout#PAGE_AXIS} or {@link BoxLayout#LINE_AXIS}
	 * @param formatter Custom formatter which is used to derive the labels of the checkboxes from the enum constants -
	 *           if null the name of the constants will be used
	 */
	private EnumCheckBoxPanel(final Class<T> enumClass, final String title, final int orientation,
			final Function<T, String> formatter)
	{
		this.enumClass = enumClass;
		boxes = new EnumMap<>(enumClass);
		this.formatter = formatter;
		
		
		createBoxes(orientation);
		
		if (title != null)
		{
			setBorder(BorderFactory.createTitledBorder(title));
		}
	}
	
	
	public void addToggleAllButton()
	{
		JButton toggleButton = new JButton("(de)select all");
		toggleButton.addActionListener(e -> {
			if (getValues().size() < (boxes.size() / 2))
			{
				setSelectedBoxes(boxes.keySet());
			} else
			{
				setSelectedBoxes(Collections.emptySet());
			}
		});
		add(toggleButton);
	}
	
	
	private void createBoxes(final int orientation)
	{
		setLayout(new BoxLayout(this, orientation));
		
		List<T> sortedEntries = Arrays.stream(enumClass.getEnumConstants())
				.sorted(Comparator.comparing(Enum::toString)).collect(Collectors.toList());
		for (T type : sortedEntries)
		{
			JCheckBox checkBox = new JCheckBox(getBoxLabel(type));
			checkBox.setSelected(true);
			checkBox.addItemListener(new CheckBoxItemListener());
			boxes.put(type, checkBox);
			add(checkBox);
		}
	}
	
	
	private String getBoxLabel(final T type)
	{
		if (formatter != null)
		{
			return formatter.apply(type);
		}
		return type.name();
	}
	
	
	@Override
	public void setEnabled(final boolean enabled)
	{
		super.setEnabled(enabled);
		boxes.values().forEach(box -> box.setEnabled(enabled));
	}
	
	
	public Set<T> getValues()
	{
		Set<T> values = new HashSet<>();
		for (Map.Entry<T, JCheckBox> entry : boxes.entrySet())
		{
			JCheckBox box = entry.getValue();
			if (box.isSelected())
			{
				values.add(entry.getKey());
			}
		}
		return values;
	}
	
	
	public void setSelectedBoxes(final Set<T> enabledBoxes)
	{
		boxes.keySet().forEach(t -> boxes.get(t).setSelected(enabledBoxes.contains(t)));
	}
	
	private class CheckBoxItemListener implements ItemListener
	{
		@Override
		public void itemStateChanged(final ItemEvent e)
		{
			try
			{
				T enumValue = Enum.valueOf(enumClass, ((JCheckBox) e.getSource()).getActionCommand());
				boolean value = ((JCheckBox) e.getSource()).isSelected();
				onSelectionChange(enumValue, value);
			} catch (IllegalArgumentException ex)
			{
				log.warn("Unable to parse \"" + ((JCheckBox) e.getSource()).getActionCommand() + "\" to enum value", ex);
			}
		}
		
		
		private void onSelectionChange(final T type, final boolean value)
		{
			informObserver(obs -> obs.onValueTicked(type, value));
		}
	}
	
	/**
	 * The observer interface of the {@link EnumCheckBoxPanel} class
	 *
	 * @param <E>
	 */
	public interface IEnumPanelObserver<E>
	{
		void onValueTicked(E type, boolean value);
	}
}
