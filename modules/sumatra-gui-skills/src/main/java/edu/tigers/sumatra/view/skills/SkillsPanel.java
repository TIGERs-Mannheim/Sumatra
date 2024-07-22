/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.skills;

import com.github.g3force.instanceables.InstanceablePanel;
import edu.tigers.sumatra.botmanager.botskills.EBotSkill;
import edu.tigers.sumatra.components.BetterScrollPane;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ESkill;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;


public class SkillsPanel extends JPanel
{
	private final JComboBox<BotID> cmbBots = new JComboBox<>();
	private final InstanceablePanel skillPanel;
	private final InstanceablePanel botSkillPanel;
	private final MotorEnhancedInputPanel enhancedInputPanel = new MotorEnhancedInputPanel();
	private final JButton resetButton = new JButton("Reset");


	public SkillsPanel()
	{
		setLayout(new BorderLayout());

		JPanel componentPanel = new JPanel();
		componentPanel.setLayout(new MigLayout());

		JPanel selBotPanel = new JPanel();
		selBotPanel.add(new JLabel("Choose Bot: "));
		cmbBots.setPreferredSize(new Dimension(150, 25));
		selBotPanel.add(cmbBots);

		var enums = ESkill.values();
		Arrays.sort(enums, (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.name(), o2.name()));
		skillPanel = new InstanceablePanel(enums, SumatraModel.getInstance().getUserSettings());
		skillPanel.setShowCreate(true);

		var enums2 = EBotSkill.values();
		Arrays.sort(enums2, (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.name(), o2.name()));
		botSkillPanel = new InstanceablePanel(enums2, SumatraModel.getInstance().getUserSettings());
		botSkillPanel.setShowCreate(true);

		JPanel createSkillsPanel = new JPanel();
		createSkillsPanel.add(skillPanel);
		createSkillsPanel.add(botSkillPanel);

		JPanel resetPanel = new JPanel();
		resetPanel.setBorder(BorderFactory.createTitledBorder("Reset"));
		resetPanel.add(resetButton);

		componentPanel.add(selBotPanel, "wrap");
		componentPanel.add(enhancedInputPanel);
		componentPanel.add(createSkillsPanel);
		componentPanel.add(resetPanel);

		BetterScrollPane scrollPane = new BetterScrollPane(componentPanel);
		add(scrollPane, BorderLayout.CENTER);
	}


	public JComboBox<BotID> getCmbBots()
	{
		return cmbBots;
	}


	public InstanceablePanel getSkillPanel()
	{
		return skillPanel;
	}


	public InstanceablePanel getBotSkillPanel()
	{
		return botSkillPanel;
	}


	public MotorEnhancedInputPanel getEnhancedInputPanel()
	{
		return enhancedInputPanel;
	}


	public JButton getResetButton()
	{
		return resetButton;
	}
}
