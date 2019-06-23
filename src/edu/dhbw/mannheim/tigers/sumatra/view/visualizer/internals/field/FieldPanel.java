/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.10.2011
 * Author(s): Oliver Steinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.FieldRasterConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.EVisualizerOptions;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IFieldPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.AFieldLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.BallBotLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.BorderLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.CoordinatesLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.EFieldLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.FieldMarksLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.GoalPointsLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.MultiFieldLayerUI;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.OffensivePointsLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.PatternLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.RoleNameLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.ShapeLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.raster.AnalysingRasterLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.raster.PositioningRaster;


/**
 * Visualization of the field.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class FieldPanel extends JPanel implements IFieldPanel
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --- repaint ---
	private static final int						REPAINT_INTERVAL	= 50;
	private long										lastRepaint			= 0L;
	private Image										offImage				= null;
	
	/**  */
	private static final long						serialVersionUID	= 4330620225157027091L;
	
	// --- observer ---
	private final List<IFieldPanelObserver>	observers			= new CopyOnWriteArrayList<IFieldPanelObserver>();
	
	// --- field constants / size of the loaded image in pixel ---
	/** */
	public static final int							FIELD_MARGIN		= 25;
	private static final int						SCROLL_SPEED		= 20;
	private static final int						DEF_FIELD_WIDTH	= 400;
	private static final int						DEF_FIELD_HEIGHT	= 600;
	
	private float										scaleFactor			= 1;
	
	private static int								fieldWidth			= DEF_FIELD_WIDTH;
	/** x */
	private int											fieldTotalWidth;
	/** y */
	private int											fieldTotalHeight;
	
	// --- field scrolling ---
	private float										fieldOriginY		= 0;
	private float										fieldOriginX		= 0;
	
	
	private final MultiFieldLayerUI				multiLayer;
	private final BallBotLayer						botBallLayer;
	private final CoordinatesLayer				coordinatesLayer;
	
	private EFieldTurn								fieldTurn			= EFieldTurn.NORMAL;
	
	/**
	 */
	public enum EFieldTurn
	{
		/**  */
		NORMAL,
		/**  */
		T90,
		/**  */
		T180,
		/**  */
		T270;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public FieldPanel()
	{
		fieldTotalHeight = DEF_FIELD_HEIGHT + (2 * FIELD_MARGIN);
		fieldTotalWidth = DEF_FIELD_WIDTH + (2 * FIELD_MARGIN);
		multiLayer = new MultiFieldLayerUI();
		botBallLayer = new BallBotLayer();
		coordinatesLayer = new CoordinatesLayer();
		setLayout(new MigLayout("fill, inset 0", "[left]", "[top]"));
		
		// --- add listener ---
		final MouseEvents me = new MouseEvents();
		addMouseListener(me);
		addMouseMotionListener(me);
		addMouseWheelListener(me);
		
		// add layers in painting order
		multiLayer.addLayer(new BorderLayer());
		multiLayer.addLayer(new FieldMarksLayer());
		multiLayer.addLayer(new PositioningRaster());
		multiLayer.addLayer(new AnalysingRasterLayer());
		multiLayer.addLayer(botBallLayer);
		multiLayer.addLayer(new GoalPointsLayer());
		multiLayer.addLayer(new PatternLayer());
		multiLayer.addLayer(new ShapeLayer());
		multiLayer.addLayer(coordinatesLayer);
		multiLayer.addLayer(new RoleNameLayer());
		multiLayer.addLayer(new OffensivePointsLayer());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void paint(Graphics g1)
	{
		if ((offImage == null) || (offImage.getHeight(this) != getHeight()) || (offImage.getWidth(this) != getWidth()))
		{
			offImage = createImage(getWidth(), getHeight());
			lastRepaint = System.nanoTime();
		}
		
		if (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastRepaint) < REPAINT_INTERVAL)
		{
			g1.drawImage(offImage, 0, 0, this);
			return;
		}
		lastRepaint = System.nanoTime();
		
		final Graphics2D g2 = (Graphics2D) offImage.getGraphics();
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.translate(fieldOriginX, fieldOriginY);
		g2.scale(scaleFactor, scaleFactor);
		
		turnField(fieldTurn, -Math.PI / 2, g2);
		
		multiLayer.paint(g2);
		
		g1.drawImage(offImage, 0, 0, this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- Coordinate transformation --------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Turn the field in desired angle
	 * 
	 * @param fieldTurn
	 * @param angle [rad]
	 * @param g2
	 */
	private static void turnField(EFieldTurn fieldTurn, double angle, Graphics2D g2)
	{
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
		}
	}
	
	
	/**
	 * Transforms a global(field)position into a gui position.
	 * 
	 * @param globalPosition
	 * @return guiPosition
	 */
	public static IVector2 transformToGuiCoordinates(IVector2 globalPosition)
	{
		final float yScaleFactor = fieldWidth / AIConfig.getGeometry().getFieldWidth();
		final float xScaleFactor = getFieldHeight() / AIConfig.getGeometry().getFieldLength();
		
		final IVector2 transPosition = globalPosition.addNew(new Vector2(AIConfig.getGeometry().getFieldLength() / 2,
				AIConfig.getGeometry().getFieldWidth() / 2));
		
		int x = (int) (transPosition.x() * xScaleFactor);
		int y = (int) (transPosition.y() * yScaleFactor);
		
		x += FIELD_MARGIN;
		y += FIELD_MARGIN;
		
		return new Vector2(y, x);
	}
	
	
	/**
	 * Transforms a gui position into a global(field)position.
	 * 
	 * @param guiPosition
	 * @return globalPosition
	 */
	public static IVector2 transformToGlobalCoordinates(IVector2 guiPosition)
	{
		final float xScaleFactor = (AIConfig.getGeometry().getFieldWidth() / fieldWidth);
		final float yScaleFactor = (AIConfig.getGeometry().getFieldLength() / getFieldHeight());
		
		final IVector2 transPosition = guiPosition.subtractNew(new Vector2(FIELD_MARGIN, FIELD_MARGIN));
		
		int x = (int) (transPosition.x() * xScaleFactor);
		int y = (int) (transPosition.y() * yScaleFactor);
		
		y -= AIConfig.getGeometry().getFieldLength() / 2;
		x -= AIConfig.getGeometry().getFieldWidth() / 2;
		
		return new Vector2(y, x);
	}
	
	
	/**
	 * Scales a global x length to a gui x length.
	 * 
	 * @param length length on field
	 * @return length in gui
	 */
	public static int scaleXLength(float length)
	{
		final float xScaleFactor = getFieldHeight() / AIConfig.getGeometry().getFieldLength();
		return (int) (length * xScaleFactor);
	}
	
	
	/**
	 * Scales a global y length to a gui y length.
	 * 
	 * @param length length on field
	 * @return length in gui
	 */
	public static int scaleYLength(float length)
	{
		final float yScaleFactor = fieldWidth / AIConfig.getGeometry().getFieldWidth();
		return (int) (length * yScaleFactor);
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer -------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void addObserver(IFieldPanelObserver o)
	{
		observers.add(o);
	}
	
	
	@Override
	public void removeObserver(IFieldPanelObserver o)
	{
		observers.remove(o);
	}
	
	// --------------------------------------------------------------
	// --- action listener ------------------------------------------
	// --------------------------------------------------------------
	
	protected class MouseEvents extends MouseAdapter
	{
		private static final double	SCROLL_FACTOR	= 250.0;
		private int							mousePressedY	= 0;
		private int							mousePressedX	= 0;
		
		
		@Override
		public void mouseClicked(MouseEvent e)
		{
			// take care of fieldOriginY
			final Vector2 guiPos = new Vector2((e.getX()) - fieldOriginX, (e.getY()) - fieldOriginY);
			guiPos.multiply(1f / scaleFactor);
			
			boolean ctrl = false;
			boolean alt = false;
			boolean shift = false;
			boolean meta = false;
			
			int onmask = InputEvent.CTRL_DOWN_MASK;
			int offmask = InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
			if ((e.getModifiersEx() & (onmask | offmask)) == onmask)
			{
				ctrl = true;
			}
			
			onmask = InputEvent.ALT_DOWN_MASK;
			offmask = InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
			if ((e.getModifiersEx() & (onmask | offmask)) == onmask)
			{
				alt = true;
			}
			
			onmask = InputEvent.SHIFT_DOWN_MASK;
			offmask = InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK;
			if ((e.getModifiersEx() & (onmask | offmask)) == onmask)
			{
				shift = true;
			}
			
			onmask = InputEvent.META_DOWN_MASK;
			offmask = InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK;
			if ((e.getModifiersEx() & (onmask)) == onmask)
			{
				meta = true;
			}
			
			// --- notify observer ---
			synchronized (observers)
			{
				for (final IFieldPanelObserver observer : observers)
				{
					observer.onFieldClick(transformToGlobalCoordinates(guiPos), ctrl, alt, shift, meta);
				}
			}
		}
		
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			mousePressedY = e.getY();
			mousePressedX = e.getX();
		}
		
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			// --- move the field over the panel ---
			final int dy = e.getY() - mousePressedY;
			final int dx = e.getX() - mousePressedX;
			fieldOriginY = fieldOriginY + dy;
			fieldOriginX = fieldOriginX + dx;
			mousePressedY += dy;
			mousePressedX += dx;
			repaint();
		}
		
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			final int rot = -e.getWheelRotation();
			final int scroll = SCROLL_SPEED * rot;
			
			final Point mPoint = e.getPoint();
			final float xLen = ((mPoint.x - fieldOriginX) / scaleFactor) * 2;
			final float yLen = ((mPoint.y - fieldOriginY) / scaleFactor) * 2;
			
			final float oldLenX = (xLen) * scaleFactor;
			final float oldLenY = (yLen) * scaleFactor;
			scaleFactor *= 1 + (scroll / SCROLL_FACTOR);
			final float newLenX = (xLen) * scaleFactor;
			final float newLenY = (yLen) * scaleFactor;
			fieldOriginX -= (newLenX - oldLenX) / 2;
			fieldOriginY -= (newLenY - oldLenY) / 2;
			repaint();
		}
		
		
		@Override
		public void mouseMoved(MouseEvent e)
		{
			// take care of fieldOriginY
			final Vector2 guiPos = new Vector2((e.getX()) - fieldOriginX, (e.getY()) - fieldOriginY);
			guiPos.multiply(1f / scaleFactor);
			coordinatesLayer.updateMouseLocation(transformToGlobalCoordinates(guiPos));
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- Presenter Interface --------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void setPanelVisible(boolean visible)
	{
		setVisible(visible);
		multiLayer.setInitialVisibility();
	}
	
	
	@Override
	public void drawAIFrame(AIInfoFrame aiFrame)
	{
		multiLayer.update(aiFrame);
		repaint();
	}
	
	
	@Override
	public void drawRecordFrame(IRecordFrame recFrame)
	{
		multiLayer.updateRecord(recFrame);
		repaint();
	}
	
	
	@Override
	public void setNewFieldRaster(FieldRasterConfig fieldRasterconfig)
	{
		final PositioningRaster layer = (PositioningRaster) multiLayer.getFieldLayer(EFieldLayer.POSITIONING_RASTER);
		if (layer != null)
		{
			layer.setNewFieldRaster(fieldRasterconfig);
		}
		
		final AnalysingRasterLayer layer2 = (AnalysingRasterLayer) multiLayer.getFieldLayer(EFieldLayer.ANALYSING_RASTER);
		if (layer2 != null)
		{
			layer2.setNewFieldRaster(fieldRasterconfig);
		}
	}
	
	
	@Override
	public void setPaths(List<Path> paths)
	{
		botBallLayer.setPaths(paths);
		// little hack to get pathes in replay
		multiLayer.setPaths(paths);
	}
	
	
	/**
	 * width with margins
	 * 
	 * @return
	 */
	public static int getFieldTotalWidth()
	{
		return fieldWidth + (2 * FIELD_MARGIN);
	}
	
	
	/**
	 * height width margins
	 * 
	 * @return
	 */
	public static int getFieldTotalHeight()
	{
		return getFieldHeight() + (2 * FIELD_MARGIN);
	}
	
	
	private static float getFieldRatio()
	{
		if (AIConfig.getGeometry() == null)
		{
			return DEF_FIELD_WIDTH / DEF_FIELD_HEIGHT;
		}
		return AIConfig.getGeometry().getFieldLength() / AIConfig.getGeometry().getFieldWidth();
	}
	
	
	private static int getFieldHeight()
	{
		return (int) (getFieldRatio() * fieldWidth);
	}
	
	
	@Override
	public void onOptionChanged(EVisualizerOptions option, boolean isSelected)
	{
		AFieldLayer layer = null;
		
		switch (option)
		{
			case ACCELERATION:
				final BallBotLayer bblayerA = (BallBotLayer) multiLayer.getFieldLayer(EFieldLayer.BALL_BOTS);
				if (bblayerA != null)
				{
					bblayerA.showAcceleration(isSelected);
				}
				break;
			case ANALYSIS_GRID:
				layer = multiLayer.getFieldLayer(EFieldLayer.ANALYSING_RASTER);
				break;
			case COORDINATES:
				layer = multiLayer.getFieldLayer(EFieldLayer.COORDINATES);
				break;
			case DEFENSE_GOAL_POINTS:
				layer = multiLayer.getFieldLayer(EFieldLayer.DEFENSE_GOAL_POINTS);
				break;
			case OFFENSIVE_POINTS:
				layer = multiLayer.getFieldLayer(EFieldLayer.OFFENSIVE_POINTS);
				break;
			case FANCY:
				multiLayer.setFancyPainting(isSelected);
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
				switch (fieldTurn)
				{
					case NORMAL:
					case T180:
						fieldOriginX = 0;
						fieldOriginY = 0;
						break;
					case T90:
					case T270:
						fieldOriginX = (int) (((getFieldTotalHeight() * scaleFactor) / 2) - ((getFieldTotalWidth() * scaleFactor) / 2));
						fieldOriginY = -fieldOriginX;
						break;
				}
				break;
			case LAYER_DEBUG_INFOS:
				multiLayer.setDebugInformationVisible(isSelected);
				break;
			case PATHS:
				final BallBotLayer bblayerP = (BallBotLayer) multiLayer.getFieldLayer(EFieldLayer.BALL_BOTS);
				if (bblayerP != null)
				{
					bblayerP.showPaths(isSelected);
				}
				break;
			case PATTERNS:
				layer = multiLayer.getFieldLayer(EFieldLayer.PATTERN_LAYER);
				break;
			case POSITION_GRID:
				layer = multiLayer.getFieldLayer(EFieldLayer.POSITIONING_RASTER);
				break;
			case SHAPES:
				layer = multiLayer.getFieldLayer(EFieldLayer.SHAPES);
				break;
			case SPLINES:
				final BallBotLayer bblayerS = (BallBotLayer) multiLayer.getFieldLayer(EFieldLayer.BALL_BOTS);
				if (bblayerS != null)
				{
					bblayerS.showSplines(isSelected);
				}
				break;
			case ERROR_TREE:
				final BallBotLayer bblayerET = (BallBotLayer) multiLayer.getFieldLayer(EFieldLayer.BALL_BOTS);
				if (bblayerET != null)
				{
					bblayerET.showPPErrorTree(isSelected);
				}
				break;
			case VELOCITY:
				final BallBotLayer bblayerV = (BallBotLayer) multiLayer.getFieldLayer(EFieldLayer.BALL_BOTS);
				if (bblayerV != null)
				{
					bblayerV.showVelocity(isSelected);
				}
				break;
			case BALL_BUFFER:
				final BallBotLayer bblayerB = (BallBotLayer) multiLayer.getFieldLayer(EFieldLayer.BALL_BOTS);
				if (bblayerB != null)
				{
					bblayerB.showBallBuffer(isSelected);
				}
				break;
			case ROLE_NAME:
				layer = multiLayer.getFieldLayer(EFieldLayer.ROLE_NAME);
				break;
			case FIELD_MARKS:
				layer = multiLayer.getFieldLayer(EFieldLayer.FIELD_MARKS);
				break;
			default:
				break;
		
		}
		
		if (layer != null)
		{
			layer.setVisible(isSelected);
		}
		repaint();
	}
	
	
	/**
	 * @return the multiLayer
	 */
	@Override
	public final MultiFieldLayerUI getMultiLayer()
	{
		return multiLayer;
	}
	
	
	@Override
	public Dimension getMinimumSize()
	{
		return new Dimension(fieldTotalWidth, fieldTotalHeight);
	}
	
	
	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(fieldTotalWidth, fieldTotalHeight);
	}
	
	
	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(fieldTotalWidth, fieldTotalHeight);
	}
	
	
	/**
	 * @return the fieldTurn
	 */
	public final EFieldTurn getFieldTurn()
	{
		return fieldTurn;
	}
	
	
	/**
	 * @param fieldTurn the fieldTurn to set
	 */
	private final void setFieldTurn(EFieldTurn fieldTurn)
	{
		this.fieldTurn = fieldTurn;
		multiLayer.setFieldTurn(fieldTurn);
	}
}
