/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.offensive.view;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveAnalysedBotFrame;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveBotFrame;
import edu.tigers.sumatra.ids.BotID;
import net.miginfocom.swing.MigLayout;


/**
 * Strategy panel for one team.
 */
public class OffensiveStatisticsBotPanel extends JPanel
{
	/**  */
	private static final long																serialVersionUID	= 5289153581163080231L;
	private Map<EOffensiveActionMove, Map<EActionViability, JProgressBar>>	viaProgressBars	= new EnumMap<>(
			EOffensiveActionMove.class);
	private Map<EOffensiveActionMove, JProgressBar>									scorePorgress		= new EnumMap<>(
			EOffensiveActionMove.class);
	
	private JProgressBar																		primaryBar			= new JProgressBar(0, 100);
	
	
	/**
	 * Team Panel
	 * 
	 * @param id
	 */
	public OffensiveStatisticsBotPanel(final BotID id)
	{
		setLayout(new MigLayout());
		setBorder(BorderFactory.createTitledBorder(id.toString()));
		for (EOffensiveActionMove moves : EOffensiveActionMove.values())
		{
			viaProgressBars.put(moves, new EnumMap<>(EActionViability.class));
		}
		JPanel vias = createViabilityPanel();
		add(vias, "wrap");
	}
	
	
	private JPanel createViabilityPanel()
	{
		JPanel panel = new JPanel(new MigLayout());
		panel.add(primaryBar, "wrap");
		primaryBar.setBorderPainted(true);
		primaryBar.setStringPainted(true);
		primaryBar.setForeground(Color.magenta);
		primaryBar.setToolTipText("primary");
		for (EOffensiveActionMove key : EOffensiveActionMove.values())
		{
			panel.add(new JLabel(key.toString()), "wrap");
			for (EActionViability via : EActionViability.values())
			{
				JProgressBar bar = new JProgressBar(0, 100);
				bar.setName(via.toString());
				viaProgressBars.get(key).put(via, bar);
				bar.setBorderPainted(true);
				bar.setStringPainted(true);
				bar.setToolTipText(via.name());
				panel.add(bar, "wrap");
				switch (via)
				{
					case FALSE:
						bar.setForeground(Color.RED);
						break;
					case PARTIALLY:
						bar.setForeground(Color.BLUE);
						break;
					case TRUE:
						bar.setForeground(Color.green);
						break;
				}
			}
			JProgressBar score = new JProgressBar(0, 100);
			score.setBorderPainted(true);
			score.setStringPainted(true);
			score.setForeground(Color.DARK_GRAY);
			score.setToolTipText("Score");
			scorePorgress.put(key, score);
			panel.add(score, "wrap");
		}
		return panel;
	}
	
	
	/**
	 * @param val
	 */
	public void updatePercentage(double val)
	{
		primaryBar.setValue((int) (val * 100));
	}
	
	
	/**
	 * @param frame
	 */
	public void updateBotFrame(final OffensiveAnalysedBotFrame frame)
	{
		for (EOffensiveActionMove move : frame.getMoveViabilitiesAvg().keySet())
		{
			Map<EActionViability, Double> vias = frame.getMoveViabilitiesAvg().get(move);
			for (Map.Entry<EActionViability, Double> via : vias.entrySet())
			{
				int val = (int) (via.getValue() * 100);
				viaProgressBars.get(move).get(via.getKey()).setValue(val);
			}
			int val = (int) ((frame.getMoveViabilitiyScoreAvg().get(move)) * 100);
			scorePorgress.get(move).setValue(val);
		}
	}

	/**
	 * @param frame
	 */
	public void updateBotFrame(final OffensiveBotFrame frame)
	{
		for (EOffensiveActionMove move : frame.getMoveViabilityScores().keySet())
		{
			EActionViability via = frame.getMoveViabilities().get(move);
			for (EActionViability key : EActionViability.values())
			{
				if (key.toString().equals(via.toString()))
				{
					viaProgressBars.get(move).get(key).setValue(100);
				} else {
					viaProgressBars.get(move).get(key).setValue(0);
				}
			}
			scorePorgress.get(move).setValue((int)(frame.getMoveViabilityScores().get(move)*100));
		}
	}
}
