/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 17, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.configurable;

import com.github.g3force.s2vconverter.String2ValueConverter;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Read from a given set of classes all {@link Configurable} annotations
 * and fill the associated fields with data from config file
 */
public class ConfigAnnotationProcessor
{
	private static final Logger log = LogManager.getLogger(ConfigAnnotationProcessor.class.getName());

	private static String2ValueConverter s2vConv = String2ValueConverter.getDefault();

	private final String category;
	private final Map<String, ConfigurableFieldData> data = new LinkedHashMap<>();
	private final Set<String> spezis = new HashSet<>();


	private static class ConfigurableFieldData implements Comparable<ConfigurableFieldData>
	{
		private String className;
		private String fieldName;
		private String fieldSpezi = "";

		private String fieldValue = "";
		private String fieldDefValue = "";
		private String comment = "";
		private String[] tags = {};
		private Class<?> fieldType;


		private String getKey()
		{
			return className + "." + fieldName + ":" + fieldSpezi;
		}


		@Override
		public int compareTo(final ConfigurableFieldData o)
		{
			int cmp = className.compareToIgnoreCase(o.className);
			if (cmp != 0)
			{
				return cmp;
			}
			return fieldName.compareToIgnoreCase(o.fieldName);
		}


		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;

			if (o == null || getClass() != o.getClass())
				return false;

			ConfigurableFieldData that = (ConfigurableFieldData) o;

			return new EqualsBuilder()
					.append(className, that.className)
					.append(fieldName, that.fieldName)
					.append(fieldSpezi, that.fieldSpezi)
					.append(fieldValue, that.fieldValue)
					.append(fieldDefValue, that.fieldDefValue)
					.append(comment, that.comment)
					.append(tags, that.tags)
					.append(fieldType, that.fieldType)
					.isEquals();
		}


		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(17, 37)
					.append(className)
					.append(fieldName)
					.append(fieldSpezi)
					.append(fieldValue)
					.append(fieldDefValue)
					.append(comment)
					.append(tags)
					.append(fieldType)
					.toHashCode();
		}
	}


	/**
	 * @param name the category name
	 */
	@SuppressWarnings("WeakerAccess")
	public ConfigAnnotationProcessor(final String name)
	{
		category = name;
	}


	private String getAttribute(final ConfigurationNode node, final String name)
	{
		List<ConfigurationNode> attrs = node.getAttributes(name);
		String base = "";
		if (attrs.size() == 1)
		{
			base = attrs.getFirst().getValue().toString();
		}
		return base;
	}


	/**
	 * Load field data from given config object. Existing values will be overwritten
	 * Note: It will not be applies yet, use one of the apply methods for this.
	 *
	 * @param config full merged config to be used
	 */
	@SuppressWarnings("WeakerAccess")
	public synchronized void loadConfiguration(final HierarchicalConfiguration config)
	{
		String base = getAttribute(config.getRoot(), "base");

		Map<String, ConfigurationNode> classes = getClassNodesFromConfigRec(base, config.getRoot());

		for (Map.Entry<String, ConfigurationNode> entry : classes.entrySet())
		{
			String className = entry.getKey();
			List<ConfigurationNode> fieldNodes = entry.getValue().getChildren();
			for (ConfigurationNode fieldNode : fieldNodes)
			{
				loadFieldNode(className, fieldNode);
			}
		}
	}


	private void loadFieldNode(String className, ConfigurationNode fieldNode)
	{
		String[] split = fieldNode.getName().split(":");
		String fieldName = split[0];
		try
		{
			Class.forName(className).getDeclaredField(fieldName);
			String fieldSpezi = split.length > 1 ? split[1] : "";
			String fieldValue = fieldNode.getValue() == null ? ""
					: unescape(s2vConv.toString(
					fieldNode
							.getValue().getClass(),
					fieldNode.getValue()));
			String comment = getAttribute(fieldNode, "comment");
			String[] tags = getAttribute(fieldNode, "tags").split(" ");
			ConfigurableFieldData fieldData = new ConfigurableFieldData();
			fieldData.className = className;
			fieldData.fieldName = fieldName;
			fieldData.fieldSpezi = fieldSpezi;
			fieldData.fieldValue = fieldValue;
			fieldData.comment = comment;
			fieldData.tags = tags;
			if (data.containsKey(fieldData.getKey()))
			{
				data.get(fieldData.getKey()).fieldValue = fieldValue;
			} else
			{
				data.put(fieldData.getKey(), fieldData);
			}
		} catch (NoSuchFieldException e)
		{
			log.info("Field vanished in {}: {}#{}", category, className, fieldName);
		} catch (SecurityException e)
		{
			log.error("Sec exception", e);
		} catch (ClassNotFoundException e)
		{
			log.info("Class vanished in {}: {}", category, className);
		}
	}


	/**
	 * Load all configurable fields from clazz and its subclasses
	 *
	 * @param clazz     the class to load
	 * @param overwrite existing values
	 */
	@SuppressWarnings("WeakerAccess")
	public synchronized void loadClass(final Class<?> clazz, final boolean overwrite)
	{
		for (Class<?> c : getClassAndSubClasses(clazz))
		{
			for (ConfigurableFieldData fd : read(c))
			{
				ConfigurableFieldData fdCur = data.get(fd.getKey());
				if ((fdCur == null) || overwrite)
				{
					applyFieldData(fd, overwrite);
				} else
				{
					fdCur.comment = fd.comment;
					fdCur.fieldDefValue = fd.fieldDefValue;
					fdCur.fieldType = fd.fieldType;
					fdCur.tags = fd.tags;
				}
			}
		}
	}


	private void applyFieldData(ConfigurableFieldData fd, boolean overwrite)
	{
		data.put(fd.getKey(), fd);
		if (!overwrite && !fd.fieldDefValue.isEmpty())
		{
			fd.fieldValue = fd.fieldDefValue;
		}
		// only apply the default spezi
		if ("".equals(fd.fieldSpezi))
		{
			applyFieldData(fd, null);
		}
	}


	/**
	 * Create the effective config from currently loaded fields
	 *
	 * @return the effective config
	 */
	@SuppressWarnings("WeakerAccess")
	public synchronized HierarchicalConfiguration getEffectiveConfig()
	{
		return getConfig(new ArrayList<>(data.values()), true);
	}


	/**
	 * Create the minimal config from currently loaded fields.
	 * All fields that have their default value, will be skipped
	 *
	 * @return the minimal config
	 */
	@SuppressWarnings("WeakerAccess")
	public synchronized HierarchicalConfiguration getMinimalConfig()
	{
		List<ConfigurableFieldData> minData = new ArrayList<>(data.size());

		for (ConfigurableFieldData fd : data.values())
		{
			if (!fd.fieldValue.equals(fd.fieldDefValue))
			{
				minData.add(fd);
			}
		}

		return getConfig(minData, false);
	}


	/**
	 * @param obj apply all spezis to the given instance
	 */
	@SuppressWarnings("unused")
	public synchronized void applyAll(final Object obj)
	{
		applySpezi(obj, "");
		spezis.forEach(s -> applySpezi(obj, s));
	}


	/**
	 * Apply all spezis to all static fields
	 */
	@SuppressWarnings("WeakerAccess")
	public synchronized void applyAll()
	{
		spezis.forEach(this::applySpezi);
		applySpezi("");
	}


	/**
	 * @param spezi apply this spezi only
	 */
	@SuppressWarnings("WeakerAccess")
	public synchronized void applySpezi(final String spezi)
	{
		addSpezi(spezi);
		data.values().stream()
				.filter(fd -> fd.fieldSpezi.equals(spezi))
				.forEach(fd -> applyFieldData(fd, null));
	}


	/**
	 * Apply values to all fields of the given object. SubClasses will be considered.
	 *
	 * @param obj   apply to this instance only
	 * @param spezi apply this spezi only
	 */
	@SuppressWarnings("WeakerAccess")
	public synchronized void applySpezi(final Object obj, final String spezi)
	{
		for (Class<?> c = obj.getClass(); ((c != null) && !c.equals(Object.class)); c = c
				.getSuperclass())
		{
			String className = c.getCanonicalName();
			data.values().stream()
					.filter(fd -> fd.className.equals(className))
					.filter(fd -> fd.fieldSpezi.equals(spezi))
					.forEach(fd -> applyFieldData(fd, obj));
		}
	}


	/**
	 * Apply values to all fields of the given class. SubClasses will be considered.
	 *
	 * @param clazz apply to this class only
	 * @param spezi apply this spezi only
	 */
	@SuppressWarnings("unused")
	public synchronized void applySpezi(final Class<?> clazz, final String spezi)
	{
		for (Class<?> c = clazz; ((c != null) && !c.equals(Object.class)); c = c
				.getSuperclass())
		{
			String className = c.getCanonicalName();
			data.values().stream()
					.filter(fd -> fd.className.equals(className))
					.filter(fd -> fd.fieldSpezi.equals(spezi))
					.forEach(fd -> applyFieldData(fd, null));
		}
	}


	/**
	 * @param spezi the spezi to add
	 */
	@SuppressWarnings("WeakerAccess")
	public synchronized void addSpezi(final String spezi)
	{
		spezis.add(spezi);
	}


	/**
	 * @param spezi the spezi to remove
	 */
	@SuppressWarnings("unused")
	public synchronized void removeSpezi(final String spezi)
	{
		spezis.remove(spezi);
	}


	/**
	 * Override a fields value
	 *
	 * @param obj       the object to apply the config to after overriding
	 * @param fieldName the name of the field to override
	 * @param value     the value to apply
	 */
	@SuppressWarnings("WeakerAccess")
	public synchronized void overrideField(Object obj, String fieldName, String value)
	{
		for (Class<?> c = obj.getClass(); ((c != null) && !c.equals(Object.class)); c = c
				.getSuperclass())
		{
			String className = c.getCanonicalName();
			data.values().stream()
					.filter(fd -> fd.className.equals(className))
					.filter(fd -> fd.fieldName.equals(fieldName))
					.forEach(fd -> {
						fd.fieldValue = value;
						applyFieldData(fd, obj);
					});
		}
	}


	/**
	 * Override a fields value
	 *
	 * @param clazz     the class to apply the config to after overriding
	 * @param fieldName the name of the field to override
	 * @param value     the value to apply
	 */
	@SuppressWarnings("WeakerAccess")
	public synchronized void overrideField(Class<?> clazz, String fieldName, String value)
	{
		for (Class<?> c = clazz; ((c != null) && !c.equals(Object.class)); c = c
				.getSuperclass())
		{
			String className = c.getCanonicalName();
			data.values().stream()
					.filter(fd -> fd.className.equals(className))
					.filter(fd -> fd.fieldName.equals(fieldName))
					.forEach(fd -> {
						fd.fieldValue = value;
						applyFieldData(fd, null);
					});
		}
	}


	private HierarchicalConfiguration getConfig(final List<ConfigurableFieldData> fData,
			final boolean exportMetadata)
	{
		final HierarchicalConfiguration config = new HierarchicalConfiguration();
		config.setDelimiterParsingDisabled(true);

		Collections.sort(fData);

		String base = null;
		for (ConfigurableFieldData fd : fData)
		{
			if (base == null)
			{
				base = fd.className;
			} else
			{
				base = greatestCommonPrefix(base, fd.className);
			}
		}

		if (base == null)
		{
			return config;
		}

		config.getRoot().setName(base);
		config.getRoot().addAttribute(new HierarchicalConfiguration.Node("base", base));

		for (ConfigurableFieldData fieldData : fData)
		{
			if (fieldData.fieldType == null)
			{
				continue;
			}
			String clazzKey = fieldData.className.substring(Math.min(base.length() + 1, fieldData.className.length()));
			String spezi = fieldData.fieldSpezi.isEmpty() ? "" : ":" + fieldData.fieldSpezi;
			final HierarchicalConfiguration cfg = new HierarchicalConfiguration();
			cfg.setDelimiterParsingDisabled(true);
			cfg.addProperty(clazzKey + "." + fieldData.fieldName + spezi, escape(fieldData.fieldValue));
			if (exportMetadata)
			{
				cfg.addProperty(clazzKey + "." + fieldData.fieldName + spezi + "[@comment]", escape(fieldData.comment));
				cfg.addProperty(clazzKey + "." + fieldData.fieldName + spezi + "[@class]",
						fieldData.fieldType.getName());
				cfg.addProperty(clazzKey + "." + fieldData.fieldName + spezi + "[@tags]", escape(
						String.join(" ", fieldData.tags)));
			}
			config.append(cfg);
		}

		return config;
	}


	private List<Class<?>> getClassAndSubClasses(final Class<?> mainClazz)
	{
		Class<?> clazz = mainClazz;
		final List<Class<?>> classes = new ArrayList<>();
		while ((clazz != null) && !clazz.equals(Object.class))
		{
			classes.add(clazz);
			clazz = clazz.getSuperclass();
		}
		return classes;
	}


	private String greatestCommonPrefix(final String a, final String b)
	{
		String[] pkgsA = a.split("\\.");
		String[] pkgsB = b.split("\\.");
		int minLength = Math.min(pkgsA.length, pkgsB.length);
		StringBuilder prefix = new StringBuilder();
		for (int i = 0; i < minLength; i++)
		{
			if (!pkgsA[i].equals(pkgsB[i]))
			{
				return prefix.toString();
			}
			if (i != 0)
			{
				prefix.append('.');
			}
			prefix.append(pkgsA[i]);
		}
		return prefix.toString();
	}


	private Map<String, ConfigurationNode> getClassNodesFromConfigRec(final String base,
			final ConfigurationNode node)
	{
		Map<String, ConfigurationNode> classes = new LinkedHashMap<>();
		try
		{
			Class.forName(base);
			classes.put(base, node);
		} catch (ClassNotFoundException | NoClassDefFoundError err)
		{
			// not a class. Maybe a package
			for (ConfigurationNode child : node.getChildren())
			{
				classes.putAll(getClassNodesFromConfigRec(base + "." + child.getName(), child));
			}

		}
		return classes;
	}


	private String escape(final String str)
	{
		return StringEscapeUtils.escapeXml(str);
	}


	private String unescape(final String str)
	{
		return StringEscapeUtils.unescapeXml(str);
	}


	private void applyFieldData(final ConfigurableFieldData fieldData, final Object obj)
	{
		try
		{
			Class<?> clazz = Class.forName(fieldData.className);
			write(clazz, obj, fieldData);
		} catch (ClassNotFoundException err)
		{
			log.error("Could not find class with name {}", fieldData.className);
		}
	}


	/**
	 * Read values from fields and generate {@link ConfigurableFieldData}
	 *
	 * @param clazz the class to read from
	 * @return a list of all found fields
	 */
	private List<ConfigurableFieldData> read(final Class<?> clazz)
	{
		List<ConfigurableFieldData> dataRead = new ArrayList<>();

		for (Field field : clazz.getDeclaredFields())
		{
			if (field.isAnnotationPresent(Configurable.class))
			{
				Class<?> type = field.getType();
				String name = field.getName();
				field.setAccessible(true);

				if ((field.getModifiers() & Modifier.FINAL) != 0)
				{
					log.error("Configurable field {}#{} must not be final", clazz.getName(), name);
					continue;
				}

				Configurable conf = field.getAnnotation(Configurable.class);
				String cat = conf.category();

				if (cat.isEmpty() || cat.equals(category))
				{
					String[] tags = conf.tags();
					String comment = conf.comment();
					String[] declaredSpezis = conf.spezis();

					if (declaredSpezis.length == 0)
					{
						declaredSpezis = new String[] { "" };
					}

					int speziId = 0;
					for (String spezi : declaredSpezis)
					{
						String defValue = conf.defValue();
						if (conf.defValueSpezis().length > speziId)
						{
							defValue = conf.defValueSpezis()[speziId];
						}
						if (!"".equals(defValue))
						{
							try
							{
								defValue = s2vConv.toString(type, s2vConv.parseString(type, defValue));
							} catch (IllegalArgumentException err1)
							{
								log.warn("Could not convert defValue of field {}: {}", name, defValue, err1);
							}
						}

						String value;
						if (((field.getModifiers() & Modifier.STATIC) == 0) // non static field
								|| (!spezi.isEmpty() && !defValue.isEmpty()))
						{
							value = defValue;
						} else
						{
							// static field: read the assigned value
							try
							{
								Object obj = field.get(null);
								if (obj != null)
								{
									value = s2vConv.toString(type, obj);
								} else
								{
									value = "";
								}
							} catch (IllegalArgumentException | IllegalAccessException err1)
							{
								log.error("Could not get value of field {}", name, err1);
								continue;
							}
						}

						ConfigurableFieldData fieldDataSpezi = new ConfigurableFieldData();
						fieldDataSpezi.className = clazz.getName();
						fieldDataSpezi.fieldName = name;
						fieldDataSpezi.fieldValue = value;
						fieldDataSpezi.fieldDefValue = defValue;
						fieldDataSpezi.fieldSpezi = spezi;
						fieldDataSpezi.comment = comment;
						fieldDataSpezi.tags = tags;
						fieldDataSpezi.fieldType = type;
						dataRead.add(fieldDataSpezi);
						speziId++;
					}
				}
			}
		}
		return dataRead;
	}


	/**
	 * Write provided fieldData into field of clazz.
	 *
	 * @param clazz     The class containing the field in fieldData
	 * @param obj       null for static fields or the field instance
	 * @param fieldData information about what to write into the field
	 */
	private void write(final Class<?> clazz, final Object obj, final ConfigurableFieldData fieldData)
	{
		for (Field field : clazz.getDeclaredFields())
		{
			if (field.getName().equals(fieldData.fieldName))
			{
				field.setAccessible(true);
				Class<?> type = field.getType();
				boolean isStatic = ((field.getModifiers() & Modifier.STATIC) != 0);
				if (!isStatic && (obj == null))
				{
					return;
				}
				Object value = s2vConv.parseString(type, fieldData.fieldValue);
				try
				{
					field.set(obj, value);
				} catch (IllegalArgumentException | IllegalAccessException err)
				{
					log.error("Could not set value on field {}", field.getName(), err);
				}
				return;
			}
		}
		log.warn("Could not find field: {}", fieldData.fieldName);
	}
}
