/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 17, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.configurable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation will automatically save fields.
 * Do not use final.
 * If field is not static, value must be applied after instantiation.
 * This may be done for you by super classes.
 * Preferably use static, if you do not consider to have different values at the same time.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Configurable
{
	/**
	 * Available specializations of this field.
	 */
	String[] spezis() default {};


	/**
	 * Documentation of this field
	 */
	String comment() default "";


	/**
	 * Set a default value. This is only needed, if field is not static.
	 */
	String defValue() default "";


	String[] defValueSpezis() default {};


	String category() default "";

	String[] tags() default {};
}
