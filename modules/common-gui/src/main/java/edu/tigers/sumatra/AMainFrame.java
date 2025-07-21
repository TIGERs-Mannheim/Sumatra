/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra;

import edu.tigers.sumatra.views.ASumatraView;
import lombok.Getter;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Common main frame for all sumatra windows.
 */
@Getter
public abstract class AMainFrame extends JFrame
{
	private final JMenu menuViews = new JMenu("Views");
	private final JMenu menuLayout = new JMenu("Layout");
	private final JMenuItem menuItemLayoutSave = new JMenuItem("Save layout");

	private final List<JMenuItem> layoutItems = new ArrayList<>();


	protected AMainFrame()
	{
		setLayout(new BorderLayout());
		setSize(new Dimension(800, 600));
		setIconImage("/kralle-icon.png");

		menuItemLayoutSave.setToolTipText("Saves current layout to file");

		setJMenuBar(new JMenuBar());
		menuViews.setMnemonic(KeyEvent.VK_V);
		menuLayout.setMnemonic(KeyEvent.VK_L);
		menuLayout.add(menuItemLayoutSave);
	}


	/**
	 * Add menu items of abstract frame.
	 * Allows the subclass to add its own menu items before and after the default ones.
	 */
	protected void addMenuItems()
	{
		getJMenuBar().add(menuLayout);
		getJMenuBar().add(menuViews);
	}


	protected void setIconImage(String url)
	{
		URL iconUrl = Objects.requireNonNull(AMainFrame.class.getResource(url));
		Image image = new ImageIcon(iconUrl).getImage();
		setIconImage(image);
		if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE))
		{
			Taskbar.getTaskbar().setIconImage(image);
		}
	}


	public Map<String, JMenuItem> setMenuLayoutItems(final List<String> names)
	{
		// remove all layout items from menu
		for (final JMenuItem item : layoutItems)
		{
			menuLayout.remove(item);
		}
		layoutItems.clear();

		var menuItems = names.stream()
				.collect(Collectors.toMap(Function.identity(), JMenuItem::new));
		layoutItems.addAll(menuItems.values());

		ButtonGroup group = new ButtonGroup();
		menuItems.values().forEach(group::add);

		int pos = 2;
		for (var item : layoutItems)
		{
			menuLayout.insert(item, pos++);
		}

		return menuItems;
	}


	public Map<ASumatraView, JMenuItem> setViewItems(Collection<ASumatraView> views)
	{
		var menuItems = views.stream()
				.collect(Collectors.toMap(Function.identity(), view -> new JMenuItem(view.getType().getTitle())));

		menuViews.removeAll();
		menuItems.values()
				.stream().sorted(Comparator.comparing(JMenuItem::getText))
				.forEach(menuViews::add);

		return menuItems;
	}
}
