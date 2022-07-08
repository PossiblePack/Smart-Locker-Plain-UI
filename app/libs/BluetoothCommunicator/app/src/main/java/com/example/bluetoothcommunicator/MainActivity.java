package com.example.bluetoothcommunicator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import SmartLockerSdk.BoxController;
import SmartLockerSdk.BoxControllerConfig;
import SmartLockerSdk.BoxException;
import SmartLockerSdk.BoxManager;
import SmartLockerSdk.BoxStatus;
import SmartLockerSdk.EventsInformation;
import SmartLockerSdk.StatusEventArgs;
import SmartLockerSdk.Utility;

import static com.example.bluetoothcommunicator.DeviceListActivity.mTargetDevice;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, FileSelectDlg.OnFileSelectListener
{
   /* AES Key Select */
        /* 0: default */
    public static int select_aes_no = 0;

    /* AES Key Select */
    /* 0: AES Key(*.dat) */
    /* 1: FirmWare(*.gbl) */
    /* 2: Change Key(*.dat) */
    private static int file_select_type = 0;

    // constant
    private static final int REQUEST_CONNECTDEVICE = 1;
    private static final int TRUE  = 1;
    private static final int FALSE = 0;

    public static int max_device_num = 0;
    public static int select_device_no = -1;

    public static ArrayList<String> HardwareDeviceCode = new ArrayList<>();

    // member
    private ConnectThread           mConnectThread;
    private DisconnectThread        mDisconnectThread;
    private UnlockThread            mUnlockThread;
    private LockThread              mLockThread;
    private GetConfigurationThread  mGetConfigurationThread;
    private SetConfigurationThread  mSetConfigurationThread;
    private GetDateTimeThread       mGetDateTimeThread;
    private SetDateTimeThread       mSetDateTimeThread;
    private UpdateFirmwareThread    mUpdateFirmwareThread;
    private GetBatteryStatusThread  mGetBatteryStatusThread;
    private GetStatusThread         mGetStatusThread;
    private IsDoorOpenedThread      mIsDoorOpenedThread;
    private IsLockedThread          mIsLockedThread;
    private GetEventsThread         mGetEventsThread;
    private DeleteEventsThread      mDeleteEventsThread;
    private ResetDeviceThread       mResetDeviceThread;
    private SetPasswordThread       mSetPasswordThread;
    private GetPasswordThread       mGetPasswordThread;
    private ChangeKeyThread         mChangeKeyThread;

    // GUI Item
    private TextView mTextView_getsdkapiversion;
    private Button mButton_AddAes;
    private Button mButton_Connect;
    private Button mButton_Disconnect;
    private Button mButton_Unlock;
    private Button mButton_Lock;
    private Button mButton_GetConfiguration;
    private TextView mTextView_retGetConfiguration;
    private Button mButton_SetConfiguration;
    private Button mButton_GetDateTime;
    private TextView mTextView_retGetDateTime;
    private Button mButton_SetDateTime;
    private Button mButton_FirmwareFileSelect;
    private TextView mTextView_FirmwareFilePath;
    private Button mButton_UpdateFirmware;
    private Button mButton_GetBatteryStatus;
    private TextView mTextView_retGetBatteryStatus;
    private Button mButton_GetStatus;
    private TextView mTextView_retGetStatus;
    private Button mButton_IsDoorOpened;
    private TextView mTextView_retIsDoorOpened;
    private Button mButton_IsLocked;
    private TextView mTextView_retIsLocked;
    private CheckBox mCheckBox_isDeleteEvents;
    private Button mButton_GetEvents;
    private TextView mTextView_retGetEvents;
    private Button mButton_DeleteEvents;
    private TextView mTextView_retDeleteEvents;
    private Button mButton_ResetDevice;
    private Button mButton_SetPassword;
    private Button mButton_GetPassword;
    private TextView mTextView_retGetPassword;
    private Button mButton_ChangeKeyFileSelect;
    private TextView mTextView_ChangeKeyFilePath;
    private Button mButton_ChangeKey;

    public static Spinner mSpinner_Aes;
    public static Spinner mSpinner_Token;
    private Spinner mSpinner_UnlockPass;
    private Spinner mSpinner_Cfg;
    private Spinner mSpinner_Password;

    // Status
    public static ArrayList<Boolean> isConnect = new ArrayList<>();
    public static ArrayList<Boolean> isLocked = new ArrayList<>();
    public static ArrayList<Boolean> isLockUnknown = new ArrayList<>();
    public static ArrayList<Boolean> isUpdating = new ArrayList<>();
    public static ArrayList<Boolean> isCmdRunning = new ArrayList<>();

	// Connect Box Number
	public static ArrayList<Integer> mConnectBoxNo = new ArrayList<>();

    // Get Data
    public static ArrayList<BoxControllerConfig> retGetConfiguration = new ArrayList<>();
    public static ArrayList<Date> retGetDateTime = new ArrayList<>();
    public static ArrayList<Integer> retGetBatteryStatus = new ArrayList<>();
    public static ArrayList<BoxStatus> retGetStatus = new ArrayList<>();
    public static ArrayList<Boolean> retIsDoorOpened = new ArrayList<>();
    public static ArrayList<Boolean> retIsLocked = new ArrayList<>();
    public static ArrayList<EventsInformation> retGetEvents = new ArrayList<>();
    public static ArrayList<Integer> retDeleteEvents = new ArrayList<>();
    public static ArrayList<byte[][]> retGetPassword = new ArrayList<>();

    // View Data
    public static ArrayList<String> retStringGetConfiguration = new ArrayList<>();
    public static ArrayList<String> retStringGetDateTime = new ArrayList<>();
    public static ArrayList<String> retStringGetBatteryStatus = new ArrayList<>();
    public static ArrayList<String> retStringGetStatus = new ArrayList<>();
    public static ArrayList<String> retStringIsDoorOpened = new ArrayList<>();
    public static ArrayList<String> retStringIsLocked = new ArrayList<>();
    public static ArrayList<String> retStringGetEvents = new ArrayList<>();
    public static ArrayList<String> retStringDeleteEvents = new ArrayList<>();
    public static ArrayList<String> retStringGetPassword = new ArrayList<>();

    // Key
    public static ArrayList<byte[]> mIvKey = new ArrayList<>();
    public static ArrayList<byte[]> mAesKey = new ArrayList<>();

	// Select Adapter
    public static ArrayAdapter adapterSelectAes = null;
    public static ArrayAdapter adapterSelectToken = null;

    // Progress Dialog
    public  ProgressDialog mProgressDialog;

    // Handler
    final Handler mErrorMessageHandler = new Handler() {
        public void handleMessage(Message msg) {
            String str = (String) msg.obj;
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Error");
            builder.setMessage(str);
            builder.setPositiveButton("OK", null);
            builder.show();
        }
    };

    final Handler mViewUpdateHandler = new Handler() {
        public void handleMessage(Message msg) {
        	buttonUpdateAll();
        }
    };

    final Handler mButtonConnectEnableChangeHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg.what == 1)
            {
                isConnect.set(select_device_no, false);
            }
            else
            {
                isConnect.set(select_device_no, true);
            }
        	buttonUpdateAll();
        }
    };

    final Handler mButtonDisconnectEnableChangeHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg.what == 1)
            {
                isConnect.set(select_device_no, true);
            }
            else
            {
                isConnect.set(select_device_no, false);
            }
        	buttonUpdateAll();
        }
    };

    final Handler mButtonUnlockEnableChangeHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg.what == 1)
            {
                isLocked.set(select_device_no, true);
            }
            else
            {
                isLocked.set(select_device_no, false);
            }
        	buttonUpdateAll();
        }
    };

    final Handler mButtonLockEnableChangeHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg.what == 1)
            {
                isLocked.set(select_device_no, false);
            }
            else
            {
                isLocked.set(select_device_no, true);
            }
         	buttonUpdateAll();
       }
    };

    final Handler mProgressDialogHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg.what == 1)
            {
                mProgressDialog.show();
            }
            else
            {
                mProgressDialog.dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView_getsdkapiversion = findViewById( R.id.text_getsdkapiversion );
        BoxManager boxMng = null;
        try {
            boxMng = new BoxManager(getApplicationContext());
        }
        catch (Exception e)
        {
        }
        if(null != boxMng) {
            mTextView_getsdkapiversion.setText(boxMng.GetSdkApiVersion());
        }
        else
        {
            mTextView_getsdkapiversion.setText("Unknown");
        }
        mButton_AddAes = findViewById( R.id.button_AddAes );
        mButton_AddAes.setOnClickListener( this );
        mButton_Connect = findViewById( R.id.button_connect );
        mButton_Connect.setOnClickListener( this );
        mButton_Disconnect = findViewById( R.id.button_disconnect );
        mButton_Disconnect.setOnClickListener( this );
        mButton_Unlock = findViewById( R.id.button_unlock );
        mButton_Unlock.setOnClickListener( this );
        mButton_Lock = findViewById( R.id.button_lock );
        mButton_Lock.setOnClickListener( this );
        mButton_GetConfiguration = findViewById( R.id.button_getconfiguration );
        mButton_GetConfiguration.setOnClickListener( this );
        mTextView_retGetConfiguration = findViewById( R.id.text_getconfiguration );
        mButton_SetConfiguration = findViewById( R.id.button_setconfiguration );
        mButton_SetConfiguration.setOnClickListener( this );
        mButton_GetDateTime = findViewById( R.id.button_getdatetime );
        mButton_GetDateTime.setOnClickListener( this );
        mTextView_retGetDateTime = findViewById( R.id.text_getdatetime );
        mButton_SetDateTime = findViewById( R.id.button_setdatetime );
        mButton_SetDateTime.setOnClickListener( this );
        mButton_FirmwareFileSelect = findViewById( R.id.button_FirmwareFileSelect );
        mButton_FirmwareFileSelect.setOnClickListener( this );
        mButton_UpdateFirmware = findViewById( R.id.button_updatefirmware );
        mTextView_FirmwareFilePath = findViewById( R.id.text_firmwareFilePath );
        mButton_UpdateFirmware.setOnClickListener( this );
        mButton_GetBatteryStatus = findViewById( R.id.button_getbatterystatus );
        mButton_GetBatteryStatus.setOnClickListener( this );
        mTextView_retGetBatteryStatus = findViewById( R.id.text_getbatterystatus );
        mButton_GetStatus = findViewById( R.id.button_getstatus );
        mButton_GetStatus.setOnClickListener( this );
        mTextView_retGetStatus = findViewById( R.id.text_getstatus );
        mButton_IsDoorOpened = findViewById( R.id.button_isdooropened );
        mButton_IsDoorOpened.setOnClickListener( this );
        mTextView_retIsDoorOpened = findViewById( R.id.text_isdooropened );
        mButton_IsLocked = findViewById( R.id.button_islocked );
        mButton_IsLocked.setOnClickListener( this );
        mTextView_retIsLocked = findViewById( R.id.text_islocked );
        mCheckBox_isDeleteEvents = findViewById( R.id.checkbox_isdeleteevents );
        mCheckBox_isDeleteEvents.setOnClickListener( this );
        mButton_GetEvents = findViewById( R.id.button_getevents );
        mButton_GetEvents.setOnClickListener( this );
        mTextView_retGetEvents = findViewById( R.id.text_getevents );
        mButton_DeleteEvents = findViewById( R.id.button_deleteevents );
        mButton_DeleteEvents.setOnClickListener( this );
        mTextView_retDeleteEvents = findViewById( R.id.text_deleteevents );
        mButton_ResetDevice = findViewById( R.id.button_resetdevice );
        mButton_ResetDevice.setOnClickListener( this );
        mButton_SetPassword = findViewById( R.id.button_setpassword );
        mButton_SetPassword.setOnClickListener( this );
        mButton_GetPassword = findViewById( R.id.button_getpassword );
        mButton_GetPassword.setOnClickListener( this );
        mTextView_retGetPassword = findViewById( R.id.text_getpassword );
        mButton_ChangeKeyFileSelect = findViewById( R.id.button_changeKeyFileSelect );
        mButton_ChangeKeyFileSelect.setOnClickListener( this );
        mButton_ChangeKey = findViewById( R.id.button_changeKey );
        mTextView_ChangeKeyFilePath = findViewById( R.id.text_changeKeyFilePath );
        mButton_ChangeKey.setOnClickListener( this );

        // Progress Dialog
        mProgressDialog = new ProgressDialog(this);

        // Select Aes
        adapterSelectAes = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item);
        adapterSelectAes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner_Aes= findViewById( R.id.spinner_aes );
        mSpinner_Aes.setAdapter(adapterSelectAes);
        mSpinner_Aes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,
                                       long id) {
                select_aes_no = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        byte[][] aeskey = {
                /* 0: default */
                {
                        (byte)0xDB, (byte)0x33, (byte)0x62, (byte)0x02,
                        (byte)0x53, (byte)0xF6, (byte)0x48, (byte)0xD3,
                        (byte)0xF4, (byte)0x02, (byte)0x70, (byte)0xB4,
                        (byte)0xD2, (byte)0xCB, (byte)0xDF, (byte)0x33,
                        (byte)0xB7, (byte)0x50, (byte)0x98, (byte)0x1C,
                        (byte)0xEC, (byte)0xB0, (byte)0xE4, (byte)0xB1,
                        (byte)0xD5, (byte)0xD5, (byte)0x22, (byte)0x9E,
                        (byte)0x94, (byte)0x07, (byte)0xB4, (byte)0x3F
                }
        };

        for (int i = 0; i < aeskey.length; i++) {
            mAesKey.add(aeskey[i]);
        }
        adapterSelectAes.add("default");

        // Select Token
        adapterSelectToken = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item);
        adapterSelectToken.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner_Token = findViewById( R.id.spinner_token );
        mSpinner_Token.setAdapter(adapterSelectToken);
        mSpinner_Token.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,
                                       long id) {
                select_device_no = position;
                mConnectBoxNo.set(select_device_no, mSpinner_Token.getSelectedItemPosition());
                ( (TextView)findViewById( R.id.textview_hardwareDeviceCode) ).setText( MainActivity.HardwareDeviceCode.get(select_device_no) );
                buttonUpdateAll();
           }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
             }
        });

        // Select Unlock Password
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item);
        adapter.add("Unlock pass:0,1,2,3,4,5,6,7,8,9");
        adapter.add("Unlock pass:0000,1111,...,9999");
        adapter.add("Unlock pass:1234567890123456,...,1234567");
        adapter.add("Unlock pass:0*16,1*16,...,9*16");
        adapter.add("Unlock pass:0,1,2,3,4,5,6,7,8,-");
        adapter.add("Unlock pass:0*16,1,...,8,-");
        adapter.add("Unlock pass:0,-,-,-,-,-,-,-,-,-");
        adapter.add("Unlock pass:-,-,-,-,-,-,-,-,-,-");
        adapter.add("Unlock pass:None");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner_UnlockPass = findViewById( R.id.spinner_unlockpass );
        mSpinner_UnlockPass.setAdapter(adapter);

        // Select Configration
        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item);
        adapter.add("default");
        adapter.add("txpow=0");
        adapter.add("conn_tout=0");
        adapter.add("conn_tout=256");
        adapter.add("conn_tout=257(error)");
        adapter.add("txpow=85(error)");
        adapter.add("txpow=80");
        adapter.add("txpow=-183(error)");
        adapter.add("txpow=-185");
        adapter.add("txpow=-270");
        adapter.add("txpow=-275(error)");
        adapter.add("default(txpow=0)");
        adapter.add("default(txpow=-30)");
        adapter.add("default(txpow=-60)");
        adapter.add("default(txpow=-90)");
        adapter.add("default(txpow=-120)");
        adapter.add("default(txpow=-125)");
        adapter.add("default(txpow=-130)");
        adapter.add("default(txpow=-135)");
        adapter.add("default(txpow=-140)");
        adapter.add("default(txpow=-145)");
        adapter.add("default(txpow=-150)");
        adapter.add("default(txpow=-180)");
        adapter.add("default(txpow=-210)");
        adapter.add("default(txpow=-240)");
        adapter.add("default(txpow=-270)");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner_Cfg = findViewById( R.id.spinner_cfg );
        mSpinner_Cfg.setAdapter(adapter);

        // Select Password
        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item);
        adapter.add("pass:0,1,2,3,4,5,6,7,8,9");
        adapter.add("pass:0000,...,9999");
        adapter.add("pass:1234567890123456,...,1234567");
        adapter.add("pass:0*16,1*16,...,9*16");
        adapter.add("pass:0,1,2,3,4,5,6,7,8,-");
        adapter.add("pass:0*16,1,...,8,-");
        adapter.add("pass:0,-,-,-,-,-,-,-,-,-");
        adapter.add("pass:-,-,-,-,-,-,-,-,-,-");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner_Password = findViewById( R.id.spinner_password );
        mSpinner_Password.setAdapter(adapter);

        for (int i = 0; i < max_device_num; i++) {
            isConnect.set(i, false);
            retGetConfiguration.set(i, null);
            retGetDateTime.set(i, null);
            isLocked.set(i, true);
            isLockUnknown.set(i, true);
            retGetEvents.set(i, null);
            retDeleteEvents.set(i, null);
            retGetPassword.set(i, null);

            isUpdating.set(i, false);
            isCmdRunning.set(i, false);

            retStringGetConfiguration.set(i, new String(""));
            retStringGetDateTime.set(i, new String(""));
            retStringGetBatteryStatus.set(i, new String(""));
            retStringGetStatus.set(i, new String(""));
            retStringIsDoorOpened.set(i, new String(""));
            retStringIsLocked.set(i, new String(""));
            retStringGetEvents.set(i, new String(""));
            retStringDeleteEvents.set(i, new String(""));
            retStringGetPassword.set(i, new String(""));
        }

        buttonDisableAll();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        switch( requestCode )
        {
            case REQUEST_CONNECTDEVICE:
                 for (int i = 0; i < max_device_num; i++) {
                     if (mTargetDevice.get(i) != null) {
                     	if (isConnect.get(i) != true) {
			                 mTargetDevice.get(i).box.OnDoorChange.addListener("DoorChangeEventHandler", new BoxController.StatusEventHandler() {
			                     @Override
			                     public void invoke(BoxController sender, StatusEventArgs e) {
			                         DoorChangeEventHandler(sender, e);
			                     }
			                 });

			                 mTargetDevice.get(i).box.OnLockChange.addListener("LockChangeEventHandler", new BoxController.StatusEventHandler() {
			                     @Override
			                     public void invoke(BoxController sender, StatusEventArgs e) {
			                         LockChangeEventHandler(sender, e);
			                     }
			                 });

			                 mTargetDevice.get(i).box.OnConnectionChange.addListener("ConnectionChangeEventHandler", new BoxController.StatusEventHandler() {
			                     @Override
			                     public void invoke(BoxController sender, StatusEventArgs e) {
			                         ConnectionChangeEventHandler(sender, e);
			                     }
			                 });
                     	}
                     	if (i == select_device_no) {
							buttonUpdateAll();
						}
                     }
                     else {
                     }
                 }

	        	if (select_device_no >= 0)
	        	{
         	       ( (TextView)findViewById( R.id.textview_hardwareDeviceCode) ).setText( MainActivity.HardwareDeviceCode.get(select_device_no) );
	        	}

                break;
        }
        super.onActivityResult( requestCode, resultCode, data );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.activity_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.menuitem_search:
                Intent devicelistactivityIntent = new Intent( this, DeviceListActivity.class );
                startActivityForResult( devicelistactivityIntent, REQUEST_CONNECTDEVICE );
                return true;
        }
        return false;
    }
    @Override
    public void onClick( View v )
    {
        if( mButton_AddAes.getId() == v.getId() )
        {
            String dir = "/mnt/sdcard";
            FileSelectDlg dlg = new FileSelectDlg( this, this, "dat" );
            file_select_type = 0;
            dlg.show( new File( dir ) );
            return;
        }
        if( mButton_Connect.getId() == v.getId() )
        {
            connect();
            return;
        }
        if( mButton_Disconnect.getId() == v.getId() )
        {
            disconnect();
            return;
        }
        if( mButton_Unlock.getId() == v.getId() )
        {
            unlock();
            return;
        }
        if( mButton_Lock.getId() == v.getId() )
        {
            lock();
            return;
        }
        if( mButton_GetConfiguration.getId() == v.getId() )
        {
            getconfiguration();
            return;
        }
        if( mButton_SetConfiguration.getId() == v.getId() )
        {
            setconfiguration();
            return;
        }
        if( mButton_GetDateTime.getId() == v.getId() )
        {
            getdatetime();
            return;
        }
        if( mButton_SetDateTime.getId() == v.getId() )
        {
            setdatetime();
            return;
        }
        if( mButton_FirmwareFileSelect.getId() == v.getId() )
        {
            String dir = "/mnt/sdcard";
            FileSelectDlg dlg = new FileSelectDlg( this, this, "gbl" );
            file_select_type = 1;
            dlg.show( new File( dir ) );
            return;
        }
        if( mButton_UpdateFirmware.getId() == v.getId() )
        {
            updatefirmware();
            return;
        }
        if( mButton_GetBatteryStatus.getId() == v.getId() )
        {
            getbatterystatus();
            return;
        }
        if( mButton_GetStatus.getId() == v.getId() )
        {
            getstatus();
            return;
        }
        if( mButton_IsDoorOpened.getId() == v.getId() )
        {
            isdooropened();
            return;
        }
        if( mButton_IsLocked.getId() == v.getId() )
        {
            islocked();
            return;
        }
        if( mCheckBox_isDeleteEvents.getId() == v.getId() )
        {
            isdeleteevents();
            return;
        }
        if( mButton_GetEvents.getId() == v.getId() )
        {
            getevents();
            return;
        }
        if( mButton_DeleteEvents.getId() == v.getId() )
        {
            deleteevents();
            return;
        }
        if( mButton_ResetDevice.getId() == v.getId() )
        {
            resetdevice();
            return;
        }
        if( mButton_SetPassword.getId() == v.getId() )
        {
            setpassword();
            return;
        }
        if( mButton_GetPassword.getId() == v.getId() )
        {
            getpassword();
            return;
        }
        if( mButton_ChangeKeyFileSelect.getId() == v.getId() )
        {
            String dir = "/mnt/sdcard";
            FileSelectDlg dlg = new FileSelectDlg( this, this, "dat" );
            file_select_type = 2;
            dlg.show( new File( dir ) );
            return;
        }
        if( mButton_ChangeKey.getId() == v.getId() )
        {
            changekey();
            return;
        }
    }

    private void buttonDisableAll()
    {
        mButton_Connect.setEnabled( false );
        mButton_Disconnect.setEnabled( false );
        mButton_Unlock.setEnabled( false );
        mButton_Lock.setEnabled( false );
        mButton_GetConfiguration.setEnabled( false );
        mButton_SetConfiguration.setEnabled( false );
        mButton_GetDateTime.setEnabled( false );
        mButton_SetDateTime.setEnabled( false );
        mButton_UpdateFirmware.setEnabled( false );
        mButton_GetBatteryStatus.setEnabled( false );
        mButton_GetStatus.setEnabled( false );
        mButton_IsDoorOpened.setEnabled( false );
        mButton_IsLocked.setEnabled( false );
        mButton_GetEvents.setEnabled( false );
        mButton_DeleteEvents.setEnabled( false );
        mButton_ResetDevice.setEnabled( false );
        mButton_SetPassword.setEnabled( false );
        mButton_GetPassword.setEnabled( false );
        mButton_ChangeKey.setEnabled( false );
	    if (select_device_no >= 0)
	    {
	        mTextView_retGetConfiguration.setText(retStringGetConfiguration.get(select_device_no));
	        mTextView_retGetDateTime.setText(retStringGetDateTime.get(select_device_no));
	        mTextView_retGetBatteryStatus.setText(retStringGetBatteryStatus.get(select_device_no));
	        mTextView_retGetStatus.setText(retStringGetStatus.get(select_device_no));
	        mTextView_retIsDoorOpened.setText(retStringIsDoorOpened.get(select_device_no));
	        mTextView_retIsLocked.setText(retStringIsLocked.get(select_device_no));
	        mTextView_retGetEvents.setText(retStringGetEvents.get(select_device_no));
	        mTextView_retDeleteEvents.setText(retStringDeleteEvents.get(select_device_no));
            mTextView_retGetPassword.setText(retStringGetPassword.get(select_device_no));
	    }
    	else
    	{
	        mTextView_retGetConfiguration.setText("");
	        mTextView_retGetDateTime.setText("");
	        mTextView_retGetBatteryStatus.setText("");
	        mTextView_retGetStatus.setText("");
	        mTextView_retIsDoorOpened.setText("");
	        mTextView_retIsLocked.setText("");
	        mTextView_retGetEvents.setText("");
	        mTextView_retDeleteEvents.setText("");
            mTextView_retGetPassword.setText("");
    	}
    }

    private void buttonUpdateAll()
    {
	    if (select_device_no < 0)
	    {
            buttonDisableAll();
            return;
	    }
        if (isUpdating.get(select_device_no)) {
            buttonDisableAll();
            return;
        }
        if (isCmdRunning.get(select_device_no)) {
            buttonDisableAll();
            return;
        }
        if (mTargetDevice.get(select_device_no) == null) {
            buttonDisableAll();
            return;
        }
        if (isConnect.get(select_device_no))
        {
            mButton_Connect.setEnabled( false );
            mButton_Disconnect.setEnabled( true );
        	if (isLockUnknown.get(select_device_no))
        	{
	            mButton_Unlock.setEnabled( true );
	            mButton_Lock.setEnabled( true );
        	}
            else if (isLocked.get(select_device_no))
            {
	            mButton_Unlock.setEnabled( true );
	            mButton_Lock.setEnabled( false );
            }
            else
            {
	            mButton_Unlock.setEnabled( false );
	            mButton_Lock.setEnabled( true );
            }
            mButton_GetConfiguration.setEnabled( true );
	        mTextView_retGetConfiguration.setText(retStringGetConfiguration.get(select_device_no));
            mButton_SetConfiguration.setEnabled( true );
            mButton_GetDateTime.setEnabled( true );
	        mTextView_retGetDateTime.setText(retStringGetDateTime.get(select_device_no));
            mButton_SetDateTime.setEnabled( true );
            mButton_UpdateFirmware.setEnabled( true );
            mButton_GetBatteryStatus.setEnabled( true );
	        mTextView_retGetBatteryStatus.setText(retStringGetBatteryStatus.get(select_device_no));
            mButton_GetStatus.setEnabled( true );
	        mTextView_retGetStatus.setText(retStringGetStatus.get(select_device_no));
            mButton_IsDoorOpened.setEnabled( true );
	        mTextView_retIsDoorOpened.setText(retStringIsDoorOpened.get(select_device_no));
            mButton_IsLocked.setEnabled( true );
	        mTextView_retIsLocked.setText(retStringIsLocked.get(select_device_no));
            mButton_GetEvents.setEnabled( true );
	        mTextView_retGetEvents.setText(retStringGetEvents.get(select_device_no));
            mButton_DeleteEvents.setEnabled( true );
	        mTextView_retDeleteEvents.setText(retStringDeleteEvents.get(select_device_no));
            mButton_ResetDevice.setEnabled( true );
            mButton_SetPassword.setEnabled( true );
            mButton_GetPassword.setEnabled( true );
            mTextView_retGetPassword.setText(retStringGetPassword.get(select_device_no));
            mButton_ChangeKey.setEnabled( true );
        }
        else
        {
            mButton_Connect.setEnabled( true );
            mButton_Disconnect.setEnabled( true );
            mButton_Unlock.setEnabled( false );
            mButton_Lock.setEnabled( false );
            mButton_GetConfiguration.setEnabled( false );
	        mTextView_retGetConfiguration.setText("");
            mButton_SetConfiguration.setEnabled( false );
            mButton_GetDateTime.setEnabled( false );
	        mTextView_retGetDateTime.setText("");
            mButton_SetDateTime.setEnabled( false );
            mButton_UpdateFirmware.setEnabled( false );
            mButton_GetBatteryStatus.setEnabled( false );
	        mTextView_retGetBatteryStatus.setText("");
            mButton_GetStatus.setEnabled( false );
	        mTextView_retGetStatus.setText("");
            mButton_IsDoorOpened.setEnabled( false );
	        mTextView_retIsDoorOpened.setText("");
            mButton_IsLocked.setEnabled( false );
	        mTextView_retIsLocked.setText("");
            mButton_GetEvents.setEnabled( false );
	        mTextView_retGetEvents.setText("");
            mButton_DeleteEvents.setEnabled( false );
	        mTextView_retDeleteEvents.setText("");
            mButton_ResetDevice.setEnabled( false );
            mButton_SetPassword.setEnabled( false );
            mButton_GetPassword.setEnabled( false );
            mTextView_retGetPassword.setText("");
            mButton_ChangeKey.setEnabled( false );
       }
    }

    private void connect()
    {
        if( select_device_no < 0 )
        {
            return;
        }

    	isConnect.set(select_device_no, false);
        mButton_Connect.setEnabled( false );
        isCmdRunning.set(select_device_no, true);

        mConnectThread = new ConnectThread();
        mConnectThread.start();
    }

    private void disconnect()
    {
        if( (select_device_no >= MainActivity.HardwareDeviceCode.size()) || (mButton_Disconnect.isEnabled() == false) )
        {
            return;
        }

        isConnect.set(select_device_no, false);
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mDisconnectThread = new DisconnectThread();
        mDisconnectThread.start();
    }

    private void unlock()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mUnlockThread = new UnlockThread();
        mUnlockThread.start();
    }

    private void lock()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mLockThread = new LockThread();
        mLockThread.start();
    }

    private void getconfiguration()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mGetConfigurationThread = new GetConfigurationThread();
        mGetConfigurationThread.start();
    }

    private void setconfiguration()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mSetConfigurationThread = new SetConfigurationThread();
        mSetConfigurationThread.start();
    }

    private void getdatetime()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mGetDateTimeThread = new GetDateTimeThread();
        mGetDateTimeThread.start();
    }

    private void setdatetime()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mSetDateTimeThread = new SetDateTimeThread();
        mSetDateTimeThread.start();
    }

    private void updatefirmware()
    {
        buttonDisableAll();
        isUpdating.set(select_device_no, true);
        isCmdRunning.set(select_device_no, true);

        mUpdateFirmwareThread = new UpdateFirmwareThread();
        mUpdateFirmwareThread.start();
    }

    private void getbatterystatus()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mGetBatteryStatusThread = new GetBatteryStatusThread();
        mGetBatteryStatusThread.start();
    }

	private void getstatus()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mGetStatusThread = new GetStatusThread();
        mGetStatusThread.start();
    }

    private void isdooropened()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mIsDoorOpenedThread = new IsDoorOpenedThread();
        mIsDoorOpenedThread.start();
    }

	private void islocked()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mIsLockedThread = new IsLockedThread();
        mIsLockedThread.start();
    }

    private void isdeleteevents()
    {
    }

    private void getevents()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mGetEventsThread = new GetEventsThread();
        mGetEventsThread.start();
    }

    private void deleteevents()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mDeleteEventsThread = new DeleteEventsThread();
        mDeleteEventsThread.start();
    }

    private void resetdevice()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mResetDeviceThread = new ResetDeviceThread();
        mResetDeviceThread.start();
    }

    private void setpassword()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mSetPasswordThread = new SetPasswordThread();
        mSetPasswordThread.start();
    }

    private void getpassword()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mGetPasswordThread = new GetPasswordThread();
        mGetPasswordThread.start();
    }

    private void changekey()
    {
        buttonDisableAll();
        isCmdRunning.set(select_device_no, true);

        mChangeKeyThread = new ChangeKeyThread();
        mChangeKeyThread.start();
    }

    private class ConnectThread extends Thread {
        public void run() {
            try {
                Cipher encrypter;
                byte[] token = {
                        (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, /* Token ID */
                        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, /* - */
                        (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, /* Privilege */
                        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00  /* - */
                };
                byte[] cipheredTmp= new byte[32];
                byte[] cipheredToken= new byte[16];
                byte[] hashTokenByte= new byte[4];

                IvParameterSpec iv = new IvParameterSpec(mIvKey.get(select_device_no));
                SecretKeySpec key = new SecretKeySpec(mAesKey.get(select_aes_no), "AES");
                encrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
                encrypter.init(Cipher.ENCRYPT_MODE, key, iv);
                cipheredTmp = encrypter.doFinal(token);
                for (int i = 0; i < 16; i++) {
                    cipheredToken[i] = cipheredTmp[i];
                }

                byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(token);
                for (int i = 0; i < 4; i++) {
                    hashTokenByte[i] = sha256[28 + i];
                }

                int hashToken = 0;
                if (hashTokenByte != null) {
                    hashToken = Utility.ToInt32(hashTokenByte, 0);
                }

                isLockUnknown.set(select_device_no, true);
                mTargetDevice.get(select_device_no).box.Connect(cipheredToken, hashToken);

            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                mErrorMessageHandler.sendMessage(msg);
            	isConnect.set(select_device_no, false);
                isCmdRunning.set(select_device_no, false);
                mButtonConnectEnableChangeHandler.sendEmptyMessage(TRUE);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                mErrorMessageHandler.sendMessage(msg);
            	isConnect.set(select_device_no, false);
                isCmdRunning.set(select_device_no, false);
                mButtonConnectEnableChangeHandler.sendEmptyMessage(TRUE);
            }
        	isConnect.set(select_device_no, true);
                isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class DisconnectThread extends Thread {
        public void run()
        {
            isLockUnknown.set(select_device_no, true);
            try {
                mTargetDevice.get(select_device_no).box.Disconnect();
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

        	isConnect.set(select_device_no, false);
            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class UnlockThread extends Thread {
        public void run()
        {
            try {
                byte[][] passwordTmp = new byte[10][1];
                byte[][] encryptedPassword = new byte[10][16];
                byte[][] hashPasswordByte = new byte[10][4];
                boolean isSetPassword = true;

                switch (mSpinner_UnlockPass.getSelectedItemPosition())
                {
                    case 8:
                        isSetPassword = false;
                        passwordTmp[0] = new String("").getBytes();
                        passwordTmp[1] = new String("").getBytes();
                        passwordTmp[2] = new String("").getBytes();
                        passwordTmp[3] = new String("").getBytes();
                        passwordTmp[4] = new String("").getBytes();
                        passwordTmp[5] = new String("").getBytes();
                        passwordTmp[6] = new String("").getBytes();
                        passwordTmp[7] = new String("").getBytes();
                        passwordTmp[8] = new String("").getBytes();
                        passwordTmp[9] = new String("").getBytes();
                        break;
                    case 7:
                        passwordTmp[0] = new String("").getBytes();
                        passwordTmp[1] = new String("").getBytes();
                        passwordTmp[2] = new String("").getBytes();
                        passwordTmp[3] = new String("").getBytes();
                        passwordTmp[4] = new String("").getBytes();
                        passwordTmp[5] = new String("").getBytes();
                        passwordTmp[6] = new String("").getBytes();
                        passwordTmp[7] = new String("").getBytes();
                        passwordTmp[8] = new String("").getBytes();
                        passwordTmp[9] = new String("").getBytes();
                        break;

                    case 6:
                        passwordTmp[0] = new String("0").getBytes();
                        passwordTmp[1] = new String("").getBytes();
                        passwordTmp[2] = new String("").getBytes();
                        passwordTmp[3] = new String("").getBytes();
                        passwordTmp[4] = new String("").getBytes();
                        passwordTmp[5] = new String("").getBytes();
                        passwordTmp[6] = new String("").getBytes();
                        passwordTmp[7] = new String("").getBytes();
                        passwordTmp[8] = new String("").getBytes();
                        passwordTmp[9] = new String("").getBytes();
                        break;

                    case 5:
                        passwordTmp[0] = new String("0000000000000000").getBytes();
                        passwordTmp[1] = new String("1").getBytes();
                        passwordTmp[2] = new String("2").getBytes();
                        passwordTmp[3] = new String("3").getBytes();
                        passwordTmp[4] = new String("4").getBytes();
                        passwordTmp[5] = new String("5").getBytes();
                        passwordTmp[6] = new String("6").getBytes();
                        passwordTmp[7] = new String("7").getBytes();
                        passwordTmp[8] = new String("8").getBytes();
                        passwordTmp[9] = new String("").getBytes();
                        break;

                    case 4:
                        passwordTmp[0] = new String("0").getBytes();
                        passwordTmp[1] = new String("1").getBytes();
                        passwordTmp[2] = new String("2").getBytes();
                        passwordTmp[3] = new String("3").getBytes();
                        passwordTmp[4] = new String("4").getBytes();
                        passwordTmp[5] = new String("5").getBytes();
                        passwordTmp[6] = new String("6").getBytes();
                        passwordTmp[7] = new String("7").getBytes();
                        passwordTmp[8] = new String("8").getBytes();
                        passwordTmp[9] = new String("").getBytes();
                        break;

                    case 3:
                        passwordTmp[0] = new String("0000000000000000").getBytes();
                        passwordTmp[1] = new String("1111111111111111").getBytes();
                        passwordTmp[2] = new String("2222222222222222").getBytes();
                        passwordTmp[3] = new String("3333333333333333").getBytes();
                        passwordTmp[4] = new String("4444444444444444").getBytes();
                        passwordTmp[5] = new String("5555555555555555").getBytes();
                        passwordTmp[6] = new String("6666666666666666").getBytes();
                        passwordTmp[7] = new String("7777777777777777").getBytes();
                        passwordTmp[8] = new String("8888888888888888").getBytes();
                        passwordTmp[9] = new String("9999999999999999").getBytes();
                        break;

                    case 2:
                        passwordTmp[0] = new String("1234567890123456").getBytes();
                        passwordTmp[1] = new String("123456789012345").getBytes();
                        passwordTmp[2] = new String("12345678901234").getBytes();
                        passwordTmp[3] = new String("1234567890123").getBytes();
                        passwordTmp[4] = new String("123456789012").getBytes();
                        passwordTmp[5] = new String("12345678901").getBytes();
                        passwordTmp[6] = new String("1234567890").getBytes();
                        passwordTmp[7] = new String("123456789").getBytes();
                        passwordTmp[8] = new String("12345678").getBytes();
                        passwordTmp[9] = new String("1234567").getBytes();
                        break;

                    case 1:
                        passwordTmp[0] = new String("0000").getBytes();
                        passwordTmp[1] = new String("1111").getBytes();
                        passwordTmp[2] = new String("2222").getBytes();
                        passwordTmp[3] = new String("3333").getBytes();
                        passwordTmp[4] = new String("4444").getBytes();
                        passwordTmp[5] = new String("5555").getBytes();
                        passwordTmp[6] = new String("6666").getBytes();
                        passwordTmp[7] = new String("7777").getBytes();
                        passwordTmp[8] = new String("8888").getBytes();
                        passwordTmp[9] = new String("9999").getBytes();
                        break;

                    case 0:
                    default:
                        passwordTmp[0] = new String("0").getBytes();
                        passwordTmp[1] = new String("1").getBytes();
                        passwordTmp[2] = new String("2").getBytes();
                        passwordTmp[3] = new String("3").getBytes();
                        passwordTmp[4] = new String("4").getBytes();
                        passwordTmp[5] = new String("5").getBytes();
                        passwordTmp[6] = new String("6").getBytes();
                        passwordTmp[7] = new String("7").getBytes();
                        passwordTmp[8] = new String("8").getBytes();
                        passwordTmp[9] = new String("9").getBytes();
                        break;
                }

                Integer[] hashPassword = new Integer[10];
                if (hashPasswordByte != null) {
                    for (int j = 0; j < 10; j++) {
                        if (Arrays.equals(passwordTmp[j], "".getBytes()))
                        {
                            // Empty password
                            for (int i = 0; i < 16; i++) {
                                encryptedPassword[j][i] = 0;
                            }
                            hashPassword[j] = 0;
                            continue;
                        }

                        byte[] password = new byte[16];
                        for (int i = 0; i < passwordTmp[j].length; i++) {
                            password[i] = passwordTmp[j][i];
                        }
                        Cipher encrypter;
                        IvParameterSpec iv = new IvParameterSpec(mIvKey.get(select_device_no));
                        SecretKeySpec key = new SecretKeySpec(mAesKey.get(select_aes_no), "AES");
                        encrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        encrypter.init(Cipher.ENCRYPT_MODE, key, iv);
                        byte[] cipheredTmp = new byte[32];
                        cipheredTmp = encrypter.doFinal(password);
                        for (int i = 0; i < 16; i++) {
                            encryptedPassword[j][i] = cipheredTmp[i];
                        }

                        byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(passwordTmp[j]);
                        for (int i = 0; i < 4; i++) {
                            hashPasswordByte[j][i] = sha256[28 + i];
                        }
                        hashPassword[j] = Utility.ToInt32(hashPasswordByte[j], 0);
                    }
                }

                if(isSetPassword == true)
                {
                    // Set Password
                    mTargetDevice.get(select_device_no).box.Unlock(encryptedPassword, hashPassword);
                }
                else
                {
                    // Not Set Password
                    mTargetDevice.get(select_device_no).box.Unlock();
                }
            } catch (BoxException e) {
                isLockUnknown.set(select_device_no, true);
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                isLockUnknown.set(select_device_no, true);
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class LockThread extends Thread {
        public void run()
        {
            try {
                mTargetDevice.get(select_device_no).box.Lock();
            } catch (BoxException e) {
                isLockUnknown.set(select_device_no, true);
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                isLockUnknown.set(select_device_no, true);
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class GetConfigurationThread extends Thread {
        public void run()
        {
            try {
            	retStringGetConfiguration.set(select_device_no, "");
                retGetConfiguration.set(select_device_no, mTargetDevice.get(select_device_no).box.GetConfiguration());
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            if (null != retGetConfiguration.get(select_device_no))
            {
               retStringGetConfiguration.set(select_device_no,
                   "advertisingInterval=" + retGetConfiguration.get(select_device_no).get_advertisingInterval() + "\n" +
                   "strengthPower=" + retGetConfiguration.get(select_device_no).get_strengthPower() + "\n" +
                   "channelUsed=" + retGetConfiguration.get(select_device_no).get_channelUsed().getValue() + "\n" +
                   "attemptMax=" + retGetConfiguration.get(select_device_no).get_attemptMax() + "\n" +
                   "autoCloseTime=" + retGetConfiguration.get(select_device_no).get_autoCloseTime() + "\n" +
                   "inputImpossibleTime=" + retGetConfiguration.get(select_device_no).get_inputImpossibleTime() + "\n" +
                   "warnEventNum=" + retGetConfiguration.get(select_device_no).get_warnEventNum() + "\n" +
                   "connectionTimeOut=" + retGetConfiguration.get(select_device_no).get_connectionTimeOut() + "\n" +
                   "passwordDeleteTime=" + retGetConfiguration.get(select_device_no).get_passwordDeleteTime()
               );
            }
            else
            {
                retStringGetConfiguration.set(select_device_no,  "GetConfiguration return is null.");
            }
            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class SetConfigurationThread extends Thread {
        public void run()
        {
            try {
                byte[] config = null;
                byte[] cipheredBoxConfig = new byte[48];
                byte[] hashConfigByte = new byte[4];

                switch (mSpinner_Cfg.getSelectedItemPosition())
                {
                    case 25:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0xF2, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF,	//txpow=-270
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 24:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x10, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-240
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 23:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x2E, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-210
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 22:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x4C, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-180
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 21:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x6A, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-150
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 20:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x6F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-145
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 19:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x74, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-140
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 18:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x79, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-135
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 17:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x7E, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-130
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 16:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x83, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-125
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 15:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x88, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-120
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 14:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0xA6, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-90
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 13:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0xC4, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-60
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 12:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0xE2, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-30
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 11:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//txpow=0
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                    case 10:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0xED, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF,	//txpow=-275
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;

                    case 9:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0xF2, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF,	//txpow=-270
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;

                    case 8:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x47, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-185
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;

                    case 7:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x49, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-183
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;

                    case 6:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x50, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//txpow=80
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=3
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;

                    case 5:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x55, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//txpow=85
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;

                    case 4:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0xA6, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-90
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x0A, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=10
                                (byte) 0x2C, (byte) 0x01, (byte) 0x00, (byte) 0x00,	//input_impossible_time=300
                                (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=3
                                (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00,	//conn_tout=257
                                (byte) 0x2C, (byte) 0x01, (byte) 0x00, (byte) 0x00   //password_delete_time=300
                        };
                        break;

                    case 3:
                        config = new byte[]{
                                (byte) 0xFB, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=251
                                (byte) 0xC4, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-60
                                (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=6
                                (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=6
                                (byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=9
                                (byte) 0x14, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//input_impossible_time=20
                                (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=4
                                (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,	//conn_tout=256
                                (byte) 0x1E, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=30
                        };
                        break;

                    case 2:
                        config = new byte[]{
                                (byte) 0xFB, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=251
                                (byte) 0xE2, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-30
                                (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=6
                                (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=6
                                (byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=9
                                (byte) 0x14, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//input_impossible_time=20
                                (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=4
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=0
                                (byte) 0x1E, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=30
                        };
                        break;

                    case 1:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//txpow=0
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;

                    case 0:
                    default:
                        config = new byte[]{
                                (byte) 0xFA, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//interval=250
                                (byte) 0x79, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,	//txpow=-135
                                (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//ch=7
                                (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//attmpt_max=5
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//auto_close=60
                                (byte) 0x84, (byte) 0x03, (byte) 0x00, (byte) 0x00,	//input_impossible_time=900
                                (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//warn_event=1
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00,	//conn_tout=60
                                (byte) 0x3C, (byte) 0x00, (byte) 0x00, (byte) 0x00	//password_delete_time=60
                        };
                        break;
                }


                int hashConfig = 0;
                if (hashConfigByte != null) {
                    Cipher encrypter;
                    IvParameterSpec iv = new IvParameterSpec(mIvKey.get(select_device_no));
                    SecretKeySpec key = new SecretKeySpec(mAesKey.get(select_aes_no), "AES");
                    encrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    encrypter.init(Cipher.ENCRYPT_MODE, key, iv);
                    byte[] cipheredTmp =  encrypter.doFinal(config);
                    for (int i = 0; i < 48; i++) {
                        cipheredBoxConfig[i]=cipheredTmp[i];
                    }

                    byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(config);
                    for (int i = 0; i < 4; i++) {
                        hashConfigByte[i] = sha256[28 + i];
                    }
                    hashConfig = Utility.ToInt32(hashConfigByte, 0);
                }
                isCmdRunning.set(select_device_no, false);
                mTargetDevice.get(select_device_no).box.SetConfiguration(cipheredBoxConfig, hashConfig);
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

                isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class GetDateTimeThread extends Thread {
        public void run()
        {
            try {
                retStringGetDateTime.set(select_device_no, "");
                retGetDateTime.set(select_device_no, mTargetDevice.get(select_device_no).box.GetDateTime());
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            if (null != retGetDateTime.get(select_device_no))
            {
            	retStringGetDateTime.set(select_device_no, retGetDateTime.get(select_device_no).toString());
            }
            else
            {
                retStringGetDateTime.set(select_device_no, "GetDateTime return is null.");
            }
            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class SetDateTimeThread extends Thread {
        public void run()
        {
            try {
                Date dateTime = new Date();

                mTargetDevice.get(select_device_no).box.SetDateTime(dateTime);
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class UpdateFirmwareThread extends Thread {
        public void run()
        {
            try {
                byte[] firmware = null;

                byte[] hashFirmwareByte = new byte[]{
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
                };

                FileInputStream fs = null;
                String path = "/temp/sample.gbl";
                path = mTextView_FirmwareFilePath.getText().toString();
                File f = new File(path);
                int fileSize = (int)f.length();
                firmware = new byte[fileSize];
                FileInputStream fis = new FileInputStream(/*dir +*/ path);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream dis = new DataInputStream(bis);
                dis.read(firmware, 0, fileSize);
                dis.close();

                int hashFirmware = 0;
                if (hashFirmwareByte != null) {
                    hashFirmware = Utility.ToInt32(hashFirmwareByte, 0);
                }

                mProgressDialog.setTitle("Update Firmware");
                mProgressDialog.setMessage("Please Wait..");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCanceledOnTouchOutside(false);

                mProgressDialogHandler.sendEmptyMessage(TRUE);
                mTargetDevice.get(select_device_no).box.UpdateFirmware(firmware,  hashFirmware);
                mProgressDialogHandler.sendEmptyMessage(FALSE);
            } catch (Exception e) {
                mProgressDialogHandler.sendEmptyMessage(FALSE);
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            isUpdating.set(select_device_no, false);
            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class GetBatteryStatusThread extends Thread {
        public void run()
        {
            try {
                retStringGetBatteryStatus.set(select_device_no, "");
                retGetBatteryStatus.set(select_device_no, mTargetDevice.get(select_device_no).box.GetBatteryStatus());
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            retStringGetBatteryStatus.set(select_device_no, retGetBatteryStatus.get(select_device_no) + "%");
            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class GetStatusThread extends Thread {
        public void run()
        {
            try {
                retStringGetStatus.set(select_device_no, "");
                retGetStatus.set(select_device_no, mTargetDevice.get(select_device_no).box.GetStatus());
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            if (null != retStringGetStatus.get(select_device_no))
            {
               retStringGetStatus.set(select_device_no, 
                   "status:isDoorOpen=" + retGetStatus.get(select_device_no).get_isDoorOpen() + "\n" +
                   "status:isLockLock=" + retGetStatus.get(select_device_no).get_isLockLock() + "\n" +
                   "status:isTooMuchEvent=" + retGetStatus.get(select_device_no).get_isTooMuchEvent() + "\n" +
                   "status:isPasswordSet=" + retGetStatus.get(select_device_no).get_isPasswordSet() + "\n"
               );
            }
            else
            {
                retStringGetStatus.set(select_device_no, "GetStatus return is null.");
            }
            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class IsDoorOpenedThread extends Thread {
        public void run()
        {
            try {
                retStringIsDoorOpened.set(select_device_no, "");
                retIsDoorOpened.set(select_device_no, mTargetDevice.get(select_device_no).box.IsDoorOpened());
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            retStringIsDoorOpened.set(select_device_no, retIsDoorOpened.get(select_device_no).toString());
            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class IsLockedThread extends Thread {
        public void run()
        {
            try {
                retStringIsLocked.set(select_device_no, "");
                retIsLocked.set(select_device_no, mTargetDevice.get(select_device_no).box.IsLocked());
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            retStringIsLocked.set(select_device_no, retIsLocked.get(select_device_no).toString());
            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class GetEventsThread extends Thread {
        public void run()
        {
            try {
                Boolean isDeleteEvents = mCheckBox_isDeleteEvents.isChecked();

                retStringGetEvents.set(select_device_no, "");
                retGetEvents.set(select_device_no, mTargetDevice.get(select_device_no).box.GetEvents(isDeleteEvents));
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            if (null != retGetEvents.get(select_device_no))
            {
                Date date = retGetEvents.get(select_device_no).get_lastEventDate();
                if (null != date)
                {
                	retStringGetEvents.set(select_device_no, date.toString());
                }
	            else
	            {
	                retStringGetEvents.set(select_device_no, "GetEvents Date is null.");
	            }
                retStringGetEvents.set(select_device_no, retStringGetEvents.get(select_device_no) + "\n");
                byte[] events = retGetEvents.get(select_device_no).get_events();
                if (null != events)
                {
            retStringGetEvents.set(select_device_no, retStringGetEvents.get(select_device_no) + Utility.toString(events, events.length));
        }
	            else
        {
            retStringGetEvents.set(select_device_no, retStringGetEvents.get(select_device_no) + "GetEvents events is null.");
        }
    }
            else
    {
        retStringGetEvents.set(select_device_no, "GetEvents return is null.");
    }
            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
}
    }

private class DeleteEventsThread extends Thread {
    public void run()
        {
            try {
                retStringDeleteEvents.set(select_device_no, "");
                retDeleteEvents.set(select_device_no, mTargetDevice.get(select_device_no).box.DeleteEvents());
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            if (null != retDeleteEvents.get(select_device_no))
            {
                retStringDeleteEvents.set(select_device_no, retDeleteEvents.toString());
            }
            else
            {
                retStringDeleteEvents.set(select_device_no, "DeleteEvents return is null.");
            }
            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class ResetDeviceThread extends Thread {
        public void run()
        {
            try {
                mTargetDevice.get(select_device_no).box.ResetDevice();
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class SetPasswordThread extends Thread {
        public void run()
        {
            try {
                byte[][] passwordTmp = new byte[10][1];
                byte[][] encryptedPassword = new byte[10][16];
                byte[][] hashPasswordByte = new byte[10][4];

                switch (mSpinner_Password.getSelectedItemPosition())
                {
                    case 7:
                        passwordTmp[0] = new String("").getBytes();
                        passwordTmp[1] = new String("").getBytes();
                        passwordTmp[2] = new String("").getBytes();
                        passwordTmp[3] = new String("").getBytes();
                        passwordTmp[4] = new String("").getBytes();
                        passwordTmp[5] = new String("").getBytes();
                        passwordTmp[6] = new String("").getBytes();
                        passwordTmp[7] = new String("").getBytes();
                        passwordTmp[8] = new String("").getBytes();
                        passwordTmp[9] = new String("").getBytes();
                        break;

                    case 6:
                        passwordTmp[0] = new String("0").getBytes();
                        passwordTmp[1] = new String("").getBytes();
                        passwordTmp[2] = new String("").getBytes();
                        passwordTmp[3] = new String("").getBytes();
                        passwordTmp[4] = new String("").getBytes();
                        passwordTmp[5] = new String("").getBytes();
                        passwordTmp[6] = new String("").getBytes();
                        passwordTmp[7] = new String("").getBytes();
                        passwordTmp[8] = new String("").getBytes();
                        passwordTmp[9] = new String("").getBytes();
                        break;

                    case 5:
                        passwordTmp[0] = new String("0000000000000000").getBytes();
                        passwordTmp[1] = new String("1").getBytes();
                        passwordTmp[2] = new String("2").getBytes();
                        passwordTmp[3] = new String("3").getBytes();
                        passwordTmp[4] = new String("4").getBytes();
                        passwordTmp[5] = new String("5").getBytes();
                        passwordTmp[6] = new String("6").getBytes();
                        passwordTmp[7] = new String("7").getBytes();
                        passwordTmp[8] = new String("8").getBytes();
                        passwordTmp[9] = new String("").getBytes();
                        break;

                    case 4:
                        passwordTmp[0] = new String("0").getBytes();
                        passwordTmp[1] = new String("1").getBytes();
                        passwordTmp[2] = new String("2").getBytes();
                        passwordTmp[3] = new String("3").getBytes();
                        passwordTmp[4] = new String("4").getBytes();
                        passwordTmp[5] = new String("5").getBytes();
                        passwordTmp[6] = new String("6").getBytes();
                        passwordTmp[7] = new String("7").getBytes();
                        passwordTmp[8] = new String("8").getBytes();
                        passwordTmp[9] = new String("").getBytes();
                        break;

                    case 3:
                        passwordTmp[0] = new String("0000000000000000").getBytes();
                        passwordTmp[1] = new String("1111111111111111").getBytes();
                        passwordTmp[2] = new String("2222222222222222").getBytes();
                        passwordTmp[3] = new String("3333333333333333").getBytes();
                        passwordTmp[4] = new String("4444444444444444").getBytes();
                        passwordTmp[5] = new String("5555555555555555").getBytes();
                        passwordTmp[6] = new String("6666666666666666").getBytes();
                        passwordTmp[7] = new String("7777777777777777").getBytes();
                        passwordTmp[8] = new String("8888888888888888").getBytes();
                        passwordTmp[9] = new String("9999999999999999").getBytes();
                        break;

                    case 2:
                        passwordTmp[0] = new String("1234567890123456").getBytes();
                        passwordTmp[1] = new String("123456789012345").getBytes();
                        passwordTmp[2] = new String("12345678901234").getBytes();
                        passwordTmp[3] = new String("1234567890123").getBytes();
                        passwordTmp[4] = new String("123456789012").getBytes();
                        passwordTmp[5] = new String("12345678901").getBytes();
                        passwordTmp[6] = new String("1234567890").getBytes();
                        passwordTmp[7] = new String("123456789").getBytes();
                        passwordTmp[8] = new String("12345678").getBytes();
                        passwordTmp[9] = new String("1234567").getBytes();
                         break;

                    case 1:
                        passwordTmp[0] = new String("0000").getBytes();
                        passwordTmp[1] = new String("1111").getBytes();
                        passwordTmp[2] = new String("2222").getBytes();
                        passwordTmp[3] = new String("3333").getBytes();
                        passwordTmp[4] = new String("4444").getBytes();
                        passwordTmp[5] = new String("5555").getBytes();
                        passwordTmp[6] = new String("6666").getBytes();
                        passwordTmp[7] = new String("7777").getBytes();
                        passwordTmp[8] = new String("8888").getBytes();
                        passwordTmp[9] = new String("9999").getBytes();
                        break;

                    case 0:
                    default:
                        passwordTmp[0] = new String("0").getBytes();
                        passwordTmp[1] = new String("1").getBytes();
                        passwordTmp[2] = new String("2").getBytes();
                        passwordTmp[3] = new String("3").getBytes();
                        passwordTmp[4] = new String("4").getBytes();
                        passwordTmp[5] = new String("5").getBytes();
                        passwordTmp[6] = new String("6").getBytes();
                        passwordTmp[7] = new String("7").getBytes();
                        passwordTmp[8] = new String("8").getBytes();
                        passwordTmp[9] = new String("9").getBytes();
                        break;
                }

                Integer[] hashPassword = new Integer[10];
                if (hashPasswordByte != null) {
                    for (int j = 0; j < 10; j++) {
                        if (Arrays.equals(passwordTmp[j], "".getBytes()))
                        {
                            // Empty password
                            for (int i = 0; i < 16; i++) {
                                encryptedPassword[j][i] = 0;
                            }
                            hashPassword[j] = 0;
                            continue;
                        }

                        byte[] password = new byte[16];
                        for (int i = 0; i < passwordTmp[j].length; i++) {
                            password[i] = passwordTmp[j][i];
                        }
                        Cipher encrypter;
                        IvParameterSpec iv = new IvParameterSpec(mIvKey.get(select_device_no));
                        SecretKeySpec key = new SecretKeySpec(mAesKey.get(select_aes_no), "AES");
                        encrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        encrypter.init(Cipher.ENCRYPT_MODE, key, iv);
                        byte[] cipheredTmp = new byte[32];
                        cipheredTmp = encrypter.doFinal(password);
                        for (int i = 0; i < 16; i++) {
                            encryptedPassword[j][i] = cipheredTmp[i];
                        }

                        byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(passwordTmp[j]);
                        for (int i = 0; i < 4; i++) {
                            hashPasswordByte[j][i] = sha256[28 + i];
                        }
                        hashPassword[j] = Utility.ToInt32(hashPasswordByte[j], 0);
                    }
                }

                mTargetDevice.get(select_device_no).box.SetPassword(encryptedPassword, hashPassword);
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class GetPasswordThread extends Thread {
        public void run()
        {
            try {
                Boolean isDeleteEvents = mCheckBox_isDeleteEvents.isChecked();

                retGetPassword.set(select_device_no, mTargetDevice.get(select_device_no).box.GetPassword());
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            retStringGetPassword.set(select_device_no, "");
            if (null != retGetPassword.get(select_device_no))
            {
                for (int i = 0; i < retGetPassword.get(select_device_no).length; i++)
                {
                    if (retGetPassword.get(select_device_no)[i][0] == '\0')
                    {
                        retStringGetPassword.set(select_device_no, retStringGetPassword.get(select_device_no) + "null");
                    }
                    else
                    {
                        retStringGetPassword.set(select_device_no, retStringGetPassword.get(select_device_no) + Utility.toString(retGetPassword.get(select_device_no)[i], retGetPassword.get(select_device_no)[i].length));
                    }
                    retStringGetPassword.set(select_device_no, retStringGetPassword.get(select_device_no) + "\n");
                }
            }
            else
            {
                retStringGetPassword.set(select_device_no, "GetPassword return is null.");
            }
            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

    private class ChangeKeyThread extends Thread {
        public void run()
        {
            try {
                Cipher encrypter;
                byte[] aeskey = null;
                byte[] cipheredTmp= new byte[32];
                byte[] cipheredKey= new byte[32];

                byte[] hashkeyByte = new byte[]{
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
                };

                FileInputStream fs = null;
                String path = "/temp/sample.dat";
                path = mTextView_ChangeKeyFilePath.getText().toString();
                File f = new File(path);
                int fileSize = (int)f.length();
                aeskey = new byte[fileSize];
                FileInputStream fis = new FileInputStream(/*dir +*/ path);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream dis = new DataInputStream(bis);
                dis.read(aeskey, 0, fileSize);
                dis.close();

                IvParameterSpec iv = new IvParameterSpec(mIvKey.get(select_device_no));
                SecretKeySpec key = new SecretKeySpec(mAesKey.get(select_aes_no), "AES");
                encrypter = Cipher.getInstance("AES/CBC/PKCS5Padding");
                encrypter.init(Cipher.ENCRYPT_MODE, key, iv);
                cipheredTmp = encrypter.doFinal(aeskey);
                for (int i = 0; i < 32; i++) {
                    cipheredKey[i] = cipheredTmp[i];
                }

                byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(aeskey);
                for (int i = 0; i < 4; i++) {
                    hashkeyByte[i] = sha256[28 + i];
                }

                int hashkey = 0;
                if (hashkeyByte != null) {
                    hashkey = Utility.ToInt32(hashkeyByte, 0);
                }

                mTargetDevice.get(select_device_no).box.ChangeKey(cipheredKey,  hashkey);
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                mErrorMessageHandler.sendMessage(msg);
            }

            isUpdating.set(select_device_no, false);
            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }


    public void DoorChangeEventHandler(BoxController sender, StatusEventArgs e)
    {
        if(e.status == true)
        {
            // OPEN
        }
        else
        {
            // Close
        }
    }

    public void LockChangeEventHandler(BoxController sender, StatusEventArgs e)
    {
    	for (int i = 0; i < max_device_num; i++) {
    		if (HardwareDeviceCode.get(i).equals(sender.hardwareDeviceCode)) {
		    	isLockUnknown.set(i, false);
		        if(e.status == true)
		        {
		            // Lock
		            isLocked.set(i, true);
		        	if (select_device_no == i) {
		            	mButtonUnlockEnableChangeHandler.sendEmptyMessage(TRUE);
		        	}
		        }
		        else
		        {
		            // Unlock
		            isLocked.set(i, false);
		        	if (select_device_no == i) {
						mButtonLockEnableChangeHandler.sendEmptyMessage(TRUE);
		        	}
		        }
    		}
    	}
    }

    public void ConnectionChangeEventHandler(BoxController sender, StatusEventArgs e)
    {
    	for (int i = 0; i < max_device_num; i++) {
    		if (HardwareDeviceCode.get(i).equals(sender.hardwareDeviceCode)) {
		        if(e.status == true)
		        {
		            // Disconnect
		            isConnect.set(i, false);
		        	if (select_device_no == i) {
			            mButtonConnectEnableChangeHandler.sendEmptyMessage(TRUE);
		        	}
		        } 
		        else
		        {
		            // None
		        }
		        break;
		    }
    	}
    }

    public void onFileSelect( File file )
    {
        switch (file_select_type) {
            case 0:
                try {
                    FileInputStream fs = null;
                    String path = file.getPath();
                    File f = new File(path);
                    int fileSize = (int)f.length();
                    byte[] tmp = new byte[32];
                    FileInputStream fis = new FileInputStream(/*dir +*/ path);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    DataInputStream dis = new DataInputStream(bis);
                    dis.read(tmp, 0, 32);
                    dis.close();
                    mAesKey.add(tmp);
                    adapterSelectAes.add(f.getName());
                } catch (Exception e) {
                    Message msg = new Message();
                    msg.obj = e.getMessage();
                    mErrorMessageHandler.sendMessage(msg);
                }
                break;
            case 1:
                mTextView_FirmwareFilePath.setText(file.getPath());
                break;
            case 2:
                mTextView_ChangeKeyFilePath.setText(file.getPath());
                break;
            default:
                /* Do Nothing */
        }
    }
}
