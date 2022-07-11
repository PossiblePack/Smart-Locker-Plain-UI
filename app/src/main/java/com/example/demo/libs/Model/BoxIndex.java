package com.example.demo.libs.Model;

import java.util.HashMap;

public enum BoxIndex
{
	BOX_1(0),
	BOX_2(1),
	BOX_3(2),
	BOX_4(3);

	public static final int SIZE = Integer.SIZE;

	private int intValue;
	private static HashMap<Integer, BoxIndex> mappings;
	private static HashMap<Integer, BoxIndex> getMappings()
	{
		if (mappings == null)
		{
			synchronized (BoxIndex.class)
			{
				if (mappings == null)
				{
					mappings = new HashMap<Integer, BoxIndex>();
				}
			}
		}
		return mappings;
	}

	private BoxIndex(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static BoxIndex forValue(int value)
	{
		return getMappings().get(value);
	}
}