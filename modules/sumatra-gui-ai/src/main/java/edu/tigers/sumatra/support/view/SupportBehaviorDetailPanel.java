/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.support.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import edu.tigers.sumatra.ai.pandora.roles.support.ESupportBehavior;
import edu.tigers.sumatra.ids.BotID;


public class SupportBehaviorDetailPanel extends JPanel
{
	private Map<ESupportBehavior, BehaviorBar> behaviorBarMap = new EnumMap<>(ESupportBehavior.class);
	private transient List<ESupportBehavior> hiddenBehaviors = null;
	private boolean shortLabels = false;
	
	protected class BehaviorBar extends JPanel
	{
		protected JProgressBar progressBar;
		protected String labelText;
		protected JLabel label;
		
		public BehaviorBar(ESupportBehavior behavior)
		{
			labelText = behavior.name();
			label = new JLabel(behavior.name());
			setLayout(new GridLayout(1, 2));
			add(label);
			progressBar = new JProgressBar();
			progressBar.setStringPainted(true);
			add(progressBar);
		}

		public void updateLabels() {
			if (shortLabels) {
				StringBuilder temp = new StringBuilder();
				temp.append(labelText.charAt(0));
				for(int x = 0; x < labelText.length() - 1; x++)
				{
					if(labelText.charAt(x) == '_')
					{
						temp.append(labelText.charAt(x+1));
					}
				}
				label.setText(temp.toString());
			} else {
				label.setText(labelText);
			}
		}
		
		
		public JProgressBar getProgressBar()
		{
			return progressBar;
		}
	}
	
	
	public SupportBehaviorDetailPanel(BotID id)
	{
		super();
		setLayout(new GridLayout(0, 1));
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.DARK_GRAY, 2), id.toString()),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		setValues(null);
	}
	
	
	public synchronized void setValues(Map<ESupportBehavior, Double> values)
	{
		if (values != null)
		{
			SupportBehaviorDetailPanel.setEnabled(this, true);
			for (Map.Entry<ESupportBehavior, Double> entry : values.entrySet())
			{
				if (assureBehaviorHasEntry(entry.getKey()))
				{
					behaviorBarMap.get(entry.getKey()).getProgressBar().setValue((int) (100 * entry.getValue()));
				}
			}
			removeIgnored();
		} else
		{
			SupportBehaviorDetailPanel.setEnabled(this, false);
			for (BehaviorBar behaviorBar : behaviorBarMap.values())
			{
				behaviorBar.getProgressBar().setValue(0);
			}
		}

		this.repaint();
	}

	public void setShortLabels(final boolean shortLabels) {
		this.shortLabels = shortLabels;
		for(BehaviorBar behaviorBar : behaviorBarMap.values())
		{
			behaviorBar.updateLabels();
		}
		this.revalidate();

	}
	
	/**
	 * Removes all ignored behaviors from the details
	 */
	private void removeIgnored()
	{
		if (hiddenBehaviors == null)
		{
			return;
		}
		
		Map<ESupportBehavior, BehaviorBar> newValues = new EnumMap<>(ESupportBehavior.class);
		for (Map.Entry<ESupportBehavior, BehaviorBar> behaviorEntry : behaviorBarMap.entrySet())
		{
			if (hiddenBehaviors.contains(behaviorEntry.getKey()))
			{
				remove(behaviorEntry.getValue());
			} else
			{
				newValues.put(behaviorEntry.getKey(), behaviorEntry.getValue());
			}
		}
		behaviorBarMap = newValues;
	}
	
	
	/**
	 * @param supportBehavior The behavior that should be checked
	 * @return true if behavior has an entry or if one was created
	 */
	private boolean assureBehaviorHasEntry(ESupportBehavior supportBehavior)
	{
		if (!behaviorBarMap.containsKey(supportBehavior))
		{
			if (hiddenBehaviors != null && hiddenBehaviors.contains(supportBehavior))
			{
				return false;
			}
			
			BehaviorBar behaviorBar = new BehaviorBar(supportBehavior);
			behaviorBar.updateLabels();
			add(behaviorBar);
			behaviorBarMap.put(supportBehavior, behaviorBar);
		}
		return true;
	}
	
	
	private static void setEnabled(Component component, boolean enabled)
	{
		component.setEnabled(enabled);
		if (component instanceof Container)
		{
			for (Component child : ((Container) component).getComponents())
			{
				setEnabled(child, enabled);
			}
		}
	}
	
	
	@Override
	public Dimension getPreferredSize()
	{
		if(shortLabels)
		{
			return new Dimension(150,180);
		}

		return new Dimension(350, 180);
	}
	
	
	public void setHiddenBehaviors(final List<ESupportBehavior> hiddenBehaviors)
	{
		this.hiddenBehaviors = hiddenBehaviors;
	}
}
