/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 27, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support;

import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clReleaseMemObject;
import static org.jocl.CL.clSetKernelArg;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;
import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ValuedField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.OpenClContext;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.OpenClHandler;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Calculate redirector positions on GPU
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectPosGPUCalc extends ACalculator implements IConfigObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger			log									= Logger.getLogger(RedirectPosGPUCalc.class
																									.getName());
	private OpenClContext					pm;
	
	@Configurable(comment = "RaySize for visibility checks")
	private static int						raySize								= 100;
	
	@Configurable(comment = "Number of points to check on x axis")
	private static int						numX									= 128;
	@Configurable(comment = "Number of points to check on y axis")
	private static int						numY									= 64;
	
	@Configurable(comment = "Distance to the Ball (Free Kicks)")
	private static int						distanceBallFreeKick				= 600;
	
	@Configurable
	private static int						fieldUpperBoundX					= 200;
	@Configurable
	private static int						fieldLowerBoundX					= -2000;
	
	@Configurable
	private static float						maxAngleTol							= AngleMath.PI_QUART;
	
	private boolean							recreateKernel						= false;
	@Configurable
	private static float						nearBallTol							= 500;
	@Configurable
	private static float						dist2BotsTol						= 100;
	@Configurable
	private static float						dist2BlockLineTol					= AIConfig.getGeometry().getBotRadius();
	@Configurable
	private static float						rotationWeight						= 0.3f;
	@Configurable
	private static float						dist2BotsWeight					= 0.05f;
	@Configurable
	private static float						p2pVisTargetWeight				= 0.2f;
	@Configurable
	private static float						p2pVisReceiverWeight				= 0.2f;
	@Configurable
	private static float						dist2BallWeight					= 0.05f;
	@Configurable
	private static float						dist2TargetWeight					= 0.1f;
	@Configurable
	private static float						dist2ShooterWeight				= 0.1f;
	@Configurable
	private static float						goalPostAngleWeight				= 0.1f;
	@Configurable
	private static float						visibilityOfOtherBotsWeight	= 1.0f;
	@Configurable
	private static float						blockPosWeight						= 0.2f;
	@Configurable
	private static float						blockPosDefenseWeight			= 0.5f;
	@Configurable
	private static float						directShootingLineWeight		= 0.2f;
	@Configurable
	private static float						freeZoneAroundPenalty			= 1000.0f;
	
	private static float						dist2TargetTol						= AIConfig.getGeometry().getPenaltyAreaTheir()
																									.getRadiusOfPenaltyArea()
																									+ AIConfig.getGeometry()
																											.getPenaltyAreaTheir()
																											.getLengthOfPenaltyAreaFrontLineHalf()
																									+ 50;
	
	private static float						dist2OurGoal						= AIConfig.getGeometry().getPenaltyAreaTheir()
																									.getRadiusOfPenaltyArea()
																									+ AIConfig.getGeometry()
																											.getPenaltyAreaTheir()
																											.getLengthOfPenaltyAreaFrontLineHalf()
																									+ freeZoneAroundPenalty;
	
	private int[]								gameStateID							= new int[1];
	
	private int[]								offenseOrDefense					= new int[1];
	private EOffenseOrDeffenseSupporter	offenseOrDefenseEnum;
	
	
	private enum GameStateID
	{
		RUNNING,
		DIST_BALL,
		DIST_BALL_BLOCK,
		DIST_BALL_OURFIELD,
		PENALTY
	}
	
	private enum EOffenseOrDeffenseSupporter
	{
		OFFENSE,
		DEFENSE
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public RedirectPosGPUCalc()
	{
		super();
		init();
		AIConfig.getMetisClient().addObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private void init()
	{
		dist2TargetTol = AIConfig.getGeometry().getPenaltyAreaTheir().getRadiusOfPenaltyArea()
				+ AIConfig.getGeometry().getPenaltyAreaTheir().getLengthOfPenaltyAreaFrontLineHalf();
		dist2OurGoal = AIConfig.getGeometry().getPenaltyAreaTheir().getRadiusOfPenaltyArea()
				+ AIConfig.getGeometry().getPenaltyAreaTheir().getLengthOfPenaltyAreaFrontLineHalf()
				+ freeZoneAroundPenalty;
		
		if (OpenClHandler.isOpenClSupported())
		{
			pm = new OpenClContext();
		} else
		{
			pm = null;
			return;
		}
		
		pm.loadSourceFile("data.h");
		
		pm.loadSource("#ifndef MPI\n#define M_PI " + AngleMath.PI + "\n#endif\n");
		
		// Enum EOffenseOrDeffenseSupporter
		pm.loadSource("#define OFFENSE_SUPPORTER " + EOffenseOrDeffenseSupporter.OFFENSE.ordinal() + "\n");
		pm.loadSource("#define DEFENSE_SUPPORTER " + EOffenseOrDeffenseSupporter.DEFENSE.ordinal() + "\n");
		
		// Enum GameStateID
		pm.loadSource("#define RUNNING " + GameStateID.RUNNING.ordinal() + "\n");
		pm.loadSource("#define DIST_BALL " + GameStateID.DIST_BALL.ordinal() + "\n");
		pm.loadSource("#define DIST_BALL_BLOCK " + GameStateID.DIST_BALL_BLOCK.ordinal() + "\n");
		pm.loadSource("#define DIST_BALL_OURFIELD " + GameStateID.DIST_BALL_OURFIELD.ordinal() + "\n");
		pm.loadSource("#define PENALTY " + GameStateID.PENALTY.ordinal() + "\n");
		
		// Sumatra const
		pm.loadSource("#define GOAL_WIDTH " + AIConfig.getGeometry().getGoalSize() + "\n");
		pm.loadSource("#define CENTER_CIRCLE_RADIUS " + AIConfig.getGeometry().getCenterCircleRadius() + "\n");
		pm.loadSource("#define FIELD_LENGTH " + AIConfig.getGeometry().getField().xExtend() + "\n");
		pm.loadSource("#define FIELD_WIDTH " + AIConfig.getGeometry().getField().yExtend() + "\n");
		pm.loadSource("#define BALL_RADIUS " + AIConfig.getGeometry().getBallRadius() + "\n");
		pm.loadSource("#define BOT_RADIUS " + AIConfig.getGeometry().getBotRadius() + "\n");
		pm.loadSource("#define DRIBBLER_DIST " + AIConfig.getGeometry().getBotCenterToDribblerDist() + "\n");
		pm.loadSource("#define PENALTY_LINE_THEIR_X " + AIConfig.getGeometry().getPenaltyLineTheir().x() + "\n");
		
		// Config Parameter
		pm.loadSource("#define DIST_BALL_FREEKICK " + distanceBallFreeKick + "\n");
		pm.loadSource("#define MAX_ANGLE_TOL " + maxAngleTol + "\n");
		pm.loadSource("#define RAY_SIZE " + raySize + "\n");
		pm.loadSource("#define FIELD_UPPER_BOUND_X " + fieldUpperBoundX + "\n");
		pm.loadSource("#define FIELD_LOWER_BOUND_X " + fieldLowerBoundX + "\n");
		pm.loadSource("#define NEAR_BALL ((float)" + nearBallTol + ")\n");
		pm.loadSource("#define DIST_2_BOTS ((float)" + dist2BotsTol + ")\n");
		pm.loadSource("#define DIST_2_TARGET ((float)" + dist2TargetTol + ")\n");
		pm.loadSource("#define DIST_2_OUR_GOAL ((float)" + dist2OurGoal + ")\n");
		pm.loadSource("#define DIST_2_BLOCKLINE ((float)" + dist2BlockLineTol + ")\n");
		
		// Config Parameter Weight
		pm.loadSource("#define W_ROTATION " + rotationWeight + "\n");
		pm.loadSource("#define W_DIST2BOTS " + dist2BotsWeight + "\n");
		pm.loadSource("#define W_P2P_VIS_TARGET " + p2pVisTargetWeight + "\n");
		pm.loadSource("#define W_P2P_VIS_RECEIVER " + p2pVisReceiverWeight + "\n");
		pm.loadSource("#define W_DIST_TARGET " + dist2TargetWeight + "\n");
		pm.loadSource("#define W_DIST_SHOOTER " + dist2ShooterWeight + "\n");
		pm.loadSource("#define W_DIST_BALL " + dist2BallWeight + "\n");
		pm.loadSource("#define W_BLOCK " + blockPosWeight + "\n");
		pm.loadSource("#define W_BLOCK_DEFENSE " + blockPosDefenseWeight + "\n");
		pm.loadSource("#define W_GOAL_POST_ANGLE " + goalPostAngleWeight + "\n");
		pm.loadSource("#define W_VIS_BOTS " + visibilityOfOtherBotsWeight + "\n");
		pm.loadSource("#define W_DSHOOT_LINE " + directShootingLineWeight + "\n");
		
		pm.loadSourceFile("GeoMath.c");
		pm.loadSourceFile("AiMath.c");
		pm.loadSourceFile("redirectPos.c");
		pm.createProgram("redirect_pos");
	}
	
	
	@Override
	public void onLoad(final HierarchicalConfiguration newConfig)
	{
	}
	
	
	@Override
	public void onReload(final HierarchicalConfiguration freshConfig)
	{
		recreateKernel = true;
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		EGameState gameState = newTacticalField.getGameState();
		
		if (pm == null)
		{
			return;
		}
		
		if (recreateKernel)
		{
			recreateKernel = false;
			log.info("Load new Kernel");
			pm.close();
			init();
		}
		
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		int numBots = wFrame.getBots().size();
		
		if (wFrame.getTigerBotsAvailable().isEmpty())
		{
			return;
		}
		
		int numB = wFrame.getTigerBotsAvailable().size();
		int botPosX[] = new int[numBots];
		int botPosY[] = new int[numBots];
		int ballPos[] = new int[2];
		gameStateID = getGameStateID(gameState);
		offenseOrDefense = getOffenseOrDeffenseForSupporters(newTacticalField);
		
		int botI = 0;
		for (TrackedTigerBot bot : wFrame.getTigerBotsAvailable().values())
		{
			botPosX[botI] = (int) bot.getPos().x();
			botPosY[botI] = (int) bot.getPos().y();
			botI++;
		}
		for (TrackedTigerBot bot : wFrame.getFoeBots().values())
		{
			if (!AIConfig.getGeometry().getFieldWBorders().isPointInShape(bot.getPos()))
			{
				continue;
			}
			botPosX[botI] = (int) bot.getPos().x();
			botPosY[botI] = (int) bot.getPos().y();
			botI++;
		}
		
		numBots = botI;
		
		ballPos[0] = (int) wFrame.getBall().getPos().x();
		ballPos[1] = (int) wFrame.getBall().getPos().y();
		
		float dstArray[] = new float[numY * numX * numB];
		Pointer dst = Pointer.to(dstArray);
		
		cl_mem memObjects[] = new cl_mem[7];
		
		memObjects[0] = clCreateBuffer(pm.getContext(), CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * numBots,
				Pointer.to(botPosX), null);
		memObjects[1] = clCreateBuffer(pm.getContext(), CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * numBots,
				Pointer.to(botPosY), null);
		memObjects[2] = clCreateBuffer(pm.getContext(), CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,
				Pointer.to(new int[] { numBots }), null);
		memObjects[3] = clCreateBuffer(pm.getContext(), CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 2,
				Pointer.to(ballPos), null);
		memObjects[4] = clCreateBuffer(pm.getContext(), CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,
				Pointer.to(gameStateID), null);
		memObjects[5] = clCreateBuffer(pm.getContext(), CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int,
				Pointer.to(offenseOrDefense), null);
		
		// output
		memObjects[6] = clCreateBuffer(pm.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float
				* numY * numX * numB, dst,
				null);
		
		// Set the arguments for the kernel
		clSetKernelArg(pm.getKernel(), 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
		clSetKernelArg(pm.getKernel(), 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
		clSetKernelArg(pm.getKernel(), 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
		clSetKernelArg(pm.getKernel(), 3, Sizeof.cl_mem, Pointer.to(memObjects[3]));
		clSetKernelArg(pm.getKernel(), 4, Sizeof.cl_mem, Pointer.to(memObjects[4]));
		clSetKernelArg(pm.getKernel(), 5, Sizeof.cl_mem, Pointer.to(memObjects[5]));
		clSetKernelArg(pm.getKernel(), 6, Sizeof.cl_mem, Pointer.to(memObjects[6]));
		
		
		long globalWorkSize[] = new long[] { numX, numY, numB };
		// long local_work_size[] = new long[] { 1, 1 };
		clEnqueueNDRangeKernel(pm.getCommandQueue(), pm.getKernel(), globalWorkSize.length, null, globalWorkSize, null,
				0, null, null);
		
		// Read the output data
		clEnqueueReadBuffer(pm.getCommandQueue(), memObjects[6], CL_TRUE, 0, Sizeof.cl_float * numX * numY * numB, dst,
				0, null, null);
		
		// Release kernel, program, and memory objects
		for (cl_mem memobj : memObjects)
		{
			clReleaseMemObject(memobj);
		}
		
		int offset = 0;
		for (TrackedTigerBot bot : wFrame.getTigerBotsAvailable().values())
		{
			newTacticalField.getSupportValues().put(bot.getId(), new ValuedField(dstArray, numX, numY, offset));
			offset += numX * numY;
		}
	}
	
	
	/**
	 * @param wFrame
	 * @return
	 */
	public Map<BotID, ValuedField> calc(final WorldFrame wFrame)
	{
		Map<BotID, ValuedField> result = new HashMap<BotID, ValuedField>();
		
		return result;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the numX
	 */
	public final int getNumX()
	{
		return numX;
	}
	
	
	/**
	 * @return the numY
	 */
	public final int getNumY()
	{
		return numY;
	}
	
	
	private final int[] getGameStateID(final EGameState gameState)
	{
		int[] result = new int[1];
		result[0] = GameStateID.RUNNING.ordinal();
		
		switch (gameState)
		{
			case TIMEOUT_WE:
			case TIMEOUT_THEY:
			case HALTED:
			case CORNER_KICK_WE:
			case GOAL_KICK_WE:
			case THROW_IN_WE:
			case DIRECT_KICK_WE:
			case RUNNING:
				result[0] = GameStateID.RUNNING.ordinal();
				break;
			
			case STOPPED:
				// 500mm distance to the ball
				result[0] = GameStateID.DIST_BALL.ordinal();
				break;
			
			case PREPARE_KICKOFF_THEY:
			case PREPARE_KICKOFF_WE:
				// our half of field, 500mm distance to te ball
				result[0] = GameStateID.DIST_BALL_OURFIELD.ordinal();
				break;
			
			case PREPARE_PENALTY_THEY:
			case PREPARE_PENALTY_WE:
				// 400mm behind penalty mark (getPenaltyLine.*())
				result[0] = GameStateID.PENALTY.ordinal();
				break;
			
			case CORNER_KICK_THEY:
			case GOAL_KICK_THEY:
			case THROW_IN_THEY:
			case DIRECT_KICK_THEY:
				// keep distance 500 mm from the ball.
				// set blockPos Weight high
				result[0] = GameStateID.DIST_BALL_BLOCK.ordinal();
				break;
			
			default:
				result[0] = GameStateID.RUNNING.ordinal();
				break;
		}
		return result;
	}
	
	
	private final int[] getOffenseOrDeffenseForSupporters(final TacticalField tacticalField)
	{
		EGameState gameState = tacticalField.getGameState();
		switch (gameState)
		{
			case TIMEOUT_WE:
			case TIMEOUT_THEY:
			case HALTED:
			case RUNNING:
				if ((offenseOrDefenseEnum == EOffenseOrDeffenseSupporter.DEFENSE)
						&& !tacticalField.getBallPossession().getEBallPossession().equals(EBallPossession.WE))
				{
					offenseOrDefenseEnum = EOffenseOrDeffenseSupporter.DEFENSE;
				} else
				{
					offenseOrDefenseEnum = EOffenseOrDeffenseSupporter.OFFENSE;
				}
				break;
			
			case STOPPED:
			case PREPARE_KICKOFF_WE:
			case CORNER_KICK_WE:
			case GOAL_KICK_WE:
			case THROW_IN_WE:
			case DIRECT_KICK_WE:
			case PREPARE_PENALTY_WE:
				offenseOrDefenseEnum = EOffenseOrDeffenseSupporter.OFFENSE;
				break;
			
			case PREPARE_KICKOFF_THEY:
			case PREPARE_PENALTY_THEY:
			case CORNER_KICK_THEY:
			case GOAL_KICK_THEY:
			case THROW_IN_THEY:
			case DIRECT_KICK_THEY:
				offenseOrDefenseEnum = EOffenseOrDeffenseSupporter.DEFENSE;
				break;
			default:
				offenseOrDefenseEnum = EOffenseOrDeffenseSupporter.OFFENSE;
				break;
		}
		int[] result = new int[1];
		result[0] = offenseOrDefenseEnum.ordinal();
		
		return result;
	}
}
