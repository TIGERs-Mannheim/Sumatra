/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.support.view;

import edu.tigers.sumatra.ai.metis.support.behaviors.ESupportBehavior;
import edu.tigers.sumatra.ai.metis.support.behaviors.SupportBehaviorPosition;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.util.MigLayoutResizeListener;
import lombok.extern.log4j.Log4j2;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.io.Serial;
import java.util.EnumMap;
import java.util.Map;


/**
 * Main Panel for SupportBehaviors Visualisation
 */
@Log4j2
public class SupportBehaviorsPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = 1905122041950251207L;
	private boolean showInactive = false;
	private boolean shortNames = false;

	private Map<ETeamColor, SupportBehaviorsTeamPanel> teamPanelMap = new EnumMap<>(ETeamColor.class);


	public SupportBehaviorsPanel()
	{
		setLayout(new MigLayout(new LC().wrapAfter(6), new AC().gap("10"), new AC()));
		new MigLayoutResizeListener(this, this, 6);

		teamPanelMap.put(ETeamColor.YELLOW, new SupportBehaviorsTeamPanel());
		teamPanelMap.put(ETeamColor.BLUE, new SupportBehaviorsTeamPanel());

		final JCheckBox showInactiveCheckBox = new JCheckBox("Show inactive", false);
		showInactiveCheckBox.addActionListener(actionEvent -> {
			showInactive = !showInactive;
			teamPanelMap.values().forEach(panel -> panel.updateShowInactive(showInactive));
		});

		final JCheckBox shortNamesCheckBox = new JCheckBox("Compact", false);
		shortNamesCheckBox.addActionListener(actionEvent -> {
			shortNames = !shortNames;
			teamPanelMap.values().forEach(panel -> panel.updateLabels(shortNames));
		});

		add(shortNamesCheckBox);
		add(showInactiveCheckBox, "wrap");
		JTabbedPane teamPanel = new JTabbedPane();

		teamPanelMap.forEach((key, value) -> teamPanel.addTab(key.name(), value));

		add(teamPanel, "spanx, grow, pushy, pushx");
	}


	public void updateData(
			ETeamColor color, Map<BotID, ESupportBehavior> supportBehaviorAssignment,
			Map<BotID, EnumMap<ESupportBehavior, SupportBehaviorPosition>> supportBehaviorViabilities,
			Map<ESupportBehavior, Boolean> activeSupportBehaviors
	)
	{
		teamPanelMap.get(color).updateData(supportBehaviorAssignment, supportBehaviorViabilities, activeSupportBehaviors);
	}
}
