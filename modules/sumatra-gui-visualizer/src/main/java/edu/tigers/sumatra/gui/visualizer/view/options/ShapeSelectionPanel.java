/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.view.options;


import com.jidesoft.swing.CheckBoxTree;
import edu.tigers.sumatra.components.BetterScrollPane;
import edu.tigers.sumatra.util.ImageScaler;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
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

		expandAll.setIcon(ImageScaler.scaleSmallButtonImageIcon("/icons8-expand-50.png"));
		expandAll.setToolTipText("Expand all");
		collapseAll.setIcon(ImageScaler.scaleSmallButtonImageIcon("/icons8-collapse-50.png"));
		collapseAll.setToolTipText("Collapse all");


		open.setIcon(ImageScaler.scaleSmallButtonImageIcon("/open.png"));
		open.setToolTipText("Open from file");
		open.addActionListener(a -> pressOpenButton());
		save.setIcon(ImageScaler.scaleSmallButtonImageIcon("/save.png"));
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
		pressAddButton();


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
		getPresetFilePathFromUser(true).map(File::new).ifPresent(this::saveToFile);
	}


	private void pressOpenButton()
	{
		getPresetFilePathFromUser(false).map(File::new).ifPresent(this::openFromFile);
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
				button.setToolTipText(String.format("Save current layers to quick select %d", num));
			}
			presetQuick.forEach(button -> button.setEnabled(true));

			open.setEnabled(false);
			save.setEnabled(false);
			add.setIcon(ImageScaler.scaleSmallButtonImageIcon("/icons8-cancel-2-60.png"));
			add.setToolTipText("Cancel setting quick access");
		} else
		{
			presetDef.setEnabled(true);
			for (int i = 0; i < presetQuick.size(); ++i)
			{
				var num = i + 1;
				var button = presetQuick.get(i);
				button.setEnabled(getQuickPresetFile(num).exists());
				button.setToolTipText(String.format("Switch layers to quick select %d", num));
			}

			open.setEnabled(true);
			save.setEnabled(true);

			add.setIcon(ImageScaler.scaleSmallButtonImageIcon("/icons8-add-50.png"));
			add.setToolTipText("Set current layers to quick access");
		}
	}


	private void pressQuickPreset(int presetNumber)
	{
		if (add.isSelected())
		{
			saveToFile(getQuickPresetFile(presetNumber));
			add.setSelected(false);
			pressAddButton();
		} else
		{
			openFromFile(getQuickPresetFile(presetNumber));
		}
	}


	private File getQuickPresetFile(int presetNumber)
	{
		Paths.get(CONFIG_DIR).toFile().mkdirs();
		return Paths.get(CONFIG_DIR, String.format(PRESET_QUICK_FILE_PATH_FORMAT, presetNumber)).toFile();
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
				return Optional.of(fcOpenSnapshot.getSelectedFile().getCanonicalPath());
			} catch (IOException e)
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
