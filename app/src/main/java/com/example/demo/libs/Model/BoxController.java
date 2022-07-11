package com.example.demo.libs.Model;

import android.bluetooth.BluetoothDevice;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BoxController
{
	private static final String LOG_TAG = "SLSDK";
	private static final String LOG_CLASS = "[BoxController]";

	/* public member */
	public Advertising advertisedMessage;
	public String hardwareDeviceCode;
	public VendorApi vendorApi;
	public BoxControllerConfig boxControllerConfig;


	/* Event */
	@FunctionalInterface
	public interface StatusEventHandler
	{
		void invoke(BoxController sender, StatusEventArgs e);
	}
	public Event<StatusEventHandler> OnDoorChange = new Event<StatusEventHandler>();
	public Event<StatusEventHandler> OnLockChange = new Event<StatusEventHandler>();
	public Event<StatusEventHandler> OnConnectionChange = new Event<StatusEventHandler>();


	/* private member */
	private BleDevice bleDevice;
	private BleAccess bleAccess;
	private BcdTimeFormat bcd = new BcdTimeFormat();
	private BluetoothDevice mDevice;


	/* public member get/set */
	public final Advertising getAdvertisedMessage()
	{
		return advertisedMessage;
	}
	public final void setAdvertisedMessage(Advertising value)
	{
		advertisedMessage = value;
	}

	public final String get_HardwareDeviceCode()
	{
		return hardwareDeviceCode;
	}
	public final void set_HardwareDeviceCode(String value)
	{
		hardwareDeviceCode = value;
	}

	public final VendorApi get_VendorApi()
	{
		return vendorApi;
	}
	public final void set_VendorApi(VendorApi value)
	{
		vendorApi = value;
	}

	public final BoxControllerConfig get_BoxControllerConfig()
	{
		return boxControllerConfig;
	}
	public final void se_tBoxControllerConfig(BoxControllerConfig value)
	{
		boxControllerConfig = value;
	}


	/*Constructor*/
	public BoxController(BleAccess effectiveAccess, BleDevice foundDevice)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[BoxController] foundDevice.DeviceCodeData=" +  foundDevice.get_DeviceCodeData());

		bleDevice = foundDevice;
		bleAccess = effectiveAccess;
		mDevice = foundDevice.get_Device();
		bleAccess.doorChangeCallback.add(new DoorStatusChangetCallback());
		bleAccess.lockChangeCallback.add(new LockStatusChangeCallback());
		bleAccess.disconnectCallback.add( new ConnectionStateChangetCallback());
	}


	/*Callback Function*/
	private void DoorStatusChange(boolean status)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[DoorStatusChange] status=" +  status);

		StatusEventArgs e = new StatusEventArgs(status);

		DoorChange(e);
	}

	public class DoorStatusChangetCallback implements BleAccess.DoorChangeCallback
	{
		public void invoke(boolean status)
		{
			DoorStatusChange(status);
		}
	}

	/*Callback Function*/
	private void LockStatusChange(boolean status)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[LockStatusChange] status=" +  status);

		StatusEventArgs e = new StatusEventArgs(status);

		LockChange(e);
	}

	public class LockStatusChangeCallback implements BleAccess.LockChangeCallback
	{
		public void invoke(boolean status)
		{
			LockStatusChange(status);
		}
	}

	/*Callback Function*/
	private void ConnectionStateChange(boolean status)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[ConnectionStateChange] status=" +  status);

		StatusEventArgs e = new StatusEventArgs(status);

		ConnectionChange(e);
	}

	public class ConnectionStateChangetCallback implements BleAccess.DisconnectCallback
	{
		public void invoke(boolean status)
		{
			ConnectionStateChange(status);
		}
	}

	/*Event : DoorChange*/
	protected void DoorChange(StatusEventArgs e)
	{
		if (OnDoorChange != null)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[DoorChange] listeners size=" +  OnDoorChange.listeners().size());
			for (StatusEventHandler listener : OnDoorChange.listeners())
			{
				listener.invoke(this, e);
			}
		}
		else
		{
			/*Do Nothing*/
			BleLog.e(LOG_TAG, LOG_CLASS + "[DoorChange] OnDoorChange is null.");
		}
	}

	/*Event : LockChange*/
	protected void LockChange(StatusEventArgs e)
	{
		if (OnLockChange != null)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[LockChange] listeners size=" +  OnLockChange.listeners().size());
			for (StatusEventHandler listener : OnLockChange.listeners())
			{
				listener.invoke(this, e);
			}
		}
		else
		{
			/*Do Nothing*/
			BleLog.e(LOG_TAG, LOG_CLASS + "[LockChange] OnLockChange is null.");
		}
	}

	/*Event : ConnectionChange*/
	protected void ConnectionChange(StatusEventArgs e)
	{
		if (OnConnectionChange != null)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[ConnectionChange] listeners size=" +  OnConnectionChange.listeners().size());
			for (StatusEventHandler listener : OnConnectionChange.listeners())
			{
				listener.invoke(this, e);
			}
		}
		else
		{
			/*Do Nothing*/
			BleLog.e(LOG_TAG, LOG_CLASS + "[ConnectionChange] OnConnectionChange is null.");
		}
	}

	/**
	 Connect the specified cipheredToken.

	 @param cipheredToken Ciphered token.
	*/

	public final void Connect(byte[] cipheredToken) throws BoxException
	{
		if (null == cipheredToken)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] Start cipheredToken=null");
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] Start cipheredToken.length=" + cipheredToken.length + " cipheredToken=" + Utility.toString(cipheredToken, cipheredToken.length));
		}

		Connect(cipheredToken, 0);

		BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] End");
	}

	/**
	 Connect the specified cipheredToken and hashToken.

	 @param cipheredToken Ciphered token.
	 @param hashToken Hash token.
	 */

	public final void Connect(byte[] cipheredToken, Integer hashToken) throws BoxException
	{
		if (null == cipheredToken)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] Start cipheredToken=null" + " hashToken=" + hashToken);
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] Start cipheredToken.length=" + cipheredToken.length + " cipheredToken=" + Utility.toString(cipheredToken, cipheredToken.length) + " hashToken=" + hashToken);
		}

		/*Connect to BleDevice with no Token*/
		if ((cipheredToken == null) && (hashToken.intValue() == 0))
		{
			if (mDevice != null)
			{
				bleAccess.Connect(mDevice);
			}
			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			}
		}
		/*Connect to BleDevice with Token*/
		else
		{
			/*Argument Check(HashToken is 0)*/
			if (hashToken.intValue() == 0)
			{
				BoxError.ThrowError(BoxErrorCode.BOX_003, " hashToken = null");
			}
			else
			{
				/*Do Nothing*/
			}

			int ret = 0;
			BleCommandProtocol cmd;
			BleCommandItem item;
			/*Start to Connect*/
			bleAccess.Connect(mDevice);

			/*Making Command*/
			cmd = MakeCommandConnect(cipheredToken, hashToken.intValue());
			/*Sending Command*/
			ret = bleAccess.SendCommand(cmd);

			/*Check Responce*/
			if (ret == 0)
			{
				item = cmd.GetNextItem();

				if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
				{
					if (item.get_data()[0] == 0x00)
					{
						/*Success*/
					}

					else
					{
						switch (item.get_data()[0])
						{
							case (byte)0xFF:
								BoxError.ThrowError(BoxErrorCode.SDK_000, " (Faied)");
								break;

							case (byte)0xFC:
								BoxError.ThrowError(BoxErrorCode.BOX_001, null);
								break;

							case (byte)0xFD:
								BoxError.ThrowError(BoxErrorCode.BOX_002, null);
								break;

							case (byte)0xFE:
								BoxError.ThrowError(BoxErrorCode.BOX_003, null);
								break;

							case (byte)0xFA:
								BoxError.ThrowError(BoxErrorCode.BOX_004, null);
								break;

							case (byte)0xF5:
								BoxError.ThrowError(BoxErrorCode.BOX_005, null);
								break;

							case (byte)0xF8:
								BoxError.ThrowError(BoxErrorCode.BOX_025, null);
								break;

							case (byte)0xF9:
								BoxError.ThrowError(BoxErrorCode.BOX_026, null);
								break;

							default:
								break;
						}
					}
				}
				else
				{
					BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemID is not ErrorCode)");
				}
			}

			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
			}
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] End");
	}

	/**
	 Disconnect current connected Device.
	*/
	public final void Disconnect() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[Disconnect] Start");

		if (mDevice != null)
		{
		    bleAccess.Disconnect(mDevice);
		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[Disconnect] End");
	}

	/**
	 Updates Token.

	 @param cipheredToken Ciphered token.
	 @param hashToken Hash token.
	*/
	public final void UpdateToken(byte[] cipheredToken, Integer hashToken) throws BoxException
	{
		if (null == cipheredToken)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateToken] Start cipheredToken=null" + " hashToken=" + hashToken);
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateToken] Start cipheredToken.length=" + cipheredToken.length + " cipheredToken=" + Utility.toString(cipheredToken, cipheredToken.length) + " hashToken=" + hashToken);
		}

		int ret = 0;
		BleCommandProtocol cmd;
		BleCommandItem item;

		/*Check Argument(cipheredToken is null)*/
		if (cipheredToken == null)
		{
			BoxError.ThrowError(BoxErrorCode.SDK_003, " cipheredToken = null");
		}
		else
		{
			/*Do Nothing*/
		}

		if (hashToken.intValue() == 0)
		{
			BoxError.ThrowError(BoxErrorCode.BOX_005, null);
		}
		else
		{
			/*Do Nothing*/
		}

		/*Making Command*/
		cmd = MakeCommandUpdateToken(cipheredToken, hashToken.intValue());

		if (cmd != null)
		{
			/*Sending Command*/
			ret = bleAccess.SendCommand(cmd);

			/*Check Responce*/
			if (ret == 0)
			{
				item = cmd.GetNextItem();

				if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
				{
					if (item.get_data()[0] == 0x00)
					{
						/*Success*/
					}

					else
					{
						switch (item.get_data()[0])
						{
							case (byte)0xFF:
								BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
								break;

							case (byte)0xFC:
								BoxError.ThrowError(BoxErrorCode.BOX_001, null);
								break;

							case (byte)0xFD:
								BoxError.ThrowError(BoxErrorCode.BOX_002, null);
								break;

							case (byte)0xFE:
								BoxError.ThrowError(BoxErrorCode.BOX_003, null);
								break;

							case (byte)0xFA:
								BoxError.ThrowError(BoxErrorCode.BOX_004, null);
								break;

							case (byte)0xF8:
								BoxError.ThrowError(BoxErrorCode.BOX_025, null);
								break;

							default:
								break;
						}
					}
				}

				else
				{
					BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
				}
			}

			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
			}
		}

		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Making Command is faied)");
		}
		/*Argument Check(HashToken is null)*/

		BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateToken] End");
	}

	/**
	 Gets the configuration.

	 @return Return configuration. Return null when failed to get Configulation
	*/
	public final BoxControllerConfig GetConfiguration() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[GetConfiguration] Start");

		int ret = 0;
		BleCommandItem item;
		BoxControllerConfig cfg = null;
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.GetConfiguration);

		/*Sending commnad*/
		ret = bleAccess.SendCommand(cmd);

		/*Check Responce and Get Configulation from Responce*/
		if (ret == 0)
		{
			item = cmd.GetNextItem();

			if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
			{
				if (item.get_data()[0] == 0x00)
				{
					/*Success*/
					try
					{
						cfg = GetBoxConfigFromCommand(cmd);
					}
					catch (RuntimeException e)
					{
						BoxError.ThrowError(BoxErrorCode.SDK_000, " (Coverting Responce(byte) to BoxConfig is Failed)");
					}

				}
				else
				{
					switch (item.get_data()[0])
					{
						case (byte) 0xFF:
							BoxError.ThrowError(BoxErrorCode.SDK_000, " (Faied)");
							break;

						case (byte)0xFC:
							BoxError.ThrowError(BoxErrorCode.BOX_001, null);
							break;

						case (byte)0xFD:
							BoxError.ThrowError(BoxErrorCode.BOX_002, null);
							break;

						case (byte)0xFE:
							BoxError.ThrowError(BoxErrorCode.BOX_003, null);
							break;

						case (byte)0xFA:
							BoxError.ThrowError(BoxErrorCode.BOX_004, null);
							break;

						case (byte)0xF6:
							BoxError.ThrowError(BoxErrorCode.BOX_006, null);
							break;

						case (byte)0xF8:
							BoxError.ThrowError(BoxErrorCode.BOX_025, null);
							break;

						default:
							cfg = null;
							break;
					}
				}
			}

			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (Making Command is faied)");
			}

		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[GetConfiguration] End");

		/*Argument Check(HashToken is null)*/
		return (cfg);
	}
	/**
	 Sets the configuration.

	 @param cipheredBoxConfig Ciphered box config.
	 @param hashConfig Hash config.
	*/
	public final void SetConfiguration(byte[] cipheredBoxConfig, int hashConfig) throws BoxException
	{
		if (null == cipheredBoxConfig)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[SetConfiguration] Start cipheredBoxConfig=null" + " hashConfig=" + hashConfig);
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[SetConfiguration] Start cipheredBoxConfig.length=" + cipheredBoxConfig.length + " cipheredToken=" + Utility.toString(cipheredBoxConfig, cipheredBoxConfig.length) + " hashConfig=" + hashConfig);
		}

		int ret = 0;
		BleCommandItem item;
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.SetConfiguration);

		/*Check Argument*/
		if (cipheredBoxConfig == null)
		{
			BoxError.ThrowError(BoxErrorCode.BOX_003, " cipheredBoxConfig = null");
		}
		else
		{
			/*Do Nothing*/
		}

		/*Making Command*/
		cmd = MakeCommandSetConfiguration(cipheredBoxConfig, hashConfig);

		if (cmd != null)
		{
			/*Sending Command*/
			ret = bleAccess.SendCommand(cmd);

			/*Check Responce*/
			if (ret == 0)
			{
				item = cmd.GetNextItem();

				if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
				{
					if (item.get_data()[0] == 0x00)
					{
						/*Success*/
					}

					else
					{
						switch (item.get_data()[0])
						{
							case (byte)0xFF:
								BoxError.ThrowError(BoxErrorCode.SDK_000, " (Faied)");
								break;

							case (byte)0xFC:
								BoxError.ThrowError(BoxErrorCode.BOX_001, null);
								break;

							case (byte)0xFD:
								BoxError.ThrowError(BoxErrorCode.BOX_002, null);
								break;

							case (byte)0xFE:
								BoxError.ThrowError(BoxErrorCode.BOX_003, null);
								break;

							case (byte)0xFA:
								BoxError.ThrowError(BoxErrorCode.BOX_004, null);
								break;

							case (byte)0xF8:
								BoxError.ThrowError(BoxErrorCode.BOX_025, null);
								break;

							default:
								break;
						}
					}
				}

				else
				{
					BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
				}
			}

			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
			}
		}

		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Making Command is failed)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[SetConfiguration] End");
	}

	/**
	 Gets box's current date time.

	 @return The date time. Fail to get : "1111/11/11 11:11:11"
	*/
	public final Date GetDateTime() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[GetDateTime] Start");

		int ret = 0;
		BleCommandItem item;
		Date dt = new Date();
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.GetDateTime);
		DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try
		{
			dt = dateTimeFormat.parse("1111/11/11 11:11:11");
		}
		catch (Exception e)
		{
			/* Do nothing */
		}

		/*Sending commnad*/
		ret = bleAccess.SendCommand(cmd);

		/*Check Responce and Get Date time from Responce*/
		if (ret == 0)
		{
			item = cmd.GetNextItem();

			if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
			{
				if (item.get_data()[0] == 0x00)
				{
					/*Success*/

					item = cmd.GetNextItem();

					if ((item != null) && (item.get_id() == BleItemId.BoxTime))
					{
						try
						{
							dt = bcd.BcdToDateTime(item.get_data());
						}
						catch (RuntimeException e)
						{
							/* Do nothing */
						}

					}
					else
					{
						/* Do nothing */
					}
				}
				else
				{
					switch (item.get_data()[0])
					{
						case (byte) 0xFF:
							BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
							break;

						case (byte)0xFC:
							BoxError.ThrowError(BoxErrorCode.BOX_001, null);
							break;

						case (byte)0xFD:
							BoxError.ThrowError(BoxErrorCode.BOX_002, null);
							break;

						case (byte)0xFE:
							BoxError.ThrowError(BoxErrorCode.BOX_003, null);
							break;

						case (byte)0xFA:
							BoxError.ThrowError(BoxErrorCode.BOX_004, null);
							break;

						case (byte)0xF8:
							BoxError.ThrowError(BoxErrorCode.BOX_025, null);
							break;

						default:
							/* Do Nothing */
							break;
					}
				}
			}

			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
			}
		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[GetDateTime] End dt=" + dt);

		return (dt);
	}

	/**
	 Sets the date time to box.

	 @return The date time.(YYYY/MM/DD HH:MM:SS)
	 @param dateTime Date time.
	*/
	public final void SetDateTime(Date dateTime) throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[SetDateTime] Start dateTime=" + dateTime.toString());

		int ret = 0;
		BleCommandItem item;
		BleCommandProtocol cmd;

		/*Check Argument*/
		if (dateTime == null)
		{
			BoxError.ThrowError(BoxErrorCode.SDK_003, " dateTime = null");
		}
		else
		{
			/*Do Nothing*/
		}

		/*Making Command*/
		cmd = MakeCommandSetDateTime(dateTime);

		/*Check Responce*/
		if (cmd != null)
		{
			/*Sending Command*/
			ret = bleAccess.SendCommand(cmd);

			if (ret == 0)
			{
				item = cmd.GetNextItem();

				if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
				{
					if (item.get_data()[0] == 0x00)
					{
						/*Success*/
					}

					else
					{
						switch (item.get_data()[0])
						{
							case (byte)0xFF:
								BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
								break;

							case (byte)0xFC:
								BoxError.ThrowError(BoxErrorCode.BOX_001, null);
								break;

							case (byte)0xFD:
								BoxError.ThrowError(BoxErrorCode.BOX_002, null);
								break;

							case (byte)0xFE:
								BoxError.ThrowError(BoxErrorCode.BOX_003, null);
								break;

							case (byte)0xFA:
								BoxError.ThrowError(BoxErrorCode.BOX_004, null);
								break;

							case (byte)0xF8:
								BoxError.ThrowError(BoxErrorCode.BOX_025, null);
								break;

							default:
								break;
						}
					}
				}
				else
				{
					BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrrorCode)");
				}
			}
			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
			}
		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Making Command is failed)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[SetDateTime] End");
	}

	/**
	 Updates the box's firmware.

	 @return Returns 0:success, -1:failed
	 @param firmware Firmware data#(byte)
	*/
	public final Integer UpdateFirmware(byte[] firmware) throws BoxException
	{
		if (null == firmware)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Start firmware=null");
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Start firmware.length=" + firmware.length);
		}

		return UpdateFirmware(firmware, 0);
	}

	public final Integer UpdateFirmware(byte[] firmware, int hashFirmware) throws BoxException
	{
		if (null == firmware)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Start firmware=null" + " hashFirmware=" + hashFirmware);
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Start firmware.length=" + firmware.length + " firmware.length=" + firmware.length + " hashFirmware=" + hashFirmware);
		}

		int ret = 0;

		/*Check Argument*/
		if (firmware == null)
		{
			BoxError.ThrowError(BoxErrorCode.SDK_003, " firmware = null");
		}
		else
		{
			/*Do Nothing*/
		}

		/*Start UpdateFirmware*/
		ret = bleAccess.UpdateFirmware(firmware);

		BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] End ret=" + ret);

		return ret;
	}

	/**
	 Gets box's battery status.

	 @return return battery status (unit:%),failed to get battery status returns 0
	*/
	public final Integer GetBatteryStatus() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[GetBatteryStatus] Start");

		int ret = 0;
		BleCommandItem item;
		int remain = 0;
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.GetBatteryStatus);

		/*Sending commnad*/
		ret = bleAccess.SendCommand(cmd);

		/*Check Responce and Get Battery Status from Responce*/
		if (ret == 0)
		{
			item = cmd.GetNextItem();

			if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
			{
				if (item.get_data()[0] == 0x00)
				{
					/*Success*/
					item = cmd.GetNextItem();

					if ((item != null) && (item.get_id() == BleItemId.BatteryStatus))
					{
						/*Change endian*/
						remain = Utility.ToInt32(item.get_data(), 0);
					}
					else
					{
						remain = 0;
					}
				}

				else
				{
					switch (item.get_data()[0])
					{
						case (byte)0xFF:
							BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
							break;

						case (byte)0xFC:
							BoxError.ThrowError(BoxErrorCode.BOX_001, null);
							break;

						case (byte)0xFD:
							BoxError.ThrowError(BoxErrorCode.BOX_002, null);
							break;

						case (byte)0xFE:
							BoxError.ThrowError(BoxErrorCode.BOX_003, null);
							break;

						case (byte)0xFA:
							BoxError.ThrowError(BoxErrorCode.BOX_004, null);
							break;

						case (byte)0xF8:
							BoxError.ThrowError(BoxErrorCode.BOX_025, null);
							break;

						case (byte)0xF7:
							BoxError.ThrowError(BoxErrorCode.BOX_008, null);
							break;

						default:
							remain = 0;
							break;
					}

				}

			}

			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
			}
		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[GetBatteryStatus] End remain=" + remain);

		return (remain);
	}

	/**
	 Gets box's status.

	 @return Return current box's status. If failed to get box status,retrun null
	*/
	public final BoxStatus GetStatus() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[GetStatus] Start");

		int ret = 0;
		BleCommandItem item;
		BoxStatus status = null;
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.GetStatus);

		/*Sending commnad*/
		ret = bleAccess.SendCommand(cmd);

		/*Check Responce and Get box status from Responce*/
		if (ret == 0)
		{
			item = cmd.GetNextItem();
			if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
			{
				if (item.get_data()[0] == 0x00)
				{
					/*Success*/
					try
					{
						status =
						GetBoxStatusFromCommand(cmd);
					}
					catch (RuntimeException e)
					{
						status = null;
					}
				}
				else
				{
					switch (item.get_data()[0])
					{
						case (byte)0xFF:
							BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
							break;

						case (byte)0xFC:
							BoxError.ThrowError(BoxErrorCode.BOX_001, null);
							break;

						case (byte)0xFD:
							BoxError.ThrowError(BoxErrorCode.BOX_002, null);
							break;

						case (byte)0xFE:
							BoxError.ThrowError(BoxErrorCode.BOX_003, null);
							break;

						case (byte)0xFA:
							BoxError.ThrowError(BoxErrorCode.BOX_004, null);
							break;

						case (byte)0xF8:
							BoxError.ThrowError(BoxErrorCode.BOX_025, null);
							break;

						default:
							status = null;
							break;
					}
				}
			}

			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
			}

		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[GetStatus] End status=" + status);

		return (status);
	}

	/**
	 Check box door is opened.

	 @return  true:opend,false:closed.
	*/
	public final Boolean IsDoorOpened() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[IsDoorOpened] Start");

		int ret = 0;
		BleCommandItem item;
		boolean opened = false;
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.IsDoorOpened);

		/*Sending commnad*/
		ret = bleAccess.SendCommand(cmd);

		/*Check Responce and Get door status from Responce*/
		if (ret == 0)
		{
			/*Success*/
			item = cmd.GetNextItem();

			if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
			{
				if (item.get_data()[0] == 0x00)
				{
					/*Success*/
					item = cmd.GetNextItem();

					if ((item != null) && (item.get_id() == BleItemId.Status))
					{
						opened = BleCommandProtocol.GetStatusBit(item.get_data(), BleBoxStatus.isDoorOpened);
					}
					else
					{
						/*Do Nothing*/
					}
				}

				else
				{
					switch (item.get_data()[0])
					{
						case (byte)0xFF:
							BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
							break;

						case (byte)0xFC:
							BoxError.ThrowError(BoxErrorCode.BOX_001, null);
							break;

						case (byte)0xFD:
							BoxError.ThrowError(BoxErrorCode.BOX_002, null);
							break;

						case (byte)0xFE:
							BoxError.ThrowError(BoxErrorCode.BOX_003, null);
							break;

						case (byte)0xFA:
							BoxError.ThrowError(BoxErrorCode.BOX_004, null);
							break;

						case (byte)0xF8:
							BoxError.ThrowError(BoxErrorCode.BOX_025, null);
							break;

						default:
							break;
					}
				}
			}
			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
			}

		}
		else
		{
		   BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[IsDoorOpened] End opened=" + opened);

		return (opened);
	}

	/**
	 Check box lock is locked.

	 @return  true:locked,false:unlocked.
	*/
	public final Boolean IsLocked() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[IsLocked] Start");

		int ret = 0;
		BleCommandItem item;
		boolean locked = false;
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.IsLocked);

		/*Sending commnad*/
		ret = bleAccess.SendCommand(cmd);

		/*Check Responce and Get lock status from Responce*/
		if (ret == 0)
		{
			item = cmd.GetNextItem();

			if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
			{
				if (item.get_data()[0] == 0x00)
				{
					/*Success*/
					item = cmd.GetNextItem();

					if ((item != null) && (item.get_id() == BleItemId.Status))
					{
						locked = BleCommandProtocol.GetStatusBit(item.get_data(), BleBoxStatus.isLockLock);
					}
					else
					{
						/*Do Nothing*/
					}
				}

				else
				{
					switch (item.get_data()[0])
					{
						case (byte)0xFF:
							BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
							break;

						case (byte)0xFC:
							BoxError.ThrowError(BoxErrorCode.BOX_001, null);
							break;

						case (byte)0xFD:
							BoxError.ThrowError(BoxErrorCode.BOX_002, null);
							break;

						case (byte)0xFE:
							BoxError.ThrowError(BoxErrorCode.BOX_003, null);
							break;

						case (byte)0xFA:
							BoxError.ThrowError(BoxErrorCode.BOX_004, null);
							break;

						case (byte)0xF8:
							BoxError.ThrowError(BoxErrorCode.BOX_025, null);
							break;

						default:
							break;
					}
				}
			}

			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
			}
		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[IsLocked] End locked=" + locked);

		return (locked);
	}

	/**
	 Unlock try to unlock box lock.
	 */
	public final void Unlock() throws BoxException
	{
		// for debug log start
		BleLog.d(LOG_TAG, LOG_CLASS + "[Unlock] Start");
		// for debug log end

		int ret = 0;
		BleCommandItem item;
		BleCommandProtocol cmd;

		/*Making Command*/
		cmd = MakeCommandUnlock();
		/*Sending Command*/
		ret = bleAccess.SendCommand(cmd);

		/*Check Responce*/
		if (ret == 0)
		{
			item = cmd.GetNextItem();

			if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
			{
				if (item.get_data()[0] == 0x00)
				{
					/*Success*/
				}
				else
				{
					switch (item.get_data()[0])
					{
						case (byte)0xFF:
							BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
							break;

						case (byte)0xFC:
							BoxError.ThrowError(BoxErrorCode.BOX_001, null);
							break;

						case (byte)0xFD:
							BoxError.ThrowError(BoxErrorCode.BOX_002, null);
							break;

						case (byte)0xFE:
							BoxError.ThrowError(BoxErrorCode.BOX_003, null);
							break;

						case (byte)0xFA:
							BoxError.ThrowError(BoxErrorCode.BOX_004, null);
							break;

						case (byte)0xF8:
							BoxError.ThrowError(BoxErrorCode.BOX_025, null);
							break;

						default:
							break;
					}
				}
			}
			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
			}
		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is failed)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[Unlock] End");
	}

	/**
	 Unlock try to unlock box lock.

	 @param encryptedPassword Lock password.
	 @param hashPassword Hash.
	*/
	public final void Unlock(byte[][] encryptedPassword, Integer[] hashPassword) throws BoxException
	{
		// for debug log start
		if (null == encryptedPassword)
		{
			if (null == hashPassword)
			{
				BleLog.d(LOG_TAG, LOG_CLASS + "[Unlock] Start encryptedPassword=null" + " hashPassword=null" + hashPassword);
			}
			else
			{
				BleLog.d(LOG_TAG, LOG_CLASS + "[Unlock] Start encryptedPassword=null" + " hashPassword=" + hashPassword);
			}
		}
		else
		{
			if (null == hashPassword)
			{
				BleLog.d(LOG_TAG, LOG_CLASS + "[Unlock] Start encryptedPassword.length=" + encryptedPassword.length + " hashPassword=null" + hashPassword);
			}
			else
			{
				BleLog.d(LOG_TAG, LOG_CLASS + "[Unlock] Start encryptedPassword.length=" + encryptedPassword.length + " hashPassword=" + hashPassword);
			}
		}
		// for debug log end

		int ret = 0;
		BleCommandItem item;
		BleCommandProtocol cmd;
		Integer num = encryptedPassword.length;
		Integer passwordNum = 0;
		boolean isDeletePassword = false;

		if (encryptedPassword == null)
		{
			encryptedPassword = new byte[num][1];
		}

		if (hashPassword == null)
		{
			hashPassword = new Integer[num];
		}

		int[] hashPasswordTmp = new int[num];
		for (int i = 0; i < num; i++, passwordNum++)
		{
			if(hashPassword[i].intValue() == 0)
			{
				break;
			}
			hashPasswordTmp[i] = hashPassword[i].intValue();
		}

		if(passwordNum == 0)
		{
			isDeletePassword = true;
		}

		/*Making Command*/
		cmd = MakeCommandUnlock(encryptedPassword, hashPasswordTmp, passwordNum, isDeletePassword);
		/*Sending Command*/
		ret = bleAccess.SendCommand(cmd);

		/*Check Responce*/
		if (ret == 0)
		{
			item = cmd.GetNextItem();

			if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
			{
				if (item.get_data()[0] == 0x00)
				{
					/*Success*/
				}
				else
				{
					switch (item.get_data()[0])
					{
						case (byte)0xFF:
							BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
							break;

						case (byte)0xFC:
							BoxError.ThrowError(BoxErrorCode.BOX_001, null);
							break;

						case (byte)0xFD:
							BoxError.ThrowError(BoxErrorCode.BOX_002, null);
							break;

						case (byte)0xFE:
							BoxError.ThrowError(BoxErrorCode.BOX_003, null);
							break;

						case (byte)0xFA:
							BoxError.ThrowError(BoxErrorCode.BOX_004, null);
							break;

						case (byte)0xF8:
							BoxError.ThrowError(BoxErrorCode.BOX_025, null);
							break;

						default:
							break;
					}
				}
			}
			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
			}
		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is failed)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[Unlock] End");
	}

	/**
	 Try to Lock box lock.
	*/
	public final void Lock() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[Lock] Start");

		int ret = 0;
		BleCommandItem item;
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.Lock);

		/*Sending commnad*/
		ret = bleAccess.SendCommand(cmd);

		/*Check Responce*/
		if (ret == 0)
		{
			item = cmd.GetNextItem();

			if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
			{
				if (item.get_data()[0] == 0x00)
				{
					/*Success*/
				}
				else
				{
					switch (item.get_data()[0])
					{
						case (byte)0xFF:
							BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
							break;

						case (byte)0xFC:
							BoxError.ThrowError(BoxErrorCode.BOX_001, null);
							break;

						case (byte)0xFD:
							BoxError.ThrowError(BoxErrorCode.BOX_002, null);
							break;

						case (byte)0xFE:
							BoxError.ThrowError(BoxErrorCode.BOX_003, null);
							break;

						case (byte)0xFA:
							BoxError.ThrowError(BoxErrorCode.BOX_004, null);
							break;

						case (byte)0xF8:
							BoxError.ThrowError(BoxErrorCode.BOX_025, null);
							break;

						default:
							break;
					}
				}
			}
			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
			}
		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[Lock] End");
	}

	/**
	 Get events from box.

	 @return events(byte).If failed to get event,return null.
	*/
	public final EventsInformation GetEvents(Boolean isDeleteEvents) throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[GetEvents] Start");

		int i = 0;
		int ret = 0;
		int eventNum = 0;
		BleCommandItem item = null;
		BleCommandProtocol cmd;;
		int eventMax = 256;
		//int buffMax = 4096;
		EventsInformation eventsInformation = new EventsInformation();
		//byte[] eventBuff = new byte[buffMax];

		/*Making Command*/
		cmd = MakeCommandGetEvents(isDeleteEvents);

		/*Sending commnad*/
		ret = bleAccess.SendCommand(cmd);

		/*Get Number of Item */
		eventNum = cmd.GetItemNum();

		/*Check Responce and Get event infomation from Responce*/
		if (ret == 0)
		{
			item = cmd.GetNextItem();

			if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
			{
				if (item.get_data()[0] == 0x00)
				{
					/*Success*/
					item = cmd.GetNextItem();

					if ((item != null) && (item.get_id() == BleItemId.LastEventDate))
					{
						eventsInformation.set_lastEventDate(bcd.BcdToDateTime(item.get_data()));
						byte[] eventBuff = new byte[eventMax * item.get_data().length];

						for (i = 0; i < eventNum - 2; i++)
						{
							item = cmd.GetNextItem();

							if ((item != null) && (item.get_id() == BleItemId.CipheredEvent))
							{
								/*Success*/
								Utility.BlockCopy(item.get_data(), 0, eventBuff, i * eventMax, item.get_data().length);
							}
							else
							{
								eventsInformation.set_events(null);
							}
						}

						eventsInformation.set_events(eventBuff);
					}
					else
					{
						DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						Date datetime = new Date();
						try {
							datetime = dateTimeFormat.parse("1111/11/11 11:11:11");
						}
						catch (Exception e)
						{
							/* Do Nothing */
						}
						eventsInformation.set_lastEventDate(datetime);
					}
				}
				else
				{
					switch (item.get_data()[0])
					{
						case (byte)0xFF:
							BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
							break;

						case (byte)0xFC:
							BoxError.ThrowError(BoxErrorCode.BOX_001, null);
							break;

						case (byte)0xFD:
							BoxError.ThrowError(BoxErrorCode.BOX_002, null);
							break;

						case (byte)0xFE:
							BoxError.ThrowError(BoxErrorCode.BOX_003, null);
							break;

						case (byte)0xFA:
							BoxError.ThrowError(BoxErrorCode.BOX_004, null);
							break;

						case (byte)0xF8:
							BoxError.ThrowError(BoxErrorCode.BOX_025, null);
							break;

						default:
							eventsInformation.set_events(null);
							break;
					}
				}
			}
			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
			}
		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[GetEvents] End");

		return (eventsInformation);
	}

	/**
	 Deletes events stored in box.

	 @return number of deleted event
	*/
	public final Integer DeleteEvents() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[DeleteEvents] Start");

		int ret = 0;
		BleCommandItem item;
		int eventNum = 0;
		BleCommandProtocol cmd;

		/*Check Argument*/

		/*Making Command*/
		cmd = MakeCommandDeleteEvents();

		/*Sending commnad*/
		ret = bleAccess.SendCommand(cmd);

		if (ret == 0)
		{
			item = cmd.GetNextItem();

			if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
			{
				if (item.get_data()[0] == 0x00)
				{
					/*Success*/
					item = cmd.GetNextItem();

					if ((item != null) && (item.get_id() == BleItemId.EventDeleteNum))
					{
						/*Success*/
						/*Change endian*/
						eventNum = Utility.ToInt32(item.get_data(), 0);
					}
					else
					{
						eventNum = 0;
					}
				}
				else
				{
					switch (item.get_data()[0])
					{
						case (byte)0xFF:
							BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
							break;

						case (byte)0xFC:
							BoxError.ThrowError(BoxErrorCode.BOX_001, null);
							break;

						case (byte)0xFD:
							BoxError.ThrowError(BoxErrorCode.BOX_002, null);
							break;

						case (byte)0xFE:
							BoxError.ThrowError(BoxErrorCode.BOX_003, null);
							break;

						case (byte)0xFA:
							BoxError.ThrowError(BoxErrorCode.BOX_004, null);
							break;

						case (byte)0xF8:
							BoxError.ThrowError(BoxErrorCode.BOX_025, null);
							break;

						default:
							eventNum = 0;
							break;
					}
				}
			}
			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
			}
		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[DeleteEvents] End eventNum=" + eventNum);

		return (eventNum);
	}

	/**
	 Resets the device. Clear all storaged data in Box.

	 @return None
	*/
	public final void ResetDevice() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[ResetDevice] Start");

		int ret = 0;
		BleCommandItem item;
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.ResetDevice);

		/*Sending commnad*/
		ret = bleAccess.SendCommand(cmd);

		/*Check Responce*/
		if (ret == 0)
		{
			item = cmd.GetNextItem();

			if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
			{
				if (item.get_data()[0] == 0x00)
				{
					/*Success*/
				}
				else
				{
					switch (item.get_data()[0])
					{
						case (byte)0xFF:
							BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
							break;

						case (byte)0xFC:
							BoxError.ThrowError(BoxErrorCode.BOX_001, null);
							break;

						case (byte)0xFD:
							BoxError.ThrowError(BoxErrorCode.BOX_002, null);
							break;

						case (byte)0xFE:
							BoxError.ThrowError(BoxErrorCode.BOX_003, null);
							break;

						case (byte)0xFA:
							BoxError.ThrowError(BoxErrorCode.BOX_004, null);
							break;

						case (byte)0xF8:
							BoxError.ThrowError(BoxErrorCode.BOX_025, null);
							break;

						default:
							break;
					}
				}
			}
			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
			}
		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[ResetDevice] End");
	}

	/**
	SetPassword try to set password box lock.

	 @param encryptedPassword Lock password.
	 @param hashPassword Hash.
	*/
	public final void SetPassword(byte[][] encryptedPassword, Integer[] hashPassword) throws BoxException
	{
		// for debug log start
		if (null == encryptedPassword)
		{
			if (null == hashPassword)
			{
				BleLog.d(LOG_TAG, LOG_CLASS + "[SetPassword] Start encryptedPassword=null" + " hashPassword=null" + hashPassword);
			}
			else
			{
				BleLog.d(LOG_TAG, LOG_CLASS + "[SetPassword] Start encryptedPassword=null" + " hashPassword=" + hashPassword);
			}
		}
		else
		{
			if (null == hashPassword)
			{
				BleLog.d(LOG_TAG, LOG_CLASS + "[SetPassword] Start encryptedPassword.length=" + encryptedPassword.length + " hashPassword=null" + hashPassword);
			}
			else
			{
				BleLog.d(LOG_TAG, LOG_CLASS + "[SetPassword] Start encryptedPassword.length=" + encryptedPassword.length + " hashPassword=" + hashPassword);
			}
		}
		// for debug log end

		int ret = 0;
		BleCommandItem item;
		BleCommandProtocol cmd;
		Integer num = encryptedPassword.length;
		Integer passwordNum = 0;

		if (encryptedPassword == null)
		{
			encryptedPassword = new byte[num][1];
		}

		if (hashPassword == null)
		{
			hashPassword = new Integer[num];
		}

		int[] hashPasswordTmp = new int[num];
		for (int i = 0; i < num; i++, passwordNum++)
		{
			if(hashPassword[i].intValue() == 0)
			{
				break;
			}
			hashPasswordTmp[i] = hashPassword[i].intValue();
		}

		/*Making Command*/
		cmd = MakeCommandSetPassword(encryptedPassword, hashPasswordTmp, passwordNum);
		/*Sending Command*/
		ret = bleAccess.SendCommand(cmd);

		/*Check Responce*/
		if (ret == 0)
		{
			item = cmd.GetNextItem();

			if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
			{
				if (item.get_data()[0] == 0x00)
				{
					/*Success*/
				}
				else
				{
					switch (item.get_data()[0])
					{
						case (byte)0xFF:
							BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
							break;

						case (byte)0xFC:
							BoxError.ThrowError(BoxErrorCode.BOX_001, null);
							break;

						case (byte)0xFD:
							BoxError.ThrowError(BoxErrorCode.BOX_002, null);
							break;

						case (byte)0xFE:
							BoxError.ThrowError(BoxErrorCode.BOX_003, null);
							break;

						case (byte)0xFA:
							BoxError.ThrowError(BoxErrorCode.BOX_004, null);
							break;

						case (byte)0xF8:
							BoxError.ThrowError(BoxErrorCode.BOX_025, null);
							break;

						default:
							break;
					}
				}
			}
			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
			}
		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is failed)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[SetPassword] End");
	}

	/**
	 Get password from box.

	 @return password(byte).If failed to get event,return null.
	*/
	public final byte[][] GetPassword() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[GetPassword] Start");

		int i = 0;
		int ret = 0;
		int passwordNum = 0;
		BleCommandItem item = null;
		BleCommandProtocol cmd;;
		byte[][] password = null;

		/*Making Command*/
		cmd = MakeCommandGetPassword();

		/*Sending commnad*/
		ret = bleAccess.SendCommand(cmd);

		/*Get Number of Item */

		/*Check Responce and Get event infomation from Responce*/
		if (ret == 0)
		{
			item = cmd.GetNextItem();

			if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
			{
				if (item.get_data()[0] == 0x00)
				{
					passwordNum = cmd.GetItemNum() - 1;
					if (passwordNum > 0) {
						password = new byte[passwordNum][16];
						for (i = 0; i < passwordNum; i++) {
							item = cmd.GetNextItem();

							if ((item != null) && (item.get_id() == BleItemId.CipheredPassword)) {
								/*Success*/
								Utility.BlockCopy(item.get_data(), 0, password[i], 0, item.get_data().length);
							} else {
								password[i] = null;
							}
						}
					}
				}
				else
				{
					switch (item.get_data()[0])
					{
						case (byte)0xFF:
							BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
							break;

						case (byte)0xFC:
							BoxError.ThrowError(BoxErrorCode.BOX_001, null);
							break;

						case (byte)0xFD:
							BoxError.ThrowError(BoxErrorCode.BOX_002, null);
							break;

						case (byte)0xFE:
							BoxError.ThrowError(BoxErrorCode.BOX_003, null);
							break;

						case (byte)0xFA:
							BoxError.ThrowError(BoxErrorCode.BOX_004, null);
							break;

						case (byte)0xF8:
							BoxError.ThrowError(BoxErrorCode.BOX_025, null);
							break;

						default:
							password = null;
							break;
					}
				}
			}
			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
			}
		}
		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[GetPassword] End");

		return (password);
	}

	/**
	 Change Key.

	 @param cipheredKey Ciphered Key.
	 @param hashKey Hash Key.
	 */
	public final void ChangeKey(byte[] cipheredKey, Integer hashKey) throws BoxException
	{
		if (null == cipheredKey)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[ChangeKey] Start cipheredKey=null" + " hashKey=" + hashKey);
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[ChangeKey] Start cipheredKey.length=" + cipheredKey.length + " cipheredKey=" + Utility.toString(cipheredKey, cipheredKey.length) + " hashKey=" + hashKey);
		}

		int ret = 0;
		BleCommandProtocol cmd;
		BleCommandItem item;

		/*Check Argument(cipheredKey is null)*/
		if (cipheredKey == null)
		{
			BoxError.ThrowError(BoxErrorCode.SDK_003, " cipheredKey = null");
		}
		else
		{
			/*Do Nothing*/
		}

		if (hashKey.intValue() == 0)
		{
			BoxError.ThrowError(BoxErrorCode.BOX_005, null);
		}
		else
		{
			/*Do Nothing*/
		}

		/*Making Command*/
		cmd = MakeCommandChangeKey(cipheredKey, hashKey.intValue());

		if (cmd != null)
		{
			/*Sending Command*/
			ret = bleAccess.SendCommand(cmd);

			/*Check Responce*/
			if (ret == 0)
			{
				item = cmd.GetNextItem();

				if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
				{
					if (item.get_data()[0] == 0x00)
					{
						/*Success*/
					}

					else
					{
						switch (item.get_data()[0])
						{
							case (byte)0xFF:
								BoxError.ThrowError(BoxErrorCode.SDK_000, " (Failed)");
								break;

							case (byte)0xFC:
								BoxError.ThrowError(BoxErrorCode.BOX_001, null);
								break;

							case (byte)0xFD:
								BoxError.ThrowError(BoxErrorCode.BOX_002, null);
								break;

							case (byte)0xFE:
								BoxError.ThrowError(BoxErrorCode.BOX_003, null);
								break;

							case (byte)0xFA:
								BoxError.ThrowError(BoxErrorCode.BOX_004, null);
								break;

							case (byte)0xF8:
								BoxError.ThrowError(BoxErrorCode.BOX_025, null);
								break;

							default:
								break;
						}
					}
				}

				else
				{
					BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
				}
			}

			else
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (Sending Command is faied)");
			}
		}

		else
		{
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (Making Command is faied)");
		}
		/*Argument Check(HashToken is null)*/

		BleLog.d(LOG_TAG, LOG_CLASS + "[ChangeKey] End");
	}

	/*Make Command for Connect Function*/
	private BleCommandProtocol MakeCommandConnect(byte[] cipheredToken, int hashToken)
	{
		if (null == cipheredToken)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandConnect] Start cipheredToken=null" + " hashToken" + hashToken);
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandConnect] Start cipheredToken.length=" + cipheredToken.length + " firmware=" + Utility.toString(cipheredToken, cipheredToken.length) + " hashToken" + hashToken);
		}

		int ret = 0;
		byte[] hashTokenByte;
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.UpdateToken);

		/*AddItem:cipheredToken*/
		ret = cmd.AddItem(BleItemId.Token, cipheredToken);

		if (ret == 0)
		{
			hashTokenByte = Utility.GetBytes(hashToken);
			/*Change endian*/
			Utility.Reverse(hashTokenByte);
			/*AddItem:HashToken*/
			ret = cmd.AddItem(BleItemId.Hash, hashTokenByte);
		}
		else
		{
			/*Do Nothing*/
			BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandConnect] Token add error ret=" + ret);
		}

		if (ret == 0)
		{
			/*Making Command Success*/
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandConnect] Token/Hash add error ret=" + ret);
			cmd = null;
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandConnect] End CurrCommand=" + cmd.get_CurrCommand() + " PacketLen=" + cmd.get_SendPacketLen() + " SendBuffer=" + Utility.toString(cmd.get_SendBuffer(), cmd.get_SendPacketLen()));

		return (cmd);
	}

	/*Make Command for UpdateToken Function*/
	private BleCommandProtocol MakeCommandUpdateToken(byte[] cipheredToken, int hashToken)
	{
		if (null == cipheredToken)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandUpdateToken] Start cipheredToken=null" + " hashToken" + hashToken);
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandUpdateToken] Start cipheredToken.length=" + cipheredToken.length + " cipheredToken=" + Utility.toString(cipheredToken, cipheredToken.length) + " hashToken" + hashToken);
		}

		int ret = 0;
		byte[] hashTokenByte;
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.UpdateToken);

		/*AddItem:cipheredToken*/
		ret = cmd.AddItem(BleItemId.Token, cipheredToken);

		if (ret == 0)
		{
			hashTokenByte = Utility.GetBytes(hashToken);
			/*Change endian*/
			Utility.Reverse(hashTokenByte);
			/*AddItem:HashToken*/
			ret = cmd.AddItem(BleItemId.Hash, hashTokenByte);
		}
		else
		{
			/*Do Nothing*/
		}

		if (ret == 0)
		{
			/*Making Command Success*/
		}

		else
		{
			cmd = null;
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandUpdateToken] End CurrCommand=" + cmd.get_CurrCommand() + " PacketLen=" + cmd.get_SendPacketLen() + " SendBuffer=" + Utility.toString(cmd.get_SendBuffer(), cmd.get_SendPacketLen()));

		return (cmd);
	}

	/*Make Command for SetConfiguration Function*/
	private BleCommandProtocol MakeCommandSetConfiguration(byte[] cipheredBoxConfig, int hashConfig)
	{
		if (null == cipheredBoxConfig)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandSetConfiguration] Start cipheredBoxConfig=null" + " hashConfig" + hashConfig);
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandSetConfiguration] Start cipheredBoxConfig.length=" + cipheredBoxConfig.length + " cipheredBoxConfig=" + Utility.toString(cipheredBoxConfig, cipheredBoxConfig.length) + " hashConfig" + hashConfig);
		}

		int ret = 0;
		byte[] hashconfigByte;
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.SetConfiguration);

		/*AddItem:cipheredBoxConfig*/
		ret = cmd.AddItem(BleItemId.CipheredConfig, cipheredBoxConfig);

		if (ret == 0)
		{
			hashconfigByte = Utility.GetBytes(hashConfig);
			/*Change endian*/
			Utility.Reverse(hashconfigByte);
			/*AddItem:HashConfig*/
			ret = cmd.AddItem(BleItemId.Hash, hashconfigByte);
		}

		else
		{
			/*Do Nothing*/
		}

		if (ret == 0)
		{
			/*Making Command Success*/
		}

		else
		{
			cmd = null;
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandSetConfiguration] End CurrCommand=" + cmd.get_CurrCommand() + " PacketLen=" + cmd.get_SendPacketLen() + " SendBuffer=" + Utility.toString(cmd.get_SendBuffer(), cmd.get_SendPacketLen()));

		return (cmd);

	}

	/*Make Command for SetDateTime Function*/
	private BleCommandProtocol MakeCommandSetDateTime(Date dateTime)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandSetDateTime] Start dateTime" + dateTime.toString());

		int ret = 0;
		byte[] datetimeByte;
		BcdTimeFormat bcdTimeFormat = new BcdTimeFormat();
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.SetDateTime);

		/*Convert to byte*/
		datetimeByte = bcdTimeFormat.DateTimeToBcd(dateTime);
		/*AddItem:DateTime*/
		ret = cmd.AddItem(BleItemId.BoxTime, datetimeByte);

		if (ret == 0)
		{
			/*Making Command Success*/
		}

		else
		{
			cmd = null;
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandSetDateTime] End CurrCommand=" + cmd.get_CurrCommand() + " PacketLen=" + cmd.get_SendPacketLen() + " SendBuffer=" + Utility.toString(cmd.get_SendBuffer(), cmd.get_SendPacketLen()));

		return (cmd);
	}

	/*Make Command for Unlock Function*/
	private BleCommandProtocol MakeCommandUnlock() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandUnlock] Start");

		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.Unlock);

		int ret;

		ret = cmd.AddItem(BleItemId.CipheredPassword, new byte[1]);

		if (ret == 0)
		{
			ret = cmd.AddItem(BleItemId.Hash,  new byte[1]);
		}

		if (ret == 0)
		{
			ret = cmd.AddItem(BleItemId.IsDeletePassword, Utility.GetBytes(false));
		}

		if (ret == 0)
		{
			/*Making Command Success*/
		}

		else
		{
			cmd = null;
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandSetDateTime] End CurrCommand=" + cmd.get_CurrCommand() + " PacketLen=" + cmd.get_SendPacketLen() + " SendBuffer=" + Utility.toString(cmd.get_SendBuffer(), cmd.get_SendPacketLen()));

		return (cmd);

	}

	/*Make Command for Unlock Function*/
	private BleCommandProtocol MakeCommandUnlock(byte[][] encryptedPassword, int[] hashPassword, int num, boolean isDeletePassword) throws BoxException
	{
		if (null == encryptedPassword)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandUnlock] Start encryptedPassword=null" + " hashPassword" + hashPassword[0]);
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandUnlock] Start encryptedPassword.length=" + encryptedPassword.length + " encryptedPassword=" + Utility.toString(encryptedPassword[0], encryptedPassword[0].length) + " hashPassword" + hashPassword[0]);
		}

		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.Unlock);

		int ret = 0;

		byte[] hashPasswordByte = new byte[1];
		for (int i = 0; i < num; i++)
		{
			if (ret == 0)
			{
				/*AddItem:encryptedPassword*/
				ret = cmd.AddItem(BleItemId.CipheredPassword, encryptedPassword[i]);
			}

			if (ret == 0)
			{
				hashPasswordByte = Utility.GetBytes(hashPassword[i]);
				/*Change endian*/
				Utility.Reverse(hashPasswordByte);
				/*AddItem:HashConfig*/
				ret = cmd.AddItem(BleItemId.Hash, hashPasswordByte);
			}
		}

		if (ret == 0)
		{
			ret = cmd.AddItem(BleItemId.IsDeletePassword, Utility.GetBytes(isDeletePassword));
		}

		if (ret == 0)
		{
			/*Making Command Success*/
		}

		else
		{
			cmd = null;
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandSetDateTime] End CurrCommand=" + cmd.get_CurrCommand() + " PacketLen=" + cmd.get_SendPacketLen() + " SendBuffer=" + Utility.toString(cmd.get_SendBuffer(), cmd.get_SendPacketLen()));

		return (cmd);

	}

	/*Make Command for DeleteEvents Function*/
	private BleCommandProtocol MakeCommandGetEvents(Boolean isDeleteEvents)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandGetEvents] Start isDeleteEvents=" + isDeleteEvents);

		int ret = 0;
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.GetEvents);

		/*AddItem:IsDeleteEvents*/
		ret = cmd.AddItem(BleItemId.IsDeleteEvents, Utility.GetBytes(isDeleteEvents));

		if (ret == 0)
		{
			/*Making Command Success*/
		}

		else
		{
			cmd = null;
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandGetEvents] End CurrCommand=" + cmd.get_CurrCommand() + " PacketLen=" + cmd.get_SendPacketLen() + " SendBuffer=" + Utility.toString(cmd.get_SendBuffer(), cmd.get_SendPacketLen()));

		return (cmd);
	}

	/*Make Command for DeleteEvents Function*/
	private BleCommandProtocol MakeCommandDeleteEvents()
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandDeleteEvents] Start");

		int ret = 0;
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.DeleteEvents);

		if (ret == 0)
		{
			/*Making Command Success*/
		}

		else
		{
			cmd = null;
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandDeleteEvents] End CurrCommand=" + cmd.get_CurrCommand() + " PacketLen=" + cmd.get_SendPacketLen() + " SendBuffer=" + Utility.toString(cmd.get_SendBuffer(), cmd.get_SendPacketLen()));

		return (cmd);
	}

	/*Make Command for SetPassword Function*/
	private BleCommandProtocol MakeCommandSetPassword(byte[][] encryptedPassword, int[] hashPassword, int num) throws BoxException
	{
		if (null == encryptedPassword)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandSetPassword] Start encryptedPassword=null" + " hashPassword" + hashPassword);
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandSetPassword] Start encryptedPassword.length=" + encryptedPassword.length + " encryptedPassword=" + Utility.toString(encryptedPassword[0], encryptedPassword[0].length) + " hashPassword" + hashPassword[0]);
		}

		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.SetPassword);

		int ret = 0;
		byte[] hashPasswordByte = new byte[1];
		for (int i = 0; i < num; i++)
		{

			/*AddItem:encryptedPassword*/
			ret = cmd.AddItem(BleItemId.CipheredPassword, encryptedPassword[i]);

			if (ret == 0)
			{
				hashPasswordByte = Utility.GetBytes(hashPassword[i]);
				/*Change endian*/
				Utility.Reverse(hashPasswordByte);
				/*AddItem:HashConfig*/
				ret = cmd.AddItem(BleItemId.Hash, hashPasswordByte);
			}

			if (ret == 0)
			{
				/*Making Command Success*/
			}

			else
			{
				cmd = null;
			}
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandSetPassword] End CurrCommand=" + cmd.get_CurrCommand() + " PacketLen=" + cmd.get_SendPacketLen() + " SendBuffer=" + Utility.toString(cmd.get_SendBuffer(), cmd.get_SendPacketLen()));

		return (cmd);

	}

	/*Make Command for GetPassword Function*/
	private BleCommandProtocol MakeCommandGetPassword() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandGetPassword] Start");

		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.GetPassword);

		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandGetPassword] End CurrCommand=" + cmd.get_CurrCommand() + " PacketLen=" + cmd.get_SendPacketLen() + " SendBuffer=" + Utility.toString(cmd.get_SendBuffer(), cmd.get_SendPacketLen()));

		return (cmd);

	}

	/*Make Command for ChangeKey Function*/
	private BleCommandProtocol MakeCommandChangeKey(byte[] cipheredKey, int hashKey)
	{
		if (null == cipheredKey)
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandChangeKey] Start cipheredKey=null" + " hashKey" + hashKey);
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandChangeKey] Start cipheredKey.length=" + cipheredKey.length + " cipheredKey=" + Utility.toString(cipheredKey, cipheredKey.length) + " hashToken" + hashKey);
		}

		int ret = 0;
		byte[] hashKeyByte;
		BleCommandProtocol cmd = new BleCommandProtocol(BleCommand.ChangeKey);

		/*AddItem:cipheredToken*/
		ret = cmd.AddItem(BleItemId.AESKey, cipheredKey);

		if (ret == 0)
		{
			hashKeyByte = Utility.GetBytes(hashKey);
			/*Change endian*/
			Utility.Reverse(hashKeyByte);
			/*AddItem:HashKey*/
			ret = cmd.AddItem(BleItemId.Hash, hashKeyByte);
		}
		else
		{
			/*Do Nothing*/
		}

		if (ret == 0)
		{
			/*Making Command Success*/
		}

		else
		{
			cmd = null;
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[MakeCommandChangeKey] End CurrCommand=" + cmd.get_CurrCommand() + " PacketLen=" + cmd.get_SendPacketLen() + " SendBuffer=" + Utility.toString(cmd.get_SendBuffer(), cmd.get_SendPacketLen()));

		return (cmd);
	}

	/*Convert Command to BoxConfiguration*/
	private BoxControllerConfig GetBoxConfigFromCommand(BleCommandProtocol cmd)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[GetBoxConfigFromCommand] Start CurrCommand=" + cmd.get_CurrCommand() + " PacketLen=" + cmd.get_SendPacketLen() + " SendBuffer=" + Utility.toString(cmd.get_SendBuffer(), cmd.get_SendPacketLen()));

		int itemNum = 0;
		int i = 0;
		byte[] channelUsedByte = new byte[4];
		BleCommandItem item;
		BoxControllerConfig cfg = new BoxControllerConfig();

		itemNum = cmd.GetItemNum();

		/*Get Item data from Command*/
		for (i = 0; i < itemNum; i++)
		{
			item = cmd.GetNextItem();

			if (item != null)
			{
				switch (item.get_id())
				{
					/*Get AdvertiseInterval data*/
					case AdvertiseInterval :
						cfg.set_advertisingInterval(Utility.ToInt32(item.get_data(), 0));
						BleLog.d(LOG_TAG, LOG_CLASS + "[GetBoxConfigFromCommand] AdvertiseInterval" +
							" advertisingInterval=" + cfg.get_advertisingInterval()
						);

						break;
					/*Get StrengthPower data*/
					case StrengthPower :
						cfg.set_strengthPower(Utility.ToInt32(item.get_data(), 0));
						BleLog.d(LOG_TAG, LOG_CLASS + "[GetBoxConfigFromCommand] StrengthPower" +
							"strengthPower=" + cfg.get_strengthPower()
						);

						break;
					/*Get ChannelUsed data*/
					case ChannelUsed :

						/*Prepare for Convert item data to uint*/
						if (item.get_data().length < 4)
						{
							Utility.BlockCopy(item.get_data(), 0, channelUsedByte, 0, item.get_data().length);
							Utility.Reverse(channelUsedByte);
						}
						else
						{
							/*Do Nothing*/
						}

						cfg.set_channelUsed(BleChannel.forValue(Utility.ToInt32(channelUsedByte,0)));
						BleLog.d(LOG_TAG, LOG_CLASS + "[GetBoxConfigFromCommand] ChannelUsed" +
							"channelUsed=" + cfg.get_channelUsed()
						);

						break;
					/*Get AttemptMax data*/
					case AttemptMax:
						cfg.set_attemptMax(Utility.ToInt32(item.get_data(), 0));
						BleLog.d(LOG_TAG, LOG_CLASS + "[GetBoxConfigFromCommand] AttemptMax" +
							"attemptMax=" + cfg.get_attemptMax()
						);

						break;
					/*Get AutomaticallyCloseTime data*/
					case AutoClose :
						cfg.set_autoCloseTime(Utility.ToInt32(item.get_data(), 0));
						BleLog.d(LOG_TAG, LOG_CLASS + "[GetBoxConfigFromCommand] AutoClose" +
							"autoCloseTime=" + cfg.get_autoCloseTime()
						);

						break
					/*Get InputImpossibleTime data*/;
					case InputImpossibleTime:
						cfg.set_inputImpossibleTime(Utility.ToInt32(item.get_data(), 0));
						BleLog.d(LOG_TAG, LOG_CLASS + "[GetBoxConfigFromCommand] InputImpossibleTime" +
							"inputImpossibleTime=" + cfg.get_inputImpossibleTime()
						);

						break;
					/*Get WarningEventNumber data*/
					case WarnEventNum:
						cfg.set_warnEventNum(Utility.ToInt32(item.get_data(), 0));
						BleLog.d(LOG_TAG, LOG_CLASS + "[GetBoxConfigFromCommand] WarnEventNum" +
							"warnEventNum=" + cfg.get_warnEventNum()
						);

						break;
					/*Get ConnectionTimeOut data*/
					case ConnTimeOut:
						cfg.set_connectionTimeOut(Utility.ToInt32(item.get_data(), 0));
						BleLog.d(LOG_TAG, LOG_CLASS + "[GetBoxConfigFromCommand] ConnTimeOut" +
							"connectionTimeOut=" + cfg.get_connectionTimeOut()
						);

						break;
					/*Get ConnectionTimeOut data*/
					case PasswordDeleteTime:
						cfg.set_passwordDeleteTime(Utility.ToInt32(item.get_data(), 0));
						BleLog.d(LOG_TAG, LOG_CLASS + "[GetBoxConfigFromCommand] PasswordDeleteTime" +
							"passwordDeleteTime=" + cfg.get_passwordDeleteTime()
						);

						break;


					default :

						break;
				}
			}
			else
			{
				/*Do Nothing*/
			}
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[GetBoxConfigFromCommand] End");

		return (cfg);
	}

	/*Convert Command to BoxStatus*/
	private BoxStatus GetBoxStatusFromCommand(BleCommandProtocol cmd)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[GetBoxStatusFromCommand] Start CurrCommand=" + cmd.get_CurrCommand() + " PacketLen=" + cmd.get_SendPacketLen() + " SendBuffer=" + Utility.toString(cmd.get_SendBuffer(), cmd.get_SendPacketLen()));

		int itemNum = 0;
		int i = 0;
		BleCommandItem item;
		BcdTimeFormat bcd = new BcdTimeFormat();
		BoxStatus boxStatus = new BoxStatus();

		itemNum = cmd.GetItemNum();

		/*Get Item data from Command*/
		for (i = 0; i < itemNum; i++)
		{
			item = cmd.GetNextItem();

			if (item != null)
			{
				switch (item.get_id())
				{
					/*Get Status data*/
					case Status:

						boxStatus.set_isDoorOpen(BleCommandProtocol.GetStatusBit(item.get_data(), BleBoxStatus.isDoorOpened));
						boxStatus.set_isLockLock(BleCommandProtocol.GetStatusBit(item.get_data(), BleBoxStatus.isLockLock));
						boxStatus.set_isTooMuchEvent(BleCommandProtocol.GetStatusBit(item.get_data(), BleBoxStatus.isTooMuchEvent));
						boxStatus.set_isPasswordSet(BleCommandProtocol.GetStatusBit(item.get_data(), BleBoxStatus.passwordSet));

						break;

					default:

						break;

				}

			}

			else
			{
				/*Do Nothing*/
			}

		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[GetBoxStatusFromCommand] End boxStatus=" + boxStatus);

		return (boxStatus);
	}
}