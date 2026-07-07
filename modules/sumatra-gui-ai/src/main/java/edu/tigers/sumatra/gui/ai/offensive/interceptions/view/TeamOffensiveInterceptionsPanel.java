package edu.tigers.sumatra.gui.ai.offensive.interceptions.view;

import edu.tigers.sumatra.ai.metis.ballinterception.BallInterceptionInformation;
import edu.tigers.sumatra.ids.BotID;
import lombok.Setter;

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

	@Setter
	private boolean enableAutomaticSelectionOfInterceptingBotId = true;


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
		if (enableAutomaticSelectionOfInterceptingBotId)
		{
			ballInterceptionsInformation.entrySet().stream()
					.filter(e -> bots.contains(e.getKey()))
					.filter(e -> e.getValue().getInitialIterations().stream()
							.anyMatch(it -> it.getIncludedSlackTimeBonus() > 0))
					.findFirst()
					.ifPresent(e -> botBox.setSelectedItem(e.getKey()));
		}
		canvas.setInterceptionInformation(ballInterceptionsInformation);
		canvas.repaint();
	}


	public void fillComboBox(Collection<BotID> bots)
	{
		if (bots.stream().anyMatch(e -> !this.bots.contains(e)))
		{
			botBox.removeAllItems();
			bots.stream().sorted().forEach(e -> botBox.addItem(e));
			this.bots = new ArrayList<>(bots);
			if (!bots.isEmpty())
			{
				canvas.setSelectedBot(this.bots.getFirst());
				canvas.repaint();
			}
		}
	}
}

