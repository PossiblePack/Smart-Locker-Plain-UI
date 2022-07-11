package com.example.demo.libs.Model;

public class BoxControllerConfig
{
	private int advertisingInterval;
	public final int get_advertisingInterval()
	{
		return advertisingInterval;
	}
	public final void set_advertisingInterval(int value)
	{
		advertisingInterval = value;
	}
	private int strengthPower;
	public final int get_strengthPower()
	{
		return strengthPower;
	}
	public final void set_strengthPower(int value)
	{
		strengthPower = value;
	}
	private BleChannel channelUsed = BleChannel.values()[0];
	public final BleChannel get_channelUsed()
	{
		return channelUsed;
	}
	public final void set_channelUsed(BleChannel value)
	{
		channelUsed = value;
	}
	private int attemptMax;
	public final int get_attemptMax()
	{
		return attemptMax;
	}
	public final void set_attemptMax(int value)
	{
		attemptMax = value;
	}
	private int autoCloseTime;
	public final int get_autoCloseTime()
	{
		return autoCloseTime;
	}
	public final void set_autoCloseTime(int value)
	{
		autoCloseTime = value;
	}
	private int inputImpossibleTime;
	public final int get_inputImpossibleTime()
	{
		return inputImpossibleTime;
	}
	public final void set_inputImpossibleTime(int value)
	{
		inputImpossibleTime = value;
	}
	private int warnEventNum;
	public final int get_warnEventNum()
	{
		return warnEventNum;
	}
	public final void set_warnEventNum(int value)
	{
		warnEventNum = value;
	}
	private int connectionTimeOut;
	public final int get_connectionTimeOut()
	{
		return connectionTimeOut;
	}
	public final void set_connectionTimeOut(int value)
	{
		connectionTimeOut = value;
	}
	private int passwordDeleteTime;
	public final int get_passwordDeleteTime()
	{
		return passwordDeleteTime;
	}
	public final void set_passwordDeleteTime(int value)
	{
		passwordDeleteTime = value;
	}
}