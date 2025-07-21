/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.support.view;

import edu.tigers.sumatra.ai.metis.support.behaviors.ESupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.SupportBehaviorPosition;
import edu.tigers.sumatra.ids.BotID;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


/**
 * Displays the SupportBehavior Details for a single
 * BotID.
 */
public class SupportBehaviorBotDetailPanel extends JPanel
{
	private Map<ESupportBehavior, BehaviorBar> behaviorBarMap = new EnumMap<>(ESupportBehavior.class);
	private transient List<ESupportBehavior> hiddenBehaviors = null;
	private boolean shortLabels = false;
	
	public SupportBehaviorBotDetailPanel(BotID id)
	{
		super();
		setLayout(new GridLayout(0, 1));
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.DARK_GRAY, 2), id.toString()),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		setValues(null);
	}


	public synchronized void setValues(Map<ESupportBehavior, SupportBehaviorPosition> values)
	{
		if (values != null)
		{
			SupportBehaviorBotDetailPanel.setEnabled(this, true);
			for (var entry : values.entrySet())
			{
				if (assureBehaviorHasEntry(entry.getKey()))
				{
					behaviorBarMap.get(entry.getKey()).getProgressBar()
							.setValue((int) (100 * entry.getValue().getViability()));
				}
			}
			removeIgnored();
		} else
		{
			SupportBehaviorBotDetailPanel.setEnabled(this, false);
			for (BehaviorBar behaviorBar : behaviorBarMap.values())
			{
				behaviorBar.getProgressBar().setValue(0);
			}
		}

		this.repaint();
	}


	public void setAssignedBehavior(ESupportBehavior behavior)
	{
		behaviorBarMap.values().forEach(bar -> bar.getProgressBar().setBorder(BorderFactory.createEmptyBorder()));
		if (behavior != null)
		{
			behaviorBarMap.get(behavior).getProgressBar().setBorder(BorderFactory.createLineBorder(Color.GREEN));
		}
	}


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
				StringBuilder shortNameBuilder = new StringBuilder();
				shortNameBuilder.append(labelText.charAt(0));
				for(int x = 0; x < labelText.length() - 1; x++)
				{
					if(labelText.charAt(x) == '_')
					{
						shortNameBuilder.append(labelText.charAt(x+1));
					}
				}
				label.setText(shortNameBuilder.toString());
			} else {
				label.setText(labelText);
			}
		}


		public JProgressBar getProgressBar()
		{
			return progressBar;
		}
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

			add(behaviorBar, getSupportBehaviorIndex(supportBehavior));
			behaviorBarMap.put(supportBehavior, behaviorBar);
		}
		return true;
	}


	private int getSupportBehaviorIndex(ESupportBehavior supportBehavior)
	{
		int count = 0;
		for (ESupportBehavior behavior : ESupportBehavior.values())
		{
			if (behavior == supportBehavior)
			{
				break;
			} else if (behaviorBarMap.containsKey(behavior))
			{
				count++;
			}
		}
		return count;
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
