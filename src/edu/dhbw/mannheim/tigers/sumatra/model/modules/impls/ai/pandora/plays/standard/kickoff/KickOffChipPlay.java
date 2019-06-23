/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.04.2012
 * Author(s): Mark Geiger
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.kickoff;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.AStandardPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ChipKickRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.RedirectRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.ShooterV2Role;


/**
 * This play starts an direct shot ( chip or normal ) on the opponent goal. Play waits until
 * a referee 'READY' cmd is recieved.
 * 
 * (see also {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.IndirectShotV2Play})
 * 
 * @author Mark Geiger
 * 
 * 
 */
public class KickOffChipPlay extends AStandardPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// private static final Logger log = Logger.getLogger(KickOffChipPlay.class.getName());
	
	// ------------ variables to adjust the play -------------------------------- //
	
	private final int			firstBotChipKickDistance	= 2700;
	private final int			firstBotChipKickRoll			= 1000;
	private final float		passToRecOffset				= 65;
	private final float		aimingPoint						= 10;
	
	// ------------------------------------------------------------------------- //
	private ChipKickRole		chipShooter;
	private ShooterV2Role	directShooter;
	
	private PassSenderRole	sender;
	private RedirectRole		receiver							= new RedirectRole();
	
	private final MoveRole	move;
	private final MoveRole	moveLeft;
	private final MoveRole	moveLeft2;
	private final MoveRole	moveRight;
	private final MoveRole	moveRight2;
	private final MoveRole	moveLeft3;
	private final MoveRole	moveRight3;
	
	/** ready flag used for triggering referee 'READY' CMD */
	private boolean			refereeReady					= false;
	private boolean			enableRedirector				= false;
	
	private Vector2			passingDestLeft				= new Vector2(0,
																				-((AIConfig.getGeometry().getFieldWidth() / 2) - 200));
	private ValuePoint		chipShotTarget					= new ValuePoint(new Vector2(AIConfig.getGeometry()
																				.getPenaltyMarkTheir().x() - 400, 100));
	private ValuePoint		chipShotTargetLeft			= new ValuePoint(new Vector2(AIConfig.getGeometry()
																				.getPenaltyMarkTheir().x() - 400, 100));
	private ValuePoint		chipShotTargetRight			= new ValuePoint(new Vector2(AIConfig.getGeometry()
																				.getPenaltyMarkTheir().x() - 400, 100));
	private Vector2			passingDestRight				= new Vector2(0,
																				(AIConfig.getGeometry().getFieldWidth() / 2) - 200);
	private Vector2			defaultTarget					= new Vector2(
																				AIConfig.getGeometry().getPenaltyMarkTheir().x() - 400, 100);
	
	private int					numberOfRoles;
	
	private Desicion			desicion							= Desicion.CENTER;
	private Desicion			finalDesicion;
	
	enum Desicion
	{
		CENTER,
		LEFT,
		RIGHT;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public KickOffChipPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		numberOfRoles = numAssignedRoles;
		directShooter = new ShooterV2Role();
		move = new MoveRole(EMoveBehavior.NORMAL);
		moveLeft = new MoveRole(EMoveBehavior.DO_COMPLETE);
		moveLeft2 = new MoveRole(EMoveBehavior.NORMAL);
		moveRight2 = new MoveRole(EMoveBehavior.NORMAL);
		moveLeft3 = new MoveRole(EMoveBehavior.NORMAL);
		moveRight3 = new MoveRole(EMoveBehavior.NORMAL);
		moveRight = new MoveRole(EMoveBehavior.DO_COMPLETE);
		
		chipShooter = new ChipKickRole(new Vector2(), 0);
		
		for (int i = 0; i < numAssignedRoles; i++)
		{
			if (i == 0)
			{
				addAggressiveRole(move, AIConfig.getGeometry().getCenter().addNew(new Vector2(-200, 0)));
			} else if (i == 1)
			{
				addAggressiveRole(
						moveLeft,
						AIConfig.getGeometry().getCenter()
								.addNew(new Vector2(-100, -(AIConfig.getGeometry().getFieldWidth() / 4) + 250)));
			} else if (i == 2)
			{
				addAggressiveRole(
						moveRight,
						AIConfig.getGeometry().getCenter()
								.addNew(new Vector2(-100, (AIConfig.getGeometry().getFieldWidth() / 4) - 250)));
			}
		}
		numberOfRoles = numAssignedRoles;
		move.updateTargetAngle(0f);
		
		// wait for ever, until ready signal will reset timer
		setTimeout(Long.MAX_VALUE);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		
		frame.addDebugShape(new DrawableCircle(new Circle(passingDestLeft, 100), Color.red));
		frame.addDebugShape(new DrawableCircle(new Circle(passingDestRight, 100), Color.red));
		
		boolean freeWayToGoal = frame.getTacticalInfo().getTigersApproximateScoringChance();
		if (frame.tacticalInfo.getBestDirectShootTarget() != null)
		{
			// target to score with directShoot
			frame.addDebugShape(new DrawableCircle(new Circle(frame.getTacticalInfo().getBestDirectShootTarget(), 100),
					Color.MAGENTA));
		}
		
		for (int i = 0; i < numberOfRoles; i++)
		{
			
			if (i == 0)
			{
				chipShotTarget = AiMath.determinChipShotTarget(frame.worldFrame, 800, frame.worldFrame.getBall().getPos(),
						firstBotChipKickDistance);
				frame.addDebugShape(new DrawableCircle(new Circle(chipShotTarget, 100), Color.cyan));
			} else if (i == 1)
			{
				chipShotTargetLeft = AiMath.determinChipShotTarget(frame.worldFrame, 800, passingDestLeft,
						firstBotChipKickDistance);
				frame.addDebugShape(new DrawableCircle(new Circle(chipShotTargetLeft, 100), Color.cyan));
			} else if (i == 2)
			{
				chipShotTargetRight = AiMath.determinChipShotTarget(frame.worldFrame, 800, passingDestRight,
						firstBotChipKickDistance);
				frame.addDebugShape(new DrawableCircle(new Circle(chipShotTargetRight, 100), Color.cyan));
			}
		}
		
		desicion = determinDesicion(chipShotTarget, chipShotTargetLeft, chipShotTargetRight);
		
		if ((frame.refereeMsgCached != null) && (frame.refereeMsgCached.getCommand() == Command.NORMAL_START)
				&& (refereeReady == false))
		{
			// wird nur einmal ausgeführt
			refereeReady = true;
			enableRedirector = true;
			switch (desicion)
			{
				case CENTER:
					
					resetTimer();
					setTimeout(5);
					if (chipShotTarget.isZeroVector())
					{
						chipShotTarget = new ValuePoint(defaultTarget, 0);
					}
					
					chipShooter = new ChipKickRole(chipShotTarget, firstBotChipKickRoll);
					directShooter = new ShooterV2Role();
					if (freeWayToGoal)
					{
						switchRoles(move, directShooter, frame);
					} else
					{
						switchRoles(move, chipShooter, frame);
					}
					finalDesicion = Desicion.CENTER;
					break;
				case LEFT:
					sender = new PassSenderRole(AiMath.getBotKickerPos(moveLeft.getBot()).addNew(
							new Vector2(passToRecOffset, 0)));
					sender.setReceiverReady();
					switchRoles(move, sender, frame);
					switchRoles(moveLeft, moveLeft2, frame);
					if (numberOfRoles > 2)
					{
						switchRoles(moveRight, moveRight2, frame);
						moveRight2.updateDestination(passingDestRight);
					}
					finalDesicion = Desicion.LEFT;
					
					break;
				case RIGHT:
					sender = new PassSenderRole(AiMath.getBotKickerPos(moveRight.getBot()).addNew(
							new Vector2(passToRecOffset, 0)));
					sender.setReceiverReady();
					switchRoles(move, sender, frame);
					switchRoles(moveRight, moveRight2, frame);
					finalDesicion = Desicion.RIGHT;
					
					switchRoles(moveLeft, moveLeft2, frame);
					moveLeft2.updateDestination(passingDestLeft);
					
					break;
			}
			
		}
		
		if (refereeReady)
		{
			switch (finalDesicion)
			{
				case CENTER:
					break;
				
				
				case LEFT:
				{
					Vector2 bisector = GeoMath.calculateBisector(passingDestLeft, chipShotTargetLeft, AIConfig.getGeometry()
							.getGoalTheir().getGoalPostLeft().addNew(new Vector2(0, aimingPoint)));
					
					moveLeft2.updateDestination(passingDestLeft);
					moveLeft2.updateLookAtTarget(bisector);
					
					if (((GeoMath.distancePP(passingDestLeft, frame.worldFrame.ball.getPos()) < 900) && (GeoMath.distancePP(
							moveLeft2.getPos(), moveLeft2.getDestination()) < 800)) && (enableRedirector == true))
					{
						// Hier fahren
						if (numberOfRoles == 3)
						{
							moveRight3.updateDestination(passingDestLeft.addNew(new Vector2(-200, 100)));
							switchRoles(moveRight2, moveRight3, frame);
						}
						
						enableRedirector = false;
						receiver = new RedirectRole(passingDestLeft, true, true, false);
						switchRoles(moveLeft2, receiver, frame);
						resetTimer();
						setTimeout(2);
					} else if ((enableRedirector == false) && (numberOfRoles == 3))
					{
						moveRight3.updateDestination(passingDestLeft.addNew(new Vector2(-200, 100)));
					}
				}
					break;
				case RIGHT:
				{
					Vector2 bisector = GeoMath.calculateBisector(passingDestRight, chipShotTargetRight, AIConfig
							.getGeometry().getGoalTheir().getGoalPostRight().addNew(new Vector2(0, aimingPoint)));
					
					moveRight2.updateDestination(passingDestRight);
					moveRight2.updateLookAtTarget(bisector);
					
					if (((GeoMath.distancePP(passingDestRight, frame.worldFrame.ball.getPos()) < 900) && (GeoMath
							.distancePP(moveRight2.getPos(), moveRight2.getDestination()) < 800))
							&& (enableRedirector == true))
					{
						// Hier fahren der linke nach rechts
						moveLeft3.updateDestination(passingDestRight.addNew(new Vector2(-200, -100)));
						switchRoles(moveLeft2, moveLeft3, frame);
						
						enableRedirector = false;
						receiver = new RedirectRole(passingDestRight, true, true, false);
						switchRoles(moveRight2, receiver, frame);
						resetTimer();
						setTimeout(2);
					} else if (enableRedirector == false)
					{
						moveLeft3.updateDestination(passingDestRight.addNew(new Vector2(-200, -100)));
					}
				}
					break;
				default:
					break;
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void timedOut(AIInfoFrame currentFrame)
	{
		changeToFinished();
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		
		// nothing todo
	}
	
	
	private Desicion determinDesicion(ValuePoint center, ValuePoint left, ValuePoint right)
	{
		float c = center.value;
		float l = left.value;
		float r = right.value;
		
		c = c + 2; // Mitte wird bevorzugt.
		l = l + 1; // linke seite, der rechten bevorzugen, da besser calibriert
		
		List<Float> list = new ArrayList<Float>();
		list.add(c);
		list.add(l);
		list.add(r);
		Collections.sort(list);
		
		float result = list.get(list.size() - 1);
		
		if (Math.round(result) == Math.round(c))
		{
			return Desicion.CENTER;
		} else if ((Math.round(result) == Math.round(l)) && (numberOfRoles > 1))
		{
			return Desicion.LEFT;
		} else if ((Math.round(result) == Math.round(r)) && (numberOfRoles > 2))
		{
			return Desicion.RIGHT;
		}
		return Desicion.CENTER;
	}
}
