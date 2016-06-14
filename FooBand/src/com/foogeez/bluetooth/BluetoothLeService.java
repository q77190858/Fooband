package com.foogeez.bluetooth;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.UUID;

import com.grdn.util.Utils;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class BluetoothLeService extends Service {
	private final static String TAG = BluetoothLeService.class.getSimpleName();

	private static boolean initialized = false;

	private static BluetoothManager mBluetoothManager = null;
	private static BluetoothAdapter mBluetoothAdapter = null;
	private static BluetoothGatt mBluetoothGatt = null;
	// private static String mDeviceAddress;

	private int mConnectionState = STATE_DISCONNECTED;
	// private int mRequestedLeState = STATE_DISCONNECTED;
	private static final int STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED;
	private static final int STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;
	private static final int STATE_DISCONNECTING = BluetoothProfile.STATE_DISCONNECTING;
	private static final int STATE_CONNECTING = BluetoothProfile.STATE_CONNECTING;

	private boolean mDiscoveredFlag = false;
	private int mDiscoveredStatus = 0;

	public final static String ACTION_GATT_CONNECTED = "com.foogeez.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.foogeez.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.foogeez.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.foogeez.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.foogeez.bluetooth.le.EXTRA_DATA";
	public final static String ACTION_DATA_WRITE = "com.foogeez.bluetooth.le.ACTION_DATA_WRITE";

	/** Robin -----20151203-----连接超时 */
	public static final String ACTION_ACTIONS_SERVICE_GATT_ERROR = "com.foogeez.services.CentralService.ACTION_ACTIONS_SERVICE_GATT_ERROR";

	public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID
			.fromString(BluetoothLeGattAttributes.HEART_RATE_MEASUREMENT);

	private Object mLock = new Object();

	public final static int DEVICE_ACCESSIBLE_MODE_UNKNOW = 0;
	public final static int DEVICE_ACCESSIBLE_MODE_NORMAL = 1;
	public final static int DEVICE_ACCESSIBLE_MODE_DFU = 2;
	private int mDataAccessibleMode = DEVICE_ACCESSIBLE_MODE_UNKNOW;

	private BluetoothGattCharacteristic mBandDfuCtrlPoint = null;
	private BluetoothGattCharacteristic mBandBatteryValue = null;
	private BluetoothGattCharacteristic mBandSportDatumValue = null;
	private BluetoothGattCharacteristic mBandConfigAddrValue = null;
	private BluetoothGattCharacteristic mBandConfigDataValue = null;

	private OpThread mBluetoothIoThread = null;
	private BluetoothLeOpCallback cmdCallback = null;

	public static int BLUETOOTH_LE_REFRESH_TYPE_IDLE = 0;
	public static int BLUETOOTH_LE_REFRESH_TYPE_SCAN = 1;
	public static int BLUETOOTH_LE_REFRESH_TYPE_CONNECT = 2;
	public static int BLUETOOTH_LE_REFRESH_TYPE_CONFIG = 3;
	public static int BLUETOOTH_LE_REFRESH_TYPE_ACTIONS_DATUM = 4;
	public static int BLUETOOTH_LE_REFRESH_TYPE_BATTERY_LEVLE = 5;
	public static int BLUETOOTH_LE_REFRESH_TYPE_DFU_CONTRL_POINT = 6;
	private int mBluetoothLeRefreshType = BLUETOOTH_LE_REFRESH_TYPE_IDLE;
	
	private SharedPreferences share;

	public int getBluetoothLeRefreshType() {
		return mBluetoothLeRefreshType;
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "BluetoothLeService --- onCreate");
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "BluetoothLeService --- onDestroy");
	}

	private int initBluetoothIo() {
		mBandBatteryValue = null;
		mBandDfuCtrlPoint = null;
		mBandSportDatumValue = null;
		mBandConfigAddrValue = null;
		mBandConfigDataValue = null;
		mDataAccessibleMode = DEVICE_ACCESSIBLE_MODE_UNKNOW;
		for (BluetoothGattService service : getSupportedGattServices()) {
			for (BluetoothGattCharacteristic characteristic : service
					.getCharacteristics()) {
				if (characteristic.getUuid().toString()
						.equals(BluetoothLeGattAttributes.DEVICE_BATTERY_VALUE)) {
					mBandBatteryValue = characteristic;
				} else if (characteristic.getUuid().toString()
						.equals(BluetoothLeGattAttributes.DEVICE_DATA_VALUE)) {
					mBandSportDatumValue = characteristic;
				} else if (characteristic.getUuid().toString()
						.equals(BluetoothLeGattAttributes.DEVICE_CONFIG_ADDR)) {
					mBandConfigAddrValue = characteristic;
				} else if (characteristic.getUuid().toString()
						.equals(BluetoothLeGattAttributes.DEVICE_CONFIG_DATA)) {
					mBandConfigDataValue = characteristic;
				} else if (characteristic.getUuid().toString()
						.equals(BluetoothLeGattAttributes.DEVICE_DFU_CTRL_PNT)) {
					mBandDfuCtrlPoint = characteristic;
				}
			}
		}

		if ((mBandBatteryValue != null) && (mBandSportDatumValue != null)
				&& (mBandConfigAddrValue != null)
				&& (mBandConfigDataValue != null)) {
			Log.i(TAG, "mIsDataAvailable == true!!!");
			mDataAccessibleMode = DEVICE_ACCESSIBLE_MODE_NORMAL;
			return mDataAccessibleMode;
		} else if (mBandDfuCtrlPoint != null) {
			mDataAccessibleMode = DEVICE_ACCESSIBLE_MODE_DFU;
			return mDataAccessibleMode;
		} else {
			mDataAccessibleMode = DEVICE_ACCESSIBLE_MODE_UNKNOW;
			return mDataAccessibleMode;
		}
	}

	private static final int OP_CODE_RECEIVE_ACTIVATE_AND_RESET_KEY = 0x05; // 5
	private static final byte[] OP_CODE_ACTIVATE_AND_RESET = new byte[] { OP_CODE_RECEIVE_ACTIVATE_AND_RESET_KEY };

	public void writeDfuReset() {
		if ((mBandDfuCtrlPoint != null) && (mBluetoothGatt != null)) {
			mBandDfuCtrlPoint.setValue(OP_CODE_ACTIVATE_AND_RESET);
			mBluetoothGatt.writeCharacteristic(mBandDfuCtrlPoint);
		}
	}

	public int getDataAccessibleMode() {
		return mDataAccessibleMode;
	}

	/**
     * 
     * 
     * 
     */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			mDiscoveredFlag = false;
			mConnectionState = newState;
			if (status != BluetoothGatt.GATT_SUCCESS) {
				mConnectionState = STATE_DISCONNECTED;
				cmdCallback.onBluetoothLeConnectionStateChange(status,
						STATE_DISCONNECTED);
				if (mBluetoothIoThread != null) {
					mBluetoothIoThread.cleanCommands();
				}
				abortConnect();
				refreshDeviceCache();
				close();
			} else {
				cmdCallback
						.onBluetoothLeConnectionStateChange(status, newState);
				if (newState == STATE_CONNECTED) {
					if (clrCacheBeforeDiscovery) {
						refreshDeviceCache();
					}
					discoverServices();
				} else if (newState == STATE_DISCONNECTED) {
					close();
				}
			}

			// notify waiting thread
			synchronized (mLock) {
				mLock.notifyAll();
			}

			// if( cmdCallback != null ) {
			// cmdCallback.onBluetoothLeConnectionStateChange(status, newState);
			// }
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status != BluetoothGatt.GATT_SUCCESS) {
				if (mBluetoothIoThread != null) {
					mBluetoothIoThread.cleanCommands();
				}
				// close();
			}

			initBluetoothIo();
			mDiscoveredFlag = true;
			mDiscoveredStatus = status;
			synchronized (mLock) {
				mLock.notifyAll();
			}

			// if( (mDataAccessibleMode !=
			// DEVICE_ACCESSIBLE_MODE_NORMAL)||(status !=
			// BluetoothGatt.GATT_SUCCESS) ) {
			// if( status == BluetoothGatt.GATT_SUCCESS ) mDiscoveredStatus =
			// 997;
			// refreshDeviceCache();
			// }
			if (mDataAccessibleMode == DEVICE_ACCESSIBLE_MODE_DFU) {
				refreshDeviceCache();
			}

			cmdCallback.onBluetoothLeServicesDiscovered(mDiscoveredStatus);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			cmdCallback.onBluetoothLeCharacteristicRead(status, characteristic);
			if (status != BluetoothGatt.GATT_SUCCESS) {
				if (mBluetoothIoThread != null) {
					mBluetoothIoThread.cleanCommands();
				}
				refreshDeviceCache();
				return;
			}

			byte[] recvdata = characteristic.getValue();
			Log.e(TAG,
					"["
							+ recvdata.length
							+ "]recvdata: "
							+ ((recvdata == null) ? "null" : Utils
									.bytesToHexString(recvdata)));

			if (characteristic.getUuid().toString()
					.equals(BluetoothLeGattAttributes.DEVICE_DATA_VALUE)) {
				synchronized (mBluetoothIoThread) {
					List<Token> results = mBluetoothIoThread.getCommandResult();
					Token token = results.get(results.size() - 1);
					token.setData(recvdata);
					mBluetoothIoThread.notify();
					Log.i(TAG,
							"Data[] = "
									+ Utils.bytesToHexString(token.getData()));
				}
			} else if (characteristic.getUuid().toString()
					.equals(BluetoothLeGattAttributes.DEVICE_CONFIG_DATA)) {
				synchronized (mBluetoothIoThread) {
					List<Token> results = mBluetoothIoThread.getCommandResult();
					Token token = results.get(results.size() - 1);
					token.setData(recvdata);
					mBluetoothIoThread.notify();
					Log.i(TAG,
							"Data[0x" + Utils.bytesToHexString(token.getAddr())
									+ "] = "
									+ Utils.bytesToHexString(token.getData()));
				}
			} else if (characteristic.getUuid().toString()
					.equals(BluetoothLeGattAttributes.DEVICE_BATTERY_VALUE)) {
				synchronized (mBluetoothIoThread) {
					List<Token> results = mBluetoothIoThread.getCommandResult();
					Token token = results.get(results.size() - 1);
					token.setData(recvdata);
					mBluetoothIoThread.notify();
					Log.i(TAG,
							"Battery Level = "
									+ Utils.bytesToHexString(token.getData()));
				}
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			cmdCallback
					.onBluetoothLeCharacteristicWrite(status, characteristic);
			if (status != BluetoothGatt.GATT_SUCCESS) {
				if (mBluetoothIoThread != null) {
					mBluetoothIoThread.cleanCommands();
				}
				refreshDeviceCache();
				return;
			}

			if (characteristic.getUuid().toString()
					.equals(BluetoothLeGattAttributes.DEVICE_CONFIG_ADDR)) {
				synchronized (mBluetoothIoThread) {
					mBluetoothIoThread.notify();
				}
			}
			if (characteristic.getUuid().toString()
					.equals(BluetoothLeGattAttributes.DEVICE_CONFIG_DATA)) {
				synchronized (mBluetoothIoThread) {
					mBluetoothIoThread.notify();
				}
			}
			if (characteristic.getUuid().toString()
					.equals(BluetoothLeGattAttributes.DEVICE_DATA_VALUE)) {
				synchronized (mBluetoothIoThread) {
					mBluetoothIoThread.notify();
				}
			}

		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			cmdCallback.onBluetoothLeCharacteristicChanged(characteristic);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			cmdCallback.onBluetoothLeDescriptorWrite(status, descriptor);
			if (status != BluetoothGatt.GATT_SUCCESS) {
				if (mBluetoothIoThread != null) {
					mBluetoothIoThread.cleanCommands();
				}
				refreshDeviceCache();
				return;
			}
			if (descriptor.getCharacteristic().getUuid().toString()
					.equals(BluetoothLeGattAttributes.DEVICE_CONFIG_DATA)) {
				synchronized (mBluetoothIoThread) {
					mBluetoothIoThread.notify();
				}
			} else if (descriptor.getCharacteristic().getUuid().toString()
					.equals(BluetoothLeGattAttributes.DEVICE_DATA_VALUE)) {
				synchronized (mBluetoothIoThread) {
					mBluetoothIoThread.notify();
				}
			}
		}
	};

	/**
     * 
     * 
     * 
     **/
	public final static int OP_TYPE_UNKNOW = 0;
	public final static int OP_TYPE_CONNECT = 1;
	public final static int OP_TYPE_DISCONNECT = 2;
	public final static int OP_TYPE_READ_CONFIG = 3;
	public final static int OP_TYPE_WRITE_CONFIG = 4;
	public final static int OP_TYPE_WRITE_DATUM_CLIENT_CONFIG = 5;
	public final static int OP_TYPE_WRITE_CONFIG_CLIENT_CONFIG = 6;
	public final static int OP_TYPE_READ_BATTERY_LEVEL = 7;
	public final static int OP_TYPE_WRITE_DFU_CONTRL_POINT = 8;

	public final static int OP_TYPE_READ_DATUM = 0x10;
	public final static int OP_TYPE_READ_DATUM_ALL = OP_TYPE_READ_DATUM | 0x01;
	public final static int OP_TYPE_READ_DATUM_TODAY = OP_TYPE_READ_DATUM | 0x02;
	public final static int OP_TYPE_READ_DATUM_RECENT = OP_TYPE_READ_DATUM | 0x03;
	public final static int OP_TYPE_WRITE_DATUM = 0x20;

	public class Token {
		private int OpType = OP_TYPE_UNKNOW;
		private byte[] OpAddr = null;
		private byte[] OpData = null;

		public Token(int type, byte[] addr, byte[] data) {
			OpType = type;
			if (addr != null) {
				OpAddr = new byte[addr.length];
				for (int i = 0; i < addr.length; i++) {
					OpAddr[i] = addr[i];
				}
			}
			if (data != null) {
				OpData = new byte[data.length];
				for (int i = 0; i < data.length; i++) {
					OpData[i] = data[i];
				}
			}
		}

		public int getType() {
			return OpType;
		}

		public byte[] getAddr() {
			return OpAddr;
		}

		public byte[] getData() {
			return OpData;
		}

		public void setData(byte[] data) {
			if (data != null) {
				OpData = new byte[data.length];
				for (int i = 0; i < data.length; i++) {
					OpData[i] = data[i];
				}
			}
		}
	}

	class OpThread extends Thread {
		private boolean isBusy = false;
		private Queue<Token> cmdTokens = new LinkedList<Token>();
		private List<Token> cmdResults = new ArrayList<Token>();

		public OpThread(BluetoothLeOpCallback callback) {
			super();
			cmdCallback = callback;
		}

		public boolean addCommand(Token token) {
			boolean result = cmdTokens.add(token);
			if (!isBusy) {
				synchronized (this) {
					this.notifyAll();
				}
			}
			return result;
		}

		public boolean addCommand(Token token, boolean autodo) {
			boolean result = cmdTokens.add(token);
			if (autodo) {
				if (!isBusy) {
					synchronized (this) {
						this.notifyAll();
					}
				}
			}
			return result;
		}

		public void cleanCommands() {
			isBusy = false;
			cmdTokens.clear();
			synchronized (this) {
				this.notifyAll();
			}
			synchronized (this) {
				this.notifyAll();
			}
			synchronized (this) {
				this.notifyAll();
			}
		}

		public boolean isBusy() {
			return isBusy;
		}

		public List<Token> getCommandResult() {
			return cmdResults;
		}

		@Override
		public void run() {
			Token token = null;
			while (true) {
				synchronized (this) {
					while (cmdTokens.size() == 0) {
						if (isBusy) {
							isBusy = false;
							cmdCallback.onBluetoothLeOpCompleteAll(token);
							cmdResults.clear();
							token = null;
						}
						try {
							this.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					isBusy = true;
					/** Name:Robin  Time:20151208  Function: java.util.NoSuchElementException*/
//					token = cmdTokens.remove();
					try {
						token = cmdTokens.remove();
					} catch (NoSuchElementException e) {
						e.printStackTrace();
						Log.i(TAG, "Robin --- 抛出异常  NoSuchElementException");
					}
					cmdResults.add(token);

					cmdCallback.onBluetoothLeOpStart(token);

					if (OP_TYPE_CONNECT == token.OpType) {
						try {
							this.wait();
							if (!isBusy)
								continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else if (OP_TYPE_DISCONNECT == token.OpType) {
						try {
							this.wait();
							if (!isBusy)
								continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else if (OP_TYPE_READ_CONFIG == token.OpType) {
						writeConfigAddr(token.getAddr());
						Log.i(TAG, "readConfigValue --- Do writeConfigAddr");

						try {
							this.wait();
							if (!isBusy)
								continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						readConfigData();
						Log.i(TAG, "readConfigValue --- Do readConfigData");

						try {
							this.wait();
							if (!isBusy)
								continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						Log.e(TAG, "readConfigValue --- Complete!!!");
					} else if (OP_TYPE_WRITE_CONFIG == token.OpType) {
						writeConfigAddr(token.getAddr());
						Log.i(TAG, "writeConfigValue --- Do writeConfigAddr");

						try {
							this.wait();
							if (!isBusy)
								continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						writeConfigData(token.getData());
						Log.i(TAG, "writeConfigValue --- Do writeConfigData");

						try {
							this.wait();
							if (!isBusy)
								continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Log.e(TAG, "writeConfigValue --- Complete!!!");
					} else if (OP_TYPE_READ_DATUM == (token.OpType & OP_TYPE_READ_DATUM)) {
						readDatum();
						Log.i(TAG, "readDatumValue --- Do readDatum");

						try {
							this.wait();
							if (!isBusy)
								continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Log.e(TAG, "readDatumValue --- complete!!!");
					} else if (OP_TYPE_READ_BATTERY_LEVEL == token.OpType) {
						readBatteryLevel();
						Log.i(TAG, "readBatteryLevel --- Do readBatteryLevel");

						try {
							this.wait();
							if (!isBusy)
								continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Log.e(TAG, "readBatteryLevel --- complete!!!");
					} else if (OP_TYPE_WRITE_CONFIG_CLIENT_CONFIG == token.OpType) {
						enableNotification(mBandConfigDataValue);
						try {
							this.wait();
							if (!isBusy)
								continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Log.e(TAG, "enableNotification --- complete!!!");
					} else if (OP_TYPE_WRITE_DATUM_CLIENT_CONFIG == token.OpType) {
						enableNotification(mBandSportDatumValue);
						try {
							this.wait();
							if (!isBusy)
								continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Log.e(TAG, "enableNotification --- complete!!!");
					} else if (OP_TYPE_WRITE_DATUM == token.OpType) {
						writeDatum(token.OpData);
						try {
							this.wait();
							if (!isBusy)
								continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Log.e(TAG, "writeDatum --- complete!!!");
					}

					cmdCallback.onBluetoothLeOpComplete(token);
				}
			}
		}
	}

	public boolean BluetoothLeIsBusy() {
		if (mBluetoothIoThread != null) {
			return mBluetoothIoThread.isBusy();
		}
		return false;
	}

	public interface BluetoothLeOpCallback {
		public void onBluetoothLeOpStart(Token token);

		public void onBluetoothLeOpComplete(Token token);

		public void onBluetoothLeOpCompleteAll(Token token);

		public void onBluetoothLeConnectionStateChange(int status, int newState);

		public void onBluetoothLeServicesDiscovered(int status);

		public void onBluetoothLeCharacteristicRead(int status,
				BluetoothGattCharacteristic characteristic);

		public void onBluetoothLeCharacteristicWrite(int status,
				BluetoothGattCharacteristic characteristic);

		public void onBluetoothLeCharacteristicChanged(
				BluetoothGattCharacteristic characteristic);

		public void onBluetoothLeDescriptorWrite(int status,
				BluetoothGattDescriptor descriptor);
	}

	public class LocalBinder extends Binder {
		public BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mIBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.e(TAG, "BluetoothLeService --- onUnbind");
		// After using a given device, you should make sure that
		// BluetoothGatt.close() is called
		// such that resources are cleaned up properly. In this particular
		// example, close() is
		// invoked when the UI is disconnected from the Service.
		return super.onUnbind(intent);
	}

	private final IBinder mIBinder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 * 
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (initialized == false) {
			initialized = true;
		}

		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		mBluetoothAdapter = mBluetoothManager.getAdapter();
		Log.i(TAG, "mBluetoothAdapter = " + mBluetoothAdapter);
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		// setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 3);

		mBluetoothIoThread = new OpThread(cmdCallback);
		mBluetoothIoThread.start();

		return true;
	}

	public boolean isConnectedDevice(String addr) {
		List<BluetoothDevice> nConnectedDevices = mBluetoothManager
				.getConnectedDevices(BluetoothProfile.GATT);
		for (BluetoothDevice device : nConnectedDevices) {
			Log.e(TAG, "CONNECTED GATT DEVICE:" + device.getAddress());
			if (device.getAddress().equals(addr))
				return true;
		}
		return false;
	}

	public BluetoothManager getBluetoothManager() {
		return mBluetoothManager;
	}

	public BluetoothAdapter getBluetoothAdapter() {
		return mBluetoothAdapter;
	}

	public BluetoothAdapter refreshBluetoothAdapter() {
		mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		return mBluetoothAdapter;
	}

	public List<BluetoothDevice> getDevicesByStates(int[] states) {
		return mBluetoothManager.getDevicesMatchingConnectionStates(
				BluetoothProfile.GATT, states);
	}

	public int getConnectionStates() {
		if (mBluetoothGatt == null) {
			mConnectionState = STATE_DISCONNECTED;
		}
		return mConnectionState;
	}

	/**
	 * 通过GATT服务连接BLE设备.
	 * 
	 * @param address
	 *            The device address of the destination device.
	 * 
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */

	private static boolean timeout = false;
	private static boolean abort = false;

	/** Robin -----20151204----- 中止连接 -- */
	public void abortConnect() {
		synchronized (mLock) {
			abort = true;
			mLock.notifyAll();
		}
	}

	public boolean connect(final String address,
			final BluetoothLeOpCallback callback) {
		Log.i(TAG, "connect to ---" + address);
		Log.i(TAG, "Robin----connect to ---第一111111111111次连接--");
		cmdCallback = callback;

		if (mBluetoothAdapter == null || address == null) {
			Log.e(TAG,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		if (callback == null) {
			Log.e(TAG, "BluetoothLeOpCallback callback not initialized");
			return false;
		}

		abort = false;
		timeout = false;
		mDiscoveredFlag = false;
		mConnectionState = STATE_CONNECTING;

		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		if (mBluetoothGatt == null) {
			Log.e(TAG, "mBluetoothGatt == null");
			return false;
		}

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				timeout = true;
				synchronized (mLock) {
					mLock.notifyAll();
				}
			}
		};

		Handler handler = new Handler(BluetoothLeService.this.getMainLooper());
		handler.postDelayed(runnable, 10000);
		/** Robin -----20151203-----延迟的时间 */

		try {
			synchronized (mLock) {
				while ((!mDiscoveredFlag) && (!timeout) && (!abort)) {
					mLock.wait();
				}

				if (timeout == false) {
					handler.removeCallbacks(runnable);
				}
			}
		} catch (final InterruptedException e) {
			Log.e(TAG, "Sleeping interrupted " + e);
		}

		Log.e(TAG, "mDiscoveredFlag=" + mDiscoveredFlag + " ,timeout="
				+ timeout + " ,abort=" + abort);
		Log.e(TAG, "CONNECT REQUEST RETURN!");
		
		if (((!mDiscoveredFlag) || (mDiscoveredStatus != BluetoothGatt.GATT_SUCCESS))
				|| timeout || abort) {
			mConnectionState = STATE_DISCONNECTED;
			disconnect();
			close();
			return false;
		}
		return true;
	}

	/**/
	private boolean clrCacheBeforeDiscovery = false;

	public boolean connect(final BluetoothDevice device,
			final BluetoothLeOpCallback callback, boolean clrCacheFlag) {
		Log.i(TAG, "connect to ---" + device.getAddress());
		Log.i(TAG, "Robin----connect to ---第22222222222次连接--");
		cmdCallback = callback;

		if (mBluetoothAdapter == null || device == null) {
			Log.e(TAG,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		if (callback == null) {
			Log.e(TAG, "BluetoothLeOpCallback callback not initialized");
			return false;
		}

		abort = false;
		timeout = false;
		mDiscoveredFlag = false;
		mConnectionState = STATE_CONNECTING;

		clrCacheBeforeDiscovery = clrCacheFlag;
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		if (mBluetoothGatt == null) {
			Log.e(TAG, "mBluetoothGatt == null");
			return false;
		}

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				timeout = true;
				synchronized (mLock) {
					mLock.notifyAll();
				}
			}
		};

		Handler handler = new Handler(BluetoothLeService.this.getMainLooper());
		handler.postDelayed(runnable, 10000);
		/** Robin -----20151203-----延迟的时间 */

		try {
			synchronized (mLock) {
				while ((!mDiscoveredFlag) && (!timeout) && (!abort)) {
					mLock.wait();
				}

				if (timeout == false) {
					handler.removeCallbacks(runnable);
				}
			}
		} catch (final InterruptedException e) {
			Log.e(TAG, "Sleeping interrupted " + e);
		}

		Log.i(TAG, "mDiscoveredFlag=" + mDiscoveredFlag + " ,timeout="
				+ timeout + " ,abort=" + abort);
		Log.i(TAG, "CONNECT REQUEST RETURN!");
		
		if (((!mDiscoveredFlag) || (mDiscoveredStatus != BluetoothGatt.GATT_SUCCESS))
				|| timeout || abort) {
			mConnectionState = STATE_DISCONNECTED;
			disconnect();
			close();
			return false;
		}
		return true;
	}

	/**/

	/**
	 * 取消等待连接断开一个现有的连接 Disconnects an existing connection or cancel a pending
	 * connection. The disconnection result is reported asynchronously through
	 * the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect() {
		Log.i(TAG, "Robin--------disconnect()------------");
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}

		if (mConnectionState == STATE_CONNECTING) {
			abortConnect();
		}

		mBluetoothLeRefreshType = BLUETOOTH_LE_REFRESH_TYPE_IDLE;
		mConnectionState = STATE_DISCONNECTING;
		// mRequestedLeState = BluetoothProfile.STATE_DISCONNECTED;
		mBluetoothGatt.disconnect();

		/**
		 * if( mDiscoveredFlag ) { Log.i(TAG, "Wait()..."); try { synchronized
		 * (mLock) { while ( mConnectionState == STATE_DISCONNECTING )
		 * mLock.wait(); } } catch (final InterruptedException e) { Log.e(TAG,
		 * "Sleeping interrupted " + e); } Log.i(TAG, "disconnect... normal"); }
		 * /
		 **/
		// else {
		mConnectionState = STATE_DISCONNECTED;
		// Log.e(TAG, "disconnect... abnormal");
		// }
		 share = getSharedPreferences("record", Context.MODE_PRIVATE);
		 Log.i(TAG, "Robin---------share"+share);
		 int rid = share.getInt("recordID", 0);	
		 Log.i(TAG, "Robin---------"+rid);
		 int record = 11010;
		 if (rid == record) {			
			Editor editor = share.edit();
			editor.remove("recordID");
			editor.commit();// 提交修改
		    Log.i(TAG, "Robin---------editor.removerecordID--不发送广播");
		 }else {
			 /** Robin -----20151203-----发送连接超时的广播 */
			 Log.i(TAG, "Robin---------发送连接超时的广播 ");
			 broadcastUpdate(ACTION_ACTIONS_SERVICE_GATT_ERROR);
				
		}				
		Log.i(TAG, "DISCONNECT REQUEST RETURN!");
	}

	/** Robin -----20151203-----发送广播的方法 */
	public void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}
	
	/*
	 * There is a refresh() method in BluetoothGatt class but for now it's
	 * hidden. We will call it using reflections.
	 */
	public void refreshDeviceCache() {
		try {
			final Method refresh = mBluetoothGatt.getClass().getMethod(
					"refresh");
			if (refresh != null) {
				final boolean success = (Boolean) refresh
						.invoke(mBluetoothGatt);
				Log.i(TAG, "refreshDeviceCache result: " + success);
			}
		} catch (Exception e) {
			Log.e(TAG, "An exception occured while refreshing device " + e);
		}
	}

	public void setScanMode(int mode, int duration) {
		try {
			final Method setScanMode = mBluetoothAdapter.getClass().getMethod(
					"setScanMode");
			if (setScanMode != null) {
				final boolean success = (Boolean) setScanMode.invoke(mode,
						duration);
				Log.i(TAG, "setScanMode result: " + success);
			}
		} catch (Exception e) {
			Log.e(TAG, "An exception occured while setScanMode " + e);
		}
	}

	static void removeDeviceBond(BluetoothDevice device) {
		try {
			final Method removeBond = device.getClass().getMethod("removeBond");
			if (removeBond != null) {
				final boolean success = (Boolean) removeBond.invoke(device);
				Log.i(TAG, "removeDeviceBond result: " + success);
			}
		} catch (Exception e) {
			Log.e(TAG, "An exception occured while refreshing device " + e);
		}
	}

	static void cancelDeviceBonding(BluetoothDevice device) {
		try {
			final Method cancelDeviceBonding = device.getClass().getMethod(
					"cancelBondProcess");
			if (cancelDeviceBonding != null) {
				final boolean success = (Boolean) cancelDeviceBonding
						.invoke(device);
				Log.i(TAG, "cancelDeviceBonding result: " + success);
			}
		} catch (Exception e) {
			Log.e(TAG, "An exception occured while refreshing device " + e);
		}
	}

	public void unbond(String addr) {
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(addr);
		if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
			removeDeviceBond(device);
		} else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
			cancelDeviceBonding(device);
		}
	}

	/**
	 * 使用给定的BLE设备后,应用程序必须调用这个方法,以确保正确地释放资源.
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}

		mBluetoothGatt.close();
		// mBluetoothGatt = null;
		mConnectionState = STATE_DISCONNECTED;
	}

	/*
     * 
     * 
     *
     */

	public boolean discoverServices() {
		if (mBluetoothGatt == null) {
			Log.e(TAG, "discoverServices --- mBluetoothGatt == null");
			return false;
		}

		return mBluetoothGatt.discoverServices();
	}

	/**
	 * 对指定的characteristic进行读方法
	 * 
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 * 
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	public boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return false;
		}
		if (characteristic == null) {
			Log.w(TAG, "characteristic == null");
			return false;
		}
		Log.i(TAG, "read characteristic uuid = " + characteristic.getUuid());
		return mBluetoothGatt.readCharacteristic(characteristic);
	}

	// 对指定的characteristic进行写方法
	public boolean writeCharacteristic(
			BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return false;
		}
		if (characteristic == null) {
			Log.w(TAG, "characteristic == null");
			return false;
		}
		Log.i(TAG, "write characteristic uuid = " + characteristic.getUuid());
		return mBluetoothGatt.writeCharacteristic(characteristic);
	}

	/**
	 * 启用或禁用CharacteristicNotification Enables or disables notification on a
	 * give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}

		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

		/**
		 * if( UUID.fromString(BluetoothLeGattAttributes.DEVICE_CONFIG_DATA).
		 * equals(characteristic.getUuid()) ) { BluetoothGattDescriptor
		 * descriptor = characteristic.getDescriptor(UUID.fromString(
		 * BluetoothLeGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
		 * descriptor.setValue
		 * (enabled?BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
		 * :BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
		 * mBluetoothGatt.writeDescriptor(descriptor); } else if(
		 * UUID.fromString
		 * (BluetoothLeGattAttributes.DEVICE_DATA_VALUE).equals(characteristic
		 * .getUuid()) ) { BluetoothGattDescriptor descriptor =
		 * characteristic.getDescriptor
		 * (UUID.fromString(BluetoothLeGattAttributes
		 * .CLIENT_CHARACTERISTIC_CONFIG));
		 * descriptor.setValue(enabled?BluetoothGattDescriptor
		 * .ENABLE_NOTIFICATION_VALUE
		 * :BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
		 * mBluetoothGatt.writeDescriptor(descriptor); } /
		 **/

	}

	public void writeDescriptor(BluetoothGattDescriptor descriptor) {
		mBluetoothGatt.writeDescriptor(descriptor);
	}

	/**
     * 
     * 
     * 
     */
	private void writeConfigAddr(byte[] addr) {
		mBandConfigAddrValue.setValue(addr);
		writeCharacteristic(mBandConfigAddrValue);
	}

	private void readConfigData() {
		readCharacteristic(mBandConfigDataValue);
	}

	private void readBatteryLevel() {
		readCharacteristic(mBandBatteryValue);
	}

	private void writeConfigData(byte[] data) {
		mBandConfigDataValue.setValue(data);
		writeCharacteristic(mBandConfigDataValue);
	}

	private void readDatum() {
		readCharacteristic(mBandSportDatumValue);
	}

	private void writeDatum(byte[] data) {
		mBandSportDatumValue.setValue(data);
		writeCharacteristic(mBandSportDatumValue);
	}

	private void enableNotification(BluetoothGattCharacteristic characteristic) {
		setCharacteristicNotification(characteristic, true);
		for (BluetoothGattDescriptor descriptor : characteristic
				.getDescriptors()) {
			Log.e(TAG, "descriptor uuid: " + descriptor.getUuid().toString());
			descriptor
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			writeDescriptor(descriptor);
		}
	}

	//
	public void writeConfigValue(final byte[] addr, final byte[] data) {
		if (mDataAccessibleMode != DEVICE_ACCESSIBLE_MODE_NORMAL) {
			Log.e(TAG, "mIsDataAccessible == false");
			return;
		}

		mBluetoothLeRefreshType = BLUETOOTH_LE_REFRESH_TYPE_CONFIG;

		Log.i(TAG, "writeConfigValue(addr=0x" + Utils.bytesToHexString(addr)
				+ ", data=0x" + Utils.bytesToHexString(data) + ")");
		Token token = new Token(OP_TYPE_WRITE_CONFIG, addr, data);
		mBluetoothIoThread.addCommand(token);
	}

	public void writeConfigValue(int addr, int data) {
		writeConfigValue(Utils.int2Bytes(addr, Utils.BIG_ENDIUM),
				Utils.int2Bytes(data, Utils.BIG_ENDIUM));
	}

	public void writeConfigValue(int addr, byte[] data) {
		writeConfigValue(Utils.int2Bytes(addr, Utils.BIG_ENDIUM), data);
	}

	public void readConfigValue(final byte[] addr) {
		if (mDataAccessibleMode != DEVICE_ACCESSIBLE_MODE_NORMAL) {
			Log.e(TAG, "mIsDataAccessible == false");
			return;
		}

		mBluetoothLeRefreshType = BLUETOOTH_LE_REFRESH_TYPE_CONFIG;

		Log.i(TAG, "readConfigValue(addr==0x" + Utils.bytesToHexString(addr)
				+ ")");
		Token token = new Token(OP_TYPE_READ_CONFIG, addr, null);
		mBluetoothIoThread.addCommand(token); // ???? SOME WRONG ??????
	}

	public void readConfigValue(int addr) {
		readConfigValue(Utils.int2Bytes(addr, Utils.BIG_ENDIUM));
	}

	public void enableDatumValueNotification() {
		if (mDataAccessibleMode != DEVICE_ACCESSIBLE_MODE_NORMAL) {
			Log.e(TAG, "mIsDataAccessible == false");
			return;
		}

		mBluetoothLeRefreshType = BLUETOOTH_LE_REFRESH_TYPE_CONFIG;
		Token token = new Token(OP_TYPE_WRITE_DATUM_CLIENT_CONFIG, null, null);
		mBluetoothIoThread.addCommand(token);
	}

	public void enableConfigValueNotification() {
		if (mDataAccessibleMode != DEVICE_ACCESSIBLE_MODE_NORMAL) {
			Log.e(TAG, "mIsDataAccessible == false");
			return;
		}

		mBluetoothLeRefreshType = BLUETOOTH_LE_REFRESH_TYPE_CONFIG;
		Token token = new Token(OP_TYPE_WRITE_CONFIG_CLIENT_CONFIG, null, null);
		mBluetoothIoThread.addCommand(token);
	}

	public void readDatumValue() {
		if (mDataAccessibleMode != DEVICE_ACCESSIBLE_MODE_NORMAL) {
			Log.e(TAG, "mIsDataAccessible == false");
			return;
		}

		mBluetoothLeRefreshType = BLUETOOTH_LE_REFRESH_TYPE_ACTIONS_DATUM;

		Log.i(TAG, "readDatumValue");
		Token token = new Token(OP_TYPE_READ_DATUM_ALL, null, null);
		mBluetoothIoThread.addCommand(token);
		return;
	}

	public void readDatumValue(int type) {
		if (mDataAccessibleMode != DEVICE_ACCESSIBLE_MODE_NORMAL) {
			Log.e(TAG, "mIsDataAccessible == false");
			return;
		}

		mBluetoothLeRefreshType = BLUETOOTH_LE_REFRESH_TYPE_ACTIONS_DATUM;

		Log.i(TAG, "readDatumValue");
		Token token = new Token(type, null, null);
		mBluetoothIoThread.addCommand(token);
		return;
	}

	public void writeDatumValue(byte[] data) {
		if (mDataAccessibleMode != DEVICE_ACCESSIBLE_MODE_NORMAL) {
			Log.e(TAG, "mIsDataAccessible == false");
			return;
		}

		mBluetoothLeRefreshType = BLUETOOTH_LE_REFRESH_TYPE_ACTIONS_DATUM;
		Log.i(TAG, "writeDatumValue");
		Token token = new Token(OP_TYPE_WRITE_DATUM, null, data);
		mBluetoothIoThread.addCommand(token);
	}

	public void readBatteryValue() {
		if (mDataAccessibleMode != DEVICE_ACCESSIBLE_MODE_NORMAL) {
			Log.e(TAG, "mIsDataAccessible == false");
			return;
		}

		mBluetoothLeRefreshType = BLUETOOTH_LE_REFRESH_TYPE_BATTERY_LEVLE;

		Log.i(TAG, "readBatteryValue");
		Token token = new Token(OP_TYPE_READ_BATTERY_LEVEL, null, null);
		mBluetoothIoThread.addCommand(token);
		return;
	}

	public void destoryLeConnection() {
		mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
		// mBluetoothLeService.refreshDeviceCache();
		// mBluetoothLeService.close();
		// mBluetoothLeService.initialize();
		mBluetoothIoThread.cleanCommands();

		// unbindBluetoothLeService();
		// bindBluetoothLeService();
	}

	/**
	 * 检索列表支持GATT协定服务连接的设备上。 Retrieves a list of supported GATT services on the
	 * connected device. This should be invoked only after
	 * {@code BluetoothGatt#discoverServices()} completes successfully.
	 * 
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null)
			return null;
		return mBluetoothGatt.getServices();
	}

	public boolean waitIdle(int i) {
		while (--i > 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return i > 0;
	}

	public static boolean checkGatt() {
		if (mBluetoothAdapter == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return false;
		}
		if (mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothGatt not initialized");
			return false;
		}
		if (mBluetoothManager == null) {
			Log.w(TAG, "BluetoothManager not initialized");
			return false;
		}
		return true;
	}

	public int numConnectedDevices() {
		int n = 0;

		if (mBluetoothGatt != null) {
			List<BluetoothDevice> devList;
			devList = mBluetoothManager
					.getConnectedDevices(BluetoothProfile.GATT);
			n = devList.size();
		}
		return n;
	}

}
