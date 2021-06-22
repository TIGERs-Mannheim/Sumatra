/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.offensive.view;

import edu.tigers.sumatra.ai.metis.offense.ballinterception.BallInterceptionInformation;
import edu.tigers.sumatra.ids.BotID;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class TeamOffensiveInterceptionsPanel extends JPanel
{
	private JComboBox<BotID> botBox = new JComboBox<>();

	private InterceptionsCanvas canvas = new InterceptionsCanvas();

	private transient List<BotID> bots = new ArrayList<>();


	TeamOffensiveInterceptionsPanel()
	{
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder("Team Interceptions"));
		add(canvas, BorderLayout.CENTER);
		add(botBox, BorderLayout.NORTH);
		botBox.addItemListener(itemEvent -> canvas.setSelectedBot((BotID) itemEvent.getItem()));
	}


	public void setBallInterceptionsInformation(Map<BotID, BallInterceptionInformation> ballInterceptionsInformation)
	{
		canvas.setInterceptionInformation(ballInterceptionsInformation);
		canvas.repaint();
	}


	public void fillComboBox(Collection<BotID> bots)
	{
		if (bots.stream().anyMatch(e -> !this.bots.contains(e)))
		{
			botBox.removeAllItems();
			bots.forEach(e -> botBox.addItem(e));
			this.bots = new ArrayList<>(bots);
			if (!bots.isEmpty())
			{
				canvas.setSelectedBot(this.bots.get(0));
				canvas.repaint();
			}
		}
	}
}

