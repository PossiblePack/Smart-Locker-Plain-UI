package com.example.demo.libs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.example.demo.MainActivity;
import com.example.demo.libs.Model.BoxException;
import com.example.demo.libs.Model.Utility;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.example.demo.libs.Model.DeviceListActivity.mTargetDevice;

public class JavaServices {
    public static int select_aes_no = 0;
    public static int select_device_no = -1;

    // member
    private ConnectThread           mConnectThread;
    private DisconnectThread        mDisconnectThread;
    private UnlockThread            mUnlockThread;
    private LockThread              mLockThread;
    private GetBatteryStatusThread  mGetBatteryStatusThread;

    // Status
    public static ArrayList<Boolean> isConnect = new ArrayList<>();
    public static ArrayList<Boolean> isLocked = new ArrayList<>();
    public static ArrayList<Boolean> isLockUnknown = new ArrayList<>();
    public static ArrayList<Boolean> isUpdating = new ArrayList<>();
    public static ArrayList<Boolean> isCmdRunning = new ArrayList<>();

    // constant
    private static final int REQUEST_CONNECTDEVICE = 1;
    private static final int TRUE  = 1;
    private static final int FALSE = 0;
    public static ArrayList<String> HardwareDeviceCode = new ArrayList<>();


    // Key
    public static ArrayList<byte[]> mIvKey = new ArrayList<>();
    public static ArrayList<byte[]> mAesKey = new ArrayList<>();

    // Get Data
    public static ArrayList<Integer> retGetBatteryStatus = new ArrayList<>();

    // View Data
    public static ArrayList<String> retStringGetBatteryStatus = new ArrayList<>();

    // Progress Dialog
    public ProgressDialog mProgressDialog;

    // Handler
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
    public void onClick( View v )
    {
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
    }

    private void buttonDisableAll()
    {
        mButton_Connect.setEnabled( false );
        mButton_Disconnect.setEnabled( false );
        mButton_Unlock.setEnabled( false );
        mButton_Lock.setEnabled( false );
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

            mButton_GetBatteryStatus.setEnabled( true );
            mTextView_retGetBatteryStatus.setText(retStringGetBatteryStatus.get(select_device_no));
//            mButton_GetStatus.setEnabled( true );
//            mTextView_retGetStatus.setText(retStringGetStatus.get(select_device_no));
//            mButton_IsDoorOpened.setEnabled( true );
//            mTextView_retIsDoorOpened.setText(retStringIsDoorOpened.get(select_device_no));
//            mButton_IsLocked.setEnabled( true );
//            mTextView_retIsLocked.setText(retStringIsLocked.get(select_device_no));
//            mButton_GetEvents.setEnabled( true );
//            mTextView_retGetEvents.setText(retStringGetEvents.get(select_device_no));
//            mButton_DeleteEvents.setEnabled( true );
//            mTextView_retDeleteEvents.setText(retStringDeleteEvents.get(select_device_no));
//            mButton_ResetDevice.setEnabled( true );
//            mButton_SetPassword.setEnabled( true );
//            mButton_GetPassword.setEnabled( true );
//            mTextView_retGetPassword.setText(retStringGetPassword.get(select_device_no));
//            mButton_ChangeKey.setEnabled( true );
        }
        else
        {
            mButton_Connect.setEnabled( true );
            mButton_Disconnect.setEnabled( true );
            mButton_Unlock.setEnabled( false );
            mButton_Lock.setEnabled( false );
//            mButton_GetConfiguration.setEnabled( false );
//            mTextView_retGetConfiguration.setText("");
//            mButton_SetConfiguration.setEnabled( false );
//            mButton_GetDateTime.setEnabled( false );
//            mTextView_retGetDateTime.setText("");
//            mButton_SetDateTime.setEnabled( false );
//            mButton_UpdateFirmware.setEnabled( false );
//            mButton_GetBatteryStatus.setEnabled( false );
//            mTextView_retGetBatteryStatus.setText("");
//            mButton_GetStatus.setEnabled( false );
//            mTextView_retGetStatus.setText("");
//            mButton_IsDoorOpened.setEnabled( false );
//            mTextView_retIsDoorOpened.setText("");
//            mButton_IsLocked.setEnabled( false );
//            mTextView_retIsLocked.setText("");
//            mButton_GetEvents.setEnabled( false );
//            mTextView_retGetEvents.setText("");
//            mButton_DeleteEvents.setEnabled( false );
//            mTextView_retDeleteEvents.setText("");
//            mButton_ResetDevice.setEnabled( false );
//            mButton_SetPassword.setEnabled( false );
//            mButton_GetPassword.setEnabled( false );
//            mTextView_retGetPassword.setText("");
//            mButton_ChangeKey.setEnabled( false );
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

    private void getbatterystatus()
    {
        isCmdRunning.set(select_device_no, true);

        mGetBatteryStatusThread = new GetBatteryStatusThread();
        mGetBatteryStatusThread.start();
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
                MainActivity.mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                MainActivity.mErrorMessageHandler.sendMessage(msg);
            }

            retStringGetBatteryStatus.set(select_device_no, retGetBatteryStatus.get(select_device_no) + "%");
            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
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
                MainActivity.mErrorMessageHandler.sendMessage(msg);
                isConnect.set(select_device_no, false);
                isCmdRunning.set(select_device_no, false);
                mButtonConnectEnableChangeHandler.sendEmptyMessage(TRUE);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                MainActivity.mErrorMessageHandler.sendMessage(msg);
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
                MainActivity.mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                MainActivity.mErrorMessageHandler.sendMessage(msg);
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
                passwordTmp[0] = new String("0").getBytes();


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
                MainActivity.mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                isLockUnknown.set(select_device_no, true);
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                MainActivity.mErrorMessageHandler.sendMessage(msg);
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
                MainActivity.mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                isLockUnknown.set(select_device_no, true);
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                MainActivity.mErrorMessageHandler.sendMessage(msg);
            }

            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }

}
