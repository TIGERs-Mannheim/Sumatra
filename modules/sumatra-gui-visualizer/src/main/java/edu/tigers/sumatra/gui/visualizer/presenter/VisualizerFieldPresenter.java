/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.presenter;

import edu.tigers.sumatra.clock.FpsCounter;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableFieldBackground;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.EFieldTurn;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.gui.visualizer.presenter.drawables.DrawableCoordinates;
import edu.tigers.sumatra.gui.visualizer.presenter.drawables.DrawableFps;
import edu.tigers.sumatra.gui.visualizer.presenter.drawables.DrawableRuler;
import edu.tigers.sumatra.gui.visualizer.presenter.recorder.DrawableRecordingAnimation;
import edu.tigers.sumatra.gui.visualizer.view.field.EFieldPanelShapeLayer;
import edu.tigers.sumatra.gui.visualizer.view.field.FieldPane;
import edu.tigers.sumatra.gui.visualizer.view.field.FieldPanel;
import edu.tigers.sumatra.gui.visualizer.view.field.ISelectedRobotsChanged;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.views.ISumatraPresenter;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;


@Log4j2
@RequiredArgsConstructor
public class VisualizerFieldPresenter implements ISumatraPresenter, IWorldFrameObserver
{
	private static final ShapeMapSource PANEL_SHAPE_MAP_SOURCE = ShapeMapSource.of("Field Panel");

	@Getter
	private final FieldPanel fieldPanel;

	private final Set<ShapeMapSource> showSources = new ConcurrentSkipListSet<>();
	@Getter
	private Map<ShapeMapSource, ShapeMap> shapeMaps = new ConcurrentHashMap<>();
	private final Map<String, Boolean> shapeVisibilityMap = new ConcurrentHashMap<>();

	@Getter
	private final FieldPane fieldPane = new FieldPane();

	@Setter
	private DrawableRuler drawableRuler = null;

	@Setter
	private DrawableRectangle drawableSelectionBox = null;
	@Setter
	private Rectangle selectionBox = null;

	@Setter
	private ILineSegment robotPositionerLine = null;
	@Setter
	private DrawableLine drawableRobotPositionerLine = null;

	private Map<BotID, ITrackedBot> bots = new HashMap<>();

	private List<BotID> selectedBots = new CopyOnWriteArrayList<>();

	@Setter
	private List<DrawableCoordinates> coordinates = List.of();
	private final FpsCounter fpsCounter = new FpsCounter();
	private final DrawableFps drawableFps = new DrawableFps(fpsCounter);
	@Setter
	private DrawableRecordingAnimation drawableRecordingAnimation;

	@Setter
	private ISelectedRobotsChanged selectedRobotsChangedListener;

	@Getter
	private final List<FieldMouseInteraction> onFieldClicks = new ArrayList<>();
	@Getter
	private final List<FieldMouseInteraction> onMouseMoves = new ArrayList<>();
	@Getter
	private final List<FieldRobotMoveInteraction> onRobotMove = new ArrayList<>();

	private List<MouseAdapter> mouseAdapters = List.of();

	private final ShapeMap panelShapeMap = new ShapeMap();
	private Image imageBuffer;


	@Override
	public void onStart()
	{
		mouseAdapters = List.of(
				new CoordinatesMouseAdapter(this::getMousePointGlobal, this::setCoordinates),
				new RulerMouseAdapter(this::getMousePointGlobal, this::setDrawableRuler),
				new ZoomMouseAdapter(fieldPane::scale),
				new DragMouseAdapter(fieldPane::drag),
				new SelectionRectangleMouseAdapter(
						this::getMousePointGlobal, this::setDrawableSelectionBox,
						this::setSelectionBox
				),
				new InteractionMouseEvents(),
				new RobotPositionerMouseAdapter(
						this::getMousePointGlobal, this::setDrawableRobotPositionerLine,
						this::setRobotPositionerLine
				)
		);
		mouseAdapters.forEach(fieldPanel::addMouseAdapter);
		fieldPanel.setVisible(true);
		shapeMaps.put(PANEL_SHAPE_MAP_SOURCE, panelShapeMap);
		panelShapeMap.get(EFieldPanelShapeLayer.FPS).add(drawableFps);
	}


	@Override
	public void onStop()
	{
		mouseAdapters.forEach(fieldPanel::removeMouseAdapter);
		SwingUtilities.invokeLater(() -> fieldPanel.setVisible(false));
		fieldPanel.setOffImage(null);
		shapeMaps.clear();
	}


	@Override
	public void onNewWorldFrame(WorldFrameWrapper wFrameWrapper)
	{
		this.bots = wFrameWrapper.getSimpleWorldFrame().getBots();
	}


	@Override
	public void onNewShapeMap(final long timestamp, final ShapeMap shapeMap, final ShapeMapSource source)
	{
		shapeMaps.put(source, shapeMap);
	}


	@Override
	public void onRemoveSourceFromShapeMap(final ShapeMapSource source)
	{
		shapeMaps.remove(source);
		shapeMaps.keySet().removeIf(s -> s.contains(source));
	}


	public void setShapeLayerVisibility(final String layerId, final boolean visible)
	{
		shapeVisibilityMap.put(layerId, visible);
	}


	public void setSourceVisibility(final ShapeMapSource source, final boolean visible)
	{
		if (visible)
		{
			showSources.add(source);
		} else
		{
			showSources.remove(source);
		}
	}


	public void resetField()
	{
		fieldPane.reset();
	}


	public void turnCounterClockwise()
	{
		EFieldTurn fieldTurn = fieldPane.getTransformation().getFieldTurn().turnCounterClockwise();
		fieldPane.getTransformation().setFieldTurn(fieldTurn);
	}


	public void turnClockwise()
	{
		EFieldTurn fieldTurn = fieldPane.getTransformation().getFieldTurn().turnClockwise();
		fieldPane.getTransformation().setFieldTurn(fieldTurn);
	}


	public void setFancyPainting(boolean state)
	{
		fieldPane.setFancyPainting(state);
	}


	public void setDarkMode(boolean state)
	{
		fieldPane.getTransformation().setDarkMode(state);
	}


	private IVector2 getMousePointGlobal(int x, int y)
	{
		IVector2 guiPos = fieldPane.getFieldPos(x, y);
		return fieldPane.getTransformation().transformToGlobalCoordinates(guiPos);
	}


	public void update()
	{
		fieldPane.setWidth(fieldPanel.getWidth());
		fieldPane.setHeight(fieldPanel.getHeight());

		/*
		 * Drawing only makes sense if we have a valid/existent drawing area because creating an image with size 0 will
		 * produce an error. This scenario is possible if the moduli start up before the GUI layouting has been completed
		 * and the component size is still 0|0.
		 */
		if ((fieldPane.getWidth() < 20) || (fieldPane.getHeight() < 20))
		{
			return;
		}

		if (imageBuffer == null)
		{
			resetField();
		}
		if (imageBuffer == null
				|| imageBuffer.getHeight(fieldPanel) != fieldPane.getHeight()
				|| imageBuffer.getWidth(fieldPanel) != fieldPane.getWidth())
		{
			imageBuffer = fieldPanel.createImage(fieldPane.getWidth(), fieldPane.getHeight());
		}

		fpsCounter.newFrame(System.nanoTime());
		updateFieldBackground();
		updateInternalShapeLayers();
		List<ShapeMap.ShapeLayer> shapeLayers = visibleShapeLayers();

		Graphics2D g2 = (Graphics2D) imageBuffer.getGraphics();
		fieldPane.paint(g2, shapeLayers);

		Image currentImage = fieldPanel.getOffImage();
		fieldPanel.setOffImage(imageBuffer);
		imageBuffer = currentImage;

		fieldPanel.repaint();
	}


	private void updateInternalShapeLayers()
	{
		panelShapeMap.get(EFieldPanelShapeLayer.COORDINATES).clear();
		panelShapeMap.get(EFieldPanelShapeLayer.COORDINATES).addAll(coordinates);

		panelShapeMap.get(EFieldPanelShapeLayer.SELECTION).clear();
		Optional.ofNullable(drawableSelectionBox)
				.ifPresent(box -> panelShapeMap.get(EFieldPanelShapeLayer.SELECTION).add(box));

		Optional.ofNullable(drawableRobotPositionerLine)
				.ifPresent(line -> panelShapeMap.get(EFieldPanelShapeLayer.SELECTION).add(line));

		if (selectionBox != null)
		{
			selectedBots.clear();
			bots.values()
					.stream()
					.filter(bot -> selectionBox.isPointInShape(bot.getPos()))
					.forEach(bot ->
					{
						panelShapeMap.get(EFieldPanelShapeLayer.SELECTION).add(new DrawableCircle(
								Circle.createCircle(bot.getPos(), Geometry.getBotRadius() + 4))
								.setColor(new Color(255, 180, 0, 255)));
						selectedBots.add(bot.getBotId());
					});
			selectedRobotsChangedListener.selectedRobotsChanged(selectedBots);
		} else if (bots != null)
		{
			bots.values()
					.stream()
					.filter(bot -> selectedBots.contains(bot.getBotId()))
					.forEach(bot -> panelShapeMap.get(EFieldPanelShapeLayer.SELECTION).add(new DrawableCircle(
							Circle.createCircle(bot.getPos(), Geometry.getBotRadius() + 4))
							.setColor(new Color(0, 225, 255, 255))));
		}

		if (robotPositionerLine != null && !selectedBots.isEmpty())
		{
			int numBots = selectedBots.size();
			var start = robotPositionerLine.getPathStart();
			var end = robotPositionerLine.getPathEnd();
			var startToEnd = end.subtractNew(start);

			double step = getStepSize(startToEnd, numBots);
			for (int i = 0; i < numBots; i++)
			{
				var pos = start.addNew(startToEnd.scaleToNew(step * i));
				panelShapeMap.get(EFieldPanelShapeLayer.SELECTION).add(new DrawableCircle(
						Circle.createCircle(pos, Geometry.getBotRadius() + 4))
						.setColor(new Color(0, 225, 255, 255)));
			}
		}

		panelShapeMap.get(EFieldPanelShapeLayer.RULER).clear();
		Optional.ofNullable(drawableRuler)
				.ifPresent(ruler -> panelShapeMap.get(EFieldPanelShapeLayer.RULER).add(ruler));

		panelShapeMap.get(EFieldPanelShapeLayer.RECORDING).clear();
		Optional.ofNullable(drawableRecordingAnimation)
				.ifPresent(ruler -> panelShapeMap.get(EFieldPanelShapeLayer.RECORDING).add(ruler));
	}


	private void updateFieldBackground()
	{
		shapeMaps.values().stream()
				.flatMap(m -> m.getAllShapeLayers().stream())
				.flatMap(l -> l.getShapes().stream())
				.filter(s -> s.getClass().equals(DrawableFieldBackground.class))
				.findAny()
				.map(DrawableFieldBackground.class::cast)
				.ifPresent(fieldPane::processFieldBackground);
	}


	public List<ShapeMap.ShapeLayer> visibleShapeLayers()
	{
		return shapeMaps.entrySet().stream()
				.filter(s -> showSources.contains(s.getKey()))
				.map(Map.Entry::getValue)
				.map(ShapeMap::getAllShapeLayers)
				.flatMap(Collection::stream)
				.filter(e -> shapeVisibilityMap.getOrDefault(e.getIdentifier().getId(), false))
				.sorted()
				.toList();
	}


	private class InteractionMouseEvents extends MouseAdapter
	{
		@Override
		public void mouseClicked(final MouseEvent e)
		{
			IVector2 globalPos = getMousePointGlobal(e.getX(), e.getY());
			onFieldClicks.forEach(c -> c.onInteraction(globalPos, e));
		}


		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (e.isControlDown() && SwingUtilities.isRightMouseButton(e) && robotPositionerLine != null
					&& !selectedBots.isEmpty())
			{
				var start = robotPositionerLine.getPathStart();
				var end = robotPositionerLine.getPathEnd();
				var startToEnd = end.subtractNew(start);
				int numBots = selectedBots.size();

				double step = getStepSize(startToEnd, numBots);
				for (int i = 0; i < numBots; i++)
				{
					var pos = start.addNew(startToEnd.scaleToNew(step * i));
					final BotID botId = selectedBots.get(i);
					onRobotMove.forEach(c -> c.onInteraction(botId, pos));
				}
			}
		}


		@Override
		public void mouseMoved(final MouseEvent e)
		{
			IVector2 lastMousePoint = getMousePointGlobal(e.getX(), e.getY());
			onMouseMoves.forEach(c -> c.onInteraction(lastMousePoint, e));
		}
	}


	private static double getStepSize(Vector2 startToEnd, int numBots)
	{
		var distance = startToEnd.getLength();
		double step = distance / 2;
		if (numBots > 1)
		{
			step = distance / (numBots - 1);
		}
		return step;
	}


	@FunctionalInterface
	public interface FieldRobotMoveInteraction
	{
		void onInteraction(BotID id, IVector2 pos);
	}

	@FunctionalInterface
	public interface FieldMouseInteraction
	{
		void onInteraction(IVector2 pos, MouseEvent e);
	}
}
