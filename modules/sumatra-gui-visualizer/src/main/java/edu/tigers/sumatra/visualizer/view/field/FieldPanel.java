/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.view.field;

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
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.clock.FpsCounter;
import edu.tigers.sumatra.drawable.EFieldTurn;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.visualizer.view.EVisualizerOptions;
import edu.tigers.sumatra.visualizer.view.IFieldPanelObserver;
import net.miginfocom.swing.MigLayout;


/**
 * Visualization of the field.
 * 
 * @author Oliver Steinbrecher
 */
public class FieldPanel extends JPanel implements IFieldPanel
{
	private static final Logger log = Logger
			.getLogger(FieldPanel.class.getName());
	
	/** color of field background */
	public static final Color FIELD_COLOR = new Color(0, 180, 30);
	/** color of field background */
	public static final Color FIELD_COLOR_REFEREE = new Color(0, 150, 30);
	
	// --- repaint ---
	private transient Image offImage = null;
	private final transient Object offImageSync = new Object();
	
	/**  */
	private static final long serialVersionUID = 4330620225157027091L;
	
	// --- observer ---
	private final transient List<IFieldPanelObserver> observers = new CopyOnWriteArrayList<>();
	
	// --- field constants / size of the loaded image in pixel ---
	/** */
	public static final int FIELD_MARGIN = 35;
	private static final int SCROLL_SPEED = 20;
	private static final int DEF_FIELD_WIDTH = 10000;
	
	
	private final int fieldWidth;
	
	private final transient MouseEvents mouseEventsListener = new MouseEvents();
	
	// --- field scrolling ---
	private double scaleFactor = 1;
	private double fieldOriginY = 0;
	private double fieldOriginX = 0;
	
	
	private boolean fancyPainting = false;
	private boolean doPaintCoordinates = true;
	
	private final Set<EShapeLayerSource> showSources = EnumSet
			.allOf(
					EShapeLayerSource.class);
	private final Map<EShapeLayerSource, Boolean> teamInverted = new EnumMap<>(
			EShapeLayerSource.class);
	private final transient Map<EShapeLayerSource, ShapeMap> shapeMap = new EnumMap<>(
			EShapeLayerSource.class);
	private final Map<String, Boolean> shapeVisibilityMap = new HashMap<>();
	
	private EFieldTurn fieldTurn = EFieldTurn.NORMAL;
	
	private transient IVector2 lastMousePoint = Vector2.zero();
	private final transient FpsCounter fpsCounter = new FpsCounter();
	
	
	/**
	 * Default
	 */
	public FieldPanel()
	{
		this(DEF_FIELD_WIDTH);
	}
	
	
	/**
	 * Construct a new FieldPanel
	 * The {@code feldWidth} parameter controls how large the internal field will be in pixels. If it is set to a high
	 * value the rendering will take more time but will look less pixelated on large screens.
	 * 
	 * @param fieldWidth The width of the painted field in pixel
	 */
	public FieldPanel(final int fieldWidth)
	{
		this.fieldWidth = fieldWidth;
		
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
		
		String strScaleFactor = SumatraModel.getInstance().getUserProperty(
				FieldPanel.class.getCanonicalName() + ".scaleFactor");
		if (strScaleFactor != null)
		{
			scaleFactor = Double.valueOf(SumatraModel.getInstance().getUserProperty(
					FieldPanel.class.getCanonicalName() + ".scaleFactor"));
		}
		
		String strFieldOriginX = SumatraModel.getInstance().getUserProperty(
				FieldPanel.class.getCanonicalName() + ".fieldOriginX");
		if (strFieldOriginX != null)
		{
			fieldOriginX = Double.valueOf(SumatraModel.getInstance().getUserProperty(
					FieldPanel.class.getCanonicalName() + ".fieldOriginX"));
		}
		
		String strFieldOriginY = SumatraModel.getInstance().getUserProperty(
				FieldPanel.class.getCanonicalName() + ".fieldOriginY");
		if (strFieldOriginY != null)
		{
			fieldOriginY = Double.valueOf(SumatraModel.getInstance().getUserProperty(
					FieldPanel.class.getCanonicalName() + ".fieldOriginY"));
		}
	}
	
	
	/**
	 * 
	 */
	@Override
	public void start()
	{
		addMouseListener(mouseEventsListener);
		addMouseMotionListener(mouseEventsListener);
		addMouseWheelListener(mouseEventsListener);
	}
	
	
	/**
	 * 
	 */
	@Override
	public void stop()
	{
		removeMouseListener(mouseEventsListener);
		removeMouseMotionListener(mouseEventsListener);
		removeMouseWheelListener(mouseEventsListener);
	}
	
	
	@Override
	public void setShapeMap(final EShapeLayerSource source,
			final ShapeMap shapeMap,
			final boolean inverted)
	{
		this.shapeMap.put(source, new ShapeMap(shapeMap));
		teamInverted.put(source, inverted);
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
			g1.clearRect(0, 0, getWidth(), getHeight());
		}
	}
	
	
	@Override
	public void paintOffline()
	{
		/*
		 * Drawing only makes sense if we have a valid/existent drawing area because creating an image with size 0 will
		 * produce an error. This scenario is possible if the moduli start up before the GUI layouting has been completed
		 * and the component size is still 0|0.
		 */
		if ((getWidth() == 0) || (getHeight() == 0))
		{
			return;
		}
		
		if ((offImage == null) || (offImage.getHeight(this) != getHeight()) || (offImage.getWidth(this) != getWidth()))
		{
			offImage = createImage(getWidth(), getHeight());
			resetField();
		}
		
		synchronized (offImageSync)
		{
			final Graphics2D g2 = (Graphics2D) offImage.getGraphics();
			g2.setColor(FIELD_COLOR_REFEREE);
			g2.fillRect(0, 0, getWidth(), getHeight());
			
			
			g2.translate(fieldOriginX, fieldOriginY);
			g2.scale(scaleFactor, scaleFactor);
			
			turnField(getFieldTurn(), -AngleMath.PI_HALF, g2);
			g2.setColor(FIELD_COLOR);
			g2.fillRect(0, 0, getFieldTotalWidth(), getFieldTotalHeight());
			turnField(getFieldTurn(), AngleMath.PI_HALF, g2);
			
			if (fancyPainting)
			{
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			}
			
			ShapeMap sm = new ShapeMap();
			for (EShapeLayerSource src : showSources)
			{
				boolean invert = teamInverted.getOrDefault(src, false);
				ShapeMap shapeLayerMap = shapeMap.get(src);
				if (shapeLayerMap == null)
				{
					continue;
				}
				shapeLayerMap.setInverted(invert);
				sm.merge(shapeLayerMap);
			}
			
			shapeVisibilityMap.entrySet().stream().filter(e -> !e.getValue()).forEach(e -> sm.remove(e.getKey()));
			
			sm.paint(g2, this, new BasicStroke(Math.max(1, scaleYLength(10))));
			
			g2.scale(1.0 / scaleFactor, 1.0 / scaleFactor);
			g2.translate(-fieldOriginX, -fieldOriginY);
			
			if (doPaintCoordinates)
			{
				paintCoordinates(g2, ETeamColor.YELLOW,
						teamInverted.getOrDefault(ETeamColor.YELLOW, TeamConfig.getLeftTeam() != ETeamColor.YELLOW));
				paintCoordinates(g2, ETeamColor.BLUE,
						teamInverted.getOrDefault(ETeamColor.BLUE, TeamConfig.getLeftTeam() != ETeamColor.BLUE));
			}
			paintFps(g2);
		}
		repaint();
	}
	
	
	private void paintCoordinates(final Graphics2D g, final ETeamColor teamColor, final boolean inverted)
	{
		g.setStroke(new BasicStroke());
		g.setFont(new Font("", Font.PLAIN, 10));
		
		
		int inv = inverted ? -1 : 1;
		
		g.setColor(teamColor == ETeamColor.YELLOW ? Color.YELLOW : Color.BLUE);
		
		int x;
		int y = getHeight() - 18;
		if (teamColor == ETeamColor.YELLOW)
		{
			x = 10;
		} else
		{
			x = getWidth() - 60;
		}
		char tColor = teamColor == ETeamColor.YELLOW ? 'Y' : 'B';
		g.drawString(
				String.format("%c x:%5d", tColor, inv * (int) lastMousePoint.x()),
				x, y);
		g.drawString(
				String.format("   y:%5d", inv * (int) lastMousePoint.y()),
				x, y + 11);
	}
	
	
	private void paintFps(final Graphics2D g)
	{
		fpsCounter.newFrame(System.nanoTime());
		g.setFont(new Font("", Font.PLAIN, 12));
		g.setColor(Color.black);
		
		int x = getWidth() - 40;
		int y = 20;
		g.drawString(String.format("%.1f", fpsCounter.getAvgFps()), x, y);
	}
	
	
	/**
	 */
	@Override
	public void clearField()
	{
		offImage = null;
		shapeMap.clear();
	}
	
	
	/**
	 * @param source
	 */
	@Override
	public void clearField(final EShapeLayerSource source)
	{
		shapeMap.remove(source);
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
		switch (fieldTurn)
		{
			case T270:
				g2.rotate(angle, getFieldTotalWidth() / 2.0, getFieldTotalHeight() / 2.0);
			case T180:
				g2.rotate(angle, getFieldTotalWidth() / 2.0, getFieldTotalHeight() / 2.0);
			case T90:
				g2.rotate(angle, getFieldTotalWidth() / 2.0, getFieldTotalHeight() / 2.0);
			case NORMAL:
				break;
			default:
				throw new IllegalStateException();
		}
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
	
	
	@Override
	public IVector2 transformToGuiCoordinates(final IVector2 globalPosition)
	{
		final double yScaleFactor = fieldWidth / Geometry.getFieldWidth();
		final double xScaleFactor = getFieldHeight() / Geometry.getFieldLength();
		
		final IVector2 transPosition = globalPosition.addNew(Vector2.fromXY(Geometry.getFieldLength() / 2,
				Geometry.getFieldWidth() / 2.0));
		
		double x = transPosition.x() * xScaleFactor;
		double y = transPosition.y() * yScaleFactor;
		
		x += Geometry.getBoundaryLength() * xScaleFactor;
		y += Geometry.getBoundaryWidth() * yScaleFactor;
		
		return turnGuiPoint(fieldTurn, Vector2.fromXY(y, x));
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
	public IVector2 transformToGlobalCoordinates(final IVector2 guiPosition)
	{
		IVector2 guiPosTurned = turnGlobalPoint(fieldTurn, guiPosition);
		
		final double xScaleFactor = Geometry.getFieldWidth() / fieldWidth;
		final double yScaleFactor = Geometry.getFieldLength() / getFieldHeight();
		
		final IVector2 transPosition = guiPosTurned.subtractNew(
				Vector2.fromXY(Geometry.getBoundaryLength() / xScaleFactor, Geometry.getBoundaryWidth() / yScaleFactor));
		
		int x = (int) (transPosition.x() * xScaleFactor);
		int y = (int) (transPosition.y() * yScaleFactor);
		
		y -= Geometry.getFieldLength() / 2.0;
		x -= Geometry.getFieldWidth() / 2.0;
		
		return Vector2.fromXY(y, x);
	}
	
	
	@Override
	public IVector2 transformToGlobalCoordinates(final IVector2 globalPosition, final boolean invert)
	{
		int r = 1;
		if (invert)
		{
			r = -1;
		}
		return transformToGlobalCoordinates(Vector2.fromXY(r * globalPosition.x(), r * globalPosition.y()));
	}
	
	
	@Override
	public int scaleXLength(final double length)
	{
		final double xScaleFactor = getFieldHeight() / Geometry.getFieldLength();
		return (int) (length * xScaleFactor);
	}
	
	
	@Override
	public int scaleYLength(final double length)
	{
		final double yScaleFactor = fieldWidth / Geometry.getFieldWidth();
		return (int) (length * yScaleFactor);
	}
	
	
	@Override
	public void addObserver(final IFieldPanelObserver o)
	{
		observers.add(o);
	}
	
	
	@Override
	public void removeObserver(final IFieldPanelObserver o)
	{
		observers.remove(o);
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
			
			// --- notify observer ---
			for (final IFieldPanelObserver observer : observers)
			{
				observer.onFieldClick(transformToGlobalCoordinates(guiPos), e);
			}
		}
		
		
		@Override
		public void mousePressed(final MouseEvent e)
		{
			mousePressedY = e.getY();
			mousePressedX = e.getX();
		}
		
		
		@Override
		public void mouseDragged(final MouseEvent e)
		{
			// --- move the field over the panel ---
			final int dy = e.getY() - mousePressedY;
			final int dx = e.getX() - mousePressedX;
			setFieldOriginY(fieldOriginY + dy);
			setFieldOriginX(fieldOriginX + dx);
			mousePressedY += dy;
			mousePressedX += dx;
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
	}
	
	
	// --------------------------------------------------------------------------
	// --- Presenter Interface --------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void setPanelVisible(final boolean visible)
	{
		setVisible(visible);
	}
	
	
	/**
	 * width with margins
	 * 
	 * @return
	 */
	@Override
	public int getFieldTotalWidth()
	{
		final double yScaleFactor = getFieldWidth() / Geometry.getFieldWidth();
		return getFieldWidth() + (int) ((2 * Geometry.getBoundaryWidth()) * yScaleFactor);
	}
	
	
	/**
	 * height width margins
	 * 
	 * @return
	 */
	@Override
	public int getFieldTotalHeight()
	{
		final double xScaleFactor = getFieldHeight() / Geometry.getFieldLength();
		return getFieldHeight() + (int) ((2 * Geometry.getBoundaryLength()) * xScaleFactor);
	}
	
	
	private double getFieldRatio()
	{
		return Geometry.getFieldLength() / Geometry.getFieldWidth();
	}
	
	
	/**
	 * @return
	 */
	@Override
	public final int getFieldHeight()
	{
		return (int) Math.round(getFieldRatio() * fieldWidth);
	}
	
	
	/**
	 * @return
	 */
	@Override
	public final int getFieldWidth()
	{
		return fieldWidth;
	}
	
	
	@Override
	public void setLayerVisiblility(final String layerId, final boolean visible)
	{
		shapeVisibilityMap.put(layerId, visible);
	}
	
	
	@Override
	public void onOptionChanged(final EVisualizerOptions option, final boolean isSelected)
	{
		switch (option)
		{
			case FANCY:
				fancyPainting = isSelected;
				break;
			case TURN_NEXT:
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
				break;
			case RESET_FIELD:
				resetField();
				break;
			case BLUE_AI_PRIMARY:
				if (isSelected)
				{
					showSources.add(EShapeLayerSource.AI_BLUE_PRIMARY);
				} else
				{
					showSources.remove(EShapeLayerSource.AI_BLUE_PRIMARY);
				}
				break;
			case YELLOW_AI_PRIMARY:
				if (isSelected)
				{
					showSources.add(EShapeLayerSource.AI_YELLOW_PRIMARY);
				} else
				{
					showSources.remove(EShapeLayerSource.AI_YELLOW_PRIMARY);
				}
				break;
			case YELLOW_AI_SECONDARY:
				if (isSelected)
				{
					showSources.add(EShapeLayerSource.AI_YELLOW_SECONDARY);
				} else
				{
					showSources.remove(EShapeLayerSource.AI_YELLOW_SECONDARY);
				}
				break;
			case BLUE_AI_SECONDARY:
				if (isSelected)
				{
					showSources.add(EShapeLayerSource.AI_BLUE_SECONDARY);
				} else
				{
					showSources.remove(EShapeLayerSource.AI_BLUE_SECONDARY);
				}
				break;
			case PAINT_COORD:
				doPaintCoordinates = isSelected;
				break;
			default:
				break;
		}
	}
	
	
	private void resetField()
	{
		double heightScaleFactor;
		double widthScaleFactor;
		if (getWidth() > getHeight())
		{
			setFieldTurn(EFieldTurn.T90);
			
			heightScaleFactor = (double) getHeight() / getFieldTotalWidth();
			widthScaleFactor = (double) getWidth() / getFieldTotalHeight();
		} else
		{
			setFieldTurn(EFieldTurn.NORMAL);
			
			heightScaleFactor = ((double) getHeight()) / getFieldTotalHeight();
			widthScaleFactor = ((double) getWidth()) / getFieldTotalWidth();
		}
		
		setScaleFactor(Math.min(heightScaleFactor, widthScaleFactor));
		setFieldOriginX(0);
		setFieldOriginY(0);
	}
	
	
	/**
	 * @return the fieldTurn
	 */
	@Override
	public final EFieldTurn getFieldTurn()
	{
		return fieldTurn;
	}
	
	
	/**
	 * @param fieldTurn the fieldTurn to set
	 */
	@Override
	public void setFieldTurn(final EFieldTurn fieldTurn)
	{
		this.fieldTurn = fieldTurn;
		SumatraModel.getInstance().setUserProperty(FieldPanel.class.getCanonicalName() + ".fieldTurn", fieldTurn.name());
	}
	
	
	@Override
	public boolean isFancyPainting()
	{
		return fancyPainting;
	}
	
	
	@Override
	public void setFancyPainting(final boolean fancy)
	{
		fancyPainting = fancy;
	}
	
	
	@Override
	public boolean isPaintCoordinates()
	{
		return doPaintCoordinates;
	}
	
	
	@Override
	public void setPaintCoordinates(final boolean paint)
	{
		doPaintCoordinates = paint;
	}
	
	
	/**
	 * @param scaleFactor the scaleFactor to set
	 */
	private void setScaleFactor(final double scaleFactor)
	{
		this.scaleFactor = scaleFactor;
		SumatraModel.getInstance().setUserProperty(FieldPanel.class.getCanonicalName() + ".scaleFactor",
				String.valueOf(scaleFactor));
	}
	
	
	/**
	 * @param fieldOriginY the fieldOriginY to set
	 */
	private void setFieldOriginY(final double fieldOriginY)
	{
		this.fieldOriginY = fieldOriginY;
		SumatraModel.getInstance().setUserProperty(FieldPanel.class.getCanonicalName() + ".fieldOriginY",
				String.valueOf(fieldOriginY));
	}
	
	
	/**
	 * @param fieldOriginX the fieldOriginX to set
	 */
	private void setFieldOriginX(final double fieldOriginX)
	{
		this.fieldOriginX = fieldOriginX;
		SumatraModel.getInstance().setUserProperty(FieldPanel.class.getCanonicalName() + ".fieldOriginX",
				String.valueOf(fieldOriginX));
	}
	
	
	/**
	 * @return the scaleFactor
	 */
	@Override
	public final double getScaleFactor()
	{
		return scaleFactor;
	}
	
	
	/**
	 * @return the fieldOriginY
	 */
	@Override
	public final double getFieldOriginY()
	{
		return fieldOriginY;
	}
	
	
	/**
	 * @return the fieldOriginX
	 */
	@Override
	public final double getFieldOriginX()
	{
		return fieldOriginX;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public int getFieldMargin()
	{
		final double yScaleFactor = fieldWidth / Geometry.getFieldWidth();
		return (int) ((Geometry.getBoundaryWidth()) * yScaleFactor);
	}
}
