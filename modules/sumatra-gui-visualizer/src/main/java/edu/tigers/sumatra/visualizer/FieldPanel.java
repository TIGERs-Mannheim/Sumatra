/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.clock.FpsCounter;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableFieldBackground;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.EFieldTurn;
import edu.tigers.sumatra.drawable.EFontSize;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.ScalingUtil;
import lombok.extern.log4j.Log4j2;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Visualization of the field.
 */
@Log4j2
public class FieldPanel extends JPanel implements IDrawableTool
{
	/**
	 * color of field background
	 */
	private static final Color FIELD_COLOR = new Color(0, 160, 30);
	private static final Color FIELD_COLOR_DARK = new Color(77, 77, 77);

	/**
	 * color of field background
	 */
	private static final Color FIELD_COLOR_REFEREE = new Color(93, 93, 93);

	private transient Image offImage = null;
	private transient Image screenshotImage = null;
	private final transient Object offImageSync = new Object();

	/**
	 *
	 */
	private static final long serialVersionUID = 4330620225157027091L;

	// --- observer ---
	private final transient List<IFieldPanelObserver> observers = new CopyOnWriteArrayList<>();

	// --- field constants / size of the loaded image in pixel ---
	private static final int SCROLL_SPEED = 20;
	private static final int DEF_FIELD_WIDTH = 10000;
	private static final double BORDER_TEXT_NORMALIZED_WIDTH = 750;

	private final int fieldWidth;
	private double fieldGlobalWidth = Geometry.getFieldWidth();
	private double fieldGlobalLength = Geometry.getFieldLength();
	private double fieldGlobalBoundaryWidth = Geometry.getBoundaryWidth();

	private final transient MouseEvents mouseEventsListener = new MouseEvents();

	// --- field scrolling ---
	private double scaleFactor = 1;
	private double fieldOriginY = 0;
	private double fieldOriginX = 0;

	private boolean fancyPainting = false;
	private boolean darkMode = false;

	private final Set<String> showSources = new ConcurrentSkipListSet<>();
	private final Set<String> showCategories = new ConcurrentSkipListSet<>();
	private final transient Map<ShapeMapSource, ShapeMap> shapeMaps = new ConcurrentHashMap<>();
	private final Map<String, Boolean> shapeVisibilityMap = new ConcurrentHashMap<>();

	private EFieldTurn fieldTurn = EFieldTurn.NORMAL;

	private transient IVector2 lastMousePoint = Vector2f.ZERO_VECTOR;
	private transient IVector2 dragPointStart = null;
	private transient IVector2 dragPointEnd = null;
	private final transient FpsCounter fpsCounter = new FpsCounter();

	private String snapshotFilePath = "";
	private boolean takeScreenshot = false;
	private boolean videoIsRecording = false;
	private EMediaOption mediaOption = EMediaOption.CURRENT_SECTION;
	private int snapshotWidthBase = 5000;
	private int snapshotHeightBase = 5000;
	private int snapshotWidth = 5000;
	private int snapshotHeight = 5000;
	private int adjustedWidth = 5000;
	private int adjustedHeight = 5000;

	private VideoExporter videoExporter = null;
	private long recordingAnimation = 0;


	public FieldPanel()
	{
		this.fieldWidth = DEF_FIELD_WIDTH;

		setBorder(new EmptyBorder(0, 0, 0, 0));
		setLayout(new MigLayout("inset 0"));

		String strFieldTurn = SumatraModel.getInstance().getUserProperty(
				FieldPanel.class.getCanonicalName() + ".fieldTurn");
		if (strFieldTurn != null)
		{
			try
			{
				setFieldTurn(EFieldTurn.valueOf(strFieldTurn));
			} catch (IllegalArgumentException err)
			{
				log.error("Could not parse field turn.", err);
			}
		}
	}


	public void start()
	{
		addMouseListener(mouseEventsListener);
		addMouseMotionListener(mouseEventsListener);
		addMouseWheelListener(mouseEventsListener);
	}


	public void stop()
	{
		removeMouseListener(mouseEventsListener);
		removeMouseMotionListener(mouseEventsListener);
		removeMouseWheelListener(mouseEventsListener);
	}


	public void setShapeMap(final ShapeMapSource source, final ShapeMap shapeMap)
	{
		if (shapeMap == null)
		{
			this.shapeMaps.remove(source);
		} else
		{
			this.shapeMaps.put(source, shapeMap);
		}
	}


	@Override
	public void paint(final Graphics g1)
	{
		if (offImage != null)
		{
			synchronized (offImageSync)
			{
				g1.drawImage(offImage, 0, 0, this);
			}
		} else
		{
			g1.clearRect(0, 0, adjustedWidth, adjustedHeight);
		}
	}


	public void paintOffline()
	{
		adjustedWidth = getWidth();
		adjustedHeight = getHeight();

		/*
		 * Drawing only makes sense if we have a valid/existent drawing area because creating an image with size 0 will
		 * produce an error. This scenario is possible if the moduli start up before the GUI layouting has been completed
		 * and the component size is still 0|0.
		 */
		if ((adjustedWidth < 20) || (adjustedHeight < 20))
		{
			return;
		}

		if ((offImage == null) || (offImage.getHeight(this) != adjustedHeight)
				|| (offImage.getWidth(this) != adjustedWidth))
		{
			offImage = createImage(adjustedWidth, adjustedHeight);
			double scale = resetField(adjustedWidth, adjustedHeight);
			setScaleFactor(scale);
			setFieldOriginX(0);
			setFieldOriginY(0);
		}

		synchronized (offImageSync)
		{
			final Graphics2D g2 = (Graphics2D) offImage.getGraphics();
			drawFieldGraphics(g2, this.fieldOriginX, this.fieldOriginY, this.adjustedWidth,
					this.adjustedHeight, this.scaleFactor, EMediaOption.VISUALIZER);
		}

		handleScreenshotAndVideoRecording();
		repaint();
	}


	private void handleScreenshotAndVideoRecording()
	{
		if (takeScreenshot || videoExporter != null)
		{
			try
			{
				// Images can be huge and cause the heap to run out
				screenshotImage = createImage(this.snapshotWidth, this.snapshotHeight);
			} catch (OutOfMemoryError error)
			{
				log.error("Out of Memory, stopping recording of Video", error);
				stopRecordingVideo();
			}
			final Graphics2D g2 = (Graphics2D) screenshotImage.getGraphics();
			switch (mediaOption)
			{
				case FULL_FIELD:
					drawFullFieldScreenshot(g2);
					break;
				case CURRENT_SECTION:
					drawCurrentSectionScreenshot(g2);
					break;
				default:
					break;
			}
			if (takeScreenshot)
			{
				takeScreenshot();
				takeScreenshot = false;
			}
			if (videoExporter != null && videoExporter.isInitialized())
			{
				videoExporter.addImageToVideo(toBufferedImage(screenshotImage));
				if (!videoIsRecording)
				{
					videoExporter.close();
					videoExporter = null;
				}
			}
		}
	}


	private void calculateAndSetScreenshotDimensions()
	{
		int width = this.snapshotWidthBase;
		int height = this.snapshotHeightBase;
		EFieldTurn turn = getFieldTurn(width, height);
		double widthToHeightRatio;
		if (turn == EFieldTurn.NORMAL || turn == EFieldTurn.T180)
		{
			widthToHeightRatio = getFieldTotalHeight() / (double) getFieldTotalWidth();
		} else
		{
			widthToHeightRatio = getFieldTotalWidth() / (double) getFieldTotalHeight();
		}
		if (this.snapshotWidth <= 0 && this.snapshotHeight <= 0)
		{
			log.warn("Invalid screenshot size, taking image with default values");
			width = 1024;
			height = (int) (width * widthToHeightRatio);
		}
		if (this.snapshotWidth <= 0)
		{
			width = (int) (height / widthToHeightRatio);
		}
		if (this.snapshotHeight <= 0)
		{
			height = (int) (width * widthToHeightRatio);
		}

		double borderTextScale = calculateBorderTextScale(mediaOption);
		int refAreaOffset = calculateRefAreaOffset(mediaOption, borderTextScale);
		height += refAreaOffset;

		// numbers have to always be divisible by 2 for media encoding
		this.snapshotWidth = width + (width % 2);
		this.snapshotHeight = height + (height % 2);
	}


	private void drawFullFieldScreenshot(final Graphics2D g2)
	{
		double scale = getFieldScale(this.snapshotWidth, this.snapshotHeight);
		drawFieldGraphics(g2, 0, 0, this.snapshotWidth, this.snapshotHeight, scale,
				EMediaOption.FULL_FIELD);
	}


	private void drawCurrentSectionScreenshot(final Graphics2D g2)
	{
		final Point mPoint = new Point(0, 0);
		final double xLen = ((mPoint.x - fieldOriginX) / scaleFactor) * 2;
		final double yLen = ((mPoint.y - fieldOriginY) / scaleFactor) * 2;
		final double oldLenX = (xLen) * scaleFactor;
		final double oldLenY = (yLen) * scaleFactor;
		double normalizedScale = scaleFactor * Math
				.min(this.snapshotWidth / (double) getWidth(), this.snapshotHeight / (double) getHeight());
		final double newLenX = (xLen) * normalizedScale;
		final double newLenY = (yLen) * normalizedScale;
		double orgX = (fieldOriginX - ((newLenX - oldLenX) / 2.0));
		double orgY = (fieldOriginY - ((newLenY - oldLenY) / 2.0));
		drawFieldGraphics(g2, orgX, orgY, this.snapshotWidth, this.snapshotHeight,
				normalizedScale, EMediaOption.CURRENT_SECTION);
	}


	private void drawFieldGraphics(final Graphics2D g2,
			double offsetX, double offsetY,
			int width, int height, double scale,
			EMediaOption mediaOption)
	{
		final BasicStroke defaultStroke = new BasicStroke(Math.max(1, scaleYLength(10)));
		g2.setColor(FIELD_COLOR_REFEREE);
		g2.fillRect(0, 0, width, height);

		double borderTextScale = calculateBorderTextScale(mediaOption);
		int refAreaOffset = calculateRefAreaOffset(mediaOption, borderTextScale);

		g2.translate(offsetX, offsetY + refAreaOffset);
		g2.scale(scale, scale);

		EFieldTurn oldTurn = getFieldTurn();
		if (mediaOption != EMediaOption.VISUALIZER)
		{
			setFieldTurn(getFieldTurn(width, height));
		}

		if (fancyPainting)
		{
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}

		shapeMaps.values().stream()
				.flatMap(m -> m.getAllShapeLayers().stream())
				.flatMap(l -> l.getShapes().stream())
				.filter(s -> s.getClass().equals(DrawableFieldBackground.class))
				.findAny()
				.map(DrawableFieldBackground.class::cast)
				.ifPresent(s -> {
					fieldGlobalBoundaryWidth = s.getBoundaryWidth();
					fieldGlobalLength = s.getFieldWithBorder().xExtent() - 2 * fieldGlobalBoundaryWidth;
					fieldGlobalWidth = s.getFieldWithBorder().yExtent() - 2 * fieldGlobalBoundaryWidth;
				});

		shapeMaps.entrySet().stream()
				.filter(s -> showSources.contains(s.getKey().getName()))
				.filter(s -> showCategories.containsAll(s.getKey().getCategories()))
				.map(Map.Entry::getValue)
				.map(ShapeMap::getAllShapeLayers)
				.flatMap(Collection::stream)
				.sorted()
				.filter(e -> shapeVisibilityMap.getOrDefault(e.getIdentifier().getId(), true))
				.forEach(shapeLayer -> paintShapeMap(g2, shapeLayer, defaultStroke));

		paintDragPoints(g2);

		g2.scale(1.0 / scale, 1.0 / scale);
		g2.translate(-offsetX, -offsetY - refAreaOffset);

		if (mediaOption != EMediaOption.VISUALIZER)
		{
			g2.setColor(FIELD_COLOR_REFEREE);
			g2.fillRect(0, 0, width, refAreaOffset);
		}

		g2.scale(borderTextScale, borderTextScale);

		shapeMaps.entrySet().stream()
				.filter(s -> showSources.contains(s.getKey().getName()))
				.filter(s -> showCategories.containsAll(s.getKey().getCategories()))
				.map(Map.Entry::getValue)
				.map(ShapeMap::getAllShapeLayers)
				.flatMap(Collection::stream)
				.sorted()
				.filter(e -> shapeVisibilityMap.getOrDefault(e.getIdentifier().getId(), true))
				.forEach(shapeLayer -> paintShapeMapBorderText(g2, shapeLayer, defaultStroke));

		g2.scale(1 / borderTextScale, 1 / borderTextScale);

		if (mediaOption == EMediaOption.VISUALIZER)
		{
			paintCoordinates(g2, ETeamColor.YELLOW, width, height, Geometry.getNegativeHalfTeam() != ETeamColor.YELLOW);
			paintCoordinates(g2, ETeamColor.BLUE, width, height, Geometry.getNegativeHalfTeam() != ETeamColor.BLUE);
			paintCoordinates(g2, ETeamColor.NEUTRAL, width, height, false);

			if (videoExporter != null)
			{
				g2.setColor(Color.red);
				int recordingRadius = 15 + (int) ((Math.sin(recordingAnimation / 7.0)) * 10);
				int recX = getWidth() - 50 - recordingRadius / 2;
				int recY = 15 - recordingRadius / 2;
				g2.fillOval(recX, recY, recordingRadius, recordingRadius);

				final BasicStroke newStroke = new BasicStroke(Math.max(1, scaleYLength(15)));

				g2.setStroke(newStroke);
				g2.drawString("REC", getWidth() - 50 + 17, 15 + 4);
				g2.setStroke(defaultStroke);
				recordingAnimation++;
			} else
			{
				paintFps(g2, width);
			}
		}
		setFieldTurn(oldTurn);
	}


	private int calculateRefAreaOffset(final EMediaOption mediaOption, final double borderTextScale)
	{
		return mediaOption == EMediaOption.VISUALIZER ? 0 : ((int) (70 * borderTextScale));
	}


	private double calculateBorderTextScale(final EMediaOption mediaOption)
	{
		int relevantViewWidth = mediaOption == EMediaOption.VISUALIZER ? getWidth() : this.snapshotWidth;
		return relevantViewWidth / BORDER_TEXT_NORMALIZED_WIDTH;
	}


	private void takeScreenshot()
	{
		try
		{
			String path = snapshotFilePath;
			if (!snapshotFilePath.endsWith(".png"))
			{
				path += ".png";
			}
			ImageIO.write((BufferedImage) screenshotImage, "png", new File(path));
			log.info("Finished saving screenshot to: " + path);
		} catch (IOException e)
		{
			log.warn("Could not take Screenshot", e);
		}
	}


	private void paintShapeMapBorderText(Graphics2D g, ShapeMap.ShapeLayer shapeLayer, BasicStroke defaultStroke)
	{
		final Graphics2D gDerived = (Graphics2D) g.create();
		gDerived.setStroke(defaultStroke);
		shapeLayer.getShapes().stream()
				.filter(IDrawableShape::isBorderText)
				.forEach(s -> s.paintShape(gDerived, this, shapeLayer.isInverted()));
		gDerived.dispose();
	}


	private void paintShapeMap(Graphics2D g, ShapeMap.ShapeLayer shapeLayer, BasicStroke defaultStroke)
	{
		final Graphics2D gDerived = (Graphics2D) g.create();
		gDerived.setStroke(defaultStroke);
		shapeLayer.getShapes().stream()
				.filter(e -> !e.isBorderText())
				.forEach(s -> s.paintShape(gDerived, this, shapeLayer.isInverted()));
		gDerived.dispose();
	}


	private void paintDragPoints(Graphics2D g)
	{
		IVector2 start = dragPointStart;
		IVector2 end = dragPointEnd;
		if (start != null && end != null)
		{
			IVector2 start2End = end.subtractNew(start);
			new DrawableLine(Line.fromPoints(start, end)).paintShape(g, this, false);
			new DrawableAnnotation(start, String.format("%.1f/%.1f", start.x(), start.y())).paintShape(g, this, false);
			new DrawableAnnotation(end, String.format("%.1f/%.1f", end.x(), end.y())).paintShape(g, this, false);
			new DrawableAnnotation(start.addNew(start2End.multiplyNew(0.5)), start2End.toString())
					.paintShape(g, this, false);
		}
	}


	private void paintCoordinates(final Graphics2D g, final ETeamColor teamColor,
			final int width, final int height, final boolean inverted)
	{
		int fontSize = ScalingUtil.getFontSize(EFontSize.SMALL);

		g.setStroke(new BasicStroke());
		g.setFont(new Font("", Font.PLAIN, fontSize));


		int inv = inverted ? -1 : 1;

		g.setColor(
				teamColor == ETeamColor.YELLOW ? Color.YELLOW : teamColor == ETeamColor.BLUE ? Color.BLUE : Color.WHITE);

		int x;
		int y = height - (int) (fontSize * 1.5);
		if (teamColor == ETeamColor.YELLOW)
		{
			x = 10;
		} else if (teamColor == ETeamColor.BLUE)
		{
			x = width - (int) (fontSize * 5.0);
		} else
		{
			x = width / 2 - (int) (fontSize * 5.0);
		}

		g.drawString(
				String.format("x:%5d", inv * (int) lastMousePoint.x()),
				x, y);
		g.drawString(
				String.format("y:%5d", inv * (int) lastMousePoint.y()),
				x, y + fontSize + 1);
	}


	private void paintFps(final Graphics2D g, final int width)
	{
		fpsCounter.newFrame(System.nanoTime());
		int fontSize = ScalingUtil.getFontSize(EFontSize.SMALL);
		g.setFont(new Font("", Font.PLAIN, fontSize));
		g.setColor(Color.black);

		int x = width - fontSize * 3;
		int y = 20;
		g.drawString(String.format("%.1f", fpsCounter.getAvgFps()), x, y);
	}


	public void clearField()
	{
		offImage = null;
		shapeMaps.clear();
	}


	@Override
	public void turnField(final EFieldTurn fieldTurn, final double angle, final Graphics2D g2)
	{
		double translateSize = ((double) getFieldHeight() - fieldWidth) / 2.0;
		if (angle > 0)
		{
			switch (fieldTurn)
			{
				case T270:
					g2.translate(translateSize, translateSize);
					break;
				case T90:
					g2.translate(-translateSize, -translateSize);
					break;
				default:
					break;
			}
		}

		long numRotations = Math.round(fieldTurn.getAngle() / AngleMath.PI_HALF);
		g2.rotate(numRotations * angle, getFieldTotalWidth() / 2.0, getFieldTotalHeight() / 2.0);

		if (angle < 0)
		{
			switch (fieldTurn)
			{
				case T270:
					g2.translate(-translateSize, -translateSize);
					break;
				case T90:
					g2.translate(translateSize, translateSize);
					break;
				default:
					break;
			}
		}
	}


	private IVector2 turnGuiPoint(final EFieldTurn fieldTurn, final IVector2 point)
	{
		switch (fieldTurn)
		{
			case NORMAL:
				return point;
			case T90:
				return Vector2.fromXY(point.y(), getFieldTotalWidth() - point.x());
			case T180:
				return Vector2.fromXY(-point.x() + getFieldTotalWidth(), -point.y() + getFieldTotalHeight());
			case T270:
				return Vector2.fromXY(-point.y() + getFieldTotalHeight(), point.x());
			default:
				throw new IllegalStateException();
		}
	}


	/**
	 * Transforms a global(field)position into a gui position.
	 *
	 * @param globalPosition
	 * @return guiPosition
	 */
	private IVector2 transformToGuiCoordinates(final IVector2 globalPosition)
	{
		final double yScaleFactor = fieldWidth / fieldGlobalWidth;
		final double xScaleFactor = getFieldHeight() / fieldGlobalLength;

		final IVector2 transPosition = globalPosition.addNew(Vector2.fromXY(fieldGlobalLength / 2,
				fieldGlobalWidth / 2.0));

		double y = transPosition.x() * xScaleFactor + fieldGlobalBoundaryWidth * xScaleFactor;
		double x = transPosition.y() * yScaleFactor + fieldGlobalBoundaryWidth * yScaleFactor;

		return turnGuiPoint(fieldTurn, Vector2.fromXY(x, y));
	}


	@Override
	public IVector2 transformToGuiCoordinates(final IVector2 globalPosition, final boolean invert)
	{
		int r = 1;
		if (invert)
		{
			r = -1;
		}
		return transformToGuiCoordinates(Vector2.fromXY(r * globalPosition.x(), r * globalPosition.y()));
	}


	@Override
	public int scaleXLength(final double length)
	{
		final double xScaleFactor = getFieldHeight() / fieldGlobalLength;
		return (int) (length * xScaleFactor);
	}


	@Override
	public int scaleYLength(final double length)
	{
		final double yScaleFactor = fieldWidth / fieldGlobalWidth;
		return (int) (length * yScaleFactor);
	}


	public void addObserver(final IFieldPanelObserver o)
	{
		observers.add(o);
	}


	public void removeObserver(final IFieldPanelObserver o)
	{
		observers.remove(o);
	}


	/**
	 * @param scaleFactor the scaleFactor to set
	 */
	private void setScaleFactor(final double scaleFactor)
	{
		this.scaleFactor = scaleFactor;
	}


	public void setPanelVisible(final boolean visible)
	{
		setVisible(visible);
	}


	private int getFieldTotalWidth()
	{
		final double yScaleFactor = getFieldWidth() / fieldGlobalWidth;
		return getFieldWidth() + (int) ((2 * fieldGlobalBoundaryWidth) * yScaleFactor);
	}


	private int getFieldTotalHeight()
	{
		final double xScaleFactor = getFieldHeight() / fieldGlobalLength;
		return getFieldHeight() + (int) ((2 * fieldGlobalBoundaryWidth) * xScaleFactor);
	}


	private double getFieldRatio()
	{
		return fieldGlobalLength / fieldGlobalWidth;
	}


	@Override
	public final int getFieldHeight()
	{
		return (int) Math.round(getFieldRatio() * fieldWidth);
	}


	@Override
	public final int getFieldWidth()
	{
		return fieldWidth;
	}


	public void setShapeLayerVisibility(final String layerId, final boolean visible)
	{
		shapeVisibilityMap.put(layerId, visible);
	}


	public void onOptionChanged(final EVisualizerOptions option, final boolean isSelected)
	{
		switch (option)
		{
			case FANCY:
				fancyPainting = isSelected;
				break;
			case DARK:
				darkMode = isSelected;
				break;
			case TURN_NEXT:
				turnNext();
				break;
			case RESET_FIELD:
				double scale = resetField(adjustedWidth, adjustedHeight);
				setScaleFactor(scale);
				setFieldOriginX(0);
				setFieldOriginY(0);
				break;
			default:
				break;
		}
	}


	public void setSourceVisibility(final String source, final boolean visible)
	{
		if (visible)
		{
			showSources.add(source);
		} else
		{
			showSources.remove(source);
		}
	}


	public void setSourceCategoryVisibility(final String category, final boolean visible)
	{
		if (visible)
		{
			showCategories.add(category);
		} else
		{
			showCategories.remove(category);
		}
	}


	private void turnNext()
	{
		switch (fieldTurn)
		{
			case NORMAL:
				setFieldTurn(EFieldTurn.T90);
				break;
			case T90:
				setFieldTurn(EFieldTurn.T180);
				break;
			case T180:
				setFieldTurn(EFieldTurn.T270);
				break;
			case T270:
				setFieldTurn(EFieldTurn.NORMAL);
				break;
		}
		fieldOriginX = 0;
		fieldOriginY = 0;
	}


	private double getFieldScale(int width, int height)
	{
		double heightScaleFactor;
		double widthScaleFactor;
		if (width > height)
		{
			heightScaleFactor = (double) height / getFieldTotalWidth();
			widthScaleFactor = (double) width / getFieldTotalHeight();
		} else
		{
			heightScaleFactor = ((double) height) / getFieldTotalHeight();
			widthScaleFactor = ((double) width) / getFieldTotalWidth();
		}
		return Math.min(heightScaleFactor, widthScaleFactor);
	}


	private EFieldTurn getFieldTurn(int width, int height)
	{
		if (width > height)
		{
			return EFieldTurn.T90;
		}
		return EFieldTurn.NORMAL;
	}


	private double resetField(int width, int height)
	{
		setFieldTurn(getFieldTurn(width, height));
		return getFieldScale(width, height);
	}


	@Override
	public final EFieldTurn getFieldTurn()
	{
		return fieldTurn;
	}


	private void setFieldTurn(final EFieldTurn fieldTurn)
	{
		this.fieldTurn = fieldTurn;
	}


	/**
	 * @param fieldOriginY the fieldOriginY to set
	 */
	private void setFieldOriginY(final double fieldOriginY)
	{
		this.fieldOriginY = fieldOriginY;
	}


	/**
	 * @param fieldOriginX the fieldOriginX to set
	 */
	private void setFieldOriginX(final double fieldOriginX)
	{
		this.fieldOriginX = fieldOriginX;
	}


	public boolean startRecordingVideo(final String filename)
	{
		videoExporter = new VideoExporter();
		try
		{
			if (!videoExporter.open(filename, this.snapshotWidth, this.snapshotHeight))
			{
				return false;
			}
		} catch (Exception e)
		{
			log.error("Could not record video", e);
			return false;
		}
		videoIsRecording = true;
		return true;
	}


	public void stopRecordingVideo()
	{
		videoIsRecording = false;
	}


	public void setMediaParameters(final int w, final int h, final EMediaOption mediaOption)
	{
		this.snapshotWidthBase = w;
		this.snapshotHeightBase = h;
		this.snapshotWidth = w;
		this.snapshotHeight = h;
		this.mediaOption = mediaOption;
		if (getFieldTurn() == EFieldTurn.NORMAL && mediaOption == EMediaOption.CURRENT_SECTION)
		{
			this.snapshotWidthBase = h;
			this.snapshotHeightBase = w;
			this.snapshotWidth = h;
			this.snapshotHeight = w;
		}
		calculateAndSetScreenshotDimensions();
	}


	protected class MouseEvents extends MouseAdapter
	{
		private static final double SCROLL_FACTOR = 250.0;
		private int mousePressedY = 0;
		private int mousePressedX = 0;


		@Override
		public void mouseClicked(final MouseEvent e)
		{
			// take care of fieldOriginY
			IVector2 guiPos = Vector2.fromXY((e.getX()) - fieldOriginX, (e.getY()) - fieldOriginY)
					.multiply(1f / scaleFactor);

			observers.forEach(observer -> observer.onFieldClick(transformToGlobalCoordinates(guiPos), e));
		}


		@Override
		public void mousePressed(final MouseEvent e)
		{
			mousePressedY = e.getY();
			mousePressedX = e.getX();
			IVector2 guiPos = Vector2.fromXY((e.getX()) - fieldOriginX, (e.getY()) - fieldOriginY)
					.multiply(1f / scaleFactor);
			dragPointStart = transformToGlobalCoordinates(guiPos);
		}


		@Override
		public void mouseDragged(final MouseEvent e)
		{
			if (SwingUtilities.isLeftMouseButton(e) && (e.isControlDown() || e.isAltDown()))
			{
				IVector2 guiPos = Vector2.fromXY((e.getX()) - fieldOriginX, (e.getY()) - fieldOriginY)
						.multiply(1f / scaleFactor);
				dragPointEnd = transformToGlobalCoordinates(guiPos);
			} else
			{
				// --- move the field over the panel ---
				final int dy = e.getY() - mousePressedY;
				final int dx = e.getX() - mousePressedX;
				setFieldOriginY(fieldOriginY + dy);
				setFieldOriginX(fieldOriginX + dx);
				mousePressedY += dy;
				mousePressedX += dx;
			}
		}


		@Override
		public void mouseReleased(final MouseEvent e)
		{
			super.mouseReleased(e);

			if (dragPointStart != null && dragPointEnd != null)
			{
				dragPointStart = null;
				dragPointEnd = null;
			}
		}


		@Override
		public void mouseWheelMoved(final MouseWheelEvent e)
		{
			final int rot = -e.getWheelRotation();
			final int scroll = SCROLL_SPEED * rot;

			final Point mPoint = e.getPoint();
			final double xLen = ((mPoint.x - fieldOriginX) / scaleFactor) * 2;
			final double yLen = ((mPoint.y - fieldOriginY) / scaleFactor) * 2;

			final double oldLenX = (xLen) * scaleFactor;
			final double oldLenY = (yLen) * scaleFactor;
			setScaleFactor(scaleFactor * (1 + (scroll / SCROLL_FACTOR)));
			final double newLenX = (xLen) * scaleFactor;
			final double newLenY = (yLen) * scaleFactor;
			setFieldOriginX(fieldOriginX - ((newLenX - oldLenX) / 2.0));
			setFieldOriginY(fieldOriginY - ((newLenY - oldLenY) / 2.0));
		}


		@Override
		public void mouseMoved(final MouseEvent e)
		{
			// take care of fieldOriginY
			IVector2 guiPos = Vector2.fromXY((e.getX()) - fieldOriginX, (e.getY()) - fieldOriginY)
					.multiply(1f / scaleFactor);

			// --- notify observer ---
			for (final IFieldPanelObserver observer : observers)
			{
				observer.onMouseMoved(transformToGlobalCoordinates(guiPos), e);
			}

			lastMousePoint = transformToGlobalCoordinates(guiPos);
		}


		/**
		 * Transforms a gui position into a global(field)position.
		 *
		 * @param guiPosition
		 * @return globalPosition
		 */
		private IVector2 transformToGlobalCoordinates(final IVector2 guiPosition)
		{
			IVector2 guiPosTurned = turnGlobalPoint(fieldTurn, guiPosition);

			final double xScaleFactor = fieldGlobalWidth / fieldWidth;
			final double yScaleFactor = fieldGlobalLength / getFieldHeight();

			final IVector2 transPosition = guiPosTurned.subtractNew(
					Vector2.fromXY(fieldGlobalBoundaryWidth / xScaleFactor, fieldGlobalBoundaryWidth / yScaleFactor));

			double x = (transPosition.y() * yScaleFactor) - fieldGlobalLength / 2.0;
			double y = (transPosition.x() * xScaleFactor) - fieldGlobalWidth / 2.0;

			return Vector2.fromXY(x, y);
		}


		private IVector2 turnGlobalPoint(final EFieldTurn fieldTurn, final IVector2 point)
		{
			switch (fieldTurn)
			{
				case NORMAL:
					return point;
				case T90:
					return Vector2.fromXY(getFieldTotalWidth() - point.y(), point.x());
				case T180:
					return Vector2.fromXY(-point.x() + getFieldTotalWidth(), -point.y() + getFieldTotalHeight());
				case T270:
					return Vector2.fromXY(point.y(), -point.x() + getFieldTotalHeight());
				default:
					throw new IllegalStateException();
			}
		}
	}


	private BufferedImage toBufferedImage(Image img)
	{
		if (img instanceof BufferedImage)
		{
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}


	public void saveScreenshot(String path)
	{
		this.snapshotFilePath = path;
		this.takeScreenshot = true;
	}


	@Override
	public int getFieldMargin()
	{
		final double yScaleFactor = fieldWidth / fieldGlobalWidth;
		return (int) (fieldGlobalBoundaryWidth * yScaleFactor);
	}


	@Override
	public Color getFieldColor()
	{
		return darkMode ? FIELD_COLOR_DARK : FIELD_COLOR;
	}
}
