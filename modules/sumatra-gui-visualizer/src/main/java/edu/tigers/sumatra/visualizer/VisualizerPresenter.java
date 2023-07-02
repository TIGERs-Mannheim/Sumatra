/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.drawable.IShapeLayerIdentifier;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.SimpleDocumentListener;
import edu.tigers.sumatra.views.ISumatraPresenter;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import edu.tigers.sumatra.visualizer.field.VisualizerFieldPresenter;
import edu.tigers.sumatra.visualizer.field.components.BallInteractor;
import edu.tigers.sumatra.visualizer.field.recorder.MediaRecorder;
import edu.tigers.sumatra.visualizer.options.ShapeSelectionModel;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import javax.swing.AbstractButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;


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
	private Thread updateThread;


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
				b -> viewPanel.getShapeSelectionPanel().setVisible(b)
		);

		viewPanel.getShapeSelectionPanel().getTree().setModel(shapeSelectionModel);
		viewPanel.getShapeSelectionPanel().getTree().setExpandsSelectedPaths(true);
		viewPanel.getShapeSelectionPanel().getTree().getCheckBoxTreeSelectionModel()
				.addTreeSelectionListener(this::onSelectionChanged);

		viewPanel.getToolbar().getTurnCounterClockwise().addActionListener(a -> fieldPresenter.turnCounterClockwise());
		viewPanel.getToolbar().getTurnClockwise().addActionListener(a -> fieldPresenter.turnClockwise());
		viewPanel.getToolbar().getResetField().addActionListener(a -> fieldPresenter.resetField());
		viewPanel.getToolbar().getRecordVideoFull().addActionListener(this::recordVideoFullField);
		viewPanel.getToolbar().getRecordVideoSelection().addActionListener(this::recordVideoCurrentSelection);
		viewPanel.getToolbar().getTakeScreenshotFull().addActionListener(this::takeScreenshotFull);
		viewPanel.getToolbar().getTakeScreenshotSelection().addActionListener(this::takeScreenshotSelection);
	}


	private void connect(JTextField textField, String key, String defValue, Consumer<String> consumer)
	{
		String value = SumatraModel.getInstance().getUserProperty(VisualizerPresenter.class, key, defValue);
		textField.setText(value);
		textField.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
			SumatraModel.getInstance().setUserProperty(VisualizerPresenter.class, key, textField.getText());
			consumer.accept(textField.getText());
		});
		consumer.accept(textField.getText());
	}


	private void connect(AbstractButton button, String key, boolean defValue, Consumer<Boolean> consumer)
	{
		boolean value = SumatraModel.getInstance().getUserProperty(VisualizerPresenter.class, key, defValue);
		button.setSelected(value);
		button.addActionListener(e -> consumer.accept(button.isSelected()));
		button.addActionListener(e -> SumatraModel.getInstance()
				.setUserProperty(VisualizerPresenter.class, key, String.valueOf(button.isSelected())));
		consumer.accept(button.isSelected());
	}


	@Override
	public void onStart()
	{
		ISumatraViewPresenter.super.onStart();

		GlobalShortcuts.add(
				"Reset field",
				viewPanel,
				fieldPresenter::resetField,
				KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0)
		);

		GlobalShortcuts.add(
				"Show / hide shape selection", viewPanel, this::toggleShapeSelection,
				KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, InputEvent.CTRL_DOWN_MASK));

		NamedThreadFactory factory = new NamedThreadFactory("VisualizerUpdater");
		updateThread = factory.newThread(this::updateLoop);
		updateThread.start();
	}


	@Override
	public void onStop()
	{
		saveVisibility();
		if (updateThread != null)
		{
			updateThread.interrupt();
			updateThread = null;
		}

		ISumatraViewPresenter.super.onStop();
		GlobalShortcuts.removeAllForComponent(viewPanel);
	}


	@Override
	public void onStartModuli()
	{
		ISumatraViewPresenter.super.onStartModuli();

		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(ballInteractor);
		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(fieldPresenter);
	}


	@Override
	public void onStopModuli()
	{
		ISumatraViewPresenter.super.onStopModuli();

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
			update(shapeMaps);
			fieldPresenter.update();
		} catch (Exception e)
		{
			log.error("Exception in visualizer updater", e);
		}
	}


	private void onSelectionChanged(TreeSelectionEvent treeSelectionEvent)
	{
		updateVisibility();
	}


	private void updateVisibility()
	{
		shapeSelectionModel.getSources().forEach((source, node) -> {
			boolean visible = isSelected(node);
			fieldPresenter.setSourceVisibility(source, visible);
		});

		shapeSelectionModel.getLayers().forEach((layer, node) -> {
			boolean visible = isSelected(node);
			fieldPresenter.setShapeLayerVisibility(layer.getId(), visible);
		});
	}


	private void saveVisibility()
	{
		shapeSelectionModel.getLayers().forEach((layer, node) -> {
			boolean visible = isSelected(node);
			SumatraModel.getInstance().setUserProperty(layer.getId(), String.valueOf(visible));
		});
	}


	public void update(Map<ShapeMapSource, ShapeMap> shapeMaps)
	{
		shapeMaps.forEach(this::newShapeMap);
	}


	private void newShapeMap(final ShapeMapSource source, final ShapeMap shapeMap)
	{
		// select shape map sources by default
		shapeSelectionModel.addShapeMapSource(source).ifPresent(this::selectNode);

		List<DefaultMutableTreeNode> newNodes = shapeMap.getAllShapeLayers().stream()
				.map(ShapeMap.ShapeLayer::getIdentifier)
				.map(shapeSelectionModel::addShapeLayer)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();
		newNodes.forEach(this::setDefaultVisibility);
	}


	private void setDefaultVisibility(DefaultMutableTreeNode node)
	{
		if (node.getUserObject() instanceof IShapeLayerIdentifier shapeLayer)
		{
			boolean visible = SumatraModel.getInstance().getUserProperty(
					shapeLayer.getId(),
					shapeLayer.isVisibleByDefault()
			);
			if (visible)
			{
				selectNode(node);
			} else
			{
				deselectNode(node);
			}
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


	private boolean isSelected(DefaultMutableTreeNode node)
	{
		var treePath = new TreePath(node.getPath());
		return viewPanel.getShapeSelectionPanel().getTree().getCheckBoxTreeSelectionModel()
				.isPathSelected(treePath, true);
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

		SumatraModel.getInstance().setUserProperty(VisualizerPresenter.class, "recording.width", width);
		SumatraModel.getInstance().setUserProperty(VisualizerPresenter.class, "recording.height", height);

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
}
