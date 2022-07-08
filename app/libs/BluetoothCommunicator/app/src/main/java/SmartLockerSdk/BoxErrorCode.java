package SmartLockerSdk;

public enum BoxErrorCode
{
	SDK_000(0),
	SDK_001(1),
	SDK_002(2),
	SDK_003(3),
	SDK_004(4),
	BOX_001(5),
	BOX_002(6),
	BOX_003(7),
	BOX_004(8),
	BOX_005(9),
	BOX_006(10),
	BOX_007(11),
	BOX_008(12),
	//BOX_009(13),
	BOX_010(14),
	//BOX_011(15),
	//BOX_012(16),
	//BOX_013(17),
	//BOX_014(18),
	//BOX_015(19),
	//BOX_016(20),
	BOX_017(21),
	//BOX_018(22),
	//BOX_019(23),
	//BOX_020(24),
	//BOX_021(25),
	//BOX_022(26),
	BOX_023(27),
	//BOX_024(28),
	BOX_025(29),
	BOX_026(30),
	BOX_027(31);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, BoxErrorCode> mappings;
	private static java.util.HashMap<Integer, BoxErrorCode> getMappings()
	{
		if (mappings == null)
		{
			synchronized (BoxErrorCode.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, BoxErrorCode>();
				}
			}
		}
		return mappings;
	}

	private BoxErrorCode(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static BoxErrorCode forValue(int value)
	{
		return getMappings().get(value);
	}
}