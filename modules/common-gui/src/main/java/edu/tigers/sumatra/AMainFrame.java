/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.views.ASumatraView;
import edu.tigers.sumatra.views.DummyView;
import edu.tigers.sumatra.views.ESumatraViewType;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.ViewSerializer;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.MixedViewHandler;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;


/**
 * abstract base MainFrame
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AMainFrame extends JFrame implements IMainFrame
{
	private static final Logger log = Logger
			.getLogger(AMainFrame.class.getName());
	private static final long serialVersionUID = -6858464942004450029L;
	
	private final RootWindow rootWindow;
	
	protected final transient List<IMainFrameObserver> observers = new CopyOnWriteArrayList<>();
	private final List<JRadioButtonMenuItem> layoutItems = new ArrayList<>();
	
	private final JMenuBar jMenuBar = new JMenuBar();
	private final JMenu viewsMenu = new JMenu("Views");
	private final JMenu layoutMenu = new JMenu("Layout");
	private final transient List<ASumatraView> views = new ArrayList<>();
	
	private final transient Map<ASumatraView, List<JMenu>> customMenuMap = new HashMap<>();
	
	
	protected AMainFrame()
	{
		setLayout(new BorderLayout());
		setSize(new Dimension(800, 600));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		
		ImageIcon icon = getFrameIcon();
		if (icon != null)
		{
			setIconImage(icon.getImage());
		}
		
		addWindowListener(new Exit());
		rootWindow = createRootWindow();
		this.add(rootWindow, BorderLayout.CENTER);
		
		fillLayoutMenu();
		setJMenuBar(jMenuBar);
		viewsMenu.setMnemonic(KeyEvent.VK_V);
		layoutMenu.setMnemonic(KeyEvent.VK_L);
	}
	
	
	/**
	 * Add menu items of abstract frame
	 */
	protected void addMenuItems()
	{
		getJMenuBar().add(layoutMenu);
		getJMenuBar().add(viewsMenu);
	}
	
	
	protected ImageIcon getFrameIcon()
	{
		return loadIconImage("/kralle-icon.png");
	}
	
	
	protected ImageIcon loadIconImage(final String url)
	{
		URL iconUrl = AMainFrame.class.getResource(url);
		if (iconUrl != null)
		{
			return new ImageIcon(iconUrl);
		}
		return null;
	}
	
	
	/**
	 * Must be called after adding views
	 */
	protected void updateViewMenu()
	{
		for (ASumatraView view : views)
		{
			if (!view.isInitialized())
			{
				continue;
			}
			final List<JMenu> menu = view.getSumatraView().getCustomMenus();
			addToCustomMenu(menu, view);
			view.getSumatraView().onShown();
		}
		
		for (int i = 0; i < viewsMenu.getItemCount(); i++)
		{
			final ActionListener[] listener = viewsMenu.getItem(i).getActionListeners();
			for (final ActionListener l : listener)
			{
				viewsMenu.getItem(i).removeActionListener(l);
			}
		}
		
		viewsMenu.removeAll();
		
		for (ASumatraView sumatraView : views)
		{
			final JMenuItem item = new JMenuItem(sumatraView.getType().getTitle());
			
			item.addActionListener(new RestoreView(sumatraView));
			
			if (sumatraView.isInitialized())
			{
				View view = sumatraView.getView();
				item.setEnabled(view.getRootWindow() == null);
			}
			viewsMenu.add(item);
		}
	}
	
	
	/**
	 * Add a sumatra view
	 */
	protected void addView(final ASumatraView view)
	{
		views.add(view);
	}
	
	
	protected void exit()
	{
		for (final IMainFrameObserver o : observers)
		{
			o.onExit();
		}
	}
	
	
	/**
	 * @return the views
	 */
	public final List<ASumatraView> getViews()
	{
		return views;
	}
	
	
	/**
	 * Activate this window by setting it visible
	 */
	public void activate()
	{
		setVisible(true);
		requestFocus();
		
		// initialize all views that are currently visible
		for (ASumatraView view : getViews())
		{
			if (view.getView().isShowing() || view.getType().isForceLoad())
			{
				view.ensureInitialized();
			}
		}
	}
	
	
	@Override
	public void addObserver(final IMainFrameObserver o)
	{
		observers.add(o);
	}
	
	
	@Override
	public void removeObserver(final IMainFrameObserver o)
	{
		observers.remove(o);
	}
	
	
	@Override
	public void loadLayout(final String path)
	{
		final File f = new File(path);
		final String filename = f.getName();
		log.trace("Loading layout file " + filename);
		
		try (FileInputStream fileInputStream = new FileInputStream(f))
		{
			try (ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream))
			{
				rootWindow.read(objectInputStream, true);
			}
		} catch (IOException err)
		{
			log.error("Can't load layout.", err);
		}
		
		// select RadioButton in layoutMenu
		for (final JRadioButtonMenuItem item : layoutItems)
		{
			final String itemName = item.getText();
			if (itemName.equals(filename))
			{
				item.setSelected(true);
			}
		}
	}
	
	
	@Override
	public void saveLayout(final String filename)
	{
		try (FileOutputStream fileStream = new FileOutputStream(filename))
		{
			try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileStream))
			{
				rootWindow.write(objectOutputStream, false);
			}
		} catch (IOException err)
		{
			log.error("Can't save layout.", err);
		}
	}
	
	
	@Override
	public void setMenuLayoutItems(final List<String> names)
	{
		// remove all layout items from menu
		for (final JRadioButtonMenuItem item : layoutItems)
		{
			layoutMenu.remove(item);
		}
		
		layoutItems.clear();
		
		// --- buttonGroup for layout-files ---
		final ButtonGroup group = new ButtonGroup();
		for (final String name : names)
		{
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(name);
			group.add(item);
			item.addActionListener(new LoadLayout(name));
			layoutItems.add(item);
		}
		
		int pos = 3;
		for (final JRadioButtonMenuItem item : layoutItems)
		{
			layoutMenu.insert(item, pos++);
		}
	}
	
	
	@Override
	public void selectLayoutItem(final String name)
	{
		for (final JRadioButtonMenuItem item : layoutItems)
		{
			if (item.getText().equals(name))
			{
				item.setSelected(true);
			}
		}
	}
	
	
	private void removeFromCustomMenu(final List<JMenu> menus)
	{
		for (final JMenu menu : menus)
		{
			jMenuBar.remove(menu);
		}
		
		jMenuBar.repaint();
	}
	
	
	private void addToCustomMenu(final List<JMenu> menus, final ASumatraView view)
	{
		if (menus != null)
		{
			if (customMenuMap.containsKey(view))
			{
				removeFromCustomMenu(customMenuMap.get(view));
			}
			
			customMenuMap.put(view, menus);
			for (final JMenu menu : menus)
			{
				jMenuBar.add(menu);
			}
		}
	}
	
	
	/**
	 * Creates the root window and the views.
	 */
	protected RootWindow createRootWindow()
	{
		ViewMap viewMap = new ViewMap();
		// The mixed view map makes it easy to mix static and dynamic views inside the same root window
		MixedViewHandler handler = new MixedViewHandler(viewMap, new WindowViewSerializer());
		
		// --- create the RootWindow with MixedHandler ---
		RootWindow newRootWindow = DockingUtil.createRootWindow(viewMap, handler, true);
		
		// --- add a listener which updates the menus when a window is closing or closed.
		newRootWindow.addListener(new ViewUpdater());
		
		/*
		 * In this properties object the modified property values for close buttons etc. are stored. This object is
		 * cleared
		 * when the theme is changed.
		 */
		RootWindowProperties properties = new RootWindowProperties();
		
		// --- set gradient theme. The theme properties object is the super object of our properties object, which
		// means our property value settings will override the theme values ---
		properties.addSuperObject(new ShapedGradientDockingTheme().getRootWindowProperties());
		
		// --- our properties object is the super object of the root window properties object, so all property values of
		// the
		// theme and in our property object will be used by the root window ---
		newRootWindow.getRootWindowProperties().addSuperObject(properties);
		
		// --- enable the bottom window bar ---
		newRootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
		
		return newRootWindow;
	}
	
	
	/**
	 * Creates the frame menu bar.
	 */
	private void fillLayoutMenu()
	{
		JMenuItem saveLayoutItem = new JMenuItem("Save layout");
		saveLayoutItem.addActionListener(new SaveLayout());
		saveLayoutItem.setToolTipText("Saves current layout to file");
		JMenuItem deleteLayoutItem = new JMenuItem("Delete layout");
		deleteLayoutItem.addActionListener(new DeleteLayout());
		deleteLayoutItem.setToolTipText("Deletes current layout");
		
		layoutMenu.add(saveLayoutItem);
		layoutMenu.add(deleteLayoutItem);
	}
	
	private class SaveLayout implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (final IMainFrameObserver o : observers)
			{
				o.onSaveLayout();
			}
		}
	}
	
	private class DeleteLayout implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (final IMainFrameObserver o : observers)
			{
				o.onDeleteLayout();
			}
		}
	}
	
	private class RestoreView implements ActionListener
	{
		private final ASumatraView sumatraView;
		
		
		/**
		 * @param v
		 */
		public RestoreView(final ASumatraView v)
		{
			sumatraView = v;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			final JMenuItem item = (JMenuItem) e.getSource();
			
			sumatraView.ensureInitialized();
			sumatraView.getView().restoreFocus();
			DockingUtil.addWindow(sumatraView.getView(), rootWindow);
			item.setEnabled(false);
		}
	}
	
	private class LoadLayout implements ActionListener
	{
		private final String layoutName;
		
		
		/**
		 * @param name
		 */
		public LoadLayout(final String name)
		{
			layoutName = name;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (final IMainFrameObserver o : observers)
			{
				o.onLoadLayout(layoutName);
			}
		}
	}
	
	
	private class ViewUpdater extends DockingWindowAdapter
	{
		@Override
		public void windowAdded(final DockingWindow addedToWindow, final DockingWindow addedWindow)
		{
			updateViewMenu();
		}
		
		
		@Override
		public void windowRemoved(final DockingWindow removedFromWindow, final DockingWindow removedWindow)
		{
			updateViewMenu();
		}
		
		
		@Override
		public void windowShown(final DockingWindow window)
		{
			/*
			 * It is a completely undocumented feature(?) that the
			 * window title consists of a comma separated list if multiple
			 * windows get shown at once. This usually happens when loading
			 * a layout.
			 */
			for (final ASumatraView view : views)
			{
				if (!view.isInitialized())
				{
					continue;
				}
				final List<JMenu> menu = view.getSumatraView().getCustomMenus();
				addToCustomMenu(menu, view);
				
				view.getSumatraView().onShown();
			}
		}
		
		
		private void hideWindow(String title)
		{
			for (final ASumatraView view : views)
			{
				if (view.getType().getTitle().equals(title))
				{
					final List<JMenu> menu = customMenuMap.remove(view);
					if (menu != null)
					{
						removeFromCustomMenu(menu);
					}
					
					view.getSumatraView().onHidden();
				}
			}
		}
		
		
		@Override
		public void windowHidden(final DockingWindow window)
		{
			final String[] titles = window.getTitle().split(",");
			for (String title : titles)
			{
				hideWindow(title.trim());
			}
		}
		
		
		@Override
		public void viewFocusChanged(final View previous, final View focused)
		{
			if (previous != null)
			{
				for (final ASumatraView view : views)
				{
					if (view.getType().getTitle().equals(previous.getTitle()))
					{
						view.getSumatraView().onFocusLost();
					}
				}
			}
			
			if (focused != null)
			{
				for (final ASumatraView view : views)
				{
					if (view.getType().getTitle().equals(focused.getTitle()))
					{
						view.getSumatraView().onFocused();
					}
				}
			}
		}
	}
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public class Exit extends WindowAdapter implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			// triggered when using menu -> exit
			dispatchEvent(new WindowEvent(AMainFrame.this, WindowEvent.WINDOW_CLOSING));
		}
		
		
		@Override
		public void windowClosing(final WindowEvent windowEvent)
		{
			super.windowClosing(windowEvent);
			exit();
		}
	}
	
	private class WindowViewSerializer implements ViewSerializer
	{
		@Override
		public void writeView(final View view, final ObjectOutputStream out) throws IOException
		{
			String title = view.getTitle();
			for (ESumatraViewType viewType : ESumatraViewType.values())
			{
				if (viewType.getTitle().equals(title))
				{
					out.writeInt(viewType.getId());
					return;
				}
			}
		}
		
		
		@Override
		public View readView(final ObjectInputStream in) throws IOException
		{
			int id = in.readInt();
			
			for (ASumatraView sumatraView : views)
			{
				if (sumatraView.getType().getId() == id)
				{
					return sumatraView.getView();
				}
			}
			ESumatraViewType type = ESumatraViewType.fromId(id);
			log.warn("View " + type + " with id " + id + " has been removed.");
			if (type == null)
			{
				type = ESumatraViewType.DUMMY;
			}
			return new DummyView(type).getView();
		}
	}
}
