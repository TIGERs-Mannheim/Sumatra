package edu.tigers.sumatra.gui.kick.view;

import lombok.Getter;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;


/**
 * Panel for ball kick identification
 */
@Getter
public class BallKickIdentPanel extends JPanel
{
	private final JTabbedPane tabs = new JTabbedPane();
	private final JCheckBox chkCollectKickIdents = new JCheckBox("Collect kick ident samples");

	private static final String TUTORIAL_TEXT = ("<html>"
			+ "1. Enable collection of kick ident samples on the left<br>"
			+ "2. Use StraightChipKickSamplerRole or KickSampleSkill to generate data<br>"
			+ "3. Carefully select the best data samples<br>"
			+ "4. Copy the identified parameters to the respective models<br>"
			+ "Note: Determine ball rolling acceleration before chip model sampling"
			+ "</html>");

	public BallKickIdentPanel()
	{
		setLayout(new BorderLayout());

		JButton tutorialButton = getTutorialButton();

		chkCollectKickIdents.setFont(new Font("Arial", Font.BOLD, 14));

		JPanel top = new JPanel(new BorderLayout());
		top.add(chkCollectKickIdents, BorderLayout.WEST);
		top.add(tutorialButton, BorderLayout.EAST);

		add(top, BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
	}


	private JButton getTutorialButton()
	{
		JButton tutorialButton = new JButton("Tutorial");

		tutorialButton.addActionListener(e -> {

			JDialog tutorDialog = new JDialog(
					SwingUtilities.getWindowAncestor(this),
					"How to Use This Panel",
					Dialog.ModalityType.APPLICATION_MODAL
			);

			JEditorPane htmlPane = new JEditorPane();
			htmlPane.setEditable(false);
			htmlPane.setContentType("text/html");
			htmlPane.setText(TUTORIAL_TEXT);
			Font bigFont = new Font("Arial", Font.PLAIN, 14);
			htmlPane.setFont(bigFont);

			JScrollPane scroll = new JScrollPane(htmlPane);
			scroll.setPreferredSize(new Dimension(460, 120));

			tutorDialog.getContentPane().setLayout(new BorderLayout());
			tutorDialog.getContentPane().add(scroll, BorderLayout.CENTER);

			JButton close = new JButton("Close");
			close.addActionListener(ev -> tutorDialog.dispose());
			JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			footer.add(close);
			tutorDialog.getContentPane().add(footer, BorderLayout.SOUTH);

			tutorDialog.pack();
			tutorDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
			tutorDialog.setVisible(true);
		});
		return tutorialButton;
	}


	public JTabbedPane getTabs()
	{
		return tabs;
	}
}
