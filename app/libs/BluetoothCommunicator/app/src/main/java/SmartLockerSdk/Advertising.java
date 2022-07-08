package SmartLockerSdk;

import java.util.*;
import java.time.*;

public class Advertising
{
	private BoxType type = BoxType.values()[0];
	public final BoxType get_type()
	{
		return type;
	}
	public final void set_type(BoxType value)
	{
		type = value;
	}
	private byte[] firmVersion;
	public final byte[] get_firmVersion()
	{
		return firmVersion;
	}
	public final void set_firmVersion(byte[] value)
	{
		firmVersion = value;
	}
	private Integer remainingBattery;
	public final Integer get_remainingBattery()
	{
		return remainingBattery;
	}
	public final void set_remainingBattery(Integer value)
	{
		remainingBattery = value;
	}
	private BoxStatus status;
	public final BoxStatus get_status()
	{
		return status;
	}
	public final void set_status(BoxStatus value)
	{
		status = value;
	}
	public final boolean get_isDoorOpen()
	{
		return status.get_isDoorOpen();
	}
	public final void set_isDoorOpen(boolean value)
	{
		status.set_isDoorOpen(value);;
	}
 	public final boolean get_isLockLock()
	{
		return status.get_isLockLock();
	}
	public final void set_isLockLock(boolean value)
	{
		status.set_isLockLock(value);;
	}
	public final boolean get_isTooMuchEvent()
	{
		return status.get_isTooMuchEvent();
	}
	public final void set_isTooMuchEvent(boolean value)
	{
		status.set_isTooMuchEvent(value);;
	}
	public final boolean get_isPasswordSet()
	{
		return status.get_isPasswordSet();
	}
	public final void set_isPasswordSet(boolean value)
	{
		status.set_isPasswordSet(value);;
	}
	public final byte get_passwordNum()
	{
		return status.get_passwordNum();
	}
	public final void set_passwordNum(byte value)
	{
		status.set_passwordNum(value);;
	}
}