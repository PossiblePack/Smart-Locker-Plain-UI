package SmartLockerSdk;

public enum BleBoxStatus
{
	isTooMuchEvent(0), /* 0x0001 */
	isDoorOpened(1), /* 0x0002 */
	isLockLock(2), /* 0x0004 */
	isKeyboardEnable(3), /* 0x0008 */
	isDoorChanged(4), /* 0x0010 */
	isLockChanged(5), /* 0x0020 */
	Disconnected(6), /* 0x0040 */
	passwordSet(7), /* 0x0080 */
	passwordNum(8); /* 0x0F00 */

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, BleBoxStatus> mappings;
	private static java.util.HashMap<Integer, BleBoxStatus> getMappings()
	{
		if (mappings == null)
		{
			synchronized (BleBoxStatus.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, BleBoxStatus>();
				}
			}
		}
		return mappings;
	}

	private BleBoxStatus(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static BleBoxStatus forValue(int value)
	{
		return getMappings().get(value);
	}
}