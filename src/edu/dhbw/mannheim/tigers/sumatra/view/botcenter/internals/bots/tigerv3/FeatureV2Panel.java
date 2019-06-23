/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 14, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv3;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class FeatureV2Panel extends JPanel
{
	
	/**  */
	private static final long					serialVersionUID	= -1560622831253060761L;
	private static final Insets				INSETS				= new Insets(0, 0, 0, 20);
	private final JPanel							featuresPanel;
	private final Map<EFeature, Boolean>	featureStates		= new HashMap<>();
	private boolean								dirty					= true;
	
	
	/**
	 * 
	 */
	public FeatureV2Panel()
	{
		setLayout(new MigLayout());
		featuresPanel = new JPanel(new GridBagLayout());
		add(featuresPanel);
	}
	
	
	/**
	 * @param feature
	 * @param working
	 */
	public void setFeatureState(final EFeature feature, final boolean working)
	{
		Boolean state = featureStates.get(feature);
		if ((state == null) || (state != working))
		{
			featureStates.put(feature, working);
			dirty = true;
		}
	}
	
	
	/**
	 */
	public void update()
	{
		if (dirty)
		{
			EventQueue.invokeLater(() -> {
				featuresPanel.removeAll();
				GridBagConstraints c1 = new GridBagConstraints();
				c1.gridx = 0;
				c1.insets = INSETS;
				c1.anchor = GridBagConstraints.WEST;
				GridBagConstraints c2 = new GridBagConstraints();
				c2.gridx = 1;
				c2.anchor = GridBagConstraints.WEST;
				for (Map.Entry<EFeature, Boolean> entry : featureStates.entrySet())
				{
					featuresPanel.add(new JLabel(entry.getKey().getName()), c1);
					featuresPanel.add(new JLabel(entry.getValue() ? "OK" : "broken"), c2);
				}
				dirty = false;
			});
		}
	}
}
