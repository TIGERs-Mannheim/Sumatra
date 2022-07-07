/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.kick.view;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.Serial;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Panel for ball kick identification
 */
public class BallKickIdentPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -1008885913072780502L;

	private final JTabbedPane tabs = new JTabbedPane();
	private final JCheckBox enableIdentification = new JCheckBox("Enable Identification Mode");

	private final transient List<IMainPanelObserver> observers = new CopyOnWriteArrayList<>();


	public BallKickIdentPanel()
	{
		setLayout(new BorderLayout());

		JLabel howTo = new JLabel();
		howTo.setText("<html>"
				+ "1. Enable identification mode on the left<br>"
				+ "2. Use StraightChipKickSamplerRole or KickSampleSkill to generate data<br>"
				+ "3. Carefully select the best data samples<br>"
				+ "4. Copy the identified parameters to the respective models<br>"
				+ "Note: Determine ball rolling acceleration before chip model sampling"
				+ "</html>");
		Font bigFont = new Font("Arial", 0, 14);
		howTo.setFont(bigFont);
		TitledBorder border = BorderFactory.createTitledBorder("How-To");
		border.setTitleFont(bigFont);
		howTo.setBorder(border);
		howTo.setPreferredSize(new Dimension(480, 120));

		enableIdentification
				.addItemListener(l -> observers.forEach(o -> o.onEnableIdentification(enableIdentification.isSelected())));
		enableIdentification.setFont(new Font("Arial", Font.BOLD, 14));

		JPanel top = new JPanel(new BorderLayout());
		top.add(enableIdentification, BorderLayout.WEST);
		top.add(howTo, BorderLayout.EAST);

		add(top, BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
	}


	/**
	 * @param observer
	 */
	public void addObserver(final IMainPanelObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param observer
	 */
	public void removeObserver(final IMainPanelObserver observer)
	{
		observers.remove(observer);
	}


	/**
	 * @return
	 */
	public boolean isModelIdentificationEnabled()
	{
		return enableIdentification.isSelected();
	}


	public JTabbedPane getTabs()
	{
		return tabs;
	}


	public interface IMainPanelObserver
	{
		void onEnableIdentification(boolean enable);
	}
}
