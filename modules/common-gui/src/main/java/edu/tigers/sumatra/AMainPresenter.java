/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.ScalingUtil;
import edu.tigers.sumatra.views.ASumatraView;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.RootWindow;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.MixedViewHandler;
import net.infonode.docking.util.ViewMap;
import net.infonode.gui.laf.InfoNodeLookAndFeel;
import net.infonode.util.Direction;
import org.apache.commons.lang.StringUtils;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * Base for MainPresenter
 */
@Log4j2
public abstract class AMainPresenter<T extends AMainFrame>
{
	private static final Path LAYOUT_CONFIG_PATH = SumatraModel.getInstance().getConfigPath().resolve("layout");
	private static final String LAST_LAYOUT_FILE = "last.ly";
	private static final String DEFAULT_LAYOUT_FILE = "default.ly";

	@Getter(AccessLevel.PROTECTED)
	private final T mainFrame;
	private final RootWindow rootWindow;
	private final String name;
	private final Path layoutConfigPath;

	@Getter
	private final List<ASumatraView> views;

	static
	{
		InfoNodeLookAndFeel.install();
		FlatIntelliJLaf.installLafInfo();
		FlatLightLaf.installLafInfo();

		ScalingUtil.updateBaselineSize(new JTextPane().getFont().getSize());
		// JMenuBar on the macOS menu bar
		System.setProperty("apple.laf.useScreenMenuBar", "true");
	}


	protected AMainPresenter(T mainFrame, List<ASumatraView> views, String name)
	{
		this.views = Collections.unmodifiableList(views);
		this.mainFrame = mainFrame;
		this.rootWindow = createRootWindow(views);
		this.name = name;
		layoutConfigPath = LAYOUT_CONFIG_PATH.resolve(name);

		rootWindow.addListener(new ViewUpdater());
		mainFrame.add(rootWindow, BorderLayout.CENTER);

		updateLayoutItems();
		initializeViews();

		loadPosition();
		onLoadLayout(LAST_LAYOUT_FILE);

		mainFrame.getMenuItemLayoutSave().addActionListener(e -> onSaveLayout());
		mainFrame.addWindowListener(new WindowListener());

		mainFrame.setVisible(true);
	}


	private void initializeViews()
	{
		// initialize all views that are currently visible
		for (ASumatraView view : views)
		{
			if (view.getView().isShowing() || view.getType().isForceLoad())
			{
				view.ensureInitialized();
			}
		}
	}


	@SneakyThrows
	private void updateLayoutItems()
	{
		Files.createDirectories(layoutConfigPath);
		try (var files = Files.list(layoutConfigPath))
		{
			List<String> filenames = Stream.concat(
					files
							.map(Path::getFileName)
							.map(Path::toString)
							.filter(filename -> filename.endsWith(".ly"))
							.filter(filename -> !filename.equals(LAST_LAYOUT_FILE)),
					Stream.of(DEFAULT_LAYOUT_FILE)
			).sorted().map(filename -> filename.replace(".ly", "")).toList();
			var menuItems = mainFrame.setMenuLayoutItems(filenames);
			menuItems.forEach((filename, item) -> item.addActionListener(e -> onLoadLayout(filename + ".ly")));
		} catch (IOException e)
		{
			log.error("Could not read config files from path: {}", layoutConfigPath, e);
		}
	}


	private void onSaveLayout()
	{
		// --- Ask for the filename ---
		String filename = JOptionPane.showInputDialog(null, "Please specify the name of the layout file:");

		if (StringUtils.isBlank(filename))
		{
			return;
		}

		// --- add .ly if necessary ---
		if (!filename.endsWith(".ly"))
		{
			filename += ".ly";
		}

		saveLayout(layoutConfigPath.resolve(filename));

		updateLayoutItems();
	}


	@SneakyThrows
	private void onLoadLayout(String filename)
	{
		Files.createDirectories(layoutConfigPath);
		Path path = layoutConfigPath.resolve(filename);

		if (filename.equals(DEFAULT_LAYOUT_FILE) || !Files.exists(path))
		{
			loadLayout(getClass().getResourceAsStream("/layouts/" + name + "/" + DEFAULT_LAYOUT_FILE));
		} else
		{
			loadLayout(path);
		}
	}


	public void onClose()
	{
		log.trace("Closing {}", this.getClass().getSimpleName());

		GlobalShortcuts.removeAllForFrame(getMainFrame());

		savePosition();
		saveLayout(layoutConfigPath.resolve(LAST_LAYOUT_FILE));

		log.trace("Closed {}", this.getClass().getSimpleName());
	}


	/**
	 * Load JFrame size and position.
	 */
	private void loadPosition()
	{
		int x = SumatraModel.getInstance().getUserProperty(getClass(), getNumberOfDisplays() + ".x", 0);
		int y = SumatraModel.getInstance().getUserProperty(getClass(), getNumberOfDisplays() + ".y", 0);
		mainFrame.setLocation(x, y);

		var maximized = SumatraModel.getInstance()
				.getUserProperty(getClass(), getNumberOfDisplays() + ".maximized", true);
		if (maximized)
		{
			mainFrame.setExtendedState(mainFrame.getExtendedState() | Frame.MAXIMIZED_BOTH);
		} else
		{
			mainFrame.setSize(new Dimension(
					SumatraModel.getInstance().getUserProperty(getClass(), getNumberOfDisplays() + ".w", 1456),
					SumatraModel.getInstance().getUserProperty(getClass(), getNumberOfDisplays() + ".h", 886)
			));
		}
	}


	/**
	 * Save JFrame size and position.
	 */
	private void savePosition()
	{
		boolean maximized = (mainFrame.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;
		SumatraModel.getInstance().setUserProperty(getClass(), getNumberOfDisplays() + ".x", mainFrame.getX());
		SumatraModel.getInstance().setUserProperty(getClass(), getNumberOfDisplays() + ".x", mainFrame.getY());
		SumatraModel.getInstance().setUserProperty(getClass(), getNumberOfDisplays() + ".w", mainFrame.getWidth());
		SumatraModel.getInstance().setUserProperty(getClass(), getNumberOfDisplays() + ".h", mainFrame.getHeight());
		SumatraModel.getInstance().setUserProperty(getClass(), getNumberOfDisplays() + ".maximized", maximized);
	}


	/**
	 * Returns the number of displays.
	 *
	 * @return the number of available display devices (default return is 1)
	 */
	private int getNumberOfDisplays()
	{
		try
		{
			final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			return env.getScreenDevices().length;
		} catch (final HeadlessException e)
		{
			log.warn("Could not determine number of displays", e);
		}

		return 1;
	}


	/**
	 * Creates the root window and the views.
	 */
	private static RootWindow createRootWindow(List<ASumatraView> views)
	{
		ViewMap viewMap = new ViewMap();
		// The mixed view map makes it easy to mix static and dynamic views inside the same root window
		MixedViewHandler handler = new MixedViewHandler(viewMap, new WindowViewSerializer(views));

		// --- create the RootWindow with MixedHandler ---
		RootWindow newRootWindow = DockingUtil.createRootWindow(viewMap, handler, true);

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


	private void loadLayout(Path path)
	{
		try (FileInputStream inputStream = new FileInputStream(path.toFile()))
		{
			loadLayout(inputStream);
		} catch (IOException e)
		{
			log.warn("Failed to load layout: {}", path, e);
		}
	}


	private void loadLayout(InputStream inputStream)
	{
		try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream))
		{
			rootWindow.read(objectInputStream, true);
		} catch (IOException err)
		{
			log.error("Can't load layout.", err);
		}
	}


	private void saveLayout(Path path)
	{
		try (FileOutputStream fileStream = new FileOutputStream(path.toFile()))
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


	private class WindowListener extends WindowAdapter
	{
		@Override
		public void windowClosing(final WindowEvent windowEvent)
		{
			onClose();
		}
	}


	private class RestoreView implements ActionListener
	{
		private final ASumatraView sumatraView;


		public RestoreView(final ASumatraView v)
		{
			sumatraView = v;
		}


		@Override
		public void actionPerformed(final ActionEvent e)
		{
			sumatraView.ensureInitialized();
			sumatraView.getView().restoreFocus();
			DockingUtil.addWindow(sumatraView.getView(), rootWindow);

			JMenuItem item = (JMenuItem) e.getSource();
			item.setEnabled(false);
		}
	}


	private class ViewUpdater extends DockingWindowAdapter
	{
		@Override
		public void windowAdded(final DockingWindow addedToWindow, final DockingWindow addedWindow)
		{
			log.trace("Window {} added to {}", addedWindow, addedToWindow);
			updateViewMenu();
		}


		@Override
		public void windowRemoved(final DockingWindow removedFromWindow, final DockingWindow removedWindow)
		{
			log.trace("Window {} removed from {}", removedWindow, removedFromWindow);
			updateViewMenu();
		}


		private void updateViewMenu()
		{
			log.trace("Updating view menu");
			var menuItems = mainFrame.setViewItems(views);
			menuItems.forEach((view, item) -> {
				item.addActionListener(new RestoreView(view));
				boolean hidden = view.getView().getRootWindow() == null;
				item.setEnabled(hidden);
			});
		}


		@Override
		public void windowShown(final DockingWindow window)
		{
			log.trace("Showing docking window: {}", window);
			List<DockingWindow> dockingWindows = getAllDockingWindows(window);
			views.stream()
					.filter(view -> dockingWindows.contains(view.getView()))
					.forEach(view -> {
						log.trace("Showing view: {}", view);
						view.ensureInitialized();
						view.getPresenter().onShown();
					});
		}


		@Override
		public void windowHidden(final DockingWindow window)
		{
			log.trace("Hiding docking window: {}", window);
			List<DockingWindow> dockingWindows = getAllDockingWindows(window);
			views.stream()
					.filter(view -> dockingWindows.contains(view.getView()))
					.forEach(view -> {
						log.trace("Hiding view: {}", view);
						view.getPresenter().onHidden();
					});
		}


		private List<DockingWindow> getAllDockingWindows(DockingWindow dockingWindow)
		{
			List<DockingWindow> children = IntStream.range(0, dockingWindow.getChildWindowCount())
					.mapToObj(dockingWindow::getChildWindow)
					.toList();
			if (children.isEmpty())
			{
				return List.of(dockingWindow);
			}
			return children.stream()
					.flatMap(child -> getAllDockingWindows(child).stream())
					.toList();
		}
	}
}
