/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.presenter;

import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.gui.visualizer.presenter.recorder.MediaRecorder;
import edu.tigers.sumatra.gui.visualizer.view.VisualizerPanel;
import edu.tigers.sumatra.gui.visualizer.view.options.ShapeSelectionModel;
import edu.tigers.sumatra.gui.visualizer.view.options.ShapeTreeCellRenderer;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.SimpleDocumentListener;
import edu.tigers.sumatra.views.ISumatraPresenter;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import javax.swing.AbstractButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * Presenter for the visualizer.
 */
@Log4j2
public class VisualizerPresenter implements ISumatraViewPresenter, IWorldFrameObserver
{
	private static final int VISUALIZATION_FPS = 24;
	private final ShapeSelectionModel shapeSelectionModel = new ShapeSelectionModel();
	@Getter
	private final VisualizerPanel viewPanel = new VisualizerPanel();
	@Getter
	private final VisualizerFieldPresenter fieldPresenter = new VisualizerFieldPresenter(
			viewPanel.getFieldPanel()
	);
	private final BallInteractor ballInteractor = new BallInteractor();
	private final MediaRecorder mediaRecorder = new MediaRecorder();
	private final Set<Object> defaultVisibilityObtained = new HashSet<>();
	@Setter
	private String propertiesPrefix = VisualizerPresenter.class.getCanonicalName() + ".";
	private Thread updateThread;
	private boolean firstUpdate = true;


	public VisualizerPresenter()
	{
		fieldPresenter.getOnFieldClicks().add(ballInteractor::onFieldClick);

		connect(
				viewPanel.getToolbar().getFancyDrawing(),
				"fancyPainting",
				true,
				fieldPresenter::setFancyPainting
		);
		connect(
				viewPanel.getToolbar().getDarkMode(),
				"darkMode",
				false,
				fieldPresenter::setDarkMode
		);
		connect(
				viewPanel.getToolbar().getBorderOffset(),
				"borderOffset",
				false,
				this::setAddBorderOffset
		);
		connect(
				viewPanel.getToolbar().getCaptureSettingsDialog().getTxtRecordingWidth(),
				"recording.width",
				"1920",
				b -> {
				}
		);
		connect(
				viewPanel.getToolbar().getCaptureSettingsDialog().getTxtRecordingHeight(),
				"recording.height",
				"0",
				b -> {
				}
		);
		connect(
				viewPanel.getToolbar().getShapeSelection(),
				"shapeSelection.visible",
				true,
				this::setShapeSelectionPanelVisibility
		);

		viewPanel.getShapeSelectionPanel().getTree().setModel(shapeSelectionModel);
		viewPanel.getShapeSelectionPanel().getTree().setDigIn(false);
		viewPanel.getShapeSelectionPanel().getTree().getCheckBoxTreeSelectionModel()
				.addTreeSelectionListener(this::onSelectionChanged);
		viewPanel.getShapeSelectionPanel().getTree().addTreeExpansionListener(new MyTreeExpansionListener());
		viewPanel.getShapeSelectionPanel().getTree().setCellRenderer(new ShapeTreeCellRenderer());
		viewPanel.getShapeSelectionPanel().getExpandAll().addActionListener(a -> expandAll());
		viewPanel.getShapeSelectionPanel().getCollapseAll().addActionListener(a -> collapseAll());
		viewPanel.getShapeSelectionPanel().addLayerFileSaver(this::saveLayersToFile);
		viewPanel.getShapeSelectionPanel().addLayerFileOpener(this::openLayersFromFile);
		viewPanel.getShapeSelectionPanel().getPresetDef().addActionListener(a -> showOnlyDefaultLayers());

		viewPanel.getToolbar().getTurnCounterClockwise().addActionListener(a -> fieldPresenter.turnCounterClockwise());
		viewPanel.getToolbar().getTurnClockwise().addActionListener(a -> fieldPresenter.turnClockwise());
		viewPanel.getToolbar().getResetField().addActionListener(a -> fieldPresenter.resetField());
		viewPanel.getToolbar().getRecordVideoFull().addActionListener(this::recordVideoFullField);
		viewPanel.getToolbar().getRecordVideoSelection().addActionListener(this::recordVideoCurrentSelection);
		viewPanel.getToolbar().getTakeScreenshotFull().addActionListener(this::takeScreenshotFull);
		viewPanel.getToolbar().getTakeScreenshotSelection().addActionListener(this::takeScreenshotSelection);
	}


	private void setShapeSelectionPanelVisibility(boolean visible)
	{
		viewPanel.getShapeSelectionPanel().setVisible(visible);
		if (visible)
		{
			viewPanel.getSplitPane().setDividerLocation(0.8);
		}
	}


	private void connect(JTextField textField, String key, String defValue, Consumer<String> consumer)
	{
		String value = SumatraModel.getInstance().getUserProperty(propertiesPrefix + key, defValue);
		textField.setText(value);
		textField.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
			SumatraModel.getInstance().setUserProperty(propertiesPrefix + key, textField.getText());
			consumer.accept(textField.getText());
		});
		consumer.accept(textField.getText());
	}


	private void connect(AbstractButton button, String key, boolean defValue, Consumer<Boolean> consumer)
	{
		boolean value = SumatraModel.getInstance().getUserProperty(propertiesPrefix + key, defValue);
		button.setSelected(value);
		button.addActionListener(e -> consumer.accept(button.isSelected()));
		button.addActionListener(e -> SumatraModel.getInstance()
				.setUserProperty(propertiesPrefix + key, String.valueOf(button.isSelected())));
		consumer.accept(button.isSelected());
	}


	@Override
	public void onStart()
	{
		GlobalShortcuts.add(
				"Reset field",
				viewPanel,
				fieldPresenter::resetField,
				KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0)
		);

		GlobalShortcuts.add(
				"Show / hide shape selection", viewPanel, this::toggleShapeSelection,
				KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

		NamedThreadFactory factory = new NamedThreadFactory("VisualizerUpdater");
		updateThread = factory.newThread(this::updateLoop);
		updateThread.start();
		firstUpdate = true;
	}


	@Override
	public void onStop()
	{
		if (updateThread != null)
		{
			updateThread.interrupt();
			updateThread = null;
		}

		ISumatraViewPresenter.super.onStop();
		GlobalShortcuts.removeAllForComponent(viewPanel);
	}


	@Override
	public void onModuliStarted()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(ballInteractor);
		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(fieldPresenter);

		firstUpdate = true;
	}


	@Override
	public void onModuliStopped()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(ballInteractor);
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(fieldPresenter);
	}


	@Override
	public List<ISumatraPresenter> getChildPresenters()
	{
		return List.of(fieldPresenter);
	}


	private void updateLoop()
	{
		while (!Thread.interrupted())
		{
			long t0 = System.nanoTime();
			update();
			long t1 = System.nanoTime();
			long sleep = (1_000_000_000L / VISUALIZATION_FPS) - (t1 - t0);
			if (sleep > 0)
			{
				ThreadUtil.parkNanosSafe(sleep);
			}
		}
	}


	private void update()
	{
		try
		{
			HashMap<ShapeMapSource, ShapeMap> shapeMaps = new HashMap<>(fieldPresenter.getShapeMaps());
			shapeMaps.forEach(this::newShapeMap);
			fieldPresenter.update();
		} catch (Exception e)
		{
			log.error("Exception in visualizer updater", e);
		}

		if (firstUpdate)
		{
			firstUpdate = false;
			SwingUtilities.invokeLater(() -> viewPanel.getSplitPane().setDividerLocation(0.8));
		}
	}


	private void onSelectionChanged(TreeSelectionEvent treeSelectionEvent)
	{
		updateVisibility();
	}


	private void updateVisibility()
	{
		shapeSelectionModel.getSources().forEach((source, node) -> {
			boolean visible = isSelected(node, true);
			fieldPresenter.setSourceVisibility(source, visible);
		});

		shapeSelectionModel.getLayers().forEach((layer, node) -> {
			boolean visible = isSelected(node, true);
			fieldPresenter.setShapeLayerVisibility(layer.getId(), visible);
			if (defaultVisibilityObtained.contains(layer))
			{
				var noDigIn = isSelected(node, false);
				SumatraModel.getInstance().setUserProperty(propertiesPrefix + layer.getId(), String.valueOf(noDigIn));
			}
		});

		shapeSelectionModel.getLayerCategories().forEach((category, node) -> {
			if (defaultVisibilityObtained.contains(category))
			{
				var noDigIn = isSelected(node, false);
				SumatraModel.getInstance().setUserProperty(propertiesPrefix + category.name(), String.valueOf(noDigIn));
			}
		});
		viewPanel.getShapeSelectionPanel().getTree().updateUI();
	}


	private void newShapeMap(final ShapeMapSource source, final ShapeMap shapeMap)
	{
		// select shape map sources by default
		shapeSelectionModel.addShapeMapSource(source).ifPresent(node -> this.selectShapeMapSource(source, node));

		List<DefaultMutableTreeNode> newNodes = shapeMap.getAllShapeLayers().stream()
				.map(ShapeMap.ShapeLayer::getIdentifier)
				.map(shapeSelectionModel::addShapeLayer)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();
		newNodes.forEach(this::setDefaultVisibility);

		shapeSelectionModel.getLayerCategories().forEach((category, node) -> {
			if (!defaultVisibilityObtained.contains(category))
			{
				var visible = SumatraModel.getInstance().getUserProperty(propertiesPrefix + category.name(), false);
				setNodeSelection(node, visible);
				defaultVisibilityObtained.add(category);
			}
		});
	}


	private void selectShapeMapSource(ShapeMapSource source, DefaultMutableTreeNode node)
	{
		if (source.getParent() != null && node.getParent() instanceof DefaultMutableTreeNode parent)
		{
			selectNode(parent);
		} else
		{
			selectNode(node);
		}
	}


	private void setDefaultVisibility(DefaultMutableTreeNode node)
	{
		if (node.getUserObject() instanceof IShapeLayerIdentifier shapeLayer)
		{
			boolean visible = SumatraModel.getInstance().getUserProperty(
					propertiesPrefix + shapeLayer.getId(),
					shapeLayer.isVisibleByDefault()
			);
			setNodeSelection(node, visible);
			defaultVisibilityObtained.add(shapeLayer);
			expandCollapseDependingOnUserProperty(node);
		}
	}


	private void expandCollapseDependingOnUserProperty(DefaultMutableTreeNode node)
	{
		var tree = viewPanel.getShapeSelectionPanel().getTree();
		var path = new TreePath(node.getPath()).getParentPath();
		while (path != null)
		{
			var expanded = SumatraModel.getInstance().getUserProperty(propertiesPrefix + path, false);
			if (expanded)
			{
				if (!tree.isExpanded(path))
				{
					var constPath = path;
					SwingUtilities.invokeLater(() -> tree.expandPath(constPath));
				}
			} else
			{
				if (tree.isExpanded(path))
				{
					var constPath = path;
					SwingUtilities.invokeLater(() -> tree.collapsePath(constPath));
				}
			}
			path = path.getParentPath();
		}
	}


	private void showOnlyDefaultLayers()
	{
		shapeSelectionModel.getAllLayerNonLeafNodes().forEach(this::deselectNode);
		for (var entry : shapeSelectionModel.getLayers().entrySet())
		{
			var layer = entry.getKey();
			var node = entry.getValue();

			var visible = layer.isVisibleByDefault();
			SumatraModel.getInstance().setUserProperty(propertiesPrefix + layer.getId(), visible);
			setNodeSelection(node, visible);
		}

	}


	private void saveLayersToFile(File file)
	{
		var lines = shapeSelectionModel.getAllLayerNodes()
				.filter(node -> isSelected(node, false))
				.map(node -> new TreePath(node.getPath()).toString())
				.toList();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file)))
		{
			for (var line : lines)
			{
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException e)
		{
			log.error(e);
		}
	}


	private void openLayersFromFile(File file)
	{
		Set<String> paths;
		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			paths = reader.lines().collect(Collectors.toSet());
		} catch (IOException e)
		{
			log.error(e);
			return;
		}
		for (var node : shapeSelectionModel.getAllLayerNodes().toList())
		{
			var path = new TreePath(node.getPath()).toString();
			setNodeSelection(node, paths.contains(path));
		}
	}


	private void setNodeSelection(DefaultMutableTreeNode node, boolean select)
	{
		if (select)
		{
			selectNode(node);
		} else
		{
			deselectNode(node);
		}
	}


	private void selectNode(DefaultMutableTreeNode node)
	{
		var treePath = new TreePath(node.getPath());
		var checkBoxTreeSelectionModel = viewPanel.getShapeSelectionPanel().getTree().getCheckBoxTreeSelectionModel();
		SwingUtilities.invokeLater(() -> checkBoxTreeSelectionModel.addSelectionPath(treePath));
	}


	private void deselectNode(DefaultMutableTreeNode node)
	{
		var treePath = new TreePath(node.getPath());
		var checkBoxTreeSelectionModel = viewPanel.getShapeSelectionPanel().getTree().getCheckBoxTreeSelectionModel();
		SwingUtilities.invokeLater(() -> checkBoxTreeSelectionModel.removeSelectionPath(treePath));
	}


	private void expandAll()
	{
		var tree = viewPanel.getShapeSelectionPanel().getTree();
		shapeSelectionModel.getAllNonLeafPaths().forEach(path -> SwingUtilities.invokeLater(() -> tree.expandPath(path)));
	}


	private void collapseAll()
	{
		var tree = viewPanel.getShapeSelectionPanel().getTree();
		shapeSelectionModel.getAllNonLeafNodes()
				.filter(node -> node.getPath().length > 1)
				.filter(node -> !(shapeSelectionModel.isLayer(node) && node.getPath().length < 3))
				.filter(node -> !(shapeSelectionModel.isSource(node) && node.getPath().length < 2))
				.map(DefaultMutableTreeNode::getPath)
				.map(TreePath::new)
				.forEach(path -> SwingUtilities.invokeLater(() -> tree.collapsePath(path)));
	}


	private boolean isSelected(DefaultMutableTreeNode node, boolean digIn)
	{
		var treePath = new TreePath(node.getPath());
		return viewPanel.getShapeSelectionPanel().getTree().getCheckBoxTreeSelectionModel()
				.isPathSelected(treePath, digIn);
	}


	private void setAddBorderOffset(boolean state)
	{
		fieldPresenter.getFieldPane().setAddBorderOffset(state);
		fieldPresenter.resetField();
	}


	private void updateMediaRecorder()
	{
		int width = parseInt(viewPanel.getToolbar().getCaptureSettingsDialog().getTxtRecordingWidth().getText());
		int height = parseInt(viewPanel.getToolbar().getCaptureSettingsDialog().getTxtRecordingHeight().getText());

		SumatraModel.getInstance().setUserProperty(VisualizerPresenter.class, "recording.width", String.valueOf(width));
		SumatraModel.getInstance().setUserProperty(VisualizerPresenter.class, "recording.height", String.valueOf(width));

		mediaRecorder.getFieldPane().setAddBorderOffset(fieldPresenter.getFieldPane().isAddBorderOffset());

		// Set initial width for border offset calculation
		mediaRecorder.getFieldPane().setWidth(width);

		double fieldRatio = mediaRecorder.getFieldPane().getTransformation().getFieldTotalRatio();
		int borderOffset = mediaRecorder.getFieldPane().getBorderOffset();
		if (width == 0)
		{
			width = (int) Math.round((height - borderOffset) * fieldRatio);
		} else if (height == 0)
		{
			height = (int) Math.round(width / fieldRatio) + borderOffset;
		}

		// numbers have to always be divisible by 2 for media encoding
		width = width + (width % 2);
		height = height + (height % 2);

		mediaRecorder.getFieldPane().setWidth(width);
		mediaRecorder.getFieldPane().setHeight(height);
	}


	private int parseInt(String value)
	{
		if (StringUtils.isBlank(value))
		{
			return 0;
		}
		try
		{
			return Integer.parseInt(value);
		} catch (NumberFormatException e)
		{
			log.warn("Could not parse number: {}", value);
			return 0;
		}
	}


	private void takeScreenshotFull(ActionEvent e)
	{
		updateMediaRecorder();
		mediaRecorder.takeScreenshotFullField(fieldPresenter.visibleShapeLayers());
	}


	private void takeScreenshotSelection(ActionEvent e)
	{
		updateMediaRecorder();
		mediaRecorder.takeScreenshotCurrentSelection(fieldPresenter.getFieldPane(), fieldPresenter.visibleShapeLayers());
	}


	private void recordVideoFullField(ActionEvent e)
	{
		if (e.getSource() instanceof JToggleButton toggleButton)
		{
			if (toggleButton.isSelected())
			{
				updateMediaRecorder();
				mediaRecorder.startVideoRecordingFullField(fieldPresenter::visibleShapeLayers);
			} else
			{
				mediaRecorder.stopVideoRecording();
			}
		}
	}


	private void recordVideoCurrentSelection(ActionEvent e)
	{
		if (e.getSource() instanceof JToggleButton toggleButton)
		{
			if (toggleButton.isSelected())
			{
				updateMediaRecorder();
				mediaRecorder.startVideoRecordingCurrentSelection(fieldPresenter.getFieldPane(),
						fieldPresenter::visibleShapeLayers);
			} else
			{
				mediaRecorder.stopVideoRecording();
			}
		}
	}


	private void toggleShapeSelection()
	{
		JToggleButton shapeSelection = viewPanel.getToolbar().getShapeSelection();
		if (!viewPanel.getShapeSelectionPanel().getTree().hasFocus() && shapeSelection.isSelected())
		{
			viewPanel.getShapeSelectionPanel().getTree().requestFocus();
		} else
		{
			shapeSelection.doClick();
			if (shapeSelection.isSelected())
			{
				viewPanel.getShapeSelectionPanel().getTree().requestFocus();
			}
		}
	}


	private class MyTreeExpansionListener implements TreeExpansionListener
	{
		@Override
		public void treeCollapsed(TreeExpansionEvent event)
		{
			var obj = event.getPath().getLastPathComponent();
			if (obj instanceof DefaultMutableTreeNode node)
			{
				iterateChildrenToCollapse(node);
			}
			SumatraModel.getInstance().setUserProperty(propertiesPrefix + event.getPath(), null);
		}


		private void iterateChildrenToCollapse(DefaultMutableTreeNode node)
		{
			var children = node.children();
			while (children.hasMoreElements())
			{
				var child = (DefaultMutableTreeNode) children.nextElement();
				var path = new TreePath(child.getPath());
				SumatraModel.getInstance().setUserProperty(propertiesPrefix + path, null);
				iterateChildrenToCollapse(child);
			}
		}


		@Override
		public void treeExpanded(TreeExpansionEvent event)
		{
			SumatraModel.getInstance().setUserProperty(propertiesPrefix + event.getPath(), true);
		}
	}
}
