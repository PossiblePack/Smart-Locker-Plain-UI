package SmartLockerSdk;

import java.util.*;
import java.time.*;

/*for SetEvent Function*/
public enum EventStatus
{
	CONNEC(0),
	DISCON(1),
	GENKEY(2),
	CHAKEY(3),
	UNLLIV(4),
	LOCLOC(5),
	UNLBLK(6),
	UNLCFP(7),
	PARSTA(8),
	MAXATT(9),
	USEDAT(10),
	USEATT(11),
	LIVCFP(12),
	SETTIM(13),
	UPDFWL(14),
	DELEVT(15),
	CFGLOC(16),
	UPDSTA(17),
	LIVEXP(18),
	LIVBLK(19),
	SOLLOS(20),
	ENXCFP(21),
	ENDBLK(22),
	WROPAR(23);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, EventStatus> mappings;
	private static java.util.HashMap<Integer, EventStatus> getMappings()
	{
		if (mappings == null)
		{
			synchronized (EventStatus.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, EventStatus>();
				}
			}
		}
		return mappings;
	}

	private EventStatus(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static EventStatus forValue(int value)
	{
		return getMappings().get(value);
	}
}