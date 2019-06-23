/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.09.2011
 * Author(s): stei_ol
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.math;

import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetPlatformIDs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jocl.CL;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;


/**
 * @author PhilippP
 */
public final class OpenClHandler
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants
	// ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger		log		= Logger.getLogger(OpenClHandler.class.getName());
	private static OpenClHandler		instance	= new OpenClHandler();
	
	private final PlatformDeviceInfo	platformDevice;
	
	private static class PlatformDeviceInfo
	{
		private cl_platform_id	platform;
		private cl_device_id		device;
		private String				name;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @throws org.jocl.CLException - if a error occurs while initializing the cl device
	 */
	private OpenClHandler()
	{
		
		
		PlatformDeviceInfo pdi = null;
		try
		{
			// Enable exceptions and subsequently omit error checks in this sample
			CL.setExceptionsEnabled(true);
			pdi = findDevice();
		} catch (Throwable e)
		{
			log.error("OpenCL error'd when looking for devices.", e);
		}
		platformDevice = pdi;
		
	}
	
	
	/**
	 * @return
	 */
	public static OpenClHandler getInstance()
	{
		return instance;
	}
	
	
	// -------------------------------------------------------------------------
	// --- methods -------------------------------------------------------------
	// -------------------------------------------------------------------------
	
	
	private PlatformDeviceInfo findDevice()
	{
		List<PlatformDeviceInfo> pdInfos = new ArrayList<PlatformDeviceInfo>();
		
		// Obtain the number of platforms
		int numPlatformsArray[] = new int[1];
		clGetPlatformIDs(0, null, numPlatformsArray);
		int numPlatforms = numPlatformsArray[0];
		log.info("Detected " + numPlatforms + " opencl platforms.");
		
		// Obtain a platform ID
		cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
		clGetPlatformIDs(platforms.length, platforms, null);
		
		for (cl_platform_id platform : platforms)
		{
			String platformName = JOCLDeviceQuery.getString(platform, CL.CL_PLATFORM_NAME);
			log.info("Platform name: " + platformName);
			
			// Obtain the number of devices for the platform
			int numDevicesArray[] = new int[1];
			clGetDeviceIDs(platform, CL.CL_DEVICE_TYPE_ALL, 0, null, numDevicesArray);
			int numDevices = numDevicesArray[0];
			log.info("Detected " + numDevices + " opencl devices");
			
			// Obtain a device ID
			cl_device_id devices[] = new cl_device_id[numDevices];
			clGetDeviceIDs(platform, CL.CL_DEVICE_TYPE_ALL, numDevices, devices, null);
			for (cl_device_id device : devices)
			{
				String deviceName = JOCLDeviceQuery.getString(device, CL.CL_DEVICE_NAME);
				log.info("Device name: " + deviceName);
				PlatformDeviceInfo pdInfo = new PlatformDeviceInfo();
				pdInfo.device = device;
				pdInfo.platform = platform;
				pdInfo.name = platformName + " - " + deviceName;
				pdInfos.add(pdInfo);
			}
		}
		
		if (pdInfos.isEmpty())
		{
			return null;
		}
		
		Collections.sort(pdInfos, new PlatformDeviceInfoComparator());
		PlatformDeviceInfo pdInfo = pdInfos.get(0);
		log.info("Choosing " + pdInfo.name);
		return pdInfo;
	}
	
	
	/**
	 * @return
	 */
	public static boolean isOpenClSupported()
	{
		return getInstance().platformDevice != null;
	}
	
	
	/**
	 * @return preferred platform
	 */
	public static cl_platform_id getPlatform()
	{
		return getInstance().platformDevice.platform;
	}
	
	
	/**
	 * @return preferred device
	 */
	public static cl_device_id getDevice()
	{
		return getInstance().platformDevice.device;
	}
	
	
	private static class PlatformDeviceInfoComparator implements Comparator<PlatformDeviceInfo>, Serializable
	{
		/**  */
		private static final long	serialVersionUID	= 5936528580359702180L;
		
		
		@Override
		public int compare(final PlatformDeviceInfo o1, final PlatformDeviceInfo o2)
		{
			long deviceType1 = JOCLDeviceQuery.getLong(o1.device, CL.CL_DEVICE_TYPE);
			long deviceType2 = JOCLDeviceQuery.getLong(o2.device, CL.CL_DEVICE_TYPE);
			
			if (deviceType1 != deviceType2)
			{
				if (deviceType1 == CL.CL_DEVICE_TYPE_GPU)
				{
					return -1;
				}
				return 1;
			}
			
			long freq1 = JOCLDeviceQuery.getLong(o1.device, CL.CL_DEVICE_MAX_CLOCK_FREQUENCY);
			long freq2 = JOCLDeviceQuery.getLong(o2.device, CL.CL_DEVICE_MAX_CLOCK_FREQUENCY);
			
			return Long.compare(freq2, freq1);
		}
	}
	
}
