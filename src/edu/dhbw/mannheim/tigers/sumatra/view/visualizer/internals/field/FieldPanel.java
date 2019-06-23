/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.10.2011
 * Author(s): Oliver Steinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.IFieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.EVisualizerOptions;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IFieldPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.AFieldLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.AresLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.BallBotLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.BorderLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.BotStatusLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.CamIntersectionLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.CoordinatesLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.EFieldLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.FieldMarksLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.FieldPredictorLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.MultiFieldLayerUI;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.PositionBufferLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.RefereeLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.RoleNameLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.ShapeLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.SupportPositionsLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.ValuedFieldLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.VisionLayer;


/**
 * Visualization of the field.
 * 
 * @author Oliver Steinbrecher
 */
public class FieldPanel extends JPanel implements IFieldPanel
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --- repaint ---
	private Image										offImage				= null;
	
	/**  */
	private static final long						serialVersionUID	= 4330620225157027091L;
	
	// --- observer ---
	private final List<IFieldPanelObserver>	observers			= new CopyOnWriteArrayList<IFieldPanelObserver>();
	
	// --- field constants / size of the loaded image in pixel ---
	/** */
	public static final int							FIELD_MARGIN		= 35;
	private static final int						SCROLL_SPEED		= 20;
	private static final int						DEF_FIELD_WIDTH	= 600;
	
	private float										scaleFactor			= 1;
	
	private final int									fieldWidth			= DEF_FIELD_WIDTH;
	
	
	// --- field scrolling ---
	private float										fieldOriginY		= 0;
	private float										fieldOriginX		= 0;
	
	
	private final MultiFieldLayerUI				multiLayer;
	private final CoordinatesLayer				coordinatesLayer;
	
	private EFieldTurn								fieldTurn			= EFieldTurn.NORMAL;
	
	/**
	 */
	public enum EFieldTurn
	{
		/**  */
		NORMAL(0),
		/**  */
		T90(AngleMath.PI_HALF),
		/**  */
		T180(AngleMath.PI),
		/**  */
		T270(AngleMath.PI + AngleMath.PI_HALF);
		
		private final float	angle;
		
		
		private EFieldTurn(final float angle)
		{
			this.angle = angle;
		}
		
		
		/**
		 * @return the angle
		 */
		public final float getAngle()
		{
			return angle;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public FieldPanel()
	{
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setLayout(new MigLayout("inset 0"));
		multiLayer = new MultiFieldLayerUI();
		coordinatesLayer = new CoordinatesLayer();
		
		// add layers in painting order
		multiLayer.addAiLayer(new ValuedFieldLayer(), this);
		AFieldLayer borderLayer = new BorderLayer();
		multiLayer.addSwfLayer(borderLayer, this);
		multiLayer.addAiLayer(borderLayer, this);
		multiLayer.addSwfLayer(new FieldMarksLayer(), this);
		multiLayer.addAiLayer(coordinatesLayer, this);
		multiLayer.addSwfLayer(new CamIntersectionLayer(), this);
		multiLayer.addAiLayer(new RefereeLayer(), this);
		multiLayer.addSwfLayer(new RefereeLayer(), this);
		multiLayer.addAiLayer(new PositionBufferLayer(), this);
		multiLayer.addSwfLayer(new BallBotLayer(), this);
		multiLayer.addSwfLayer(new VisionLayer(), this);
		multiLayer.addSwfLayer(new BotStatusLayer(), this);
		multiLayer.addAiLayer(new AresLayer(), this);
		multiLayer.addAiLayer(new ShapeLayer(), this);
		multiLayer.addAiLayer(new RoleNameLayer(), this);
		multiLayer.addSwfLayer(new FieldPredictorLayer(), this);
		multiLayer.addAiLayer(new SupportPositionsLayer(), this);
		
		String strFieldTurn = SumatraModel.getInstance().getUserProperty(
				FieldPanel.class.getCanonicalName() + ".fieldTurn");
		if (strFieldTurn != null)
		{
			try
			{
				setFieldTurn(EFieldTurn.valueOf(strFieldTurn));
			} catch (IllegalArgumentException err)
			{
				// ignore
			}
		}
		
		String strScaleFactor = SumatraModel.getInstance().getUserProperty(
				FieldPanel.class.getCanonicalName() + ".scaleFactor");
		if (strScaleFactor != null)
		{
			scaleFactor = Float.valueOf(SumatraModel.getInstance().getUserProperty(
					FieldPanel.class.getCanonicalName() + ".scaleFactor"));
		}
		
		String strFieldOriginX = SumatraModel.getInstance().getUserProperty(
				FieldPanel.class.getCanonicalName() + ".fieldOriginX");
		if (strFieldOriginX != null)
		{
			fieldOriginX = Float.valueOf(SumatraModel.getInstance().getUserProperty(
					FieldPanel.class.getCanonicalName() + ".fieldOriginX"));
		}
		
		String strFieldOriginY = SumatraModel.getInstance().getUserProperty(
				FieldPanel.class.getCanonicalName() + ".fieldOriginY");
		if (strFieldOriginY != null)
		{
			fieldOriginY = Float.valueOf(SumatraModel.getInstance().getUserProperty(
					FieldPanel.class.getCanonicalName() + ".fieldOriginY"));
		}
		// --- add listener ---
		final MouseEvents me = new MouseEvents();
		addMouseListener(me);
		addMouseMotionListener(me);
		addMouseWheelListener(me);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void paint(final Graphics g1)
	{
		if ((offImage == null) || (offImage.getHeight(this) != getHeight()) || (offImage.getWidth(this) != getWidth()))
		{
			offImage = createImage(getWidth(), getHeight());
		}
		
		final Graphics2D g2 = (Graphics2D) offImage.getGraphics();
		g2.setColor(BorderLayer.FIELD_COLOR_REFEREE);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		g2.translate(fieldOriginX, fieldOriginY);
		g2.scale(scaleFactor, scaleFactor);
		
		turnField(getFieldTurn(), -AngleMath.PI_HALF, g2);
		g2.setColor(BorderLayer.FIELD_COLOR);
		g2.fillRect(0, 0, getFieldTotalWidth(), getFieldTotalHeight());
		turnField(getFieldTurn(), AngleMath.PI_HALF, g2);
		
		multiLayer.paint(g2);
		
		g1.drawImage(offImage, 0, 0, this);
	}
	
	
	/**
	 */
	@Override
	public void clearField()
	{
		multiLayer.clearField();
		offImage = null;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- Coordinate transformation --------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void turnField(final EFieldTurn fieldTurn, final double angle, final Graphics2D g2)
	{
		float translateSize = (((float) getFieldHeight() - fieldWidth) / 2);
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
				return new Vector2(point.y(), getFieldTotalWidth() - point.x());
			case T180:
				return new Vector2((-point.x() + getFieldTotalWidth()), (-point.y() + getFieldTotalHeight()));
			case T270:
				return new Vector2((-point.y() + getFieldTotalHeight()), point.x());
		}
		throw new IllegalStateException();
	}
	
	
	private IVector2 turnGlobalPoint(final EFieldTurn fieldTurn, final IVector2 point)
	{
		switch (fieldTurn)
		{
			case NORMAL:
				return point;
			case T90:
				return new Vector2(getFieldTotalWidth() - point.y(), point.x());
			case T180:
				return new Vector2(-point.x() + getFieldTotalWidth(), -point.y() + getFieldTotalHeight());
			case T270:
				return new Vector2(point.y(), -point.x() + getFieldTotalHeight());
		}
		throw new IllegalStateException();
	}
	
	
	@Override
	public IVector2 transformToGuiCoordinates(final IVector2 globalPosition)
	{
		final double yScaleFactor = fieldWidth / AIConfig.getGeometry().getFieldWidth();
		final double xScaleFactor = getFieldHeight() / AIConfig.getGeometry().getFieldLength();
		
		final IVector2 transPosition = globalPosition.addNew(new Vector2(AIConfig.getGeometry().getFieldLength() / 2f,
				AIConfig.getGeometry().getFieldWidth() / 2f));
		
		double x = (transPosition.x() * xScaleFactor);
		double y = (transPosition.y() * yScaleFactor);
		
		x += FIELD_MARGIN;
		y += FIELD_MARGIN;
		
		return turnGuiPoint(fieldTurn, new Vector2(y, x));
	}
	
	
	@Override
	public IVector2 transformToGuiCoordinates(final IVector2 globalPosition, final boolean invert)
	{
		int r = 1;
		if (invert)
		{
			r = -1;
		}
		return transformToGuiCoordinates(new Vector2(r * globalPosition.x(), r * globalPosition.y()));
	}
	
	
	@Override
	public IVector2 transformToGlobalCoordinates(final IVector2 guiPosition)
	{
		IVector2 guiPosTurned = turnGlobalPoint(fieldTurn, guiPosition);
		
		final float xScaleFactor = (AIConfig.getGeometry().getFieldWidth() / fieldWidth);
		final float yScaleFactor = (AIConfig.getGeometry().getFieldLength() / getFieldHeight());
		
		final IVector2 transPosition = guiPosTurned.subtractNew(new Vector2(FIELD_MARGIN, FIELD_MARGIN));
		
		int x = (int) (transPosition.x() * xScaleFactor);
		int y = (int) (transPosition.y() * yScaleFactor);
		
		y -= AIConfig.getGeometry().getFieldLength() / 2;
		x -= AIConfig.getGeometry().getFieldWidth() / 2;
		
		return new Vector2(y, x);
	}
	
	
	@Override
	public IVector2 transformToGlobalCoordinates(final IVector2 globalPosition, final boolean invert)
	{
		int r = 1;
		if (invert)
		{
			r = -1;
		}
		return transformToGlobalCoordinates(new Vector2(r * globalPosition.x(), r * globalPosition.y()));
	}
	
	
	@Override
	public int scaleXLength(final float length)
	{
		final float xScaleFactor = getFieldHeight() / AIConfig.getGeometry().getFieldLength();
		return (int) (length * xScaleFactor);
	}
	
	
	@Override
	public int scaleYLength(final float length)
	{
		final float yScaleFactor = fieldWidth / AIConfig.getGeometry().getFieldWidth();
		return (int) (length * yScaleFactor);
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer -------------------------------------------------------------
	// --------------------------------------------------------------------------
	
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
	
	// --------------------------------------------------------------
	// --- action listener ------------------------------------------
	// --------------------------------------------------------------
	
	protected class MouseEvents extends MouseAdapter
	{
		private static final double	SCROLL_FACTOR	= 250.0;
		private int							mousePressedY	= 0;
		private int							mousePressedX	= 0;
		
		
		@Override
		public void mouseClicked(final MouseEvent e)
		{
			// take care of fieldOriginY
			IVector2 guiPos = new Vector2((e.getX()) - fieldOriginX, (e.getY()) - fieldOriginY).multiply(1f / scaleFactor);
			
			// --- notify observer ---
			for (final IFieldPanelObserver observer : observers)
			{
				observer.onFieldClick(transformToGlobalCoordinates(guiPos), e);
			}
			
			multiLayer.onFieldClick(transformToGlobalCoordinates(guiPos));
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
			final float xLen = ((mPoint.x - fieldOriginX) / scaleFactor) * 2;
			final float yLen = ((mPoint.y - fieldOriginY) / scaleFactor) * 2;
			
			final float oldLenX = (xLen) * scaleFactor;
			final float oldLenY = (yLen) * scaleFactor;
			setScaleFactor((float) (scaleFactor * (1 + (scroll / SCROLL_FACTOR))));
			final float newLenX = (xLen) * scaleFactor;
			final float newLenY = (yLen) * scaleFactor;
			setFieldOriginX(fieldOriginX - ((newLenX - oldLenX) / 2));
			setFieldOriginY(fieldOriginY - ((newLenY - oldLenY) / 2));
		}
		
		
		@Override
		public void mouseMoved(final MouseEvent e)
		{
			// take care of fieldOriginY
			IVector2 guiPos = new Vector2((e.getX()) - fieldOriginX, (e.getY()) - fieldOriginY).multiply(1f / scaleFactor);
			
			// --- notify observer ---
			for (final IFieldPanelObserver observer : observers)
			{
				observer.onMouseMoved(transformToGlobalCoordinates(guiPos), e);
			}
			
			coordinatesLayer.updateMouseLocation(transformToGlobalCoordinates(guiPos));
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- Presenter Interface --------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void setPanelVisible(final boolean visible)
	{
		setVisible(visible);
		multiLayer.setInitialVisibility();
	}
	
	
	@Override
	public void updateAiFrame(final IRecordFrame frame)
	{
		multiLayer.update(frame);
	}
	
	
	@Override
	public void updateWFrame(final SimpleWorldFrame frame)
	{
		multiLayer.updateWf(frame);
	}
	
	
	@Override
	public void updateRefereeMsg(final RefereeMsg msg)
	{
		multiLayer.updateRefereeMsg(msg);
	}
	
	
	/**
	 * width with margins
	 * 
	 * @return
	 */
	@Override
	public int getFieldTotalWidth()
	{
		return fieldWidth + (2 * FIELD_MARGIN);
	}
	
	
	/**
	 * height width margins
	 * 
	 * @return
	 */
	@Override
	public int getFieldTotalHeight()
	{
		return getFieldHeight() + (2 * FIELD_MARGIN);
	}
	
	
	private float getFieldRatio()
	{
		if (AIConfig.getGeometry() == null)
		{
			return 1;
		}
		return AIConfig.getGeometry().getFieldLength() / AIConfig.getGeometry().getFieldWidth();
	}
	
	
	/**
	 * @return
	 */
	@Override
	public final int getFieldHeight()
	{
		return Math.round(getFieldRatio() * fieldWidth);
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
	public void setLayerVisiblility(final EDrawableShapesLayer dsLayer, final boolean visible)
	{
		ShapeLayer sLayer = (ShapeLayer) multiLayer.getFieldLayer(EFieldLayer.SHAPES);
		sLayer.setVisible(dsLayer, visible);
	}
	
	
	@Override
	public void onOptionChanged(final EVisualizerOptions option, final boolean isSelected)
	{
		AFieldLayer layer = null;
		final AresLayer aresLayer = (AresLayer) multiLayer.getFieldLayer(EFieldLayer.ARES);
		
		switch (option)
		{
			case ACCELERATION:
				final BallBotLayer bblayerA = (BallBotLayer) multiLayer.getFieldLayer(EFieldLayer.BALL_BOTS);
				if (bblayerA != null)
				{
					bblayerA.showAcceleration(isSelected);
				}
				break;
			case COORDINATES:
				layer = multiLayer.getFieldLayer(EFieldLayer.COORDINATES);
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
				fieldOriginX = 0;
				fieldOriginY = 0;
				break;
			case RESET_FIELD:
				if (getWidth() > getHeight())
				{
					setFieldTurn(EFieldTurn.T90);
					
					if ((getFieldRatio() * getHeight()) > getWidth())
					{
						setScaleFactor((float) getWidth() / getFieldTotalHeight());
					} else
					{
						setScaleFactor((float) getHeight() / getFieldTotalWidth());
					}
				} else
				{
					setFieldTurn(EFieldTurn.NORMAL);
					
					if ((getFieldRatio() * getWidth()) > getHeight())
					{
						setScaleFactor((float) getHeight() / getFieldTotalHeight());
					} else
					{
						setScaleFactor((float) getHeight() / getFieldTotalWidth());
					}
				}
				
				setFieldOriginX(0);
				setFieldOriginY(0);
				break;
			case LAYER_DEBUG_INFOS:
				multiLayer.setDebugInformationVisible(isSelected);
				break;
			case PATHS:
				if (aresLayer != null)
				{
					aresLayer.showPaths(isSelected);
				}
				break;
			case POSITION_GRID:
				layer = multiLayer.getFieldLayer(EFieldLayer.POSITIONING_RASTER);
				break;
			case SHAPES:
				layer = multiLayer.getFieldLayer(EFieldLayer.SHAPES);
				break;
			case PATH_DECORATION:
				if (aresLayer != null)
				{
					aresLayer.showDecoration(isSelected);
				}
				break;
			case POT_PATHS:
				if (aresLayer != null)
				{
					aresLayer.showPotentialPaths(isSelected);
				}
				break;
			case POT_PATH_DECORATION:
				if (aresLayer != null)
				{
					aresLayer.showPotentialDecoration(isSelected);
				}
				break;
			case PP_DEBUG:
				if (aresLayer != null)
				{
					aresLayer.showDebug(isSelected);
				}
				break;
			case POT_DEBUG:
				if (aresLayer != null)
				{
					aresLayer.showPotentialDebug(isSelected);
				}
				break;
			case PATHS_UNSMOOTHED:
				if (aresLayer != null)
				{
					aresLayer.showUnsmoothedPaths(isSelected);
				}
				break;
			case PATHS_RAMBO:
				if (aresLayer != null)
				{
					aresLayer.showRambo(isSelected);
				}
				break;
			case VELOCITY:
				final BallBotLayer bblayerV = (BallBotLayer) multiLayer.getFieldLayer(EFieldLayer.BALL_BOTS);
				if (bblayerV != null)
				{
					bblayerV.showVelocity(isSelected);
				}
				break;
			case ROLE_NAME:
				layer = multiLayer.getFieldLayer(EFieldLayer.ROLE_NAME);
				break;
			case FIELD_MARKS:
				layer = multiLayer.getFieldLayer(EFieldLayer.FIELD_MARKS);
				break;
			case BLUE_AI:
				if (isSelected)
				{
					multiLayer.addTeamColor(ETeamColor.BLUE);
				} else
				{
					multiLayer.removeTeamColor(ETeamColor.BLUE);
				}
				break;
			case YELLOW_AI:
				if (isSelected)
				{
					multiLayer.addTeamColor(ETeamColor.YELLOW);
				} else
				{
					multiLayer.removeTeamColor(ETeamColor.YELLOW);
				}
				break;
			case BOT_STATUS:
				layer = multiLayer.getFieldLayer(EFieldLayer.BOT_STATUS);
				break;
			case REFEREE:
				layer = multiLayer.getFieldLayer(EFieldLayer.REFEREE);
				break;
			case FIELD_PREDICTION:
				layer = multiLayer.getFieldLayer(EFieldLayer.FIELD_PREDICTOR);
				break;
			case SUPPORT_POS:
				layer = multiLayer.getFieldLayer(EFieldLayer.SUPPORT_POSITIONS);
				break;
			case SUPPORT_GRID:
				layer = multiLayer.getFieldLayer(EFieldLayer.VALUED_FIELD);
				break;
			case POSITION_BUFFER:
				layer = multiLayer.getFieldLayer(EFieldLayer.POSITION_BUFFER);
				break;
			case INTERSECTION:
				layer = multiLayer.getFieldLayer(EFieldLayer.INTERSECTION);
				break;
			case VISION:
				layer = multiLayer.getFieldLayer(EFieldLayer.VISION);
				break;
		}
		
		if (layer != null)
		{
			layer.setVisible(isSelected);
		}
	}
	
	
	/**
	 * @return the multiLayer
	 */
	@Override
	public final MultiFieldLayerUI getMultiLayer()
	{
		return multiLayer;
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
	private final void setFieldTurn(final EFieldTurn fieldTurn)
	{
		this.fieldTurn = fieldTurn;
		multiLayer.setFieldTurn(fieldTurn);
		SumatraModel.getInstance().setUserProperty(FieldPanel.class.getCanonicalName() + ".fieldTurn", fieldTurn.name());
	}
	
	
	/**
	 * @param scaleFactor the scaleFactor to set
	 */
	private void setScaleFactor(final float scaleFactor)
	{
		this.scaleFactor = scaleFactor;
		SumatraModel.getInstance().setUserProperty(FieldPanel.class.getCanonicalName() + ".scaleFactor",
				String.valueOf(scaleFactor));
	}
	
	
	/**
	 * @param fieldOriginY the fieldOriginY to set
	 */
	private void setFieldOriginY(final float fieldOriginY)
	{
		this.fieldOriginY = fieldOriginY;
		SumatraModel.getInstance().setUserProperty(FieldPanel.class.getCanonicalName() + ".fieldOriginY",
				String.valueOf(fieldOriginY));
	}
	
	
	/**
	 * @param fieldOriginX the fieldOriginX to set
	 */
	private void setFieldOriginX(final float fieldOriginX)
	{
		this.fieldOriginX = fieldOriginX;
		SumatraModel.getInstance().setUserProperty(FieldPanel.class.getCanonicalName() + ".fieldOriginX",
				String.valueOf(fieldOriginX));
	}
	
	
	/**
	 * @return the scaleFactor
	 */
	@Override
	public final float getScaleFactor()
	{
		return scaleFactor;
	}
	
	
	/**
	 * @return the fieldOriginY
	 */
	@Override
	public final float getFieldOriginY()
	{
		return fieldOriginY;
	}
	
	
	/**
	 * @return the fieldOriginX
	 */
	@Override
	public final float getFieldOriginX()
	{
		return fieldOriginX;
	}
}
