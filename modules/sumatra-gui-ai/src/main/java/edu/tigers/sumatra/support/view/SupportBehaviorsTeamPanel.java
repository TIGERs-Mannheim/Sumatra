/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.support.view;

import edu.tigers.sumatra.ai.metis.support.behaviors.ESupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.SupportBehaviorPosition;
import edu.tigers.sumatra.components.BetterScrollPane;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.util.MigLayoutResizeListener;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * This class contains the actual visualization of the
 * two supporter teams. There will be an instance per team.
 */
class SupportBehaviorsTeamPanel extends JPanel
{
	private transient Map<BotID, SupportBehaviorBotDetailPanel> detailPanelMap = new HashMap<>();
	private JPanel details = new JPanel();
	private Map<BotID, ESupportBehavior> supportBehaviorAssignment = null;
	private Map<BotID, EnumMap<ESupportBehavior, SupportBehaviorPosition>> supportBehaviorViabilities = null;
	private Map<ESupportBehavior, Boolean> activeSupportBehaviors;

	private boolean shortNames = false;
	private boolean showInactive = false;


	public SupportBehaviorsTeamPanel()
	{
		setLayout(new BorderLayout());
		BetterScrollPane scrollPane = new BetterScrollPane(details);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);

		details.setLayout(new MigLayout(new LC().wrapAfter(2), new AC().gap("10"), new AC()));
		new MigLayoutResizeListener(this, details, -1);
	}


	public synchronized void update()
	{
		if (supportBehaviorViabilities == null)
		{
			return;
		}
		updateBots();
		updateValues();
	}


	private void updateBots()
	{
		for (BotID botID : supportBehaviorViabilities.keySet())
		{
			if (!detailPanelMap.containsKey(botID))
			{
				SupportBehaviorBotDetailPanel detailPanel = new SupportBehaviorBotDetailPanel(botID);
				detailPanel.setShortLabels(shortNames);
				details.add(detailPanel);
				detailPanelMap.put(botID, detailPanel);
			}
		}
	}


	private void updateValues()
	{
		for (Map.Entry<BotID, SupportBehaviorBotDetailPanel> panelEntry : detailPanelMap.entrySet())
		{
			if (!showInactive)
			{
				List<ESupportBehavior> hiddenBehaviors = activeSupportBehaviors.entrySet().stream()
						.filter(entry -> !entry.getValue())
						.map(Map.Entry::getKey).collect(Collectors.toList());
				panelEntry.getValue().setHiddenBehaviors(hiddenBehaviors);
			} else
			{
				panelEntry.getValue().setHiddenBehaviors(Collections.emptyList());
			}
			panelEntry.getValue().setValues(supportBehaviorViabilities.getOrDefault(panelEntry.getKey(), null));
			panelEntry.getValue().setAssignedBehavior(supportBehaviorAssignment.getOrDefault(panelEntry.getKey(), null));
		}
	}


	public void updateLabels(boolean shortNames)
	{
		this.shortNames = shortNames;
		for (SupportBehaviorBotDetailPanel detailPanel : detailPanelMap.values())
		{
			detailPanel.setShortLabels(shortNames);
		}
	}


	public void updateData(Map<BotID, ESupportBehavior> supportBehaviorAssignment,
			Map<BotID, EnumMap<ESupportBehavior, SupportBehaviorPosition>> supportBehaviorViabilities,
			Map<ESupportBehavior, Boolean> activeSupportBehaviors)
	{
		this.supportBehaviorAssignment = supportBehaviorAssignment;
		this.supportBehaviorViabilities = supportBehaviorViabilities;
		this.activeSupportBehaviors = activeSupportBehaviors;
		update();
	}


	public void updateShowInactive(boolean showInactive)
	{
		this.showInactive = showInactive;
		update();
	}
}
