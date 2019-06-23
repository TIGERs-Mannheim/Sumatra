/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 27, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.support;

import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clReleaseMemObject;
import static org.jocl.CL.clSetKernelArg;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;
import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;
import org.json.simple.JSONValue;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.gpu.OpenClContext;
import edu.tigers.sumatra.gpu.OpenClHandler;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Calculate redirector positions on GPU
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectPosGPUCalc extends ACalculator implements IConfigObserver
{
	
	
	private static final Logger					log				= Logger
			.getLogger(RedirectPosGPUCalc.class
					.getName());
	private static final int						MAX_FOE_BOTS	= 6;
	private OpenClContext							pm;
	
	@Configurable(comment = "Number of points to check on x axis", category = "metis")
	private static int								numX				= 88;
	@Configurable(comment = "Number of points to check on y axis", category = "metis")
	private static int								numY				= 66;
	
	@Configurable(comment = "How many top values should be searched for?", category = "metis")
	private static int								numTopValues	= 100;
	
	
	private final cl_mem								memObjects[]	= new cl_mem[6];
	private boolean									runLastFrame	= false;
	private boolean									recreateKernel	= false;
	
	private final int									botPosX[]		= new int[MAX_FOE_BOTS];
	private final int									botPosY[]		= new int[MAX_FOE_BOTS];
	private final int									ballPos[]		= new int[2];
	private final int[]								paramsArr		= new int[EParameter.values().length];
	private final float[]							weightsArr		= new float[EScoringTypes.values().length];
	private float										dstArray[];
	
	private final Pointer							ptrBotPosX		= Pointer.to(botPosX);
	private final Pointer							ptrBotPosY		= Pointer.to(botPosY);
	private final Pointer							ptrBallPos		= Pointer.to(ballPos);
	private final Pointer							ptrParams		= Pointer.to(paramsArr);
	private final Pointer							ptrWeights		= Pointer.to(weightsArr);
	private Pointer									dst;
	
	private final Map<EParameter, Integer>		parameters		= new EnumMap<>(EParameter.class);
	private final Map<EScoringTypes, Double>	weights			= new EnumMap<>(EScoringTypes.class);
	
	@Configurable(comment = "[DEG] max reasonable redirect angle", category = "metis")
	private static int								maxAngle			= 120;
	@Configurable(comment = "[DEG] min reasonable redirect angle", category = "metis")
	private static int								minAngle			= 40;
	@Configurable(comment = "Max possible/reasonable passing range", category = "metis")
	private static int								maxDistBall		= 5000;
	@Configurable(comment = "Min possible/reasonable passing range", category = "metis")
	private static int								minDistBall		= 500;
	@Configurable(comment = "Max possible/reasonable shooting range", category = "metis")
	private static int								maxDistGoal		= 5000;
	@Configurable(comment = "Min possible/reasonable shooting range", category = "metis")
	private static int								minDistGoal		= 2000;
	@Configurable(comment = "ray size for visibility check (excluding bot radius)", category = "metis")
	private static int								visRaySize		= 500;
	@Configurable(comment = "Max possible/reasonable passing range", category = "metis")
	private static int								maxVisDist		= 6000;
	
	
	/**
	 */
	public enum EScoringTypes
	{
		/**  */
		DIST_TO_BALL,
		/**  */
		VIS_TO_BALL,
		/**  */
		ANGLE_BALL_GOAL,
		/**  */
		DIST_GOAL,
		/**  */
		VIS_GOAL,
		/**  */
		ANGLE_GOAL_VIEW
	}
	
	private enum EParameter
	{
		NUM_OPPONENTS,
		NUM_TIGERS,
		MAX_ANGLE,
		MIN_ANGLE,
		MAX_DIST_GOAL,
		MIN_DIST_GOAL,
		MAX_DIST_BALL,
		MIN_DIST_BALL,
		VIS_RAY_SIZE,
		MAX_VIS_DIST
	}
	
	
	/**
	 */
	public RedirectPosGPUCalc()
	{
		super();
		applyParameters();
		weights.put(EScoringTypes.ANGLE_BALL_GOAL, 0.0);
		weights.put(EScoringTypes.ANGLE_GOAL_VIEW, 0.0);
		weights.put(EScoringTypes.DIST_GOAL, 1.0);
		weights.put(EScoringTypes.DIST_TO_BALL, 0.0);
		weights.put(EScoringTypes.VIS_GOAL, 0.0);
		weights.put(EScoringTypes.VIS_TO_BALL, 0.0);
		String cfgName = SumatraModel.getInstance().getUserProperty(
				RedirectPosGPUCalc.class.getCanonicalName() + ".config",
				"default.cfg");
		loadWeights(cfgName);
		init();
		ConfigRegistration.registerConfigurableCallback("metis", this);
	}
	
	
	private void applyParameters()
	{
		parameters.put(EParameter.MAX_ANGLE, maxAngle);
		parameters.put(EParameter.MIN_ANGLE, minAngle);
		parameters.put(EParameter.MAX_DIST_BALL, maxDistBall);
		parameters.put(EParameter.MIN_DIST_BALL, minDistBall);
		parameters.put(EParameter.MAX_DIST_GOAL, maxDistGoal);
		parameters.put(EParameter.MIN_DIST_GOAL, minDistGoal);
		parameters.put(EParameter.VIS_RAY_SIZE, visRaySize);
		parameters.put(EParameter.MAX_VIS_DIST, maxVisDist);
		parameters.put(EParameter.NUM_TIGERS, 0);
		
		for (Map.Entry<EParameter, Integer> entry : parameters.entrySet())
		{
			paramsArr[entry.getKey().ordinal()] = entry.getValue();
		}
	}
	
	
	@Override
	public void deinit()
	{
		ConfigRegistration.unregisterConfigurableCallback("metis", this);
		if (runLastFrame)
		{
			// Release kernel, program, and memory objects
			for (cl_mem memobj : memObjects)
			{
				clReleaseMemObject(memobj);
			}
			if (pm != null)
			{
				pm.close();
				pm = null;
			}
		}
	}
	
	
	/**
	 * Update the given weight by adapting all either weights to that all weights
	 * are normalized again
	 * 
	 * @param type
	 * @param value
	 */
	public void updateWeight(final EScoringTypes type, final double value)
	{
		assert (value >= 0) && (value <= 1) : "Invalid value: " + value;
		double partialSum = weights.entrySet().stream().filter(e -> e.getKey() != type)
				.mapToDouble(e -> e.getValue()).sum();
		if (partialSum < 1e-6)
		{
			// reject weight change. All others already zero
			return;
		}
		// apply current value
		weights.put(type, value);
		// adapt other weights
		weights.entrySet().stream().filter(e -> e.getKey() != type)
				.forEach((e) -> e.setValue((e.getValue() / partialSum) * (1 - value)));
		// update array
		weights.entrySet().forEach(entry -> weightsArr[entry.getKey().ordinal()] = entry.getValue().floatValue());
	}
	
	
	/**
	 * @param name
	 */
	public void saveWeights(final String name)
	{
		String filename = name;
		if (!filename.endsWith(".cfg"))
		{
			filename += ".cfg";
		}
		String jsonString = JSONValue.toJSONString(weights);
		try
		{
			Files.write(Paths.get("config", "support", filename), jsonString.getBytes());
			SumatraModel.getInstance().setUserProperty(RedirectPosGPUCalc.class.getCanonicalName() + ".config", filename);
		} catch (IOException err)
		{
			log.error("Could not write weightings " + filename, err);
		}
	}
	
	
	/**
	 * @param name
	 */
	public void loadWeights(final String name)
	{
		try
		{
			byte[] bytes = Files.readAllBytes(Paths.get("config", "support", name));
			String jsonString = new String(bytes);
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) JSONValue.parse(jsonString);
			for (Map.Entry<String, Object> entry : map.entrySet())
			{
				Double val = ((Double) entry.getValue());
				EScoringTypes scType = EScoringTypes.valueOf(entry.getKey());
				weights.put(scType, val);
				weightsArr[scType.ordinal()] = val.floatValue();
			}
			SumatraModel.getInstance().setUserProperty(RedirectPosGPUCalc.class.getCanonicalName() + ".config", name);
		} catch (IOException err)
		{
			log.error("Could not read weights " + name, err);
		}
	}
	
	
	private void init()
	{
		if (OpenClHandler.isOpenClSupported())
		{
			assert pm == null;
			pm = new OpenClContext();
		} else
		{
			pm = null;
			return;
		}
		
		// normalize weights
		double sum = weights.values().stream().mapToDouble(w -> w).sum();
		weights.entrySet().forEach((e) -> e.setValue(e.getValue() / sum));
		
		for (Map.Entry<EScoringTypes, Double> entry : weights.entrySet())
		{
			weightsArr[entry.getKey().ordinal()] = entry.getValue().floatValue();
		}
		
		pm.loadSourceFile("data.h");
		
		pm.loadSource("#ifndef M_PI\n#define M_PI " + AngleMath.PI + "\n#endif\n");
		
		// Sumatra const
		pm.loadSource("#define GOAL_WIDTH " + Geometry.getGoalSize() + "\n");
		pm.loadSource("#define FIELD_LENGTH " + Geometry.getField().xExtend() + "\n");
		pm.loadSource("#define FIELD_WIDTH " + Geometry.getField().yExtend() + "\n");
		pm.loadSource("#define BALL_RADIUS " + Geometry.getBallRadius() + "\n");
		pm.loadSource("#define BOT_RADIUS " + Geometry.getBotRadius() + "\n");
		pm.loadSource("#define PEN_AREA_RADIUS "
				+ (Geometry.getPenaltyAreaTheir().getRadiusOfPenaltyArea()
						+ Geometry.getPenaltyAreaTheir().getLengthOfPenaltyAreaFrontLineHalf() + Geometry.getBotRadius())
				+ "\n");
		
		pm.loadSource("#define NUM_SCORES " + EScoringTypes.values().length + "\n");
		
		for (EParameter param : EParameter.values())
		{
			pm.loadSource("#define " + param.name() + " " + param.ordinal() + "\n");
		}
		
		for (EScoringTypes st : EScoringTypes.values())
		{
			pm.loadSource("#define " + st.name() + " " + st.ordinal() + "\n");
		}
		
		pm.loadSourceFile("GeoMath.c");
		pm.loadSourceFile("AiMath.c");
		pm.loadSourceFile("redirectPos.c");
		
		pm.loadSourceFile("non_max_sup.c");
		
		boolean created = pm.createProgram("redirect_pos") != null;
		if (!created)
		{
			log.error("Could not create kernel!");
			pm = null;
			return;
		}
		
		dstArray = new float[numY * numX];
		dst = Pointer.to(dstArray);
		
		// input args
		memObjects[0] = clCreateBuffer(pm.getContext(), CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int
				* MAX_FOE_BOTS,
				ptrBotPosX, null);
		memObjects[1] = clCreateBuffer(pm.getContext(), CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int
				* MAX_FOE_BOTS,
				ptrBotPosY, null);
		memObjects[2] = clCreateBuffer(pm.getContext(), CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 2,
				ptrBallPos, null);
		memObjects[3] = clCreateBuffer(pm.getContext(), CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int
				* paramsArr.length,
				ptrParams, null);
		memObjects[4] = clCreateBuffer(pm.getContext(), CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float
				* weightsArr.length,
				ptrWeights, null);
		
		// output
		memObjects[5] = clCreateBuffer(pm.getContext(), CL.CL_MEM_READ_WRITE | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float
				* numY * numX, dst,
				null);
		
		// Set the arguments for the kernel
		for (int i = 0; i < 6; i++)
		{
			clSetKernelArg(pm.getKernel(), i, Sizeof.cl_mem, Pointer.to(memObjects[i]));
		}
	}
	
	
	@Override
	public void afterApply(final IConfigClient configClient)
	{
		applyParameters();
		recreateKernel = true;
	}
	
	
	private static int[] findTopKHeap(final double[] arr, final int k)
	{
		PriorityQueue<KeyValue> pq = new PriorityQueue<>();
		for (int i = 0; i < arr.length; i++)
		{
			if (pq.size() < k)
			{
				pq.add(new KeyValue(i, arr[i]));
			} else if (pq.peek().f > arr[i])
			{
				pq.poll();
				pq.add(new KeyValue(i, arr[i]));
			}
		}
		int[] res = new int[k];
		for (int i = 0; i < k; i++)
		{
			res[i] = pq.poll().i;
		}
		return res;
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (pm == null)
		{
			init();
			return;
		}
		
		if (recreateKernel)
		{
			recreateKernel = false;
			log.info("Load new Kernel");
			pm.close();
			pm = null;
			init();
		}
		
		if (runLastFrame)
		{
			// Read the output data
			clEnqueueReadBuffer(pm.getCommandQueue(), memObjects[5], CL_TRUE, 0, Sizeof.cl_float * dstArray.length,
					dst, 0, null, null);
			
			double[] arr = new double[dstArray.length];
			for (int i = 0; i < dstArray.length; i++)
			{
				arr[i] = dstArray[i];
			}
			
			ValuedField field = new ValuedField(arr, numX, numY, 0, Geometry.getFieldLength(), Geometry.getFieldWidth());
			newTacticalField.setSupporterValuedField(field);
			newTacticalField.getDrawableShapes().get(EShapesLayer.GPU_GRID).add(field);
			
			int[] indexes = findTopKHeap(arr, numTopValues);
			
			List<IDrawableShape> shapes = newTacticalField.getDrawableShapes().get(EShapesLayer.TOP_GPU_GRID);
			for (int i = 0; i < numTopValues; i++)
			{
				int x = indexes[i] % numX;
				int y = indexes[i] / numX;
				IVector2 p = field.getPointOnField(x, y);
				int alpha = Math.max(35, Math.min(120, (int) (140 - (((i * 100) / ((double) numTopValues)) * 2.5)) - 50));
				
				DrawableCircle dc = new DrawableCircle(
						new Circle(p, 100 - ((i * 100) / ((double) numTopValues))), new Color(255, 10, 000, alpha));
				dc.setFill(true);
				shapes.add(dc);
				newTacticalField.getTopGpuGridPositions().add(p);
			}
		}
		runLastFrame = false;
		
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		
		ballPos[0] = (int) wFrame.getBall().getPos().x();
		ballPos[1] = (int) wFrame.getBall().getPos().y();
		
		int numBots = 0;
		for (ITrackedBot bot : wFrame.getFoeBots().values())
		{
			if (!Geometry.getFieldWBorders().isPointInShape(bot.getPos()))
			{
				continue;
			}
			if (numBots >= MAX_FOE_BOTS)
			{
				break;
			}
			botPosX[numBots] = (int) bot.getPos().x();
			botPosY[numBots] = (int) bot.getPos().y();
			numBots++;
		}
		
		paramsArr[EParameter.NUM_OPPONENTS.ordinal()] = numBots;
		
		CL.clEnqueueWriteBuffer(pm.getCommandQueue(), memObjects[0], CL.CL_FALSE, 0, Sizeof.cl_int
				* MAX_FOE_BOTS, ptrBotPosX, 0, null, null);
		CL.clEnqueueWriteBuffer(pm.getCommandQueue(), memObjects[1], CL.CL_FALSE, 0, Sizeof.cl_int
				* MAX_FOE_BOTS, ptrBotPosY, 0, null, null);
		CL.clEnqueueWriteBuffer(pm.getCommandQueue(), memObjects[2], CL.CL_FALSE, 0, Sizeof.cl_int
				* 2, ptrBallPos, 0, null, null);
		CL.clEnqueueWriteBuffer(pm.getCommandQueue(), memObjects[3], CL.CL_FALSE, 0, Sizeof.cl_int
				* paramsArr.length, ptrParams, 0, null, null);
		CL.clEnqueueWriteBuffer(pm.getCommandQueue(), memObjects[4], CL.CL_FALSE, 0, Sizeof.cl_float
				* weightsArr.length, ptrWeights, 0, null, null);
		
		long globalWorkSize[] = new long[] { numX, numY };
		clEnqueueNDRangeKernel(pm.getCommandQueue(), pm.getKernel(), globalWorkSize.length, null, globalWorkSize, null,
				0, null, null);
		
		runLastFrame = true;
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
	
	
	/**
	 * @return the weights
	 */
	public final Map<EScoringTypes, Double> getWeights()
	{
		return weights;
	}
	
	
	private static class KeyValue implements Comparable<KeyValue>
	{
		private final int		i;
		private final double	f;
		
		
		private KeyValue(final int i, final double f)
		{
			this.i = i;
			this.f = f;
		}
		
		
		@Override
		public int compareTo(final KeyValue o)
		{
			return Double.compare(o.f, f);
		}
		
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(f);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			result = (prime * result) + i;
			return result;
		}
		
		
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			KeyValue other = (KeyValue) obj;
			if (Double.doubleToLongBits(f) != Double.doubleToLongBits(other.f))
			{
				return false;
			}
			if (i != other.i)
			{
				return false;
			}
			return true;
		}
	}
}
