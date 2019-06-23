/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 21, 2010
 * Author(s): bernhard
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.XYSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ValuePoint;


/**
 * Visualizes the SSL-field with robots and balls.
 * 
 * @author bernhard
 * 
 */

public class FieldPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long						serialVersionUID					= -1141258737878520413L;
	

	// --- field constants ---
	private int											FIELD_TOTAL_WIDTH					= 455;
	private int											FIELD_TOTAL_HEIGHT				= 655;
	private int											FIELD_WIDTH							= 405;
	private int											FIELD_HEIGHT						= 605;
	private final int									FIELD_MARGIN						= 25;
	private final int									FIELD_CIRCLE_RADIUS				= 50;
	private final int									FIELD_GOAL_WIDTH					= 70;
	private final int									FIELD_GOAL_HEIGHT					= 22;
	// private final int FIELD_GOAL_MARGIN_PENALTYPOINT = 45;
	private final int									FIELD_GOAL_MARGIN_PENALTYAREA	= 50;
	private final int									FIELD_GOAL_MARGIN_PENALTYLINE	= 35;
	// private final int FIELD_LINE_WIDTH = 1;
	
	// --- robots and ball ---
	private final int									ROBOT_RADIUS						= 9;
	private final int									BALL_RADIUS							= 2;
	
	private static final int						BOT_ID_MIN							= 0;
	private static final int						BOT_ID_MAX							= 11;
	
	// --- logger ---
	private final Logger								log									= Logger.getLogger(getClass());
	
	// --- observer ---
	private final List<IFieldPanelObserver>	observers							= new ArrayList<IFieldPanelObserver>();
	
	// --- current worldframe ---
	private WorldFrame								currentWorldFrame					= null;
	
	// --- field scrolling ---
	private int											fieldOriginY						= 0;
	
	// ---- show-options ---
	private boolean									showAcceleration					= false;
	private boolean									showVelocity						= false;
	private boolean									showPositiongGrid					= false;
	private boolean									showAnalysingGrid					= false;
	private boolean									showPaths							= false;
	private boolean									showSplines							= false;
	private boolean									showDefenseGoalPoints			= false;
	private boolean									showDebugPoints					= false;
	
	// --- grid data ---
	private int											columnGridSize						= 0;
	private int											rowGridSize							= 0;
	private int											columnAnalysingGridSize			= 0;
	private int											rowAnalysingGridSize				= 0;
	
	// --- defense goal points ---
	private List<ValuePoint>						defenseGoalPoints					= new ArrayList<ValuePoint>();
	
	// --- debugpoints and paths ---
	private List<Path>								paths									= null;
	private List<IVector2>							debugPoints							= null;
	
	// --- color ---
	private Color										tigersColor							= Color.GRAY;
	private Color										foeColor								= Color.GRAY;
	
	
	// private final RenderingHints renderingHints;
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public FieldPanel()
	{
		// --- configure panel ---
		setLayout(new MigLayout("fill", "", ""));
		
		// --- add listener ---
		MouseEvents me = new MouseEvents();
		addMouseListener(me);
		addMouseMotionListener(me);
		
		// final Map<RenderingHints.Key, Object> hints = new HashMap<RenderingHints.Key, Object>();
		// // hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		// hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// // hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		// // hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
		// // hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		// // hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		// // hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		// renderingHints = new RenderingHints(hints);
	}
	

	// --------------------------------------------------------------------------
	// --- observer -------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	public void addObserver(IFieldPanelObserver o)
	{
		observers.add(o);
	}
	

	public void removeObserver(IFieldPanelObserver o)
	{
		observers.remove(o);
	}
	

	// --------------------------------------------------------------------------
	// --- paint-main-method ---------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void paintComponent(Graphics g1)
	{
		// final long start = System.nanoTime();
		
		super.paintComponent(g1);
		
		Graphics2D g2 = (Graphics2D) g1;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// BufferedImage offImage = new BufferedImage(FIELD_TOTAL_WIDTH, FIELD_TOTAL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		
		// GraphicsConfiguration gConfig = getGraphicsConfiguration();
		// BufferedImage offImage = gConfig.createCompatibleImage(FIELD_TOTAL_WIDTH, FIELD_TOTAL_HEIGHT,
		// BufferedImage.TYPE_INT_ARGB);
		// offImage.setAccelerationPriority(1);
		// Graphics2D offGraphic = (Graphics2D) offImage.getGraphics();
		// offGraphic.setRenderingHints(renderingHints);
		
		g2.translate(0, fieldOriginY);
		
		// --- draw field ---
		drawField(g2);
		

		// --- show analysing grid ---
		if (showAnalysingGrid && rowAnalysingGridSize != 0 && columnAnalysingGridSize != 0)
		{
			drawGrid(g2, Color.red, columnAnalysingGridSize, rowAnalysingGridSize);
		}
		
		// --- show positioning grid ---
		if (showPositiongGrid && rowGridSize != 0 && columnGridSize != 0)
		{
			drawGrid(g2, Color.cyan, columnGridSize, rowGridSize);
		}
		

		synchronized (this)
		{
			// --- draw worldFrame ---
			if (currentWorldFrame != null)
			{
				
				// --- draw tiger robots ---
				for (Map.Entry<Integer, TrackedTigerBot> map : currentWorldFrame.tigerBots.entrySet())
				{
					int id = map.getKey();
					if (id >= BOT_ID_MIN && id <= BOT_ID_MAX)
					{
						drawRobot(g2, map.getValue(), tigersColor, String.valueOf(id));
					}
					
				}
				
				// --- draw foe robots ---
				for (Map.Entry<Integer, TrackedBot> map : currentWorldFrame.foeBots.entrySet())
				{
					int id = map.getKey() - 100;
					if (id >= BOT_ID_MIN && id <= BOT_ID_MAX)
					{
						drawRobot(g2, map.getValue(), foeColor, String.valueOf(id));
					}
				}
				
				// --- draw balls ---
				drawBall(g2, (int) currentWorldFrame.ball.pos.y, (int) currentWorldFrame.ball.pos.x,
						currentWorldFrame.ball.vel);
				
				// --- draw paths ---
				if ((showPaths || showSplines) && paths != null && !paths.isEmpty())
				{
					IVector2 botPos = null;
					
					for (Path p : paths)
					{
						// --- if path==null ---
						if (p == null)
						{
							continue;
						}
						
						// --- get robot position ---
						ATrackedObject bot = currentWorldFrame.tigerBots.get(p.botID);
						botPos = bot == null ? null : bot.pos;
						
						// --- draw path if botPos!=null ---
						if (botPos != null)
						{
							drawPath(g2, botPos, p);
						} else
						{
							// log.debug("Can't draw path. Bot-position is NULL.");
						}
						
					}
				}
			}
		}
		
		// --- draw defense goal points ---
		if (showDefenseGoalPoints && defenseGoalPoints != null && !defenseGoalPoints.isEmpty())
		{
			for (ValuePoint point : defenseGoalPoints)
			{
				int drawingX = (int) (point.y / 10 + FIELD_TOTAL_WIDTH / 2) - 3;
				int drawingY = (int) (point.x / 10 + FIELD_TOTAL_HEIGHT / 2) - 3;
				
				if (point.getValue() <= 1.5)
				{
					g2.setColor(Color.GREEN);
					
				} else if (point.getValue() <= 2.0)
				{
					g2.setColor(Color.YELLOW);
				} else if (point.getValue() <= 2.5)
				{
					g2.setColor(Color.ORANGE);
				} else
				{
					g2.setColor(Color.RED);
				}
				
				g2.fillOval(drawingX, drawingY, 6, 6);
				
			}
		}
		

		// --- draw debug-points ---
		if (showDebugPoints && debugPoints != null && !debugPoints.isEmpty())
		{
			for (IVector2 p : debugPoints)
			{
				int drawingX = (int) (p.y() / 10 + FIELD_TOTAL_WIDTH / 2f) - 2;
				int drawingY = (int) (p.x() / 10 + FIELD_TOTAL_HEIGHT / 2f) - 2;
				g2.setColor(Color.red);
				g2.fillOval(drawingX, drawingY, 4, 4);
			}
		}
		

		// System.out.println("nanos: " + (System.nanoTime() - start) + " ns");
		// AffineTransform scaling = new AffineTransform();
		// scaling.scale(1, 1);
		// g2.drawImage(offImage, new AffineTransformOp(scaling, AffineTransformOp.TYPE_BILINEAR), 0, 0);
		// g2.drawImage(offImage, new RescaleOp(1, 1, renderingHints), 0, 0);
	}
	

	// --------------------------------------------------------------------------
	// --- paint-sub-methods ----------------------------------------------------
	// --------------------------------------------------------------------------
	
	public void drawWorldFrame(WorldFrame wf)
	{
		synchronized (this)
		{
			currentWorldFrame = wf;
		}
		
		this.repaint();
	}
	

	public void clearField()
	{
		drawWorldFrame(null);
	}
	

	/**
	 * Draws a grid for AI-development.
	 * 
	 * @param g
	 * @param columnSize
	 * @param rowSize
	 */
	public void drawGrid(Graphics2D g, Color c, int columnSize, int rowSize)
	{
		int rowSizePaint = (rowSize / 10);
		for (int i = 0; i <= FIELD_WIDTH + 3; i = i + rowSizePaint)
		{
			// --- paint halfway line ---
			g.setColor(c);
			g.setStroke(new BasicStroke(3));
			g.drawLine(FIELD_MARGIN + i, FIELD_MARGIN, FIELD_MARGIN + i, FIELD_HEIGHT + FIELD_MARGIN);
		}
		
		int columnSizePaint = (columnSize / 10);
		for (int i = 0; i <= FIELD_HEIGHT + 3; i = i + columnSizePaint)
		{
			g.setColor(c);
			g.setStroke(new BasicStroke(3));
			g.drawLine(FIELD_MARGIN, FIELD_MARGIN + i, FIELD_WIDTH + FIELD_MARGIN, FIELD_MARGIN + i);
		}
		

	}
	

	/**
	 * Draws the main-field.
	 */
	private void drawField(Graphics2D g)
	{
		// --- paint total field ---
		g.setColor(new Color(0, 180, 30));
		g.fillRect(0, 0, FIELD_TOTAL_WIDTH, FIELD_TOTAL_HEIGHT);
		
		// --- paint outline ---
		g.setColor(Color.white);
		g.drawRect(FIELD_MARGIN, FIELD_MARGIN, FIELD_TOTAL_WIDTH - 2 * FIELD_MARGIN, FIELD_TOTAL_HEIGHT - 2
				* FIELD_MARGIN);
		
		// --- paint halfway line ---
		g.setColor(Color.white);
		g.drawLine(FIELD_MARGIN, FIELD_TOTAL_HEIGHT / 2, FIELD_WIDTH + FIELD_MARGIN, FIELD_TOTAL_HEIGHT / 2);
		
		// --- paint halfway circle ---
		g.setColor(Color.white);
		g.drawOval(FIELD_TOTAL_WIDTH / 2 - FIELD_CIRCLE_RADIUS, FIELD_TOTAL_HEIGHT / 2 - FIELD_CIRCLE_RADIUS,
				FIELD_CIRCLE_RADIUS * 2, FIELD_CIRCLE_RADIUS * 2);
		
		// --- paint penalty area at top ---
		g.setColor(Color.white);
		g.drawArc(FIELD_TOTAL_WIDTH / 2 - FIELD_GOAL_MARGIN_PENALTYLINE / 2 - FIELD_GOAL_MARGIN_PENALTYAREA, FIELD_MARGIN
				- FIELD_GOAL_MARGIN_PENALTYAREA, FIELD_GOAL_MARGIN_PENALTYAREA * 2, FIELD_GOAL_MARGIN_PENALTYAREA * 2, 180,
				90);
		g.setColor(Color.white);
		g.drawArc(FIELD_TOTAL_WIDTH / 2 + FIELD_GOAL_MARGIN_PENALTYLINE / 2 - FIELD_GOAL_MARGIN_PENALTYAREA, FIELD_MARGIN
				- FIELD_GOAL_MARGIN_PENALTYAREA, FIELD_GOAL_MARGIN_PENALTYAREA * 2, FIELD_GOAL_MARGIN_PENALTYAREA * 2, 0,
				-90);
		g.setColor(Color.white);
		g.drawLine(FIELD_TOTAL_WIDTH / 2 - FIELD_GOAL_MARGIN_PENALTYLINE / 2, FIELD_MARGIN
				+ FIELD_GOAL_MARGIN_PENALTYAREA, FIELD_TOTAL_WIDTH / 2 + FIELD_GOAL_MARGIN_PENALTYLINE / 2, FIELD_MARGIN
				+ FIELD_GOAL_MARGIN_PENALTYAREA);
		
		// --- paint penalty area at top ---
		g.setColor(Color.white);
		g.drawArc(FIELD_TOTAL_WIDTH / 2 - FIELD_GOAL_MARGIN_PENALTYLINE / 2 - FIELD_GOAL_MARGIN_PENALTYAREA,
				FIELD_TOTAL_HEIGHT - FIELD_MARGIN - FIELD_GOAL_MARGIN_PENALTYAREA, FIELD_GOAL_MARGIN_PENALTYAREA * 2,
				FIELD_GOAL_MARGIN_PENALTYAREA * 2, 180, -90);
		g.setColor(Color.white);
		g.drawArc(FIELD_TOTAL_WIDTH / 2 + FIELD_GOAL_MARGIN_PENALTYLINE / 2 - FIELD_GOAL_MARGIN_PENALTYAREA,
				FIELD_TOTAL_HEIGHT - FIELD_MARGIN - FIELD_GOAL_MARGIN_PENALTYAREA, FIELD_GOAL_MARGIN_PENALTYAREA * 2,
				FIELD_GOAL_MARGIN_PENALTYAREA * 2, 0, 90);
		g.setColor(Color.white);
		g.drawLine(FIELD_TOTAL_WIDTH / 2 - FIELD_GOAL_MARGIN_PENALTYLINE / 2, FIELD_TOTAL_HEIGHT - FIELD_MARGIN
				- FIELD_GOAL_MARGIN_PENALTYAREA, FIELD_TOTAL_WIDTH / 2 + FIELD_GOAL_MARGIN_PENALTYLINE / 2,
				FIELD_TOTAL_HEIGHT - FIELD_MARGIN - FIELD_GOAL_MARGIN_PENALTYAREA);
		
		// --- paint goal at top ---
		g.setStroke(new BasicStroke(2));
		g.setColor(Color.gray);
		g.drawLine((FIELD_TOTAL_WIDTH - FIELD_GOAL_WIDTH) / 2, FIELD_MARGIN, (FIELD_TOTAL_WIDTH - FIELD_GOAL_WIDTH) / 2,
				FIELD_MARGIN - FIELD_GOAL_HEIGHT);
		g.drawLine((FIELD_TOTAL_WIDTH + FIELD_GOAL_WIDTH) / 2, FIELD_MARGIN, (FIELD_TOTAL_WIDTH + FIELD_GOAL_WIDTH) / 2,
				FIELD_MARGIN - FIELD_GOAL_HEIGHT);
		g.drawLine((FIELD_TOTAL_WIDTH - FIELD_GOAL_WIDTH) / 2, FIELD_MARGIN - FIELD_GOAL_HEIGHT,
				(FIELD_TOTAL_WIDTH + FIELD_GOAL_WIDTH) / 2, FIELD_MARGIN - FIELD_GOAL_HEIGHT);
		
		// --- paint goal at bottom ---
		g.setStroke(new BasicStroke(2));
		g.setColor(Color.gray);
		g.drawLine((FIELD_TOTAL_WIDTH - FIELD_GOAL_WIDTH) / 2, FIELD_TOTAL_HEIGHT - FIELD_MARGIN,
				(FIELD_TOTAL_WIDTH - FIELD_GOAL_WIDTH) / 2, FIELD_TOTAL_HEIGHT - FIELD_MARGIN + FIELD_GOAL_HEIGHT);
		g.drawLine((FIELD_TOTAL_WIDTH + FIELD_GOAL_WIDTH) / 2, FIELD_TOTAL_HEIGHT - FIELD_MARGIN,
				(FIELD_TOTAL_WIDTH + FIELD_GOAL_WIDTH) / 2, FIELD_TOTAL_HEIGHT - FIELD_MARGIN + FIELD_GOAL_HEIGHT);
		g.drawLine((FIELD_TOTAL_WIDTH - FIELD_GOAL_WIDTH) / 2, FIELD_TOTAL_HEIGHT - FIELD_MARGIN + FIELD_GOAL_HEIGHT,
				(FIELD_TOTAL_WIDTH + FIELD_GOAL_WIDTH) / 2, FIELD_TOTAL_HEIGHT - FIELD_MARGIN + FIELD_GOAL_HEIGHT);
		
	}
	

	/**
	 * Draws a ball on the field.
	 * 
	 * @param g graphics object
	 * @param x x-coordinate
	 * @param y y-coordinate
	 */
	private void drawBall(Graphics2D g, int x, int y, IVector2 vel)
	{
		int drawingX = (x / 10 + FIELD_TOTAL_WIDTH / 2) - BALL_RADIUS;
		int drawingY = (y / 10 + FIELD_TOTAL_HEIGHT / 2) - BALL_RADIUS;
		
		g.setColor(Color.orange);
		g.fillOval(drawingX, drawingY, BALL_RADIUS * 2, BALL_RADIUS * 2);
		
		if (showVelocity)
		{
			g.setColor(Color.cyan);
			g.setStroke(new BasicStroke(3));
			g.drawLine((int) ((drawingX + BALL_RADIUS)), (int) ((drawingY + BALL_RADIUS)),
					(int) ((drawingX + BALL_RADIUS) + vel.y() * 100), (int) ((drawingY + BALL_RADIUS) + vel.x() * 100));
		}
	}
	

	/**
	 * Draws a robot on the field.
	 * 
	 * @param g graphics object
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @param color yellow or blue
	 * @param id 1-12
	 */
	private void drawRobot(Graphics2D g, TrackedBot o, Color color, String id)
	{
		// --- get needed data ---
		int x = (int) o.pos.y;
		int y = (int) o.pos.x;
		float angle = o.angle;
		Vector2 vel = new Vector2(o.vel);
		Vector2 acc = new Vector2(o.acc);
		
		// --- check if robot has a valid color ---
		if (!color.equals(Color.yellow) && !color.equals(Color.blue))
		{
			log.debug("Can't paint robot. Wrong color (either blue or yellow)");
			return;
		}
		
		// --- determinate drawing-position ---
		int drawingX = 0;
		int drawingY = 0;
		
		// --- from SSLVision-mm to java2d-coordinates ---
		drawingX = (x / 10 + FIELD_TOTAL_WIDTH / 2) - ROBOT_RADIUS;
		drawingY = (y / 10 + FIELD_TOTAL_HEIGHT / 2) - ROBOT_RADIUS;
		
		// log.debug("orgi:" + x + "," + y);
		// log.debug("draw:" + drawingX + "," + drawingY);
		// log.debug("-----");
		
		// --- check and determinate id-length for margin ---
		int idX;
		int idY;
		
		if (id.length() == 1)
		{
			idX = drawingX + 5;
			idY = drawingY + 13;
		} else if (id.length() == 2)
		{
			idX = drawingX + 2;
			idY = drawingY + 13;
		} else
		{
			log.debug("Can't paint robot (id=" + id + ") . Wrong color (either blue or yellow)");
			return;
		}
		
		// --- draw bot-oval ---
		g.setColor(color);
		g.fillOval(drawingX, drawingY, ROBOT_RADIUS * 2, ROBOT_RADIUS * 2);
		g.setFont(new Font("Courier", Font.BOLD, 11));
		

		// --- draw id and direction-sign ---
		if (color.equals(Color.yellow))
		{
			
			g.setColor(Color.black);
			g.drawString(id, idX, idY);
			
			g.setColor(Color.red);
			g.drawLine((int) ((drawingX + ROBOT_RADIUS) + (5 * Math.sin(angle))),
					(int) ((drawingY + ROBOT_RADIUS) + (5 * Math.cos(angle))),
					(int) ((drawingX + ROBOT_RADIUS) + (9 * Math.sin(angle))),
					(int) ((drawingY + ROBOT_RADIUS) + (9 * Math.cos(angle))));
			
		} else if (color.equals(Color.blue))
		{
			

			g.setColor(Color.white);
			g.drawString(id, idX, idY);
			
			g.setColor(Color.yellow);
			g.setStroke(new BasicStroke(3));
			
			g.drawLine((int) ((drawingX + ROBOT_RADIUS) + (5 * Math.sin(angle))),
					(int) ((drawingY + ROBOT_RADIUS) + (5 * Math.cos(angle))),
					(int) ((drawingX + ROBOT_RADIUS) + (9 * Math.sin(angle))),
					(int) ((drawingY + ROBOT_RADIUS) + (9 * Math.cos(angle))));
			
		}
		
		// --- velocity ---
		if (showVelocity)
		{
			g.setColor(Color.cyan);
			g.setStroke(new BasicStroke(3));
			g.drawLine((int) ((drawingX + ROBOT_RADIUS)), (int) ((drawingY + ROBOT_RADIUS)),
					(int) ((drawingX + ROBOT_RADIUS) + vel.y * 100), (int) ((drawingY + ROBOT_RADIUS) + vel.x * 100));
		}
		
		// --- acceleration ---
		if (showAcceleration)
		{
			g.setColor(Color.magenta);
			g.setStroke(new BasicStroke(3));
			g.drawLine((int) ((drawingX + ROBOT_RADIUS)), (int) ((drawingY + ROBOT_RADIUS)),
					(int) ((drawingX + ROBOT_RADIUS) + acc.y * 100), (int) ((drawingY + ROBOT_RADIUS) + acc.x * 100));
		}
		

	}
	

	/**
	 * Draws a path.
	 * 
	 * @param g
	 * @param botPos
	 * @param path
	 */
	private void drawPath(Graphics2D g, IVector2 botPos, Path path)
	{
		
		int robotX = (int) (botPos.y() / 10 + FIELD_TOTAL_WIDTH / 2);
		int robotY = (int) (botPos.x() / 10 + FIELD_TOTAL_HEIGHT / 2);
		
		// --- draw waypoints ---
		if (showPaths)
		{
			g.setColor(Color.red);
			g.setStroke(new BasicStroke(2));
			GeneralPath drawPath = new GeneralPath();
			drawPath.moveTo(robotX, robotY);
			for (IVector2 point : path.path)
			{
				g.drawOval((int) (point.y() / 10 + FIELD_TOTAL_WIDTH / 2) - 1,
						(int) (point.x() / 10 + FIELD_TOTAL_HEIGHT / 2) - 1, 2, 2);
				drawPath.lineTo((int) (point.y() / 10 + FIELD_TOTAL_WIDTH / 2),
						(int) (point.x() / 10 + FIELD_TOTAL_HEIGHT / 2f));
			}
			g.draw(drawPath);
		}
		
		// draw splines
		if (showSplines)
		{
			XYSpline spline = path.getSpline();
			if (spline != null)
			{
				for (float t = 0; t < spline.getMaxTValue(); t += 20)
				{
					Vector2f pointToDraw = new Vector2f(spline.evaluateFunction(t));
					float curvature = Math.abs(spline.getCurvature(t)) * 300;
					if (curvature > 1)
						curvature = 1;
					
					float colorRed = curvature * 2;
					if (colorRed > 1)
					{
						colorRed = 1;
					}
					float colorGreen = 1 - curvature;
					colorGreen *= 2;
					if (colorGreen > 1)
					{
						colorGreen = 1;
					}
					
					g.setColor(new Color(colorRed, colorGreen, 0));
					// fip x and y since field is vertically drawn
					g.fillOval((int) pointToDraw.y() / 10 + FIELD_TOTAL_WIDTH / 2, (int) pointToDraw.x() / 10
							+ FIELD_TOTAL_HEIGHT / 2, 2, 2);
				}
				if (path.getPathGuiFeatures().getVirtualVehicle() != null)
				{
					Vector2f pointToDraw = new Vector2f(spline.evaluateFunction(path.getPathGuiFeatures()
							.getVirtualVehicle()));
					g.setColor(new Color(0, 100, 255));
					// fip x and y since field is vertically drawn
					g.fillOval((int) pointToDraw.y() / 10 + FIELD_TOTAL_WIDTH / 2, (int) pointToDraw.x() / 10
							+ FIELD_TOTAL_HEIGHT / 2, 4, 4);
				}
				
				IVector2 moveVec = path.getPathGuiFeatures().getCurrentMove();
				if (moveVec != null)
				{
					Vector2 line = moveVec.multiplyNew(100).addNew(botPos);
					g.setColor(new Color(50, 50, 50));
					g.drawLine(robotX, robotY, (int) line.y() / 10 + FIELD_TOTAL_WIDTH / 2, (int) line.x() / 10
							+ FIELD_TOTAL_HEIGHT / 2);
				}
			}
			
		}
	}
	
	// --------------------------------------------------------------
	// --- action listener ------------------------------------------
	// --------------------------------------------------------------
	
	protected class MouseEvents extends MouseAdapter
	{
		private int	mousePressedY	= 0;
		
		
		@Override
		public void mouseClicked(MouseEvent e)
		{
			int x = e.getX() - FIELD_MARGIN;
			int y = e.getY() - FIELD_MARGIN - fieldOriginY;
			
			x -= FIELD_WIDTH / 2;
			x *= 10;
			
			y -= FIELD_HEIGHT / 2;
			y *= 10;
			
			boolean ctrl = false;
			boolean alt = false;
			
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
			

			// --- notify observer ---
			synchronized (observers)
			{
				for (IFieldPanelObserver observer : observers)
				{
					observer.onFieldClick(y, x, ctrl, alt);
				}
			}
			
		}
		

		@Override
		public void mousePressed(MouseEvent e)
		{
			
			mousePressedY = e.getY();
			
		}
		

		@Override
		public void mouseDragged(MouseEvent e)
		{
			
			// --- move the field over the panel ---
			int dy = e.getY() - mousePressedY;
			fieldOriginY = fieldOriginY + dy;
			
			mousePressedY += dy;
			repaint();
			

		}
		
	}
	
	
	// --------------------------------------------------------------
	// --- setter/getter --------------------------------------------
	// --------------------------------------------------------------
	
	public void setTigersAreYellow(boolean tigersAreYellow)
	{
		if (tigersAreYellow)
		{
			tigersColor = Color.yellow;
			foeColor = Color.blue;
		} else
		{
			tigersColor = Color.blue;
			foeColor = Color.yellow;
		}
	}
	

	/**
	 * @return the paths
	 */
	public List<Path> getPaths()
	{
		return paths;
	}
	

	/**
	 * @param paths the paths to set
	 */
	public void setPaths(List<Path> paths)
	{
		this.paths = paths;
	}
	

	/**
	 * @return the debugPoints
	 */
	public List<IVector2> getDebugPoints()
	{
		return debugPoints;
	}
	

	/**
	 * @param debugPoints the debugPoints to set
	 */
	public void setDebugPoints(List<IVector2> debugPoints)
	{
		this.debugPoints = debugPoints;
	}
	

	/**
	 * @param defenseGoalPoints the defenseGoalPoints to set
	 */
	public void setDefensePoints(List<ValuePoint> defenseGoalPoints)
	{
		this.defenseGoalPoints = defenseGoalPoints;
	}
	

	/**
	 * @return the showVelocity
	 */
	public boolean isShowVelocity()
	{
		return showVelocity;
	}
	

	/**
	 * @param showVelocity the showVelocity to set
	 */
	public void setShowVelocity(boolean showVelocity)
	{
		this.showVelocity = showVelocity;
	}
	

	/**
	 * @return the showAcceleration
	 */
	public boolean isShowAcceleration()
	{
		return showAcceleration;
	}
	

	/**
	 * @param showAcceleration the showAcceleration to set
	 */
	public void setShowAcceleration(boolean showAcceleration)
	{
		this.showAcceleration = showAcceleration;
	}
	

	/**
	 * @return the showPositiongGrid
	 */
	public boolean isShowPositiongGrid()
	{
		return showPositiongGrid;
	}
	

	/**
	 * @param showPositiongGrid the showGrid to set
	 */
	public void setShowPositiongGrid(boolean showGrid)
	{
		this.showPositiongGrid = showGrid;
	}
	

	/**
	 * @return the showAnalysingGrid
	 */
	public boolean isShowAnalysingGrid()
	{
		return showAnalysingGrid;
	}
	

	/**
	 * @param showAnalysingGrid the showGrid to set
	 */
	public void setShowAnalysingGrid(boolean showAnalysingGrid)
	{
		this.showAnalysingGrid = showAnalysingGrid;
	}
	

	/**
	 * @return the showPaths
	 */
	public boolean isShowPaths()
	{
		return showPaths;
	}
	

	/**
	 * @param showPaths the showPaths to set
	 */
	public void setShowPaths(boolean showPaths)
	{
		this.showPaths = showPaths;
	}
	

	/**
	 * @return the showSplines
	 */
	public boolean isShowSplines()
	{
		return showSplines;
	}
	

	/**
	 * @param showSplines the showSplines to set
	 */
	public void setShowSplines(boolean showSplines)
	{
		this.showSplines = showSplines;
	}
	

	/**
	 * @return the showDebugPoints
	 */
	public boolean isShowDebugPoints()
	{
		return showDebugPoints;
	}
	

	/**
	 * @param showDebugPoints the showDebugPoints to set
	 */
	public void setShowDebugPoints(boolean showDebugPoints)
	{
		this.showDebugPoints = showDebugPoints;
	}
	

	/**
	 * @param showDefenseGoalPoints the showDefenseGoalPoints to set
	 */
	public void setShowDefenseGoalPoints(boolean showDefenseGoalPoints)
	{
		this.showDefenseGoalPoints = showDefenseGoalPoints;
	}
	

	/**
	 * @return the columnGridSize
	 */
	public int getColumnGridSize()
	{
		return columnGridSize;
	}
	

	/**
	 * @param columnGridSize the columnGridSize to set
	 */
	public void setColumnGridSize(int columnGridSize)
	{
		this.columnGridSize = columnGridSize;
	}
	

	/**
	 * @return the rowGridSize
	 */
	public int getRowGridSize()
	{
		return rowGridSize;
	}
	

	/**
	 * @param rowGridSize the rowGridSize to set
	 */
	public void setRowGridSize(int rowGridSize)
	{
		this.rowGridSize = rowGridSize;
	}
	

	public int getColumnAnalysingGridSize()
	{
		return columnAnalysingGridSize;
	}
	

	public void setColumnAnalysingGridSize(int columnAnalysingGridSize)
	{
		this.columnAnalysingGridSize = columnAnalysingGridSize;
	}
	

	public int getRowAnalysingGridSize()
	{
		return rowAnalysingGridSize;
	}
	

	public void setRowAnalysingGridSize(int rowAnalysingGridSize)
	{
		this.rowAnalysingGridSize = rowAnalysingGridSize;
	}
	

	public void setFieldSize(int width, int height)
	{
		FIELD_TOTAL_WIDTH = (int) (AIConfig.getGeometry().getFieldWidth() / 10) + FIELD_MARGIN * 2;
		FIELD_TOTAL_HEIGHT = (int) (AIConfig.getGeometry().getFieldLength() / 10 + FIELD_MARGIN * 2);
		FIELD_WIDTH = (int) (AIConfig.getGeometry().getFieldWidth() / 10);
		FIELD_HEIGHT = (int) (AIConfig.getGeometry().getFieldLength() / 10);
	}
}
