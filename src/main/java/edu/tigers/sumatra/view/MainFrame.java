package edu.tigers.sumatra.view;

import edu.tigers.sumatra.AMainFrame;
import edu.tigers.sumatra.gui.replay.view.ReplayLoadMenu;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.view.toolbar.ToolBar;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import lombok.Getter;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * The Sumatra-main view with all available JComponents as dockable views :).
 */
@Getter
public class MainFrame extends AMainFrame
{
	private final JMenu menuSumatra = new JMenu("Sumatra");
	private final JMenu menuModuli = new JMenu("Moduli");
	private final JMenu menuLookAndFeel = new JMenu("Look & Feel");
	private final ReplayLoadMenu menuReplay = new ReplayLoadMenu();

	private final JMenuItem menuItemSumatraShortcut = new JMenuItem("Shortcuts");
	private final JMenuItem menuItemSumatraAbout = new JMenuItem("About");

	private final transient ToolBar toolbar = new ToolBar();

	static
	{
		IconFontSwing.register(FontAwesome.getIconFont());
	}

	public MainFrame()
	{
		setTitle("TIGERs Mannheim - Sumatra " + SumatraModel.getVersion());
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		menuSumatra.setMnemonic(KeyEvent.VK_S);
		menuSumatra.add(menuItemSumatraShortcut);
		menuSumatra.add(menuItemSumatraAbout);
		menuModuli.setMnemonic(KeyEvent.VK_M);
		menuLookAndFeel.setMnemonic(KeyEvent.VK_F);
		menuReplay.setMnemonic(KeyEvent.VK_R);

		add(toolbar.getJToolBar(), BorderLayout.NORTH);

		getJMenuBar().add(menuSumatra);
		getJMenuBar().add(menuModuli);
		super.addMenuItems();
		getJMenuBar().add(menuLookAndFeel);
		getJMenuBar().add(menuReplay);
	}


	public Map<String, JRadioButtonMenuItem> setModuliItems(final Collection<String> names)
	{
		ButtonGroup group = new ButtonGroup();
		menuModuli.removeAll();
		Map<String, JRadioButtonMenuItem> menuItems = new HashMap<>();
		names.forEach(name -> {
			var menuItem = new JRadioButtonMenuItem(name);
			group.add(menuItem);
			menuModuli.add(menuItem);
			menuItems.put(name, menuItem);
		});
		return menuItems;
	}


	public void setModuliItemSelected(final String name)
	{
		// select RadioButton in moduliMenu
		for (int i = 0; i < menuModuli.getItemCount(); i++)
		{
			final JMenuItem item = menuModuli.getItem(i);
			if (item.getText().equals(name))
			{
				item.setSelected(true);
			}
		}
	}


	public Map<LookAndFeelInfo, JRadioButtonMenuItem> setLookAndFeelItems(final Collection<LookAndFeelInfo> lafInfos)
	{
		menuLookAndFeel.removeAll();

		Map<LookAndFeelInfo, JRadioButtonMenuItem> menuItems = new HashMap<>();

		ButtonGroup group = new ButtonGroup();
		for (LookAndFeelInfo info : lafInfos)
		{
			var menuItem = new JRadioButtonMenuItem(info.getName());
			group.add(menuItem);
			menuLookAndFeel.add(menuItem);
			menuItems.put(info, menuItem);
		}

		return Collections.unmodifiableMap(menuItems);
	}


	public void setLookAndFeelItemSelected(final String name)
	{
		for (int i = 0; i < menuLookAndFeel.getItemCount(); i++)
		{
			JMenuItem item = menuLookAndFeel.getItem(i);
			if (item.getText().equals(name))
			{
				item.setSelected(true);
			}
		}
	}
}
