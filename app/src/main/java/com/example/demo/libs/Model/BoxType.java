package com.example.demo.libs.Model;

import java.util.HashMap;

public enum BoxType
{
	UNUSED(0);

	public static final int SIZE = Integer.SIZE;

	private int intValue;
	private static HashMap<Integer, BoxType> mappings;
	private static HashMap<Integer, BoxType> getMappings()
	{
		if (mappings == null)
		{
			synchronized (BoxType.class)
			{
				if (mappings == null)
				{
					mappings = new HashMap<Integer, BoxType>();
				}
			}
		}
		return mappings;
	}

	private BoxType(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static BoxType forValue(int value)
	{
		return getMappings().get(value);
	}
}