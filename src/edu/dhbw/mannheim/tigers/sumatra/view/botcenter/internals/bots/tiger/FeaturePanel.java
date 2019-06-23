/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 20, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;


/**
 * Panel for setting bot features
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class FeaturePanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger						log					= Logger.getLogger(FeaturePanel.class.getName());
	
	/**  */
	private static final long							serialVersionUID	= -1814542181664017440L;
	private final JPanel									featuresPanel;
	
	private final List<IFeatureChangedObserver>	observers			= new ArrayList<IFeatureChangedObserver>();
	
	private Map<EFeature, EFeatureState>			features				= new HashMap<EFeature, EFeatureState>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public FeaturePanel()
	{
		setLayout(new MigLayout());
		featuresPanel = new JPanel(new GridBagLayout());
		featuresPanel.setBorder(BorderFactory.createTitledBorder("Features"));
		add(featuresPanel, "wrap");
		
		JButton btnApplyToAll = new JButton("Apply to all");
		btnApplyToAll.addActionListener(new ApplyToAllActionListener());
		add(btnApplyToAll, "wrap");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param features
	 */
	public void setFeatures(final Map<EFeature, EFeatureState> features)
	{
		this.features = features;
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				featuresPanel.removeAll();
				
				GridBagConstraints c1 = new GridBagConstraints();
				c1.gridx = 0;
				GridBagConstraints c2 = new GridBagConstraints();
				c2.gridx = 1;
				GridBagConstraints c3 = new GridBagConstraints();
				c3.gridx = 2;
				
				for (Map.Entry<EFeature, EFeatureState> entry : features.entrySet())
				{
					featuresPanel.add(new JLabel(entry.getKey().getName()), c1);
					JComboBox<EFeatureState> cmb = new JComboBox<EFeatureState>(EFeatureState.values());
					cmb.setSelectedItem(entry.getValue());
					cmb.setActionCommand(entry.getKey().name());
					cmb.addItemListener(new FeatureStateSelectedListener());
					featuresPanel.add(cmb, c2);
					featuresPanel.add(new JLabel(entry.getKey().getDesc()), c3);
				}
			}
		});
	}
	
	private class FeatureStateSelectedListener implements ItemListener
	{
		
		@Override
		public void itemStateChanged(ItemEvent e)
		{
			@SuppressWarnings("unchecked")
			JComboBox<EFeatureState> cmb = (JComboBox<EFeatureState>) e.getSource();
			EFeature feature;
			try
			{
				feature = EFeature.valueOf(cmb.getActionCommand());
			} catch (IllegalArgumentException err)
			{
				log.error("Feature " + cmb.getActionCommand() + " not found");
				return;
			}
			EFeatureState state = (EFeatureState) e.getItem();
			notifyFeatureChanged(feature, state);
			features.put(feature, state);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(IFeatureChangedObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(IFeatureChangedObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyFeatureChanged(EFeature feature, EFeatureState state)
	{
		synchronized (observers)
		{
			for (IFeatureChangedObserver observer : observers)
			{
				observer.onFeatureChanged(feature, state);
			}
		}
	}
	
	private class ApplyToAllActionListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				for (IFeatureChangedObserver observer : observers)
				{
					observer.onApplyFeaturesToAll(features);
				}
			}
		}
	}
}
