/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.09.2011
 * Author(s): stei_ol
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math;

import static org.jocl.CL.CL_CONTEXT_PLATFORM;
import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clBuildProgram;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clCreateCommandQueue;
import static org.jocl.CL.clCreateContext;
import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clCreateProgramWithSource;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clReleaseCommandQueue;
import static org.jocl.CL.clReleaseContext;
import static org.jocl.CL.clReleaseKernel;
import static org.jocl.CL.clReleaseMemObject;
import static org.jocl.CL.clReleaseProgram;
import static org.jocl.CL.clSetKernelArg;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.jocl.CL;
import org.jocl.CLException;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;


/**
 * Helper class for parallel math problems.
 * 
 * @author PhilippP
 * 
 */
public final class ParallelMath
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants
	// ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private String					kernelString	= "";
	private cl_mem[]				memObjects;
	private cl_command_queue	commandQueue;
	private cl_context			context;
	private cl_kernel				kernel;
	private Logger					logger			= Logger.getLogger(ParallelMath.class);
	private boolean				cl_device		= true;
	
	
	// --------------------------------------------------------------------------
	// --- constructors
	// ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * A Class to make calculatione on the GPU, for each GPU-Algorhitem there is a CPU variante.
	 * 
	 * @param kind - calculation with Manalobis or Standard
	 * @param mode - On CPU or on GPU
	 * @throws org.jocl.CLException - if a error occures while initialising the cl device
	 * 
	 */
	public ParallelMath(int kind, int mode) throws CLException
	{
		if (mode == 1)
		{
			try
			{
				String kernelName = "fieldRaster_v3";
				if (kind == 1)
				{
					kernelName = "fieldRaster_v3";
					
				} else if (kind == 2)
				{
					kernelName = "fieldRaster_v1";
				}
				loadKernel(kernelName + ".cl");
				
				// The platform, device type and device number
				// that will be used
				final int platformIndex = 0;
				// FIXME PhilippP über die config auslesen op cpu oder gpu device verwnedet werden soll
				final long deviceType = CL.CL_DEVICE_TYPE_GPU;
				final int deviceIndex = 0;
				
				// Enable exceptions and subsequently omit error checks in this sample
				CL.setExceptionsEnabled(true);
				
				// Obtain the number of platforms
				int numPlatformsArray[] = new int[1];
				clGetPlatformIDs(0, null, numPlatformsArray);
				int numPlatforms = numPlatformsArray[0];
				
				// Obtain a platform ID
				cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
				clGetPlatformIDs(platforms.length, platforms, null);
				cl_platform_id platform = platforms[platformIndex];
				
				// Initialize the context properties
				cl_context_properties contextProperties = new cl_context_properties();
				contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
				
				// Obtain the number of devices for the platform
				int numDevicesArray[] = new int[1];
				clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
				int numDevices = numDevicesArray[0];
				
				// Obtain a device ID
				cl_device_id devices[] = new cl_device_id[numDevices];
				clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
				cl_device_id device = devices[deviceIndex];
				
				// Create a context for the selected device
				context = clCreateContext(contextProperties, 1, new cl_device_id[] { device }, null, null, null);
				
				// Create a command-queue for the selected device
				commandQueue = clCreateCommandQueue(context, device, 0, null);
				
				// Create the program from the source code
				cl_program program = clCreateProgramWithSource(context, 1, new String[] { kernelString }, null, null);
				
				// Build the program
				clBuildProgram(program, 0, null, null, null, null);
				// Create the kernel
				kernel = clCreateKernel(program, kernelName, null);
				clReleaseProgram(program);
				
			} catch (CLException e)
			{
				logger.error("Error while initialising FieldAnalyser, possible no Open_Cl device in the system", e);
				cl_device = false;
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods
	// --------------------------------------------------------------
	// -------------------------------------------------------------------------
	
	/**
	 * Calculate the FieldRaster after the assignment form OliverS and PaulB on
	 * the GPU
	 * 
	 * @param botPos
	 *           - Position of all bots in the field
	 * @param botOurCount - Count of our bots
	 * @param m
	 *           - Rectangle count in the x-axis of the fieldRaster
	 * @param n
	 *           - Rectangle count in the y-axis of the fieldRaster
	 * @return fieldraster
	 */
	public float[] calculateFieldRasterGPU(int[] botPos, int botOurCount, int m, int n)
	{
		// If no Open_Cl device is exisiting
		if (cl_device == false)
		{
			return calculateFieldRasterCPU(botPos, botOurCount, m, n);
		}
		// Create input- and output data
		float dstArray[] = new float[m * n];
		
		Pointer srcA = Pointer.to(botPos);
		Pointer dst = Pointer.to(dstArray);
		// Allocate the memory objects for the input- and output data
		cl_mem memObjects[] = new cl_mem[3];
		memObjects[0] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * botPos.length,
				srcA, null);
		// TODO PhilippP muss nicht als Memory Obejct üebgeben werden, siehe JOCLMandelbrot example
		memObjects[1] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 4,
				Pointer.to(new int[] { m, n, botPos.length, botOurCount }), null);
		memObjects[2] = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_float * m * n, null, null);
		
		// Set the arguments for the kernel
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
		
		// Set the work-item dimensions
		long global_work_size[] = new long[] { m * n };
		long local_work_size[] = new long[] { 256 };
		// Execute the kernel
		clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size, local_work_size, 0, null, null);
		
		// Read the output data
		clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0, Sizeof.cl_float * n * m, dst, 0, null, null);
		
		// Release kernel, program, and memory objects
		for (cl_mem memobj : memObjects)
		{
			clReleaseMemObject(memobj);
		}
		
		return dstArray;
	}
	
	
	/**
	 * Calculate the FieldRaster with the MalanobisDistance for each bot on the
	 * GPU
	 * 
	 * @param botPos
	 *           - Position of all bots in the field
	 * @param botSpeed
	 *           - Speedvectors from the bots (paired 0,1 2,3 ...)
	 * @param botOurCount
	 *           - Count of our bots in use
	 * @param m
	 *           - Rectangle count in the x-axis of the fieldRaster
	 * @param n
	 *           - Rectangle count in the y-axis of the fieldRaster
	 * @return fieldraster
	 */
	public float[] calculateManalobisDistanceFieldRasterGPU(int[] botPos, float[] botSpeed, int botOurCount, int m, int n)
	{
		// If no Open_Cl device is exisiting
		if (cl_device == false)
		{
			return calculateManalobisDistanceFieldRasterCPU(botPos, botSpeed, botOurCount, m, n);
		}
		float[] dstArray = new float[m * n];
		Pointer dst = Pointer.to(dstArray);
		Pointer srcA = Pointer.to(botPos);
		Pointer srcB = Pointer.to(botSpeed);
		
		memObjects = new cl_mem[4];
		memObjects[0] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * botPos.length,
				srcA, null);
		memObjects[1] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_float
				* botSpeed.length, srcB, null);
		memObjects[2] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 4,
				Pointer.to(new int[] { m, n, botSpeed.length, botOurCount }), null);
		memObjects[3] = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_float * m * n, null, null);
		
		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
		clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
		clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
		clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(memObjects[3]));
		
		// Set the work-item dimensions
		long global_work_size[] = new long[] { m * n };
		long local_work_size[] = new long[] { 256 };
		
		// Execute the kernel
		clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size, local_work_size, 0, null, null);
		
		// Read the output data
		clEnqueueReadBuffer(commandQueue, memObjects[3], CL_TRUE, 0, Sizeof.cl_float * n * m, dst, 0, null, null);
		
		for (cl_mem memobj : memObjects)
		{
			clReleaseMemObject(memobj);
		}
		
		return dstArray;
	}
	
	
	/**
	 * Calculate the FieldRaster with the MalanobisDistance for each bot
	 * 
	 * @param botPos
	 *           - Position of all bots in the field
	 * @param botSpeed
	 *           - Speedvectors from the bots (paired 0,1 2,3 ...)
	 * @param botOurCount
	 *           - coutn of our bots
	 * @param m
	 *           - Rectangle count in the x-axis of the fieldRaster
	 * @param n
	 *           - Rectangle count in the y-axis of the fieldRaster
	 * @return fieldraster
	 */
	public float[] calculateManalobisDistanceFieldRasterCPU(int[] botPos, float[] botSpeed, int botOurCount, int m, int n)
	{
		float[] dstArray = new float[m * n];
		for (int gid = 0; gid < dstArray.length; gid++)
		{
			float shortestTiger = Float.MAX_VALUE;
			float shortestFoe = Float.MAX_VALUE;
			
			for (int i = 0; i < botPos.length; i++)
			{
				/* m koordinate des goalArray */
				/* n koordinate des goalArray */
				float goalArray_n = 0;
				float goalArray_m = gid;
				if (gid >= n)
				{
					goalArray_n = gid / n;
					goalArray_m = gid - (goalArray_n * n);
				}
				
				// Geschwindigkeitsvektor des Roboters
				float vX = botSpeed[2 * i];
				float vY = botSpeed[(2 * i) + 1];
				float vS = (float) Math.sqrt((vX * vX) + (vY * vY));
				
				/* ID des Rechtecks in dem der Bot steht */
				/* n koordinate des Rechteks */
				/* m koordinate des Rechtecks */
				int recID = botPos[i];
				float rec_n = 0;
				float rec_m = recID;
				if (recID >= n)
				{
					rec_n = recID / n;
					rec_m = recID - (rec_n * n);
				}
				
				if (vS > 0.001)
				{
					float a = (float) Math.acos(vX / (Math.sqrt((vX * vX) + (vY * vY))));
					// Fallunterscheidung ob Negativ oder Positv Rotiertwerden
					// muss
					if (vY > 0)
					{
						a = -a;
					}
					
					float[] rotMatrix = { (float) Math.cos(a), (float) -Math.sin(a), -rec_n, (float) Math.sin(a),
							(float) Math.cos(a), -rec_m, 0, 0, 1 };
					
					float temp = rec_n;
					rec_n = (rotMatrix[0] * rec_n) + (rotMatrix[1] * rec_m) + (rotMatrix[2] * 1);
					rec_m = (rotMatrix[3] * temp) + (rotMatrix[4] * rec_m) + (rotMatrix[5] * 1);
					temp = goalArray_n;
					goalArray_n = (rotMatrix[0] * goalArray_n) + (rotMatrix[1] * goalArray_m) + (rotMatrix[2] * 1);
					goalArray_m = (rotMatrix[3] * temp) + (rotMatrix[4] * goalArray_m) + (rotMatrix[5] * 1);
				}
				// --------
				
				
				float[] s_neg = { 50 + (20 * vS), 0, 0, 50 - (25 * vS) };
				if (s_neg[3] <= 1)
				{
					s_neg[3] = 1;
				}
				if (s_neg[0] > 150)
				{
					s_neg[0] = 150;
				}
				
				float[] rVec = new float[] { rec_n - goalArray_n, rec_m - goalArray_m };
				
				float x = (rVec[0] * s_neg[0]) + (rVec[1] * s_neg[2]);
				float y = (rVec[0] * s_neg[1]) + (rVec[1] * s_neg[3]);
				
				float temp = (float) Math.sqrt((x * rVec[0]) + (y * rVec[1]));
				
				if (i < botOurCount)
				{
					if (temp < shortestTiger)
					{
						shortestTiger = temp;
					}
				} else
				{
					if (temp < shortestFoe)
					{
						shortestFoe = temp;
					}
				}
			}
			dstArray[gid] = (shortestFoe - shortestTiger);
		}
		
		return dstArray;
	}
	
	
	/**
	 * Calculate the FieldRaster after the assignment form @OliverS and @PaulB
	 * on the CPU
	 * 
	 * @param botPos
	 *           - Position of all bots in the field
	 * @param outBots - Count of our bots
	 * @param m
	 *           - Rectangle count in the x-axis of the fieldRaster
	 * @param n
	 *           - Rectangle count in the y-axis of the fieldRaster
	 * @return fieldraster
	 */
	public float[] calculateFieldRasterCPU(int[] botPos, int outBots, int m, int n)
	{
		float[] destArray = new float[m * n];
		
		int k = botPos.length;
		
		for (int gid = 0; gid < (m * n); gid++)
		{
			float shortestTiger = Float.MAX_VALUE;
			float shortestFoe = Float.MAX_VALUE;
			for (int i = 0; i < k; ++i)
			{
				/* ID des Rechtecks */
				int recID = botPos[i];
				/* m koordinate des Rechtecks */
				int rec_n = 0;
				/* n koordinate des Rechteks */
				int rec_m = recID;
				if (recID >= n)
				{
					rec_n = recID / n;
					rec_m = recID - (rec_n * n);
				}
				/* m koordinate des goalArray */
				/* n koordinate des goalArray */
				int goalArray_n = 0;
				int goalArray_m = gid;
				if (gid >= n)
				{
					goalArray_n = gid / n;
					goalArray_m = gid - (goalArray_n * n);
				}
				int x = rec_m - goalArray_m;
				int y = rec_n - goalArray_n;
				
				/* Berechnen des Abstandes von goalArray und recPos */
				float value = (float) (Math.sqrt(((x * x) + (y * y))));
				
				if (i < outBots)
				{
					if (value < shortestTiger)
					{
						shortestTiger = value;
					}
				} else
				{
					if (value < shortestFoe)
					{
						shortestFoe = value;
					}
				}
			}
			destArray[gid] = shortestFoe - shortestTiger;
			
		}
		return destArray;
	}
	
	
	/**
	 * Helper function which reads the file with the given name and set the
	 * kernel for {@link ParallelMath}. Will set the kernel "" if the file can
	 * not be read.
	 * 
	 * @param kernelName
	 *           The name of the kernel to read.
	 */
	public void loadKernel(String kernelName)
	{
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new InputStreamReader(new FileInputStream("./kernels/" + kernelName)));
			StringBuffer sb = new StringBuffer();
			String line = null;
			while (true)
			{
				line = br.readLine();
				if (line == null)
				{
					break;
				}
				sb.append(line).append("\n");
			}
			br.close();
			kernelString = sb.toString();
		} catch (IOException e)
		{
			try
			{
				if (br != null)
				{
					br.close();
				}
			} catch (IOException ex)
			{
			}
			kernelString = "";
		}
	}
	
	
	/**
	 * Release all taken resources
	 * 
	 */
	public void close()
	{
		if (memObjects != null)
		{
			for (cl_mem memobj : memObjects)
			{
				clReleaseMemObject(memobj);
			}
		}
		// Release kernel, program, and memory objects
		clReleaseKernel(kernel);
		clReleaseCommandQueue(commandQueue);
		clReleaseContext(context);
	}
	// --------------------------------------------------------------------------
	// --- getter/setter
	// --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
