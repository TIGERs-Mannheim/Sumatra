/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.support.view;

import edu.tigers.sumatra.ai.pandora.roles.support.ESupportBehavior;
import edu.tigers.sumatra.components.BetterScrollPane;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.util.MigLayoutResizeListener;
import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Main Panel for SupportBehaviors Visualisation
 *
 */
public class SupportBehaviorsPanel extends JPanel implements ISumatraView
{
	protected static final Logger log = LogManager.getLogger(SupportBehaviorsPanel.class.getName());
	private static final long serialVersionUID = 1905122041950251207L;
	private static final String TEAM_YELLOW = "YELLOW";
	private static final String TEAM_BLUE = "BLUE";
	private boolean showInactive = false;
	private boolean shortNames = false;
	private SupportBehaviorsTeamPanel teamBlue = new SupportBehaviorsTeamPanel();
	private SupportBehaviorsTeamPanel teamYellow = new SupportBehaviorsTeamPanel();


	public SupportBehaviorsPanel()
	{
		super();
		setLayout(new MigLayout(new LC().wrapAfter(6), new AC().gap("10"), new AC()));
		new MigLayoutResizeListener(this, this, 6);


		final JCheckBox showInactiveCheckBox = new JCheckBox("Show inactive", false);
		showInactiveCheckBox.addActionListener(actionEvent -> {
			showInactive = !showInactive;
			this.update();
		});

		final JCheckBox shortNamesCheckBox = new JCheckBox("Compact", false);
		shortNamesCheckBox.addActionListener(actionEvent -> {
			shortNames = !shortNames;
			teamYellow.updateLabels();
			teamBlue.updateLabels();
		});

		add(shortNamesCheckBox);
		add(showInactiveCheckBox, "wrap");
		JTabbedPane teamPanel = new JTabbedPane();
		teamPanel.addTab(TEAM_BLUE, teamBlue);
		teamPanel.addTab(TEAM_YELLOW, teamYellow);


		add(teamPanel, "spanx, grow, pushy, pushx");
	}


	private void update()
	{
		teamBlue.update();
		teamYellow.update();
	}


	public synchronized void setViabilityMap(ETeamColor color,
			Map<BotID, EnumMap<ESupportBehavior, Double>> viabilityMap, List<ESupportBehavior> inactiveBehaviors)
	{
		if (viabilityMap == null)
		{
			return;
		}

		if (color == ETeamColor.BLUE)
		{
			teamBlue.setViabilityMap(viabilityMap);
			teamBlue.setInactiveBehaviors(inactiveBehaviors);
		} else
		{
			teamYellow.setViabilityMap(viabilityMap);
			teamYellow.setInactiveBehaviors(inactiveBehaviors);
		}
	}


	/**
	 * This class contains the actual visualization of the
	 * two supporter teams. There will be an instance per team.
	 */
	class SupportBehaviorsTeamPanel extends JPanel
	{
		private transient Map<BotID, EnumMap<ESupportBehavior, Double>> viabilityMap = null;
		private transient Map<BotID, SupportBehaviorBotDetailPanel> detailPanelMap = new HashMap<>();
		private transient List<ESupportBehavior> inactiveBehaviors = new ArrayList<>();
		private JPanel details = new JPanel();
		final BetterScrollPane scrollPane;


		public SupportBehaviorsTeamPanel()
		{
			super();

			setLayout(new BorderLayout());
			scrollPane = new BetterScrollPane(details);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			add(scrollPane, BorderLayout.CENTER);

			details.setLayout(new MigLayout(new LC().wrapAfter(2), new AC().gap("10"), new AC()));
			new MigLayoutResizeListener(this, details, -1);
		}


		public synchronized void update()
		{
			if (viabilityMap == null)
			{
				return;
			}
			updateBots();
			updateValues();
		}


		private void updateBots()
		{
			for (BotID botID : viabilityMap.keySet())
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


		private synchronized void updateValues()
		{
			for (Map.Entry<BotID, SupportBehaviorBotDetailPanel> panelEntry : detailPanelMap.entrySet())
			{
				if (!showInactive)
				{
					panelEntry.getValue().setHiddenBehaviors(inactiveBehaviors);
				} else
				{
					panelEntry.getValue().setHiddenBehaviors(Collections.emptyList());
				}
				panelEntry.getValue().setValues(viabilityMap.getOrDefault(panelEntry.getKey(), null));
			}
		}


		public synchronized void setViabilityMap(Map<BotID, EnumMap<ESupportBehavior, Double>> viabilityMap)
		{
			this.viabilityMap = viabilityMap;
			update();
		}


		public synchronized void setInactiveBehaviors(List<ESupportBehavior> inactiveBehaviors)
		{
			this.inactiveBehaviors = inactiveBehaviors;
			update();
		}


		public void updateLabels()
		{
			for (SupportBehaviorBotDetailPanel detailPanel : detailPanelMap.values())
			{
				detailPanel.setShortLabels(shortNames);
			}
		}
	}
}
