package edu.tigers.sumatra.view;

import edu.tigers.sumatra.AMainFrame;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import lombok.Getter;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;
import java.awt.event.KeyEvent;


@Getter
public class ReplayMainFrame extends AMainFrame
{
	private final JMenuItem shortcutMenuItem = new JMenuItem("Shortcuts");

	static
	{
		IconFontSwing.register(FontAwesome.getIconFont());
	}

	public ReplayMainFrame()
	{
		setTitle("Replay");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JMenu replayMenu = new JMenu("Replay");
		replayMenu.setMnemonic(KeyEvent.VK_R);
		replayMenu.add(shortcutMenuItem);

		getJMenuBar().add(replayMenu);
		addMenuItems();
	}
}
