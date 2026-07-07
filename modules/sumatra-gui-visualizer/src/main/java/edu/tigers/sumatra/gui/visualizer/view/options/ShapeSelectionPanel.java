package edu.tigers.sumatra.gui.visualizer.view.options;

import com.jidesoft.swing.CheckBoxTree;
import edu.tigers.sumatra.components.BetterScrollPane;
import edu.tigers.sumatra.util.ShortcutSuppressor;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
public class ShapeSelectionPanel extends JPanel
{
	private static final String PRESET_QUICK_FILE_PATH_FORMAT = "preset_quick%d.layers";
	private static final String CONFIG_DIR = "config/shape_layer";
	private static final int NUM_PRESET_QUICK_SLOTS = 5;

	@Getter
	private final JToolBar toolBarNorth = new JToolBar();
	@Getter
	private final JToolBar toolBarSouth = new JToolBar();

	@Getter
	private final CheckBoxTree tree = new CheckBoxTree();

	@Getter
	private final JButton expandAll = new JButton();
	@Getter
	private final JButton collapseAll = new JButton();
	@Getter
	private final JButton presetDef = new JButton("Def");

	private final List<JButton> presetQuick = new ArrayList<>();

	private final JToggleButton add = new JToggleButton();

	private final JButton open = new JButton();
	private final JButton save = new JButton();

	private final List<LayerFileSaver> layerFileSavers = new ArrayList<>();
	private final List<LayerFileOpener> layerFileOpeners = new ArrayList<>();


	public ShapeSelectionPanel()
	{
		setLayout(new BorderLayout());

		expandAll.setToolTipText("Expand all");

		collapseAll.setToolTipText("Collapse all");

		open.setToolTipText("Open from file");
		open.addActionListener(a -> pressOpenButton());

		save.setToolTipText("Save to file");
		save.addActionListener(a -> pressSaveButton());

		presetDef.setToolTipText("Switch layers to default");

		for (int i = 0; i < NUM_PRESET_QUICK_SLOTS; i++)
		{
			var num = i + 1;

			var button = new JButton(String.valueOf(num));
			button.addActionListener(a -> pressQuickPreset(num));

			presetQuick.add(button);
		}

		add.addActionListener(a -> pressAddButton());
		add.setSelected(false);

		var scrollPane = new BetterScrollPane(tree);

		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		toolBarNorth.setFloatable(false);

		toolBarNorth.add(expandAll);
		toolBarNorth.add(collapseAll);

		toolBarSouth.setFloatable(false);

		toolBarSouth.add(open);
		toolBarSouth.add(save);
		toolBarSouth.add(Box.createHorizontalGlue());
		toolBarSouth.add(presetDef);

		presetQuick.forEach(toolBarSouth::add);

		toolBarSouth.add(add);

		add(toolBarNorth, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(toolBarSouth, BorderLayout.SOUTH);

		ShortcutSuppressor onTreeSelect = new ShortcutSuppressor();
		tree.addFocusListener(onTreeSelect);

		updateIcons();

		pressAddButton();
	}

	@Override
	public void updateUI()
	{
		super.updateUI();
		if (expandAll != null)
		{
			updateIcons();
		}
	}

	private void updateIcons()
	{
		Color c = UIManager.getColor("Label.foreground");

		expandAll.setIcon(
				IconFontSwing.buildIcon(FontAwesome.EXPAND, 16, c));

		collapseAll.setIcon(
				IconFontSwing.buildIcon(FontAwesome.COMPRESS, 16, c));

		open.setIcon(
				IconFontSwing.buildIcon(FontAwesome.FOLDER_OPEN, 16, c));

		save.setIcon(
				IconFontSwing.buildIcon(FontAwesome.FLOPPY_O, 16, c));

		add.setIcon(add.isSelected()
				? IconFontSwing.buildIcon(FontAwesome.TIMES_CIRCLE_O, 16, Color.RED)
				: IconFontSwing.buildIcon(FontAwesome.PLUS_CIRCLE, 16, new Color(0, 180, 0)));
	}

	public void addLayerFileSaver(LayerFileSaver saver)
	{
		layerFileSavers.add(saver);
	}

	public void addLayerFileOpener(LayerFileOpener opener)
	{
		layerFileOpeners.add(opener);
	}

	private void pressSaveButton()
	{
		getPresetFilePathFromUser(true)
				.map(File::new)
				.ifPresent(this::saveToFile);
	}

	private void pressOpenButton()
	{
		getPresetFilePathFromUser(false)
				.map(File::new)
				.ifPresent(this::openFromFile);
	}

	private void saveToFile(File file)
	{
		layerFileSavers.forEach(saver -> saver.onSaveToFile(file));
	}

	private void openFromFile(File file)
	{
		if (file.exists())
		{
			layerFileOpeners.forEach(saver -> saver.onOpenFromFile(file));
		}
	}

	private void pressAddButton()
	{
		if (add.isSelected())
		{
			presetDef.setEnabled(false);

			for (int i = 0; i < presetQuick.size(); ++i)
			{
				var num = i + 1;

				var button = presetQuick.get(i);

				button.setEnabled(true);

				button.setToolTipText(
						String.format("Save current layers to quick select %d", num));
			}

			open.setEnabled(false);
			save.setEnabled(false);

			add.setToolTipText("Cancel setting quick access");
		}
		else
		{
			presetDef.setEnabled(true);

			for (int i = 0; i < presetQuick.size(); ++i)
			{
				var num = i + 1;

				var button = presetQuick.get(i);

				button.setEnabled(getQuickPresetFile(num).exists());

				button.setToolTipText(
						String.format("Switch layers to quick select %d", num));
			}

			open.setEnabled(true);
			save.setEnabled(true);

			add.setToolTipText("Set current layers to quick access");
		}

		updateIcons();
	}

	private void pressQuickPreset(int presetNumber)
	{
		if (add.isSelected())
		{
			saveToFile(getQuickPresetFile(presetNumber));

			add.setSelected(false);

			pressAddButton();
		}
		else
		{
			openFromFile(getQuickPresetFile(presetNumber));
		}
	}

	private File getQuickPresetFile(int presetNumber)
	{
		Paths.get(CONFIG_DIR).toFile().mkdirs();

		return Paths.get(
						CONFIG_DIR,
						String.format(PRESET_QUICK_FILE_PATH_FORMAT, presetNumber))
				.toFile();
	}

	private Optional<String> getPresetFilePathFromUser(boolean useSaveDialog)
	{
		Paths.get(CONFIG_DIR).toFile().mkdirs();

		File savedLayers = Paths.get(CONFIG_DIR, "preset.layers").toFile();

		var lastConfigDir = savedLayers.getParentFile();

		if (lastConfigDir.mkdirs())
		{
			log.info("New directory created: {}", lastConfigDir);
		}

		var fcOpenSnapshot = new JFileChooser(lastConfigDir);

		fcOpenSnapshot.setSelectedFile(savedLayers);

		int returnVal = useSaveDialog
				? fcOpenSnapshot.showSaveDialog(this)
				: fcOpenSnapshot.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				return Optional.of(
						fcOpenSnapshot.getSelectedFile().getCanonicalPath());
			}
			catch (IOException e)
			{
				log.error("Could not load snapshot", e);
			}
		}

		return Optional.empty();
	}

	@FunctionalInterface
	public interface LayerFileSaver
	{
		void onSaveToFile(File file);
	}

	@FunctionalInterface
	public interface LayerFileOpener
	{
		void onOpenFromFile(File file);
	}
}