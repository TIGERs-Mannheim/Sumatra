/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.09.2011
 * Author(s): stei_ol
 * *********************************************************
 */
package edu.tigers.sumatra.gpu;

import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetPlatformIDs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jocl.CL;
import org.jocl.CLException;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;


/**
 * @author PhilippP
 */
public final class OpenClHandler
{
	private static final Logger	log				= Logger.getLogger(OpenClHandler.class.getName());
																
	private PlatformDeviceInfo		platformDevice	= null;
	private int							loadedDevice	= -2;
																
	private static class PlatformDeviceInfo
	{
		private cl_platform_id	platform;
		private cl_device_id		device;
		private String				name;
	}
	
	
	@Configurable(comment = "Index of openCL device that should be used. -1 to disable openCL, 0 for auto-selection.", defValue = "0")
	private static int openClDevice = 0;
	
	
	static
	{
		ConfigRegistration.registerClass("user", OpenClHandler.class);
		ConfigRegistration.registerConfigurableCallback("user", new ConfigObserver());
	}
	
	
	private static OpenClHandler instance = new OpenClHandler();
	
	
	/**
	 * @throws org.jocl.CLException - if a error occurs while initializing the cl device
	 */
	private OpenClHandler()
	{
		// load(openClDevice);
	}
	
	
	/**
	 * @return
	 */
	public static synchronized OpenClHandler getInstance()
	{
		// if (instance == null)
		// {
		// instance = new OpenClHandler();
		// }
		return instance;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param prefDeviceId
	 */
	public void load(final int prefDeviceId)
	{
		if (loadedDevice == prefDeviceId)
		{
			return;
		}
		PlatformDeviceInfo pdi = null;
		if (prefDeviceId >= 0)
		{
			try
			{
				// Enable exceptions and subsequently omit error checks in this sample
				CL.setExceptionsEnabled(true);
				pdi = findDevice(prefDeviceId);
			} catch (Throwable e)
			{
				log.error("OpenCL error'd when looking for devices.", e);
			}
		} else
		{
			log.info("OpenCL is disabled in user config.");
		}
		platformDevice = pdi;
		loadedDevice = prefDeviceId;
	}
	
	
	// -------------------------------------------------------------------------
	// --- methods -------------------------------------------------------------
	// -------------------------------------------------------------------------
	
	
	private PlatformDeviceInfo findDevice(final int prefDeviceId)
	{
		List<PlatformDeviceInfo> pdInfos = new ArrayList<PlatformDeviceInfo>();
		
		// Obtain the number of platforms
		int numPlatformsArray[] = new int[1];
		try
		{
			clGetPlatformIDs(0, null, numPlatformsArray);
		} catch (CLException err)
		{
			log.warn("Can not use OpenCL: " + err.getMessage());
			return null;
		}
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
		
		int deviceIndex = prefDeviceId - 1;
		final PlatformDeviceInfo pdInfo;
		if ((deviceIndex >= 0) && (deviceIndex < pdInfos.size()))
		{
			pdInfo = pdInfos.get(deviceIndex);
		} else
		{
			Collections.sort(pdInfos, new PlatformDeviceInfoComparator());
			pdInfo = pdInfos.get(0);
		}
		
		log.info("Choosing " + pdInfo.name);
		return pdInfo;
	}
	
	
	/**
	 * @return
	 */
	public static boolean isOpenClSupported()
	{
		if (getInstance().loadedDevice == -2)
		{
			getInstance().load(openClDevice);
		}
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
		private static final long serialVersionUID = 5936528580359702180L;
		
		
		@Override
		public int compare(final PlatformDeviceInfo o1, final PlatformDeviceInfo o2)
		{
			long deviceType1 = JOCLDeviceQuery.getLong(o1.device, CL.CL_DEVICE_TYPE);
			long deviceType2 = JOCLDeviceQuery.getLong(o2.device, CL.CL_DEVICE_TYPE);
			
			if (deviceType1 != deviceType2)
			{
				if (deviceType1 == CL.CL_DEVICE_TYPE_CPU)
				{
					return 1;
				}
				return -1;
			}
			
			long freq1 = JOCLDeviceQuery.getLong(o1.device, CL.CL_DEVICE_MAX_CLOCK_FREQUENCY);
			long freq2 = JOCLDeviceQuery.getLong(o2.device, CL.CL_DEVICE_MAX_CLOCK_FREQUENCY);
			
			return Long.compare(freq2, freq1);
		}
	}
	
	private static class ConfigObserver implements IConfigObserver
	{
		
		@Override
		public void afterApply(final IConfigClient configClient)
		{
			instance.load(openClDevice);
		}
		
	}
}
