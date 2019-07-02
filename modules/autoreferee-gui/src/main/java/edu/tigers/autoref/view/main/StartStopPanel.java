/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.main;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JRadioButton;

import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.sumatra.components.BasePanel;


public class StartStopPanel extends BasePanel<StartStopPanel.IStartStopPanelObserver>
{
	private final Map<EAutoRefMode, ButtonModel> autoRefModeModels = new EnumMap<>(EAutoRefMode.class);
	
	private final ButtonGroup group = new ButtonGroup();
	
	
	public StartStopPanel()
	{
		JRadioButton off = new JRadioButton("Off");
		off.addActionListener(e -> autoRefModeChanged(EAutoRefMode.OFF));
		group.add(off);
		add(off);
		
		JRadioButton passive = new JRadioButton("Passive");
		passive.addActionListener(e -> autoRefModeChanged(EAutoRefMode.PASSIVE));
		group.add(passive);
		add(passive);
		
		JRadioButton active = new JRadioButton("Active");
		active.addActionListener(e -> autoRefModeChanged(EAutoRefMode.ACTIVE));
		group.add(active);
		add(active);
		
		autoRefModeModels.put(EAutoRefMode.OFF, off.getModel());
		autoRefModeModels.put(EAutoRefMode.PASSIVE, passive.getModel());
		autoRefModeModels.put(EAutoRefMode.ACTIVE, active.getModel());
		
		setAutoRefMode(EAutoRefMode.OFF);
	}
	
	
	private void autoRefModeChanged(EAutoRefMode mode)
	{
		informObserver(o -> o.changeMode(mode));
	}
	
	
	public void setAutoRefMode(EAutoRefMode mode)
	{
		group.setSelected(autoRefModeModels.get(mode), true);
	}
	
	
	@Override
	public void setEnabled(final boolean enabled)
	{
		super.setEnabled(enabled);
		Arrays.asList(getComponents()).forEach(c -> c.setEnabled(enabled));
	}
	
	public interface IStartStopPanelObserver
	{
		void changeMode(final EAutoRefMode mode);
	}
}
