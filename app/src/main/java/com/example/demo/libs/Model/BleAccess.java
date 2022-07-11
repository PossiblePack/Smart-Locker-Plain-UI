package com.example.demo.libs.Model;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BleAccess implements IBleAccess
{
	private static final String LOG_TAG = "SLSDK";
	private static final String LOG_CLASS = "[BleAccess]";

	/* Callback Interface */
	@FunctionalInterface
	public interface DeviceDiscoveredCallback
	{
		public void invoke(BleAccess bleAccess, BleDevice bleDevice);
	}
	@FunctionalInterface
	public interface ScanTimeoutCallback
	{
		public void invoke();
	}
	@FunctionalInterface
	public interface DoorChangeCallback
	{
		void invoke(boolean isDoorChanged);
	}
	@FunctionalInterface
	public interface LockChangeCallback
	{
		void invoke(boolean isLockChanged);
	}
	@FunctionalInterface
	public interface DisconnectCallback
	{
		void invoke(boolean isDisconnected);
	}

	/* Callback */
	public static DeviceDiscoveredCallback deviceDiscoveredCallback;
	public static ScanTimeoutCallback scanTimeoutCallback;
	public ArrayList<DoorChangeCallback> doorChangeCallback = new ArrayList<DoorChangeCallback>();
	public ArrayList<LockChangeCallback> lockChangeCallback = new ArrayList<LockChangeCallback>();
	public ArrayList<DisconnectCallback> disconnectCallback = new ArrayList<DisconnectCallback>();

	/* private member */
	private static Context mContext; /* for BluetoothDevice.connectGatt Parameter */
	private BluetoothAdapter mAdapter;
	//private BleAccess bleAccess = this; /* for Callback Parameter */
	private BleDevice bleDevice;
	private BluetoothDevice currentConnectedDevice;
	private BluetoothGatt mGatt;
	private BleCommandProtocol ble_cmd;

	BluetoothGattDescriptor status_descriptor;

	/*Characteristic*/
	private BluetoothGattCharacteristic status_characteristic;
	private BluetoothGattCharacteristic command_characteristic;
	private BluetoothGattCharacteristic response_characteristic;
	private BluetoothGattCharacteristic OTAControl_characteristic;
	private BluetoothGattCharacteristic OTAData_characteristic;

	/* Scan Data */
	private static final int scanRecordTopOffset = 5;
	private static final String scanRecordBoxName = "PS-Lock";
	private static final int scanRecordBoxNameStart = 12;
	private static final int scanRecordBoxNameEnd = scanRecordBoxNameStart + (8 - 1) - 1;
	private static final int scanRecordMacAddressStart = 35;
	private static final int scanRecordMacAddressEnd = scanRecordMacAddressStart + 6 - 1;

	/* UUID */
	private static final UUID service_uuid = UUID.fromString("dc8e2744-a374-458d-8d09-95af60463529");
	private static final UUID service_OTA_uuid = UUID.fromString("1d14d6ee-fd63-4fa1-bfa4-8f47b42119f0");
	private static final UUID response_uuid = UUID.fromString("b9331036-fb06-4d15-bafd-e07ceb1b75ac");
	private static final UUID command_uuid = UUID.fromString("25bd9d85-ab09-4c0c-8b98-126f6360fd3d");
	private static final UUID status_uuid = UUID.fromString("6fff8fb7-3bd8-4954-97bb-61977acb5456");
	private static final UUID OTAControl_uuid = UUID.fromString("F7BF3564-FB6D-4E53-88A4-5E37E0326063");
	private static final UUID OTAData_uuid = UUID.fromString("984227F3-34FC-4045-A5D0-2C581F81A153");

	/* for async control */
	/* Scan */
	private static boolean isScanStoped = true;
	private static final int scan_poling_interval = 100;
	private static int scan_poling_counter = 0;
	/* GATT */
	private static final int gattTimeout = 60000;
	private boolean isGattSuccess = false;
	private static final int gatt_poling_interval = 100;
	private int gatt_poling_counter = 0;
	/* Resp */
	private  boolean isRespError = false;
	/* Read */
	private static final int readTimeout = 10000;
	private boolean isReadSuccess = false;
	private static final int read_poling_interval = 100;
	private int read_poling_counter = 0;
	/* Write */
	private static final int detectionTimeout = 10000;
	private boolean isWriteSuccess = false;
	private static final int write_poling_interval = 100;
	private int write_poling_counter = 0;

	public static final void onScanSub(BluetoothDevice device, int rssi, byte[] scanRecord)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[BleAccess][onScanSub] Start" + ":device={name:" + device.getName() + " Address:" + device.getAddress() + " rssi=" + rssi +
				" scanRecord.length=" + scanRecord.length +
				" scanRecord=" + Utility.toString(scanRecord, scanRecord.length)
		);

		if (null == device)
		{
			/* Bluetooth Device is null */
			BleLog.d(LOG_TAG, LOG_CLASS + "[BleAccess][onScanSub] Bluetooth Device is null.");
			return;
		}

		if (scanRecord.length < (scanRecordMacAddressEnd + 1))
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[BleAccess][onScanSub] Bluetooth Device is not BOX(Jadge scanRecord Length).");
			return;
		}

		if (isScanStoped)
		{
			BleLog.e(LOG_TAG, LOG_CLASS + "[BleAccess][onScanSub] Already Scan Stoped.");
			return;
		}

		String macAddress = "";
		for (int i = scanRecordMacAddressStart; i <= scanRecordMacAddressEnd; i++)
		{
			macAddress += String.format("%02X", scanRecord[i]);
			if ( i != scanRecordMacAddressEnd )
			{
				macAddress += ":";
			}
		}
		BleLog.d(LOG_TAG, LOG_CLASS + "[BleAccess][onScanSub] macAddress=" + macAddress + " macAddress=" + macAddress);
		String boxNameFromscanRecord = "";
		for (int i = scanRecordBoxNameStart; i <= scanRecordBoxNameEnd; i++)
		{
			boxNameFromscanRecord += (char)scanRecord[i];
		}
		if (!(scanRecordBoxName.equals(boxNameFromscanRecord)))
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[BleAccess][onScanSub] Bluetooth Device is not BOX(Jadge scanRecord BOX Name).");
			return;
		}

		if (null != deviceDiscoveredCallback)
		{
			/* Callback to BoxManager */
			BleDevice bleDev = new BleDevice();
			bleDev.set_Device(device);
			byte[] advBuffer = new byte[scanRecord.length - scanRecordTopOffset];
			Utility.BlockCopy(scanRecord, scanRecordTopOffset, advBuffer, 0, advBuffer.length);
			bleDev.set_AdvertisedData(advBuffer);
			bleDev.set_DeviceCodeData(macAddress);

			try {
				deviceDiscoveredCallback.invoke(new BleAccess(mContext), bleDev);
			} catch (BoxException e)
			{
				BleLog.e(LOG_TAG, LOG_CLASS + "[BleAccess][onScanSub[E] new BleAccess()." + e.toString());
			}
		}
		else
		{
			BleLog.e(LOG_TAG, LOG_CLASS + "[BleAccess][onScanSub] Device Discovered Callback is null.");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[BleAccess][onScanSub] End");
	}

	/* Bluetooth Device Scan Callback */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private final ScanCallback mScanCallback = new ScanCallback()
	{
		@Override
		public void onScanResult(int callbackType, ScanResult result)
		{
			BluetoothDevice device = result.getDevice();
			int rssi = result.getRssi();
			byte[] scanRecord = result.getScanRecord().getBytes();
			BleAccess.onScanSub(device, rssi, scanRecord);
		}
	};
	private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
	{
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
		{
			BleAccess.onScanSub(device, rssi, scanRecord);
		}
	};

	/* Bluetooth GATT Callback */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		private BluetoothGattService mBluetoothGattService;

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			BleLog.d(LOG_TAG, LOG_CLASS + "[BluetoothGattCallback][onConnectionStateChange] Start" + " gatt.device=" + gatt.getDevice().getName() + " state=" + status + " newState=" + newState);
			if (null == gatt) {
				BleLog.d(LOG_TAG, LOG_CLASS + "[BluetoothGattCallback][onConnectionStateChange] gatt is null.");
				return;
			}
			BleLog.d(LOG_TAG, LOG_CLASS + "[BluetoothGattCallback][onConnectionStateChange]" + "gatt=" + gatt.toString());

			if (newState == BluetoothProfile.STATE_CONNECTED) {
				BleLog.d(LOG_TAG, LOG_CLASS + "[BluetoothGattCallback][onConnectionStateChange] Connected to GATT server.");
				if (false == gatt.discoverServices()) {
					BleLog.d(LOG_TAG, LOG_CLASS + "[BluetoothGattCallback][onConnectionStateChange] GATT Discover Services error.");
				}
				/* Start Serch Services to onServicesDiscovered */
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				BleLog.d(LOG_TAG, LOG_CLASS + "[BluetoothGattCallback][onConnectionStateChange] Disconnected from GATT server.");
				mGatt.close();
				mGatt = null;
				BleLog.d(LOG_TAG, LOG_CLASS + "[BluetoothGattCallback][onConnectionStateChange] currentConnectedDevice=null");
				currentConnectedDevice = null;
				gatt_poling_counter = 0;
				if (disconnectCallback != null) {
					/*Callback Disconnect Information*/
					for (DisconnectCallback cb : disconnectCallback) {
						cb.invoke(true);
					}
				} else {
					/*Do Nothing*/
				}
			} else if (newState == BluetoothProfile.STATE_CONNECTING) {
				BleLog.d(LOG_TAG, LOG_CLASS + "[BluetoothGattCallback][onConnectionStateChange] Connecting from GATT server.");
			} else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
				BleLog.d(LOG_TAG, LOG_CLASS + "[BluetoothGattCallback][onConnectionStateChange] Disconnecting from GATT server.");
			} else {
				BleLog.d(LOG_TAG, LOG_CLASS + "[BluetoothGattCallback][onConnectionStateChange] Unknown from GATT server.");
			}

			BleLog.d(LOG_TAG, LOG_CLASS + "[BluetoothGattCallback][onConnectionStateChange] End");
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] Start" + ":gatt=" + gatt.toString() + " gatt.device=" + gatt.getDevice().getName() + " state=" + status);

			if (null == mGatt) {
				BleLog.e(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] Bluetooth GATT is disconnected.");
			} else if (status == BluetoothGatt.GATT_SUCCESS) {
				/* Clear characteristic */
				response_characteristic = null;
				command_characteristic = null;
				status_characteristic = null;
				OTAControl_characteristic = null;
				OTAData_characteristic = null;

				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] Compare Service UUID for respons/command/status =" + service_uuid);
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] Compare Service UUID for OTA =" + service_OTA_uuid);
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] Compare Characteristic UUID for response =" + response_uuid);
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] Compare Characteristic UUID for command =" + command_uuid);
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] Compare Characteristic UUID for status =" + status_uuid);
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] Compare Characteristic UUID for OTAControl =" + OTAControl_uuid);
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] Compare Characteristic UUID for OTAData =" + OTAData_uuid);

				// Get Services list
				List<BluetoothGattService> gattService = gatt.getServices();
				for (BluetoothGattService service : gattService) {
					UUID uuid = service.getUuid();
					BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] Service UUID=" + uuid.toString());

					List<BluetoothGattCharacteristic> charastics;
					if (uuid.equals(service_uuid)) {
						charastics = service.getCharacteristics();
						for (BluetoothGattCharacteristic charastic : charastics) {
							uuid = charastic.getUuid();
							BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] Charastic UUID=" + uuid.toString());
							if (uuid.equals(response_uuid)) {
								BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] response_characteristic");
								response_characteristic = charastic;
							} else if (uuid.equals(command_uuid)) {
								BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] command_characteristic");
								command_characteristic = charastic;
							} else if (uuid.equals(status_uuid)) {
								BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] status_characteristic");
								status_characteristic = charastic;
								if (false == mGatt.setCharacteristicNotification(status_characteristic, true)) {
									BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] Bluetooth GATT setCharacteristicNotification(status, true) is not success.");
								} else {
									BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] Bluetooth GATT setCharacteristicNotification(status, true) is success.");
									BluetoothGattDescriptor descriptor = charastic.getDescriptors().get(0);
									descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
									gatt.writeDescriptor(descriptor);
								}

							} else {
								/*Do Nothing*/
							}
						}
					} else if (uuid.equals(service_OTA_uuid)) {
						charastics = service.getCharacteristics();
						for (BluetoothGattCharacteristic charastic : charastics) {
							uuid = charastic.getUuid();
							BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] Charastic UUID=" + uuid.toString());
							if (uuid.equals(OTAControl_uuid)) {
								BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] OTAControl_characteristic");
								OTAControl_characteristic = charastic;
							} else if (uuid.equals(OTAData_uuid)) {
								BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] OTAData_characteristic");
								OTAData_characteristic = charastic;
							} else {
								/*Do Nothing*/
							}
						}
					} else {
						/*Do Nothing*/
					}
				}

				isGattSuccess = true;
			} else {
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] GATT not Success.");
			}

			gatt_poling_counter = 0;

			BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onServicesDiscovered] End");
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			byte[] value = characteristic.getValue();
			if (null == value) {
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicChanged] Start" + ":gatt=" + gatt.toString() + " gatt.device=" + gatt.getDevice().getName() + " characteristic=" + characteristic.getUuid().toString() + " Value=null");
			} else {
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicChanged] Start" + ":gatt=" + gatt.toString() + " gatt.device=" + gatt.getDevice().getName() + " characteristic=" + characteristic.getUuid().toString() + " Value=" + Utility.toString(value, value.length));
			}

			if (status_uuid.equals(characteristic.getUuid())) {
				/*Check Notification and get box status*/
				byte[] st;
				st = CheckStatusResponse(value);
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicChanged] status"
						+ " isTooMuchEvent=" + BleCommandProtocol.GetStatusBit(st, BleBoxStatus.isTooMuchEvent)
						+ " isDoorOpened=" + BleCommandProtocol.GetStatusBit(st, BleBoxStatus.isDoorOpened)
						+ " isLockLock=" + BleCommandProtocol.GetStatusBit(st, BleBoxStatus.isLockLock)
						+ " isKeyboardEnable=" + BleCommandProtocol.GetStatusBit(st, BleBoxStatus.isKeyboardEnable)
						+ " isDoorChanged=" + BleCommandProtocol.GetStatusBit(st, BleBoxStatus.isDoorChanged)
						+ " isLockChanged=" + BleCommandProtocol.GetStatusBit(st, BleBoxStatus.isLockChanged)
						+ " Disconnected=" + BleCommandProtocol.GetStatusBit(st, BleBoxStatus.Disconnected)
				);

				if (doorChangeCallback != null) {
					/*isDoorChanged is true*/
					if (BleCommandProtocol.GetStatusBit(st, BleBoxStatus.isDoorChanged) == true) {
						/*Callback doorOpened Infomation*/
						for (DoorChangeCallback cb : doorChangeCallback) {
							cb.invoke(BleCommandProtocol.GetStatusBit(st, BleBoxStatus.isDoorOpened));
						}
					} else {
						/*Do Nothing*/
					}

				} else {
					/*Do Nothing*/
				}

				if (lockChangeCallback != null) {
					/*isLockChanged is true*/
					if (BleCommandProtocol.GetStatusBit(st, BleBoxStatus.isLockChanged) == true) {
						/*Callback isLockLocked Infomation*/
						for (LockChangeCallback cb : lockChangeCallback) {
							cb.invoke(BleCommandProtocol.GetStatusBit(st, BleBoxStatus.isLockLock));
						}
					} else {
						/*Do Nothing*/
					}
				} else {
					/*Do Nothing*/
				}

				if (disconnectCallback != null) {
					/*Box will Disconnect*/
					if (BleCommandProtocol.GetStatusBit(st, BleBoxStatus.Disconnected) == true) {
						BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicChanged] Disconnected is true");
						/*Reset Information*/
						/*Clear device Information*/
						BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicChanged] currentConnectedDevice=null");
						currentConnectedDevice = null;
						if ((null != mGatt) && (null != status_characteristic)) {
							/*Stop detect Notification*/
							BluetoothGattDescriptor descriptor;
							BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicChanged] Bluetooth GATT setCharacteristicNotification(status, false).");
							descriptor = status_characteristic.getDescriptors().get(0);
							descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
							mGatt.writeDescriptor(descriptor);
							mGatt.setCharacteristicNotification(status_characteristic, false);
						}

						/*Callback Disconnect Information*/
						for (DisconnectCallback cb : disconnectCallback) {
							cb.invoke(BleCommandProtocol.GetStatusBit(st, BleBoxStatus.Disconnected));
						}
					}
				} else {
					/*Do Nothing*/
				}
			}

			BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicChanged] End");
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicRead] Start" + " gatt.device=" + gatt.getDevice().getName() + " state=" + status);

			if (status == BluetoothGatt.GATT_SUCCESS) {
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicRead] Success. characteristic characteristic UUID=" + characteristic.getUuid());
				isReadSuccess = true;
			} else {
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicRead] Not Success. characteristic UUID=" + characteristic.getUuid());
			}

			read_poling_counter = 0;

			BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicRead] End");
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicWrite] Start" + ":gatt=" + gatt.toString() + " gatt.device=" + gatt.getDevice().getName() + " state=" + status);

			if (status == BluetoothGatt.GATT_SUCCESS) {
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicWrite] Success. characteristic UUID=" + characteristic.getUuid());
				isWriteSuccess = true;
			} else {
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicWrite] Not Success. characteristic UUID=" + characteristic.getUuid());
			}

			write_poling_counter = 0;

			BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onCharacteristicWrite] End");
		}

		@Override
		public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
			BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onMtuChanged] Start" + ":gatt=" + gatt.toString() + " gatt.device=" + gatt.getDevice().getName() + " state=" + status);

			if (status == BluetoothGatt.GATT_SUCCESS) {
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onMtuChanged] Success.");
				isGattSuccess = true;
			} else {
				BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onMtuChanged] Not Success.");
			}

			gatt_poling_counter = 0;

			BleLog.d(LOG_TAG, LOG_CLASS + "[mGattCallback][onMtuChanged] End");
		}
	};

	/*Constructor*/
	public BleAccess(Context context) throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[BleAccess] Start");

		/* Get Bluetooth Adapter */
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		if (null == mAdapter)
		{
			/* can't get Bluetooth Adapter */
			BleLog.e(LOG_TAG, LOG_CLASS + "[BleAccess][E] Bluetooth Adapter is null.");
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			return;
		}
		if (!mAdapter.isEnabled())
		{
			/* Bluetooth is not enabled */
			BleLog.e(LOG_TAG, LOG_CLASS + "[BleAccess][E] Bluetooth Adapter is not enabled.");
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			return;
		}

		mContext = context;

		BleLog.d(LOG_TAG, LOG_CLASS + "[BleAccess] End");
	}

	/**
	 Scan BleDevice until timeout.

	 @return The scan.
	 @param timeout Timeout# Select Timeout interval(milli sec)
	*/
	public final void Scan(int timeout) throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[Scan] Start" + ":timeout=" + timeout);

		if (null == mAdapter)
		{
			/* Bluetooth mAdapter is null */
			BleLog.e(LOG_TAG, LOG_CLASS + "[Scan][E] Bluetooth mAdapter is null.");
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			return;
		}
		if (!mAdapter.isEnabled())
		{
			/* Bluetooth Adapter is not enabled */
			BleLog.e(LOG_TAG, LOG_CLASS + "[Scan][E] Bluetooth Adapter is not enabled.");
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			return;
		}

		/*Start scanning*/
		isScanStoped = false;
		synchronized (mAdapter)
		{
			final boolean didScan;
			if(Build.VERSION.SDK_INT >= 21){
				ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
				scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
				ScanSettings scanSettings = scanSettingsBuilder.build();
				ScanFilter scanFilter = new ScanFilter.Builder().build();
				ArrayList scanFilterList = new ArrayList();
				scanFilterList.add(scanFilter);
				mAdapter.getBluetoothLeScanner().startScan(scanFilterList, scanSettings, mScanCallback);
				didScan = true;
			}
			else {
				didScan = mAdapter.startLeScan(mLeScanCallback);
			}
			if (didScan)
			{
				try {
					 //mAdapter.wait(timeout);
					if (0 < timeout)
					{
						scan_poling_counter = timeout;
					}
					else
					{
						scan_poling_counter = 1;
					}
					while (0 < scan_poling_counter)
					{
						mAdapter.wait(scan_poling_interval);
						if (0 < timeout)
						{
							scan_poling_counter -= scan_poling_interval;
						}
					}
				} catch (InterruptedException ignored)
				{
					BleLog.e(LOG_TAG, LOG_CLASS + "[Scan][E] Bluetooth Adapter Scan InterruptedException." + ignored.toString());
					BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE Scan InterruptedException)");
				}
				finally {
					/*Stop scanning*/
					if (!isScanStoped)
					{
						isScanStoped = true;
						BleLog.d(LOG_TAG, LOG_CLASS + "[Scan] Bluetooth Adapter stop Scan.");
						if(Build.VERSION.SDK_INT >= 21){
							mAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
						}
						else {
							mAdapter.stopLeScan(mLeScanCallback);
						}

						if (scanTimeoutCallback != null)
						{
							scanTimeoutCallback.invoke();
						}
					}
				}

			}
			else
			{
				BleLog.e(LOG_TAG, LOG_CLASS + "[Scan][E] Bluetooth Adapter startLeScan() error.");
				BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE Scan start error)");
			}
		}

		isScanStoped = true;

		BleLog.d(LOG_TAG, LOG_CLASS + "[Scan] End");
	}

	/**
	 Stops the scan.

	 @return The scan.
	*/
	public final void StopScan() throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[StopScan] Start");

		if (null == mAdapter)
		{
			/* Bluetooth Adapter is null */
			BleLog.e(LOG_TAG, LOG_CLASS + "][StopScan] Bluetooth Adapter is null.");
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			return;
		}
		if (!mAdapter.isEnabled())
		{
			/* Bluetooth Adapter is not enabled */
			BleLog.e(LOG_TAG, LOG_CLASS + "[StopScan] Bluetooth Adapter is not enabled.");
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			return;
		}

		if (!isScanStoped)
		{
			/*Stop scanning*/
			isScanStoped = true;
			BleLog.d(LOG_TAG, LOG_CLASS + "[StopScan] Bluetooth Adapter stop Scan.");
			if(Build.VERSION.SDK_INT >= 21){
				mAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
			}
			else {
				mAdapter.stopLeScan(mLeScanCallback);
			}
		}

		scan_poling_counter = 0;

		BleLog.d(LOG_TAG, LOG_CLASS + "[StopScan] End");
	}

	/**
	 Connect the specified bleDevice.

	 @return The connect.
	 @param bleDevice Ble device. desired bleDevice's infomation
	*/
	public final void Connect(BluetoothDevice device) throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] Start" + ":device=" + device.getName());

		int ret = 0;
		String id = "";

		if (null == mAdapter)
		{
			/* Bluetooth Adapter is null */
			BleLog.e(LOG_TAG, LOG_CLASS + "[Connect] Bluetooth Adapter is null.");
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			return;
		}
		if (!mAdapter.isEnabled())
		{
			/* Bluetooth Adapter is not enabled */
			BleLog.e(LOG_TAG, LOG_CLASS + "[Connect] Bluetooth Adapter is not enabled.");
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			return;
		}
		if (null == device)
		{
			/* Bluetooth Device is null */
			BleLog.e(LOG_TAG, LOG_CLASS + "[Connect] Bluetooth Device is null.");
			BoxError.ThrowError(BoxErrorCode.SDK_003, "Device is null");
			return;
		}

		if (!isScanStoped)
		{
			isScanStoped = true;
			/*Stop scanning*/
			BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] Bluetooth Adapter stop eScan.");
			if(Build.VERSION.SDK_INT >= 21){
				mAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
			}
			else {
				mAdapter.stopLeScan(mLeScanCallback);
			}
		}

		if (null != mGatt)
		{
			/*Disconnecet GATT*/
			if ( null != status_characteristic )
			{
				/*Stop detect Notification*/
				BluetoothGattDescriptor descriptor;
				BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] Bluetooth GATT setCharacteristicNotification(status, false).");
				descriptor = status_characteristic.getDescriptors().get(0);
				descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
				mGatt.writeDescriptor(descriptor);
				mGatt.setCharacteristicNotification(status_characteristic, false);
			}
			/*Disconnecet to Device*/
			BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] Bluetooth GATT disconnect().");
			synchronized (mGatt)
			{
				try {
					gatt_poling_counter = gattTimeout;
					while (0 < gatt_poling_counter)
					{
						if (null == mGatt)
						{
							BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] Bluetooth GATT is disconnected.");
							gatt_poling_counter = 0;
						}
						else
						{
							mGatt.disconnect();
							if (null == mGatt)
							{
								BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] Bluetooth GATT is disconnected.");
								gatt_poling_counter = 0;
							}
							else
							{
								mGatt.wait(gatt_poling_interval);
								gatt_poling_counter -= gatt_poling_interval;
							}
						}
					}
				}
				catch (InterruptedException ignored)
				{
					BleLog.e(LOG_TAG, LOG_CLASS + "[Connect][E] Bluetooth GATT Connect InterruptedException." + ignored.toString());
					BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE GATT Connect InterruptedException)");
				}
			}
			mGatt = null;
			/*Clear Device Information*/
			BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] currentConnectedDevice=null");
			currentConnectedDevice = null;
			/* Disconnect Result to BluetoothGattCallback */

			if (mAdapter != null) {
				synchronized (mAdapter) {
					try {
						/* Sleep 5sec */
						mAdapter.wait(1000);
					} catch (InterruptedException ignored) {
						BleLog.e(LOG_TAG, LOG_CLASS + "[Connect][E] Bluetooth Adapter Write InterruptedException." + ignored.toString());
						BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE Write InterruptedException)");
					}
				}
			}
		}

		currentConnectedDevice = null;

		/* Clear characteristic */
		response_characteristic = null;
		command_characteristic = null;
		status_characteristic = null;
		OTAControl_characteristic = null;
		OTAData_characteristic = null;

		/*Connecet to Device*/
		isGattSuccess = false;
		BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] Bluetooth Device connectGatt().");

		gatt_poling_counter = gattTimeout;
		mGatt = device.connectGatt(mContext, false, mGattCallback);
		if (null == mGatt)
		{
			/* Bluetooth GATT is null */
			BleLog.e(LOG_TAG, LOG_CLASS + "[Connect] Bluetooth GATT is null.");
			BoxError.ThrowError(BoxErrorCode.SDK_003, "Device is null");
			gatt_poling_counter = 0;
			return;
		}
		synchronized (mGatt)
		{
			try
			{
				while (0 < gatt_poling_counter)
				{
					if (null == mGatt)
					{
						BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] Bluetooth GATT is disconnected.");
						gatt_poling_counter = 0;
					}
					else
					{
						mGatt.wait(gatt_poling_interval);
						gatt_poling_counter -= gatt_poling_interval;
					}
				}
			}
			catch (InterruptedException ignored)
			{
				BleLog.e(LOG_TAG, LOG_CLASS + "[Connect][E] Bluetooth GATT Connect InterruptedException." + ignored.toString());
				BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE GATT Connect InterruptedException)");
			}
		}

		if (false == isGattSuccess)
		{
			BleLog.e(LOG_TAG, LOG_CLASS + "[Connect][E] Bluetooth GATT Connect is not success.");
			currentConnectedDevice = null;
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] Bluetooth GATT Connect is success.");
			currentConnectedDevice = device;
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[Connect] End");
	}

	/**
	 Disconnect the specified bleDevice.

	 @return Disconnect was succeeded(true or false)
	 @param bleDevice current connected bledevice's infomation
	*/
	public final void Disconnect(BluetoothDevice device) throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[Disconnect] Start" + ":device=" + device.getName());

		if (null == mAdapter)
		{
			/* Bluetooth Adapter is null */
			BleLog.e(LOG_TAG, LOG_CLASS + "[Disconnect] Bluetooth Adapter is null.");
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			return;
		}
		if (!mAdapter.isEnabled())
		{
			/* Bluetooth Adapter is not enabled */
			BleLog.e(LOG_TAG, LOG_CLASS + "[Disconnect] Bluetooth Adapter is not enabled.");
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			return;
		}
		if (null == device)
		{
			/* Bluetooth Device is null */
			BleLog.e(LOG_TAG, LOG_CLASS + "[Disconnect] Bluetooth Device is null.");
			BoxError.ThrowError(BoxErrorCode.SDK_003, "Device is null");
			return;
		}

		if (null != mGatt)
		{
			if ( null != status_characteristic )
			{
				/*Stop detect Notification*/
				BluetoothGattDescriptor descriptor;
				BleLog.d(LOG_TAG, LOG_CLASS + "[Disconnect] Bluetooth GATT setCharacteristicNotification(status, false).");
				descriptor = status_characteristic.getDescriptors().get(0);
				descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
				mGatt.writeDescriptor(descriptor);
				mGatt.setCharacteristicNotification(status_characteristic, false);
			}
			/*Disconnecet to Device*/
			BleLog.d(LOG_TAG, LOG_CLASS + "[Disconnect] Bluetooth GATT disconnect().");
			synchronized (mGatt)
			{
				try
				{
					gatt_poling_counter = gattTimeout;
					while (0 < gatt_poling_counter)
					{
						if (null == mGatt)
						{
							BleLog.d(LOG_TAG, LOG_CLASS + "[Disconnect] Bluetooth GATT is disconnected.");
							gatt_poling_counter = 0;
						}
						else
						{
							mGatt.disconnect();
							if (null == mGatt)
							{
								BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Bluetooth GATT is disconnected.");
								gatt_poling_counter = 0;
							}
							else
							{
								mGatt.wait(gatt_poling_interval);
								gatt_poling_counter -= gatt_poling_interval;
							}
						}
					}
				}
				catch (InterruptedException ignored)
				{
					BleLog.e(LOG_TAG, LOG_CLASS + "[Disconnect][E] Bluetooth GATT Connect InterruptedException." + ignored.toString());
					BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE GATT Connect InterruptedException)");
				}
			}
			mGatt = null;
			/*Clear Device Information*/
			BleLog.d(LOG_TAG, LOG_CLASS + "[Disconnect] currentConnectedDevice=null");
			currentConnectedDevice = null;

			/* Disconnect Result to BluetoothGattCallback */
		}
		else
		{
			/* Bluetooth Device GATT is null */
			BleLog.e(LOG_TAG, LOG_CLASS + "[Disconnect] Bluetooth GATT is null.");
			BoxError.ThrowError(BoxErrorCode.SDK_003, "Device GATT is null");
			return;
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[Disconnect] End");
	}

	/**
	 Updates box firmware.

	 @return update success returns 0 ,faied returns -1
	 @param firmware Firmware data.
	*/
	public final Integer UpdateFirmware(byte[] firmware) throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Start" + ":firmware.length=" + firmware.length);

		boolean isComplete;
		isComplete=false;
		int ret = 0;
		int dataSize = firmware.length;
		/*for Control bootloader*/
		byte[] initByte = {0x00};
		byte[] endByte = {0x03};
		byte[] disconnectByte = {0x04};

		if (null == mAdapter)
		{
			/* Bluetooth Adapter is null */
			BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Bluetooth Adapter is null.");
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			return -1;
		}
		if (!mAdapter.isEnabled())
		{
			/* Bluetooth Adapter is not enabled */
			BleLog.e(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Bluetooth Adapter is not enabled.");
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			return -1;
		}
		if (null == currentConnectedDevice)
		{
			/* Bluetooth Device is null */
			BleLog.e(LOG_TAG, LOG_CLASS + "[BleAccess][UpdateFirmware] Bluetooth Device is null.");
			BoxError.ThrowError(BoxErrorCode.SDK_003, "Device is null");
			return -1;
		}


		if (OTAControl_characteristic != null)
		{
			if (null == OTAData_characteristic)
			{
				/*Initialize bootloader*/
				OTAControl_characteristic.setValue(initByte);
				synchronized (OTAControl_characteristic)
				{
					boolean didWrite = false;
					try
					{
						//OTAControl_characteristic.wait(detectionTimeout);
						write_poling_counter = detectionTimeout;
						while (0 < write_poling_counter)
						{
							if (null == mGatt)
							{
								BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Bluetooth GATT is disconnected.");
								write_poling_counter = 0;
							}
							else
							{
								if (false == didWrite)
								{
									didWrite = mGatt.writeCharacteristic(OTAControl_characteristic);
								}
								OTAControl_characteristic.wait(write_poling_interval);
								write_poling_counter -= write_poling_interval;
							}
						}
					}
					catch (InterruptedException ignored)
					{
						BleLog.e(LOG_TAG, LOG_CLASS + "[UpdateFirmware][E] Bluetooth Adapter Write InterruptedException." + ignored.toString());
						BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE Write InterruptedException)");
					}
					if (!isWriteSuccess)
					{
						return -1;
					}

					try {
						/* Sleep 5sec */
						OTAControl_characteristic.wait(5000);
					}
					catch (InterruptedException ignored)
						{
							BleLog.e(LOG_TAG, LOG_CLASS + "[UpdateFirmware][E] Bluetooth Adapter Write InterruptedException." + ignored.toString());
							BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE Write InterruptedException)");
						}
				}

				/*Disconnect to reboot*/
				for (int i = 0; i < 3; i++)
				{
					Connect(currentConnectedDevice);
					if (null != OTAData_characteristic)
					{
						break;
					}
					synchronized (mAdapter)
					{
						try {
							/* Sleep 0.5sec */
							mAdapter.wait(500);
						}
						catch (InterruptedException ignored)
							{
								BleLog.e(LOG_TAG, LOG_CLASS + "[UpdateFirmware][E] Bluetooth Adapter Write InterruptedException." + ignored.toString());
								BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE Write InterruptedException)");
							}
						}
				}

				if (null == OTAData_characteristic)
				{
					BleLog.e(LOG_TAG, LOG_CLASS + "[UpdateFirmware] OTAData_characteristic is null.");
					BoxError.ThrowError(BoxErrorCode.SDK_000, " (OTAData_characteristic is not found)");
				}
			}
			if (null != mGatt) {
				synchronized (mGatt) {
					try {
						isGattSuccess = false;
						gatt_poling_counter = gattTimeout;
						mGatt.requestMtu(244);
						while (0 < gatt_poling_counter) {
							if (null == mGatt) {
								BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Bluetooth GATT is disconnected.");
								gatt_poling_counter = 0;
							} else {
								mGatt.wait(gatt_poling_interval);
								gatt_poling_counter -= gatt_poling_interval;
							}
						}
					} catch (InterruptedException ignored) {
						BleLog.e(LOG_TAG, LOG_CLASS + "[UpdateFirmware][E] Bluetooth GATT requestMtu InterruptedException." + ignored.toString());
						BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE GATT Connect InterruptedException)");
					}
				}
			}
			else
			{
				/* Bluetooth Device GATT is null */
				BleLog.e(LOG_TAG, LOG_CLASS + "[UpdateFirmware][E] Bluetooth GATT is null.");
				BoxError.ThrowError(BoxErrorCode.SDK_003, "Device GATT is null");
			}

			if (false == isGattSuccess)
			{
				BleLog.e(LOG_TAG, LOG_CLASS + "[UpdateFirmware][E] Bluetooth GATT requestMtu is not success.");
			}
			else
			{
				BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Bluetooth GATT requestMtu is success.");
			}

			/*Initialize bootloader*/
			OTAControl_characteristic.setValue(initByte);
			synchronized (OTAControl_characteristic)
			{
				boolean didWrite = false;
				try
				{
					//OTAControl_characteristic.wait(detectionTimeout);
					write_poling_counter = detectionTimeout;
					while (0 < write_poling_counter)
					{
						if (null == mGatt)
						{
							BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Bluetooth GATT is disconnected.");
							write_poling_counter = 0;
						}
						else
						{
							if (false == didWrite)
							{
								didWrite = mGatt.writeCharacteristic(OTAControl_characteristic);
							}
							OTAControl_characteristic.wait(write_poling_interval);
							write_poling_counter -= write_poling_interval;
						}
					}
				}
				catch (InterruptedException ignored)
				{
					BleLog.e(LOG_TAG, LOG_CLASS + "[UpdateFirmware][E] Bluetooth Adapter Write InterruptedException." + ignored.toString());
					BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE Write InterruptedException)");
				}
				if (!isWriteSuccess)
				{
					ret = -1;
				}
			}

			try
			{
				/*Write Firmware Data*/
				ret = WriteData(firmware, dataSize);
			}
			catch (RuntimeException e3)
			{
				ret = -1;
			}

			/*End Upadate*/
			isWriteSuccess = false;
			OTAControl_characteristic.setValue(endByte);
			synchronized (OTAControl_characteristic)
			{
				boolean didWrite = false;
				try
				{
					//OTAControl_characteristic.wait(detectionTimeout);
					write_poling_counter = detectionTimeout;
					while (0 < write_poling_counter)
					{
						if (null == mGatt)
						{
							BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Bluetooth GATT is disconnected.");
							write_poling_counter = 0;
						}
						else
						{
							if (false == didWrite)
							{
								didWrite = mGatt.writeCharacteristic(OTAControl_characteristic);
							}
							OTAControl_characteristic.wait(write_poling_interval);
							write_poling_counter -= write_poling_interval;
						}
					}
				}
				catch (InterruptedException ignored)
				{
					BleLog.e(LOG_TAG, LOG_CLASS + "[UpdateFirmware][E] Bluetooth Adapter Write InterruptedException." + ignored.toString());
					BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE Write InterruptedException)");
				}
				if (isWriteSuccess)
				{
					isComplete = true;
				}
			}

			synchronized (mGatt)
			{
				try
				{
					gatt_poling_counter = gattTimeout;
					while (0 < gatt_poling_counter)
					{
						if (null == mGatt)
						{
							BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Bluetooth GATT is disconnected.");
							gatt_poling_counter = 0;
						}
						else
						{
							mGatt.disconnect();
							if (null == mGatt)
							{
								BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Bluetooth GATT is disconnected.");
								gatt_poling_counter = 0;
							}
							else
							{
								mGatt.wait(gatt_poling_interval);
								gatt_poling_counter -= gatt_poling_interval;
							}
						}
					}
				}
				catch (InterruptedException ignored)
				{
					BleLog.e(LOG_TAG, LOG_CLASS + "[UpdateFirmware][E] Bluetooth GATT Connect InterruptedException." + ignored.toString());
					BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE GATT Connect InterruptedException)");
				}
			}
			mGatt = null;
			/*Clear Device Information*/
			BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] currentConnectedDevice=null");
			currentConnectedDevice = null;
			/* Disconnect Result to BluetoothGattCallback */

			/*Check Update is comleted*/
			if (isComplete == false)
			{
				BleLog.e(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Update is not Completed.");
				BoxError.ThrowError(BoxErrorCode.SDK_000, " (Update Failed)");
			}
			else
			{
				/*Success*/
				BleLog.e(LOG_TAG, LOG_CLASS + "[UpdateFirmware] Update is Completed.");
			}
		}
		else
		{
			BleLog.e(LOG_TAG, LOG_CLASS + "[UpdateFirmware] OTAControl_characteristic is null.");
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (OTAControl_characteristic is not found)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[UpdateFirmware] End");

		return ret;
	}


	/*Write Firmware data to bootloader*/
	private Integer WriteData(byte[] data, int size) throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[WriteData] Start");

		int sendByte = 120; //set value less than MTU size
		int ret = 0;
		boolean isComplete = true;
		int i = 0;
		byte[] sendBuff = new byte[sendByte];
		int sendNum;

		/*set send number of times*/
		if (size % sendByte == 0)
		{
			sendNum = size / sendByte;
		}
		else
		{
			sendNum = (size / sendByte) + 1;
		}

		/*send data*/
		for (i = 0; i < sendNum; i++)
		{
			if (size - i * sendByte > sendByte)
			{
				Utility.BlockCopy(data, i * sendByte, sendBuff, 0, sendByte);
				try
				{
					isWriteSuccess = false;
					OTAData_characteristic.setValue(sendBuff);
					synchronized (OTAData_characteristic)
					{
						boolean didWrite = false;
						try
						{
							//OTAData_characteristic.wait(detectionTimeout);
							write_poling_counter = detectionTimeout;
							while (0 < write_poling_counter)
							{
								if (null == mGatt)
								{
									BleLog.d(LOG_TAG, LOG_CLASS + "[WriteData] Bluetooth GATT is disconnected.");
									write_poling_counter = 0;
								}
								else
								{
									if (false == didWrite)
									{
										didWrite = mGatt.writeCharacteristic(OTAData_characteristic);
									}
									OTAData_characteristic.wait(write_poling_interval);
									write_poling_counter -= write_poling_interval;
								}
							}
						}
						catch (InterruptedException ignored)
						{
							BleLog.e(LOG_TAG, LOG_CLASS + "[WriteData][E] Bluetooth Adapter Write InterruptedException." + ignored.toString());
							BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE Write InterruptedException)");
						}
						if (!isWriteSuccess)
						{
							ret = -1;
						}
					}
					BleLog.d(LOG_TAG, LOG_CLASS + "{" + i + "}/{" + (sendNum - 1) + "}");
				}
				catch (RuntimeException e)
				{
					ret = -1;
				}
			}
			/*case of last data byte*/
			else
			{
				byte[] lastsendBuff = new byte[(size - i * sendByte)];
				Utility.BlockCopy(data, i * sendByte, lastsendBuff, 0, lastsendBuff.length);
				try
				{
					OTAData_characteristic.setValue(lastsendBuff);
					synchronized (OTAData_characteristic)
					{
						boolean didWrite = false;
						try
						{
							//OTAData_characteristic.wait(detectionTimeout);
							write_poling_counter = detectionTimeout;
							while (0 < write_poling_counter)
							{
								if (null == mGatt)
								{
									BleLog.d(LOG_TAG, LOG_CLASS + "[WriteData] Bluetooth GATT is disconnected.");
									write_poling_counter = 0;
								}
								else
								{
									if (false == didWrite)
									{
										didWrite = mGatt.writeCharacteristic(OTAData_characteristic);
									}
									OTAData_characteristic.wait(write_poling_interval);
									write_poling_counter -= write_poling_interval;
								}
							}
						}
						catch (InterruptedException ignored)
						{
							BleLog.e(LOG_TAG, LOG_CLASS + "[WriteData][E] Bluetooth Adapter Write InterruptedException." + ignored.toString());
							BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE Write InterruptedException)");
						}
					}
					BleLog.d(LOG_TAG, LOG_CLASS + i + "/" + (sendNum - 1));
				}
				catch (RuntimeException e2)
				{
					ret = -1;
				}
			}

		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[WriteData] End");

		return ret;
	}

	private byte[] CheckStatusResponse(byte[] resp)
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[CheckStatusResponse] Start");

		int i = 0;
		int ItemId = 0;
		int ItemLen = 0;

		byte[] ret = new byte[2];

		/*Check Response Type*/
		if (resp[0] == (byte)0xFF)
		{
			/*Status Notificaiuton Response*/
			ItemId = resp[4];
			ItemLen = resp[5];

			/*Check ItemId*/
			if (ItemId == (byte)0x23)
			{
				/*ItemId is Status*/
				for (i = 0; i < ItemLen; i++)
				{
					ret[i] = resp[i + 6];
				}
			}
			else
			{
				/*ItemId is not Status*/
				ret = null;
			}
		}
		else
		{
			/*Response is incorrect*/
			ret = null;
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[CheckStatusResponse] End");

		return ret;
	}

	/*Check Response (UpdateFirmware)*/
	private void CheckResponceError(BleCommandProtocol cmd) throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[CheckResponceError] Start");

		BleCommandItem item;

		item = cmd.GetNextItem();

		if ((item != null) && (item.get_id() == BleItemId.ErrorCode))
		{
			if (item.get_data()[0] == (byte)0x00)
			{
				/*Success*/
			}

			else
			{
				switch (item.get_data()[0])
				{
					case (byte)0xFF:
						BoxError.ThrowError(BoxErrorCode.SDK_000, null);
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
			/*ItemId is not ErrorCode*/
			if (null == item)
			{
				BleLog.d(LOG_TAG, LOG_CLASS + "[CheckResponceError][E] item is null.");
			}
			else
			{
				BleLog.d(LOG_TAG, LOG_CLASS + "[CheckResponceError][E] item id =" + item.get_id());
			}
			BoxError.ThrowError(BoxErrorCode.SDK_000, " (First ItemId is not ErrorCode)");
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[CheckResponceError] End");
	}
	/**
	 Send Command to Device. Then, detect notification(Response Characteristic) from box and store Response.

	 @return  0: success, -1:faied
	 @param cmd command you want to send
	*/
	public final Integer SendCommand(BleCommandProtocol cmd) throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[SendCommand] Start:cmd:ItemNum=" + cmd.GetItemNum() + " CurrCommand=" + cmd.get_CurrCommand());

		int ret = 0;

		if (null == mAdapter)
		{
			/* Bluetooth Adapter is null */
			BleLog.e(LOG_TAG, LOG_CLASS + "[SendCommand] Bluetooth Adapter is null.");
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			return -1;
		}
		if (!mAdapter.isEnabled())
		{
			/* Bluetooth Adapter is not enabled */
			BleLog.e(LOG_TAG, LOG_CLASS + "[SendCommand] Bluetooth Adapter is not enabled.");
			BoxError.ThrowError(BoxErrorCode.SDK_001, null);
			return -1;
		}
		if (null == currentConnectedDevice)
		{
			/* Bluetooth Device is null */
			BleLog.e(LOG_TAG, LOG_CLASS + "[SendCommand] Bluetooth Device is null.");
			BoxError.ThrowError(BoxErrorCode.SDK_003, "Device is null");
			return -1;
		}

		int packetLen = 0;
		byte[] setResp = new byte[1536];
		boolean hasResp = false;
		boolean isError = false;

		ble_cmd = cmd;

		/*MakePacket and Get Packet Length*/
		packetLen = ble_cmd.CreatePacket();

		byte[] sendBuffer = new byte[packetLen];

		if (packetLen > 0)
		{
			Utility.BlockCopy(ble_cmd.get_SendBuffer(), 0, sendBuffer, 0, packetLen);
			/* for debug log start */
			String logString = "";
			for (byte c : sendBuffer)
			{
				logString += String.format("%02X", (Byte)c);
			}
			BleLog.d(LOG_TAG, LOG_CLASS + "[SendCommand] sendBuffer.length=" + sendBuffer.length + " sendBuffer=" + logString);
			/* for debug log end */

			/*Send command to current connected BLE device*/
			BleLog.d(LOG_TAG, LOG_CLASS + "[SendCommand]Send characteristic thread:" + Thread.currentThread().getId());
			try
			{
				isWriteSuccess = false;
				command_characteristic.setValue(sendBuffer);
				synchronized (command_characteristic)
				{
					try
					{
						//command_characteristic.wait(detectionTimeout);
						write_poling_counter = detectionTimeout;
						boolean didWrite = false;
						while (0 < write_poling_counter)
						{
							if (null == mGatt)
							{
								BleLog.d(LOG_TAG, LOG_CLASS + "[SendCommand] Bluetooth GATT is disconnected.");
								write_poling_counter = 0;
							}
							else
							{
								if (false == didWrite)
								{
									didWrite = mGatt.writeCharacteristic(command_characteristic);
								}
								command_characteristic.wait(write_poling_interval);
								write_poling_counter -= write_poling_interval;
							}
						}
						/* for async timeout end */
					}
					catch (InterruptedException ignored)
					{
						BleLog.e(LOG_TAG, LOG_CLASS + "[SendCommand][E] Bluetooth Adapter Write InterruptedException." + ignored.toString());
						BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE Write InterruptedException)");
					}
					if (isWriteSuccess)
					{
						/*Notification detected*/
						BleLog.d(LOG_TAG, LOG_CLASS + "[SendCommand]signal activated thread:" + Thread.currentThread().getId());

						/*Get Response*/
						setResp = ReadResponse(response_characteristic);
						ble_cmd.SetResponse(setResp, setResp.length);

						hasResp = ble_cmd.HasResponse();
						BleLog.d(LOG_TAG, LOG_CLASS + "[SendCommand]Check Responce Data thread:" + Thread.currentThread().getId() + " isRespError=" + isRespError);

						if (hasResp == true)
						{
							/*Success*/
							/*Do Nothing*/
						}
						else
						{
							/*Failed*/
							BleLog.e(LOG_TAG, LOG_CLASS + "[SendCommand][E] has no response.");
							ret = -1;
						}
					}
					else
					{
						BleLog.e(LOG_TAG, LOG_CLASS + "[SendCommand][E] write error.");
						ret = -1;
					}
				}
			}
			catch (RuntimeException e3)
			{
				BoxError.ThrowError(BoxErrorCode.SDK_000, "(BLE Write Error)");
			}
		}
		else
		{
			BleLog.e(LOG_TAG, LOG_CLASS + "[SendCommand][E] packetLen(" + packetLen + ") <= 0");
			ret = -1;
		}

		BleLog.d(LOG_TAG, LOG_CLASS + "[SendCommand] End" + ":return " + ret);

		return ret;
	}

	/*Read Characteristic (Response)*/
	private byte[] ReadResponse(BluetoothGattCharacteristic characteristic) throws BoxException
	{
		BleLog.d(LOG_TAG, LOG_CLASS + "[ReadResponse] Start");

		int i = 0;
		/*ErrorCode + CipheredEvent*/
		int firstReadMax = 274;
		/*Ciphered Event*/
		int readMax = 258;
		/*Max Data size of Response*/
		//int dataMax = 1545;
		int dataMax = 4096;

		byte[] readBuf = new byte[firstReadMax];
		byte[] readData = new byte[dataMax];
		int eventNum = 0;

		isRespError = true;

		/*Case of (GetEvents or GetPassword)*/
		if ((ble_cmd.get_CurrCommand() == BleCommand.GetEvents) || (ble_cmd.get_CurrCommand() == BleCommand.GetPassword))
		{
			/*Read Event Data*/
			for (i = 0, eventNum = 3; i < eventNum - 2; i++)
			{
				isReadSuccess = false;
				synchronized (characteristic)
				{
					boolean didRead = false;
					if (null != mGatt)
					{
						didRead = mGatt.readCharacteristic(characteristic);
					}
					if (didRead)
					{
						try
						{
							read_poling_counter = readTimeout;
							while (0 < read_poling_counter)
							{
								if (null == mGatt)
								{
									BleLog.d(LOG_TAG, LOG_CLASS + "[ReadResponse] Bluetooth GATT is disconnected.");
									read_poling_counter = 0;
								}
								else
								{
									characteristic.wait(read_poling_interval);
									read_poling_counter -= read_poling_interval;
								}
							}
						}
						catch (InterruptedException ignored)
						{
							BleLog.e(LOG_TAG, LOG_CLASS + "[ReadResponse][E] Bluetooth Adapter Read InterruptedException." + ignored.toString());
							BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE Read InterruptedException)");
						}
						if (isReadSuccess)
						{
							readBuf = characteristic.getValue();
						}
					}
					else
					{
						BoxError.ThrowError(BoxErrorCode.SDK_000, "(BLE Read Error)");
					}
				}

				if (i == 0)
				{
					ble_cmd.SetResponse(readBuf, readBuf.length);
					/*Get Number of Event*/
					eventNum = ble_cmd.GetItemNum();
					BleLog.d(LOG_TAG, LOG_CLASS + "[ReadResponse] BleCommand.GetEvents eventNum=" + eventNum);
					if(eventNum <= 3)
					{
						readData = new byte[firstReadMax];
					}
					else
					{
						readData = new byte[firstReadMax + (eventNum - 3) * readMax];
					}
					Utility.BlockCopy(readBuf, 0, readData, 0, readBuf.length);
					/*Check Response ErrorCode*/
					if (readData[6] == 0x00)
					{
						isRespError = false;
					}
					else
					{
						isRespError = true;
					}
				}
				else
				{
					Utility.BlockCopy(readBuf, 0, readData, (firstReadMax) + (i - 1) * readMax, readBuf.length);
				}
			}
		}
		else
		{
			BleLog.d(LOG_TAG, LOG_CLASS + "[ReadResponse] command=" + ble_cmd.get_CurrCommand());
			isReadSuccess = false;
			synchronized (characteristic)
			{
				boolean didRead = false;
				if (null != mGatt)
				{
					didRead = mGatt.readCharacteristic(characteristic);
				}
				if (didRead)
				{
					try
					{
						//characteristic.wait(readTimeout);
						read_poling_counter = readTimeout;
						while (0 < read_poling_counter)
						{
							if (null == mGatt)
							{
								BleLog.d(LOG_TAG, LOG_CLASS + "[ReadResponse] Bluetooth GATT is disconnected.");
								read_poling_counter = 0;
							}
							else
							{
								characteristic.wait(read_poling_interval);
								read_poling_counter -= read_poling_interval;
							}
						}
					}
					catch (InterruptedException ignored)
					{
						BleLog.e(LOG_TAG, LOG_CLASS + "[ReadResponse][E] Bluetooth Adapter Read InterruptedException." + ignored.toString());
						BoxError.ThrowError(BoxErrorCode.SDK_002, "(BLE Read InterruptedException)");
					}
					if (isReadSuccess)
					{
						readBuf = characteristic.getValue();
					}
				}
				else
				{
					BoxError.ThrowError(BoxErrorCode.SDK_000, "(BLE Read Error)");
				}
			}
			readData = new byte[readBuf.length];
			Utility.BlockCopy(readBuf, 0, readData, 0, readBuf.length);
			/*Check Response ErrorCode*/
			if (readData[6] == 0x00)
			{
				isRespError = false;
			}
			else
			{
				isRespError = true;
			}
		}

		/* for debug log start */
		String logString = "";
		for (byte c : readData)
		{
			logString += String.format("%02X", (Byte)c);
		}
		BleLog.d(LOG_TAG, LOG_CLASS + "[SendCommand] readBuf.length=" + readData.length + " readBuf=" + logString);
		/* for debug log end */

		BleLog.d(LOG_TAG, LOG_CLASS + "[ReadResponse] End");

		return readData;
	}
}