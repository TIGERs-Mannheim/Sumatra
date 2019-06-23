/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.botcenter;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.ids.BotID;
import net.miginfocom.swing.MigLayout;


/**
 * Feature overview of all bots
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BcFeaturesPanel extends JPanel
{
	/**  */
	private static final long				serialVersionUID	= -9037900752225686663L;
	private static final Insets			INSETS				= new Insets(0, 0, 0, 20);
	private final JPanel						featuresPanel;
	private final Map<State, Boolean> featureStates = new ConcurrentHashMap<>();
	private final Map<BotID, Boolean> noHWId = new ConcurrentHashMap<>();
	private boolean							dirty					= true;
	
	
	/**
	 * Constructor.
	 */
	public BcFeaturesPanel()
	{
		setLayout(new MigLayout());
		featuresPanel = new JPanel(new GridBagLayout());
		add(new JLabel("Following features are not working:"), "wrap");
		add(featuresPanel);
	}
	
	
	/**
	 * Clear all feature states.
	 */
	public void clearFeatureStates()
	{
		featureStates.clear();
	}
	
	
	/**
	 * @param botId
	 * @param hwId
	 * @param feature
	 * @param working
	 */
	public void setFeatureState(final BotID botId, final int hwId, final EFeature feature, final boolean working)
	{
		State state = new State(feature, botId, hwId);
		Boolean curState = featureStates.get(state);
		if ((curState == null) || (curState != working))
		{
			featureStates.put(state, working);
			dirty = true;
		}
	}
	
	
	/**
	 * @param id
	 * @param set
	 */
	public void setHWIdSet(final BotID id, final boolean set)
	{
		Boolean oldState = noHWId.put(id, set);
		if ((oldState == null) || (oldState != set))
		{
			dirty = true;
		}
	}
	
	
	/**
	 * Update panel.
	 */
	public void update()
	{
		if (dirty)
		{
			GridBagConstraints c1 = new GridBagConstraints();
			c1.gridx = 0;
			c1.insets = INSETS;
			c1.anchor = GridBagConstraints.WEST;
			
			EventQueue.invokeLater(() -> {
				featuresPanel.removeAll();
				
				featureStates.entrySet().stream()
						.filter(e -> !e.getValue())
						.forEach(e -> {
							String text = "Bot " + e.getKey().hwId + " with id " + e.getKey().botId + " has broken "
									+ e.getKey().feature.name();
							featuresPanel.add(new JLabel(text), c1);
						});
						
				noHWId.entrySet().stream()
						.filter(e -> !e.getValue())
						.forEach(e -> {
							String text = "Bot with id " + e.getKey() + " has no HW id set!";
							featuresPanel.add(new JLabel(text));
						});
						
				dirty = false;
			});
		}
	}
	
	
	private static class State
	{
		private final EFeature	feature;
		private final BotID		botId;
		private final int			hwId;
		
		
		/**
		 * @param feature
		 * @param botId
		 * @param hwId
		 */
		public State(final EFeature feature, final BotID botId, final int hwId)
		{
			super();
			this.feature = feature;
			this.botId = botId;
			this.hwId = hwId;
		}
		
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((botId == null) ? 0 : botId.hashCode());
			result = (prime * result) + ((feature == null) ? 0 : feature.hashCode());
			return result;
		}
		
		
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			State other = (State) obj;
			if (botId == null)
			{
				if (other.botId != null)
				{
					return false;
				}
			} else if (!botId.equals(other.botId))
			{
				return false;
			}
			return feature == other.feature;
		}
	}
}
