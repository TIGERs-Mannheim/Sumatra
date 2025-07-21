/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.instanceables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Panel for creating custom instances
 */
public class InstanceablePanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -6272636064374504265L;
	private static final Logger log = LogManager.getLogger(InstanceablePanel.class.getName());
	private static final String DEFAULT_SELECTION = "default";
	private final JComboBox<IInstanceableEnum> cbbInstances;
	private final JPanel inputPanel;
	private final List<JComponent> inputFields = new ArrayList<>();
	private final transient List<IInstanceableObserver> observers = new CopyOnWriteArrayList<>();
	private final Properties prop;
	private JButton btnCreate = null;


	public InstanceablePanel(final IInstanceableEnum[] instanceableEnums)
	{
		this(instanceableEnums, new Properties());
	}


	public InstanceablePanel(final IInstanceableEnum[] instanceableEnums, final Properties prop)
	{
		this.prop = prop;
		cbbInstances = new JComboBox<>(instanceableEnums);
		CbbInstancesActionListener cbbInstAl = new CbbInstancesActionListener();
		cbbInstances.addActionListener(cbbInstAl);
		inputPanel = new JPanel();
		inputPanel.setLayout(new GridLayout(0, 2));

		setLayout(new BorderLayout());
		add(cbbInstances, BorderLayout.NORTH);
		add(inputPanel, BorderLayout.CENTER);

		if (instanceableEnums.length > 0)
		{
			loadDefaultValue(instanceableEnums[0]);
		}

		cbbInstAl.actionPerformed(null);
	}


	public void setShowCreate(final boolean show)
	{
		if (show && (btnCreate == null))
		{
			btnCreate = new JButton("Create");
			add(btnCreate, BorderLayout.SOUTH);
			btnCreate.addActionListener(new CreateInstanceActionListener());
		} else if (!show && (btnCreate != null))
		{
			btnCreate.removeActionListener(btnCreate.getActionListeners()[0]);
			remove(btnCreate);
			btnCreate = null;
		}
	}


	public void addObserver(final IInstanceableObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}


	public void removeObserver(final IInstanceableObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}


	private void notifyNewInstance(final Object instance)
	{
		synchronized (observers)
		{
			for (IInstanceableObserver observer : observers)
			{
				observer.onNewInstance(instance);
			}
		}
	}


	public final void setSelectedItem(final Enum<?> item)
	{
		cbbInstances.setSelectedItem(item);
	}


	public final IInstanceableEnum getSelectedItem()
	{
		return (IInstanceableEnum) cbbInstances.getSelectedItem();
	}


	@Override
	public void setEnabled(final boolean enabled)
	{
		cbbInstances.setEnabled(enabled);
		inputPanel.setEnabled(enabled);
		if (btnCreate != null)
		{
			btnCreate.setEnabled(enabled);
		}
	}


	private String getModelParameterKey(final IInstanceableEnum instance, final IInstanceableParameter param)
	{
		return instance.getClass().getCanonicalName() + "." + instance.name() + "." + param.getDescription();
	}


	private String getModelDefaultSelectionKey(final IInstanceableEnum instance)
	{
		return instance.getClass().getCanonicalName() + "." + DEFAULT_SELECTION;
	}


	private void saveParamValue(final IInstanceableEnum instance, final IInstanceableParameter param, final String value)
	{
		prop.setProperty(getModelParameterKey(instance, param), value);
	}


	private void loadDefaultValue(final IInstanceableEnum instance)
	{
		String value = prop.getProperty(getModelDefaultSelectionKey(instance));
		if (value != null)
		{
			try
			{
				IInstanceableEnum instanceableEnum = instance.parse(value);
				cbbInstances.setSelectedItem(instanceableEnum);
			} catch (IllegalArgumentException e)
			{
				log.debug("Could not parse enum value: {}", value, e);
			}
		}
	}


	private class CbbInstancesActionListener implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent e)
		{
			IInstanceableEnum instance = (IInstanceableEnum) cbbInstances.getSelectedItem();
			if (instance == null)
			{
				return;
			}
			inputPanel.removeAll();
			inputFields.clear();
			for (IInstanceableParameter param : instance.getInstanceableClass().getAllParams())
			{
				String value = loadParamValue(instance, param);
				JComponent comp = getComponent(param, value);
				inputPanel.add(new JLabel(param.getDescription()));
				inputPanel.add(comp);
				inputFields.add(comp);
			}
			saveDefaultValue(instance, instance.name());
			updateUI();
		}


		private JComponent getComponent(final IInstanceableParameter param, final String value)
		{
			if (param.getImpl().isEnum())
			{
				JComboBox<?> cb = new JComboBox<>(param.getImpl().getEnumConstants());
				for (int i = 0; i < cb.getItemCount(); i++)
				{
					if (cb.getItemAt(i).toString().equals(value))
					{
						cb.setSelectedIndex(i);
						return cb;
					}
				}
				return cb;
			} else if (param.getImpl().equals(Boolean.class) || param.getImpl().equals(Boolean.TYPE))
			{
				boolean bVal = Boolean.parseBoolean(value);
				return new JCheckBox("", bVal);
			} else if (param.getImpl().equals(Path.class))
			{
				return new FileChooserPanel(Path.of(value));
			}
			int size = value.length() + 2;
			return new JTextField(value, size);
		}


		private void saveDefaultValue(final IInstanceableEnum instance, final String value)
		{
			prop.setProperty(getModelDefaultSelectionKey(instance), value);
		}


		private String loadParamValue(final IInstanceableEnum instance, final IInstanceableParameter param)
		{
			return prop.getProperty(getModelParameterKey(instance, param), param.getDefaultValue());
		}
	}

	private class CreateInstanceActionListener implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent e)
		{
			createInstance();
		}
	}


	public void createInstance()
	{
		IInstanceableEnum instanceName = (IInstanceableEnum) cbbInstances.getSelectedItem();
		if (instanceName == null)
		{
			return;
		}

		List<String> params = inputFields.stream().map(this::getValue).toList();
		int i = 0;
		for (IInstanceableParameter param : instanceName.getInstanceableClass().getAllParams())
		{
			saveParamValue(instanceName, param, params.get(i++));
		}
		Object instance = instanceName.getInstanceableClass().newInstance(params);
		notifyNewInstance(instance);
	}


	private String getValue(final JComponent comp)
	{
		if (comp.getClass().equals(JTextField.class))
		{
			JTextField textField = (JTextField) comp;
			return textField.getText();
		} else if (comp.getClass().equals(JComboBox.class))
		{
			JComboBox<?> cb = (JComboBox<?>) comp;
			return String.valueOf(cb.getSelectedItem());
		} else if (comp.getClass().equals(JCheckBox.class))
		{
			JCheckBox cb = (JCheckBox) comp;
			return String.valueOf(cb.isSelected());
		} else if (comp.getClass().equals(FileChooserPanel.class))
		{
			FileChooserPanel fileChooserPanel = (FileChooserPanel) comp;
			return fileChooserPanel.getSelectedPath().toString();
		}
		throw new IllegalStateException("Unknown component class: " + comp.getClass());
	}
}
