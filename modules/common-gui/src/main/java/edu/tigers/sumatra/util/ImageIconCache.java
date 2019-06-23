/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 13, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.sumatra.util;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;


/**
 * Class for image retrieval and caching for images stored on the classpath. The implementation is thread safe but an
 * image might be loaded multiple times if two separate thread access it at the same time. For the same reason it is not
 * guaranteed that the cache will always return the same instance.
 * 
 * @author "Lukas Magel"
 */
public class ImageIconCache
{
	private static final ImageIconCache	globalCache	= new ImageIconCache();
	
	private Map<String, CacheEntry>		cache			= new ConcurrentHashMap<>();
	
	
	/**
	 * 
	 */
	public ImageIconCache()
	{
	}
	
	
	/**
	 * @return
	 */
	public static ImageIconCache getGlobalCache()
	{
		return globalCache;
	}
	
	
	/**
	 * Loads an image
	 * 
	 * @param name
	 * @return Returns the image or null if no image could be found
	 */
	public ImageIcon getImage(final String name)
	{
		return getOrLoadImage(name);
	}
	
	
	/**
	 * @param name
	 * @param defValue
	 * @return the image or a default value if the image cannot be found
	 */
	public ImageIcon getImageOrDefault(final String name, final ImageIcon defValue)
	{
		ImageIcon image = getOrLoadImage(name);
		if (image == null)
		{
			return defValue;
		}
		return image;
	}
	
	
	/**
	 * @param name
	 * @return the iamge or an empty instance if the image cannot be found
	 */
	public ImageIcon getImageSafe(final String name)
	{
		return getImageOrDefault(name, new ImageIcon());
	}
	
	
	private ImageIcon getOrLoadImage(final String name)
	{
		CacheEntry entry = cache.get(name);
		if (entry != null)
		{
			return entry.getImage();
		}
		
		ImageIcon image = loadImage(name);
		cache.put(name, new CacheEntry(image));
		return image;
	}
	
	
	/**
	 * @param name
	 * @return
	 */
	private ImageIcon loadImage(final String name)
	{
		URL resource = ImageIconCache.class.getResource(name);
		if (resource != null)
		{
			return new ImageIcon(resource);
		}
		return null;
	}
	
	
	/**
	 * 
	 */
	public void clearCache()
	{
		cache.clear();
	}
	
	private static class CacheEntry
	{
		
		private final ImageIcon	img;
		
		
		public CacheEntry(final ImageIcon img)
		{
			this.img = img;
		}
		
		
		public ImageIcon getImage()
		{
			return img;
		}
		
	}
}
