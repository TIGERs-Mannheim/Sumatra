/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 24, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.sumatra.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.components.IEnumPanel.IEnumPanelObserver;


/**
 * Generic Panel class that accepts an enum type as argument and displays checkboxes for each constant in the enum.
 * The checkboxes can be laid out horizontally or vertically. Observers are notified when the selection of a checkbox
 * is changed.
 * 
 * @author "Lukas Magel"
 * @param <T> The enum class to use
 */
public class EnumCheckBoxPanel<T extends Enum<T>> extends BasePanel<IEnumPanelObserver<T>> implements IEnumPanel<T>
{
	/**  */
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
	public EnumCheckBoxPanel(final Class<T> enumClass, final String title, final int orientation,
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
	
	
	private void createBoxes(final int orientation)
	{
		setLayout(new BoxLayout(this, orientation));
		
		for (T type : enumClass.getEnumConstants())
		{
			JCheckBox checkBox = new JCheckBox(getBoxLabel(type));
			checkBox.setSelected(true);
			checkBox.addActionListener(new CheckBoxActionListener());
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
	public void setPanelEnabled(final boolean enabled)
	{
		boxes.values().forEach(box -> box.setEnabled(enabled));
	}
	
	
	@Override
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
	
	
	@Override
	public void setSelectedBoxes(final Set<T> enabledBoxes)
	{
		boxes.keySet().forEach(t -> boxes.get(t).setSelected(enabledBoxes.contains(t)));
	}
	
	private class CheckBoxActionListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			try
			{
				T enumValue = Enum.valueOf(enumClass, e.getActionCommand());
				boolean value = ((JCheckBox) e.getSource()).isSelected();
				onSelectionChange(enumValue, value);
			} catch (IllegalArgumentException ex)
			{
				log.warn("Unable to parse \"" + e.getActionCommand() + "\" to enum value", ex);
			}
		}
		
		
		private void onSelectionChange(final T type, final boolean value)
		{
			informObserver(obs -> obs.onValueTicked(type, value));
		}
	}
}
