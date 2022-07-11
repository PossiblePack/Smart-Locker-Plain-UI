package com.example.demo.libs.Model;

public class BoxStatus
{
	private boolean isDoorOpen;
	public final boolean get_isDoorOpen()
	{
		return isDoorOpen;
	}
	public final void set_isDoorOpen(boolean value)
	{
		isDoorOpen = value;
	}
	private boolean isLockLock;
	public final boolean get_isLockLock()
	{
		return isLockLock;
	}
	public final void set_isLockLock(boolean value)
	{
		isLockLock = value;
	}
	private boolean isTooMuchEvent;
	public final boolean get_isTooMuchEvent()
	{
		return isTooMuchEvent;
	}
	public final void set_isTooMuchEvent(boolean value)
	{
		isTooMuchEvent = value;
	}
	private boolean isPasswordSet;
	public final boolean get_isPasswordSet()
	{
		return isPasswordSet;
	}
	public final void set_isPasswordSet(boolean value)
	{
		isPasswordSet = value;
	}
	private byte passwordNum;
	public final byte get_passwordNum()
	{
		return passwordNum;
	}
	public final void set_passwordNum(byte value)
	{
		passwordNum = value;
	}
}