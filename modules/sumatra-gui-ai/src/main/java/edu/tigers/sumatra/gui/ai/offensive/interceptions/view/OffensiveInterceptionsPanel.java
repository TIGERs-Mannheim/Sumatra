package edu.tigers.sumatra.gui.ai.offensive.interceptions.view;

import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.CardLayout;
import java.io.Serial;


/**
 * Main Panel for OffensiveStrategy from tactical Field.
 */
public class OffensiveInterceptionsPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -314343167523031597L;

	private static final String MIG_LAYOUT_DEFAULT = "dock center, span 4";

	@Getter
	private final JCheckBox enableAutomaticSelectionOfInterceptingBotId = new JCheckBox(
			"Enable automatic selection of intercepting BotID");
	private static final String YELLOW = "YELLOW";
	private static final String BLUE = "BLUE";

	@Getter
	private final TeamOffensiveInterceptionsPanel yellowPanel = new TeamOffensiveInterceptionsPanel();
	@Getter
	private final TeamOffensiveInterceptionsPanel bluePanel = new TeamOffensiveInterceptionsPanel();

	private final CardLayout cardLayout = new CardLayout();
	private final JPanel cardPanel = new JPanel(cardLayout);


	public OffensiveInterceptionsPanel()
	{
		setLayout(new MigLayout());

		setBorder(BorderFactory.createTitledBorder("Bot Interceptions"));

		ButtonGroup group = new ButtonGroup();
		JRadioButton yellowRadioButton = new JRadioButton("Yellow Team");
		group.add(yellowRadioButton);
		JRadioButton blueRadioButton = new JRadioButton("Blue Team");
		group.add(blueRadioButton);

		yellowRadioButton.setSelected(true);

		yellowRadioButton.addActionListener(e -> cardLayout.show(cardPanel, YELLOW));
		blueRadioButton.addActionListener(e -> cardLayout.show(cardPanel, BLUE));

		cardPanel.add(yellowPanel, YELLOW);
		cardPanel.add(bluePanel, BLUE);

		setBorder(BorderFactory.createTitledBorder("Bot Interceptions"));
		add(enableAutomaticSelectionOfInterceptingBotId, "wrap");
		add(yellowRadioButton, "wrap");
		add(blueRadioButton, "wrap");
		add(cardPanel, MIG_LAYOUT_DEFAULT);
	}
}