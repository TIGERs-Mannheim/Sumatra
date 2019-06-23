/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.08.2010
 * Author(s):
 * Gunther Berthold
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.area.PenaltyArea;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EMovingSpeed;


/**
 * This is a condition which can be used to check if a role/bot has reached its destination
 * and its view angle.
 * 
 * @author Oliver Steinbrecher
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MovementCon extends ACondition
{
	private final ViewAngleCondition		angleCon;
	private final DestinationCondition	destCon;
	
	/** role allowed to enter penalty area? Normally its not allowed! */
	private boolean							penaltyAreaAllowedOur	= false;
	private boolean							penaltyAreaAllowedTheir	= true;
	private boolean							isBallObstacle				= true;
	private boolean							isBotsObstacle				= true;
	private boolean							isGoalPostObstacle		= true;
	private boolean							armKicker					= false;
	private int									dribbleDuration			= 0;
	private float								forcePathAfterTime		= 0.2f;
	
	private float								speed							= -1;
	private EMovingSpeed						movingSpeed					= EMovingSpeed.NORMAL;
	
	private SimpleWorldFrame				latestWf						= SimpleWorldFrame.createEmptyWorldFrame(0);
	private BotID								botId							= BotID.createBotId();
	
	private boolean							refereeStop					= false;
	
	
	/**
	 * @return the refereeStop
	 */
	public boolean isRefereeStop()
	{
		return refereeStop;
	}
	
	
	/**
	 * @param refereeStop the refereeStop to set
	 */
	public void setRefereeStop(final boolean refereeStop)
	{
		this.refereeStop = refereeStop;
	}
	
	
	/**
	 * Modes for checking for free destination
	 */
	public enum EDestFreeMode
	{
		/**  */
		IGNORE,
		/**  */
		FREE_OF_TIGERS,
		/**  */
		FREE_OF_BOTS;
	}
	
	
	/**
	 * Creates a base MovementCon. Use the update methods to manipulate it.
	 * Note, that without updating anything, this condition is always true.
	 */
	public MovementCon()
	{
		super(ECondition.MOVEMENT);
		
		angleCon = new ViewAngleCondition();
		destCon = new DestinationCondition();
	}
	
	
	@Override
	public EConditionState doCheckCondition(final SimpleWorldFrame worldFrame, final BotID botId)
	{
		update(worldFrame, botId);
		
		// do the check first
		final EConditionState angleConCheck = angleCon.checkCondition(worldFrame, botId);
		final EConditionState destConCheck = destCon.checkCondition(worldFrame, botId);
		
		List<EConditionState> cons = new ArrayList<EConditionState>(3);
		cons.add(angleConCheck);
		cons.add(destConCheck);
		
		// after the check, the condition strings are current
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("destination:\n");
		strBuilder.append(destCon.getCondition());
		strBuilder.append("\nangle:\n");
		strBuilder.append(angleCon.getCondition());
		
		setCondition(strBuilder.toString());
		
		EConditionState finalState = EConditionState.FULFILLED;
		for (EConditionState state : cons)
		{
			if (state == EConditionState.BLOCKED)
			{
				// this has highest priority
				return EConditionState.BLOCKED;
			}
			if (state == EConditionState.PENDING)
			{
				// one is pending, so overall we are not done
				finalState = EConditionState.PENDING;
			}
		}
		
		return finalState;
	}
	
	
	@Override
	public void resetCache()
	{
		super.resetCache();
		destCon.resetCache();
		angleCon.resetCache();
	}
	
	
	@Override
	protected boolean compareContent(final ACondition condition)
	{
		final MovementCon con = (MovementCon) condition;
		return destCon.compare(con.getDestCon()) && angleCon.compare(con.getAngleCon());
	}
	
	
	/**
	 * Update dynamic targets/positions in this {@link MovementCon}
	 * 
	 * @param swf
	 */
	@Override
	public final void update(final SimpleWorldFrame swf, final BotID botId)
	{
		super.update(swf, botId);
		latestWf = swf;
		this.botId = botId;
		destCon.update(swf, botId);
		angleCon.update(swf, botId);
		angleCon.update(destCon.getDestination(), swf);
		resetCache();
		checkConstraints(swf, botId);
	}
	
	
	/**
	 * Check if all necessary constraints are fulfilled.
	 * - not in penalty area
	 * - distance to ball on referee cmd
	 * - not outside field
	 * - not equal to other bot position
	 * 
	 * @param wframe
	 * @param botId
	 */
	public final void checkConstraints(final SimpleWorldFrame wframe, final BotID botId)
	{
	}
	
	
	/**
	 * Check if destination is valid
	 * 
	 * @param latestWf
	 * @param botId
	 * @param dest
	 * @param penaltyAreaAllowedOur
	 * @param penaltyAreaAllowedTheir
	 */
	public static void assertDestinationValid(final SimpleWorldFrame latestWf, final BotID botId, final IVector2 dest,
			final boolean penaltyAreaAllowedOur, final boolean penaltyAreaAllowedTheir)
	{
		PenaltyArea penArea = AIConfig.getGeometry().getPenaltyAreaOur();
		if (null == botId)
		{
			assert penaltyAreaAllowedOur
					// || (latestWf.getBot(botId) == null)
					// || penArea.isPointInShape(latestWf.getBot(botId).getPos(), AIConfig.getGeometry().getBotRadius())
					|| !penArea.isPointInShape(dest, AIConfig.getGeometry()
							.getBotRadius()) : "Destination is inside PenaltyArea: " + dest;
		}
		else
		{
			assert penaltyAreaAllowedOur
					|| !penArea.isPointInShape(dest, AIConfig.getGeometry()
							.getBotRadius()) : "Destination of bot " + botId + " is inside PenaltyArea: " + dest;
		}
		// assert
		// // (latestWf.getBot(botId) == null)
		// // || !AIConfig.getGeometry().getFieldWBorders().isPointInShape(latestWf.getBot(botId).getPos())
		// AIConfig.getGeometry().getFieldWBorders().isPointInShape(dest) : "Destination is outside of field: "
		// + dest + " " + botId;
		if (botId != null)
		{
			for (TrackedTigerBot bot : latestWf.getBots().values())
			{
				assert bot.getId().equals(botId)
						|| !bot.getPos().equals(dest, 0.0001f) : "Destination is equal to other bot: " + dest + " "
						+ bot.getId() + "->" + botId;
			}
		}
		assert !latestWf.getBall().getPos().equals(dest, 0.0001f) : "Destination is equal to ball: " + dest
				+ " "
				+ latestWf.getBall();
	}
	
	
	/**
	 * @param destination
	 */
	public void updateDestination(final IVector2 destination)
	{
		if (destination == null)
		{
			throw new NullPointerException("destination is null!");
		}
		assertDestinationValid(latestWf, botId, destination, penaltyAreaAllowedOur,
				penaltyAreaAllowedTheir);
		destCon.updateDestination(destination);
		resetCache();
	}
	
	
	/**
	 * @param angle [rad]
	 */
	public void updateTargetAngle(final float angle)
	{
		angleCon.updateTargetAngle(angle);
		resetCache();
	}
	
	
	/**
	 * Updates the angle the bot should look at.
	 * 
	 * @param lookAtTarget
	 */
	public void updateLookAtTarget(final DynamicPosition lookAtTarget)
	{
		if (lookAtTarget == null)
		{
			throw new NullPointerException();
		}
		angleCon.updateLookAtTarget(lookAtTarget);
	}
	
	
	/**
	 * @param object
	 */
	public void updateLookAtTarget(final ATrackedObject object)
	{
		if (object == null)
		{
			throw new NullPointerException();
		}
		updateLookAtTarget(new DynamicPosition(object));
	}
	
	
	/**
	 * Updates the angle the bot should look at.
	 * 
	 * @param lookAtTarget
	 */
	public void updateLookAtTarget(final IVector2 lookAtTarget)
	{
		if (lookAtTarget == null)
		{
			throw new NullPointerException();
		}
		angleCon.updateLookAtTarget(new DynamicPosition(lookAtTarget));
	}
	
	
	/**
	 * @return the angleCon
	 */
	public ViewAngleCondition getAngleCon()
	{
		return angleCon;
	}
	
	
	/**
	 * @return the destCon
	 */
	public DestinationCondition getDestCon()
	{
		return destCon;
	}
	
	
	/**
	 * @return the penaltyAreaCheck
	 */
	public final boolean isPenaltyAreaAllowedOur()
	{
		return penaltyAreaAllowedOur;
	}
	
	
	/**
	 * @param penaltyAreaAllowed the penaltyAreaCheck to set
	 */
	public final void setPenaltyAreaAllowedOur(final boolean penaltyAreaAllowed)
	{
		penaltyAreaAllowedOur = penaltyAreaAllowed;
	}
	
	
	/**
	 * @return the penaltyAreaCheck
	 */
	public final boolean isPenaltyAreaAllowedTheir()
	{
		return penaltyAreaAllowedTheir;
	}
	
	
	/**
	 * @param penaltyAreaAllowed the penaltyAreaCheck to set
	 */
	public final void setPenaltyAreaAllowedTheir(final boolean penaltyAreaAllowed)
	{
		penaltyAreaAllowedTheir = penaltyAreaAllowed;
	}
	
	
	/**
	 * @return the isBallObstacle
	 */
	public final boolean isBallObstacle()
	{
		return isBallObstacle;
	}
	
	
	/**
	 * @param isBallObstacle the isBallObstacle to set
	 */
	public final void setBallObstacle(final boolean isBallObstacle)
	{
		this.isBallObstacle = isBallObstacle;
	}
	
	
	/**
	 * @return the speed [m/s]
	 */
	public final float getSpeed()
	{
		return speed;
	}
	
	
	/**
	 * @param movingSpeed give a hint to pathdrivers that have no acurate speed control (speed will be ignored in those
	 *           cases)
	 * @param speed the speed to set [m/s]
	 */
	public final void setSpeed(final EMovingSpeed movingSpeed, final float speed)
	{
		this.movingSpeed = movingSpeed;
		this.speed = speed;
	}
	
	
	/**
	 * @return the isBotsObstacle
	 */
	public boolean isBotsObstacle()
	{
		return isBotsObstacle;
	}
	
	
	/**
	 * @param isBotsObstacle the isBotsObstacle to set
	 */
	public void setBotsObstacle(final boolean isBotsObstacle)
	{
		this.isBotsObstacle = isBotsObstacle;
	}
	
	
	/**
	 * @param armKicker should bot arm kicker.
	 */
	public void setArmKicker(final boolean armKicker)
	{
		this.armKicker = armKicker;
	}
	
	
	/**
	 * @return is kicker armed.
	 */
	public boolean isArmKicker()
	{
		return armKicker;
	}
	
	
	/**
	 * @return the isGoalPostObstacle
	 */
	public boolean isGoalPostObstacle()
	{
		return isGoalPostObstacle;
	}
	
	
	/**
	 * @param isGoalPostObstacle the isGoalPostObstacle to set
	 */
	public void setGoalPostObstacle(final boolean isGoalPostObstacle)
	{
		this.isGoalPostObstacle = isGoalPostObstacle;
	}
	
	
	/**
	 * @return the dribbleDuration
	 */
	public final int getDribbleDuration()
	{
		return dribbleDuration;
	}
	
	
	/**
	 * @param dribbleDuration the dribbleDuration to set
	 */
	public final void setDribbleDuration(final int dribbleDuration)
	{
		this.dribbleDuration = dribbleDuration;
	}
	
	
	/**
	 * @param driveFast the driveFast to set
	 */
	public final void setDriveFast(final boolean driveFast)
	{
		movingSpeed = EMovingSpeed.FAST;
	}
	
	
	/**
	 * @return the movingSpeed
	 */
	public final EMovingSpeed getMovingSpeed()
	{
		return movingSpeed;
	}
	
	
	/**
	 * @return the forcePathAfterTime
	 */
	public final float getForcePathAfterTime()
	{
		return forcePathAfterTime;
	}
	
	
	/**
	 * @param forcePathAfterTime the forcePathAfterTime to set
	 */
	public final void setForcePathAfterTime(final float forcePathAfterTime)
	{
		this.forcePathAfterTime = forcePathAfterTime;
	}
}
