//package com.example.demo.libs.Model;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.BaseAdapter;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.demo.R;
//
//import java.security.MessageDigest;
//import java.sql.Date;
//import java.util.ArrayList;
//
//
//public class DeviceListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
//{
//	static class DeviceListAdapter extends BaseAdapter
//	{
//		private ArrayList<DiscoverEventArgs> mDeviceList;
//		private LayoutInflater mInflater;
//
//		public DeviceListAdapter( Activity activity )
//		{
//			super();
//			mDeviceList = new ArrayList<DiscoverEventArgs>();
//			mInflater = activity.getLayoutInflater();
//		}
//
//		public void addDevice( DiscoverEventArgs device )
//		{
//			for(int i = 0; i < mDeviceList.size(); i++) {
//				if(device.box.hardwareDeviceCode.equals(mDeviceList.get(i).box.hardwareDeviceCode))
//				{
//					mDeviceList.set(i, device);
//					notifyDataSetChanged();
//					return;
//				}
//			}
//
//			mDeviceList.add( device );
//			notifyDataSetChanged();
//		}
//
//		public void clear()
//		{
//			mDeviceList.clear();
//			notifyDataSetChanged();
//		}
//
//		@Override
//		public int getCount()
//		{
//			return mDeviceList.size();
//		}
//
//		@Override
//		public Object getItem( int position )
//		{
//			return mDeviceList.get( position );
//		}
//
//		@Override
//		public long getItemId( int position )
//		{
//			return position;
//		}
//
//		static class ViewHolder
//		{
//			TextView hardwareDeviceCode;
//			TextView type;
//			TextView firmVersion;
//			TextView remainingBattery;
//			TextView isDoorOpen;
//			TextView isLockLock;
//			TextView isTooMuchEvent;
//			TextView isPasswordSet;
//			TextView passwordNum;
//		}
//
//		@Override
//		public View getView( int position, View convertView, ViewGroup parent ) {
//			ViewHolder viewHolder;
//			// General ListView optimization code.
//			if (null == convertView) {
//				convertView=mInflater.inflate(R.layout.listitem_device, parent, false);
//				viewHolder=new ViewHolder();
//				viewHolder.hardwareDeviceCode=convertView.findViewById(R.id.textview_hardwareDeviceCode);
//				viewHolder.type=convertView.findViewById(R.id.textview_type);
//				viewHolder.firmVersion=convertView.findViewById(R.id.textview_firmVersion);
//				viewHolder.remainingBattery=convertView.findViewById(R.id.textview_remainingBattery);
//				viewHolder.isDoorOpen=convertView.findViewById(R.id.textview_isDoorOpen);
//				viewHolder.isLockLock=convertView.findViewById(R.id.textview_isLockLock);
//				viewHolder.isTooMuchEvent=convertView.findViewById(R.id.textview_isTooMuchEvent);
//				viewHolder.isPasswordSet=convertView.findViewById(R.id.textview_isPasswordSet);
//				viewHolder.passwordNum = convertView.findViewById( R.id.textview_passwordNum);
//				convertView.setTag(viewHolder);
//			} else {
//				viewHolder=(ViewHolder) convertView.getTag();
//			}
//
//			DiscoverEventArgs device=mDeviceList.get(position);
//
//			viewHolder.hardwareDeviceCode.setText("hardwareDeviceCode : " + device.box.get_HardwareDeviceCode());
//			viewHolder.type.setText("type : " + device.box.getAdvertisedMessage().get_type().toString());
//
//			byte[] temp=device.box.getAdvertisedMessage().get_firmVersion();
//			String firmVersion="";
//			for (byte b : temp) {
//				firmVersion+=String.format("%d.", b);
//			}
//			firmVersion=firmVersion.substring(0, firmVersion.length() - 1);
//			viewHolder.firmVersion.setText("firmVersion : " + firmVersion);
//
//			viewHolder.remainingBattery.setText("remainingBattery : " + device.box.getAdvertisedMessage().get_remainingBattery().toString() + " %");
//			viewHolder.isDoorOpen.setText("isDoorOpen : " + String.valueOf(device.box.getAdvertisedMessage().get_isDoorOpen()));
//			viewHolder.isLockLock.setText("isLockLock : " + String.valueOf(device.box.getAdvertisedMessage().get_isLockLock()));
//			viewHolder.isTooMuchEvent.setText("isTooMuchEvent : " + String.valueOf(device.box.getAdvertisedMessage().get_isTooMuchEvent()));
//			viewHolder.isPasswordSet.setText("isPasswordSet : " + String.valueOf(device.box.getAdvertisedMessage().get_isPasswordSet()));
//			viewHolder.passwordNum.setText( "passwordNum : " + String.valueOf( device.box.getAdvertisedMessage().get_passwordNum() ) );
//
//			return convertView;
//		}
//	}
//
//	// constant
//	public static final  String EXTRAS_DEVICE_ADDRESS   = "DEVICE_ADDRESS";
//
//	// member
//	private DeviceListAdapter mDeviceListAdapter;
//	private boolean mScanning = false;
//
//	private BoxManager mBoxManager;
//
//	private ScanThread mScanThread;
//
//	public static ArrayList<DiscoverEventArgs> mTargetDevice = new ArrayList<>();
//
//	// Handler
//	final Handler mErrorMessageHandler = new Handler() {
//		public void handleMessage(Message msg) {
//			String str = (String) msg.obj;
//			AlertDialog.Builder builder = new AlertDialog.Builder(DeviceListActivity.this);
//			builder.setTitle("Error");
//			builder.setMessage(str);
//			builder.setPositiveButton("OK", null);
//			builder.show();
//		}
//	};
//
//	@Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_device_list);
//
//		setResult( Activity.RESULT_CANCELED );
//
//		// ListView setting
//		mDeviceListAdapter = new DeviceListAdapter( this );
//		ListView listView = (ListView)findViewById( R.id.devicelist );
//		listView.setAdapter( mDeviceListAdapter );
//		listView.setOnItemClickListener( this );
//
//		try {
//			mBoxManager = new BoxManager(getApplicationContext());
//		} catch (BoxException e) {
//			Message msg = new Message();
//			msg.obj = e.getMessage();
//			mErrorMessageHandler.sendMessage(msg);
//		} catch (Exception e) {
//			Message msg = new Message();
//			msg.obj = e.getMessage();
//			mErrorMessageHandler.sendMessage(msg);
//		}
//
//		mBoxManager.OnBoxControllerDiscovered.addListener("addDevice", new BoxManager.DiscoverEventHandler() {
//			@Override
//			public void invoke(Object sender, DiscoverEventArgs e) {
//				DeviceListActivity.this.addDevice(sender, e);
//			}
//		});
//   }
//
//	@Override
//	protected void onResume()
//	{
//		super.onResume();
//
//		mScanning = true;
//
//		mScanThread = new ScanThread();
//		mScanThread.start();
//	}
//
//	@Override
//	protected void onPause()
//	{
//		super.onPause();
//
//		stopScan();
//	}
//
//	// StartScan
//	private void startScan()
//	{
//		mDeviceListAdapter.clear();
//
//		mScanning = true;
//
//		mScanThread = new ScanThread();
//		mScanThread.start();
//
//		invalidateOptionsMenu();
//	}
//
//	// StopScan
//	private void stopScan()
//	{
//		mScanning = false;
//		try {
//			mBoxManager.StopScanBoxControllers();
//		} catch (BoxException e) {
//			Message msg = new Message();
//			msg.obj = e.getMessage();
//			mErrorMessageHandler.sendMessage(msg);
//		} catch (Exception e) {
//			Message msg = new Message();
//			msg.obj = e.getMessage();
//			mErrorMessageHandler.sendMessage(msg);
//		}
//
//		invalidateOptionsMenu();
//	}
//
//	// back
//	private void backmain()
//	{
//		stopScan();
//		refreshDevice();
//
//		if (MainActivity.HardwareDeviceCode.size() == 0) {
//			MainActivity.select_device_no = -1;
//		}
//
//		Intent intent = new Intent();
//		setResult( Activity.RESULT_OK, intent );
//		finish();
//	}
//
//	@Override
//	public void onItemClick( AdapterView<?> parent, View view, int position, long id )
//	{
//		stopScan();
//		refreshDevice();
//
//		for (int i=0; i < MainActivity.HardwareDeviceCode.size(); i++) {
//			if (MainActivity.HardwareDeviceCode.get(i).equals(((DiscoverEventArgs)mDeviceListAdapter.getItem( position )).box.hardwareDeviceCode)) {
//				MainActivity.select_device_no = i;
//				MainActivity.mSpinner_Token.setSelection(i);
//				break;
//			}
//		}
//
//		Intent intent = new Intent();
//		setResult( Activity.RESULT_OK, intent );
//		finish();
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu( Menu menu )
//	{
//		getMenuInflater().inflate( R.menu.activity_device_list, menu );
//		menu.findItem( R.id.menuitem_back ).setVisible( true );
//		if( !mScanning )
//		{
//			menu.findItem( R.id.menuitem_stop ).setVisible( false );
//			menu.findItem( R.id.menuitem_scan ).setVisible( true );
//			menu.findItem( R.id.menuitem_progress ).setActionView( null );
//		}
//		else
//		{
//			menu.findItem( R.id.menuitem_stop ).setVisible( true );
//			menu.findItem( R.id.menuitem_scan ).setVisible( false );
//			menu.findItem( R.id.menuitem_progress ).setActionView( R.layout.actionbar_indeterminate_progress );
//		}
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected( MenuItem item )
//	{
//		switch( item.getItemId() )
//		{
//			case R.id.menuitem_scan:
//				startScan();
//				break;
//			case R.id.menuitem_stop:
//				stopScan();
//				break;
//			case R.id.menuitem_back:
//				backmain();
//				break;
//		}
//		return true;
//	}
//
//	private class ScanThread extends Thread {
//		public void run()
//		{
//			try {
//				mBoxManager.StartScanBoxControllers();
//			} catch (BoxException e) {
//				Message msg = new Message();
//				msg.obj = e.getMessage();
//				mErrorMessageHandler.sendMessage(msg);
//			} catch (Exception e) {
//				Message msg = new Message();
//				msg.obj = e.getMessage();
//				mErrorMessageHandler.sendMessage(msg);
//			}
//		}
//	}
//
//	public void addDevice(Object sender, DiscoverEventArgs e)
//	{
//		mDeviceListAdapter.addDevice( e );
//
//		for (int i=0; i < MainActivity.HardwareDeviceCode.size(); i++) {
//			if (MainActivity.HardwareDeviceCode.get(i).equals(e.box.get_HardwareDeviceCode())) {
//					mTargetDevice.set(i, e);
//				return;
//			}
//		}
//
//		MainActivity.HardwareDeviceCode.add(e.box.get_HardwareDeviceCode());
//
//		// Status
//		MainActivity.isConnect.add(new Boolean(false));
//		MainActivity.isLocked.add(new Boolean(true));
//		MainActivity.isLockUnknown.add(new Boolean(true));
//		MainActivity.isUpdating.add(new Boolean(false));
//		MainActivity.isCmdRunning.add(new Boolean(false));
//
//		// Connect Box Number
//		MainActivity.mConnectBoxNo.add(new Integer(-1));
//
//		// Get Data
//		MainActivity.retGetConfiguration.add(new BoxControllerConfig());
//		MainActivity.retGetDateTime.add(new Date(0));
//		MainActivity.retGetBatteryStatus.add(new Integer(0));
//		MainActivity.retGetStatus.add(new BoxStatus());
//		MainActivity.retIsDoorOpened.add(new Boolean(false));
//		MainActivity.retIsLocked.add(new Boolean(false));
//		MainActivity.retGetEvents.add(new EventsInformation());
//		MainActivity.retDeleteEvents.add(new Integer(0));
//		MainActivity.retGetPassword.add(new byte[10][16]);
//
//		// View Data
//		MainActivity.retStringGetConfiguration.add(new String(""));
//		MainActivity.retStringGetDateTime.add(new String(""));
//		MainActivity.retStringGetBatteryStatus.add(new String(""));
//		MainActivity.retStringGetStatus.add(new String(""));
//		MainActivity.retStringIsDoorOpened.add(new String(""));
//		MainActivity.retStringIsLocked.add(new String(""));
//		MainActivity.retStringGetEvents.add(new String(""));
//		MainActivity.retStringDeleteEvents.add(new String(""));
//		MainActivity.retStringGetPassword.add(new String(""));
//
//   		 // IV Key
//		byte[] tmp = (e.box.get_HardwareDeviceCode().replace(":", "") + "00000000").getBytes();
//		byte[] sha256 = {0};
//		try {
//			sha256 = MessageDigest.getInstance("SHA-256").digest(tmp);
//		} catch (Exception ex) {
//			Message msg=new Message();
//			msg.obj=ex.getMessage();
//			mErrorMessageHandler.sendMessage(msg);
//		}
//		byte[] ivkey = new byte[16];
//
//		for (int j = 0; j < 16; j++) {
//			ivkey[j] = sha256[16 + j];
//		}
//
//		MainActivity.mIvKey.add(ivkey);
//
//		MainActivity.adapterSelectToken.add(e.box.get_HardwareDeviceCode());
//
//		MainActivity.max_device_num = MainActivity.HardwareDeviceCode.size();
//
//		mTargetDevice.add(e);
//	}
//
//	public void refreshDevice()
//	{
//		for (int i=0; i < MainActivity.HardwareDeviceCode.size(); i++) {
//			boolean del_flag = true;
//			if (MainActivity.isConnect.get(i) == true) {
//				del_flag = false;
//			}
//
//			for (int j = 0; del_flag && (j < mDeviceListAdapter.getCount()); j++) {
//				if (MainActivity.HardwareDeviceCode.get(i).equals(((DiscoverEventArgs)mDeviceListAdapter.getItem(j)).box.hardwareDeviceCode)) {
//					del_flag = false;
//					break;
//				}
//			}
//
//			if (del_flag == true) {
//				if (MainActivity.select_device_no > i) {
//					MainActivity.select_device_no--;
//				}
//				else if (MainActivity.select_device_no == i){
//					MainActivity.select_device_no = 0;
//				}
//				else {
//					// Do Nothing
//				}
//
//				MainActivity.HardwareDeviceCode.remove(i);
//
//				// Status
//				MainActivity.isConnect.remove(i);
//				MainActivity.isLocked.remove(i);
//				MainActivity.isLockUnknown.remove(i);
//				MainActivity.isUpdating.remove(i);
//				MainActivity.isCmdRunning.remove(i);
//
//				// Connect Box Number
//				MainActivity.mConnectBoxNo.remove(i);
//
//				// Get Data
//				MainActivity.retGetConfiguration.remove(i);
//				MainActivity.retGetDateTime.remove(i);
//				MainActivity.retGetBatteryStatus.remove(i);
//				MainActivity.retGetStatus.remove(i);
//				MainActivity.retIsDoorOpened.remove(i);
//				MainActivity.retIsLocked.remove(i);
//				MainActivity.retGetEvents.remove(i);
//				MainActivity.retDeleteEvents.remove(i);
//				MainActivity.retGetPassword.remove(i);
//
//				// View Data
//				MainActivity.retStringGetConfiguration.remove(i);
//				MainActivity.retStringGetDateTime.remove(i);
//				MainActivity.retStringGetBatteryStatus.remove(i);
//				MainActivity.retStringGetStatus.remove(i);
//				MainActivity.retStringIsDoorOpened.remove(i);
//				MainActivity.retStringIsLocked.remove(i);
//				MainActivity.retStringGetEvents.remove(i);
//				MainActivity.retStringDeleteEvents.remove(i);
//				MainActivity.retStringGetPassword.remove(i);
//
//		   		 // IV Key
//				MainActivity.mIvKey.remove(i);
//
//				MainActivity.adapterSelectToken.remove(MainActivity.adapterSelectToken.getItem(i));
//
//				MainActivity.max_device_num--;
//
//				mTargetDevice.remove(i);
//
//			}
//		}
//
//		if (MainActivity.HardwareDeviceCode.size() == 0) {
//			MainActivity.select_device_no = -1;
//		}
//
//	}
//}