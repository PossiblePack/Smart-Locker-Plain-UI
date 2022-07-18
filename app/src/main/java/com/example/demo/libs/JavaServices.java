package com.example.demo.libs;


import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;

import com.example.demo.libs.Model.BoxException;
import com.example.demo.libs.Model.BoxManager;
import com.example.demo.libs.Model.DiscoverEventArgs;
import com.example.demo.libs.Model.Utility;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class JavaServices {
    public static int select_aes_no = 0;
    public static int select_device_no = -1;

    // member
    private ConnectThread           mConnectThread;
    private DisconnectThread        mDisconnectThread;
    private UnlockThread            mUnlockThread;
    private LockThread              mLockThread;
    private GetBatteryStatusThread  mGetBatteryStatusThread;

    //private DeviceListAdapter mDeviceListAdapter;
    private boolean mScanning = false;

    private BoxManager mBoxManager;

    private ScanThread mScanThread;

    public static ArrayList<DiscoverEventArgs> mTargetDevice = new ArrayList<>();



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
    public static final  String EXTRAS_DEVICE_ADDRESS   = "DEVICE_ADDRESS";
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
            //buttonUpdateAll();
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
            //buttonUpdateAll();
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
            //buttonUpdateAll();
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
            //buttonUpdateAll();
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
            //buttonUpdateAll();
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

    private class ScanThread extends Thread {
        public void run()
        {
            try {
                mBoxManager.StartScanBoxControllers();
            } catch (BoxException e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                //MainActivity.mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                //MainActivity.mErrorMessageHandler.sendMessage(msg);
            }
        }
    }

    private void connect()
    {
        if( select_device_no < 0 )
        {
            return;
        }

        isConnect.set(select_device_no, false);
        //btnLock.setEnabled( false );
        isCmdRunning.set(select_device_no, true);

        mConnectThread = new ConnectThread();
        mConnectThread.start();
    }

    private void disconnect()
    {
        if( (select_device_no >= HardwareDeviceCode.size())  )
        {
            return;
        }

        isConnect.set(select_device_no, false);
        isCmdRunning.set(select_device_no, true);

        mDisconnectThread = new DisconnectThread();
        mDisconnectThread.start();
    }

    private void unlock()
    {
        isCmdRunning.set(select_device_no, true);

        mUnlockThread = new UnlockThread();
        mUnlockThread.start();
    }

    private void lock()
    {
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
                //MainActivity.mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                //MainActivity.mErrorMessageHandler.sendMessage(msg);
            }

            retStringGetBatteryStatus.set(select_device_no, retGetBatteryStatus.get(select_device_no) + "%");
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
                passwordTmp[1] = new String("1").getBytes();
                passwordTmp[2] = new String("2").getBytes();
                passwordTmp[3] = new String("3").getBytes();
                passwordTmp[4] = new String("4").getBytes();
                passwordTmp[5] = new String("5").getBytes();
                passwordTmp[6] = new String("6").getBytes();
                passwordTmp[7] = new String("7").getBytes();
                passwordTmp[8] = new String("8").getBytes();
                passwordTmp[9] = new String("9").getBytes();
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
                //MainActivity.mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                isLockUnknown.set(select_device_no, true);
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                //MainActivity.mErrorMessageHandler.sendMessage(msg);
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
                //MainActivity.mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                isLockUnknown.set(select_device_no, true);
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                //MainActivity.mErrorMessageHandler.sendMessage(msg);
            }

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
                //MainActivity.mErrorMessageHandler.sendMessage(msg);
                isConnect.set(select_device_no, false);
                isCmdRunning.set(select_device_no, false);
                mButtonConnectEnableChangeHandler.sendEmptyMessage(TRUE);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                //MainActivity.mErrorMessageHandler.sendMessage(msg);
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
                //MainActivity.mErrorMessageHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = new Message();
                msg.obj = e.getMessage();
                isCmdRunning.set(select_device_no, false);
                //MainActivity.mErrorMessageHandler.sendMessage(msg);
            }

            isConnect.set(select_device_no, false);
            isCmdRunning.set(select_device_no, false);
            mViewUpdateHandler.sendEmptyMessage(TRUE);
        }
    }


}

