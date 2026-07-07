package edu.tigers.sumatra.gui.ai.offensive.statistics.view;

import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.CardLayout;
import java.io.Serial;


/**
 * Main Panel for OffensiveStrategy from tactical Field.
 */
public class OffensiveStatisticsPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -314343167523031597L;

	private static final String YELLOW = "YELLOW";
	private static final String BLUE = "BLUE";

	private final JRadioButton yellowRadioButton = new JRadioButton("Yellow Team");
	private final JRadioButton blueRadioButton = new JRadioButton("Blue Team");
	private final JComboBox<String> passTypeFilter = new JComboBox<>(
			new String[] { "All passes", "Redirect", "Receive  / Dont care" });

	@Getter
	private final TeamOffensiveStatisticsPanel yellowPanel = new TeamOffensiveStatisticsPanel();
	@Getter
	private final TeamOffensiveStatisticsPanel bluePanel = new TeamOffensiveStatisticsPanel();

	private final CardLayout cardLayout = new CardLayout();
	private final JPanel cardPanel = new JPanel(cardLayout);

	public OffensiveStatisticsPanel()
	{
		setLayout(new MigLayout());

		ButtonGroup group = new ButtonGroup();
		group.add(yellowRadioButton);
		group.add(blueRadioButton);

		yellowRadioButton.setSelected(true);

		yellowRadioButton.addActionListener(e -> cardLayout.show(cardPanel, YELLOW));
		blueRadioButton.addActionListener(e -> cardLayout.show(cardPanel, BLUE));

		passTypeFilter.addActionListener(e -> {
			int selection = passTypeFilter.getSelectedIndex();
			bluePanel.setFilterRegular(false);
			bluePanel.setFilterRedirects(false);
			yellowPanel.setFilterRegular(false);
			yellowPanel.setFilterRedirects(false);
			switch (selection)
			{
				case 1:
					bluePanel.setFilterRedirects(true);
					yellowPanel.setFilterRedirects(true);
					break;
				case 2:
					bluePanel.setFilterRegular(true);
					yellowPanel.setFilterRegular(true);
					break;
				default:
			}

			revalidate();
			repaint();
		});

		cardPanel.add(yellowPanel, YELLOW);
		cardPanel.add(bluePanel, BLUE);

		add(yellowRadioButton);
		add(blueRadioButton);
		add(passTypeFilter, "wrap");
		add(cardPanel, "span");

		cardLayout.show(cardPanel, YELLOW);
	}
}