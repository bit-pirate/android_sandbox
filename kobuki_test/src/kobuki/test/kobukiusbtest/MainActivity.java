package kobuki.test.kobukiusbtest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;


public class MainActivity extends Activity {
	
	private static final String TAG = "KobukiUSBTest";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Starting activity.");
		boolean kobuki_found = false;
		Intent intent = getIntent();
		UsbDevice device;
		UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		if (intent != null)
		{
			Log.i(TAG, "Activity has been envoked by intent.");
		}
		device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		if (device != null)
		{
			Log.i(TAG, Integer.toString(device.getProductId()));
			Log.i(TAG, Integer.toString(device.getVendorId()));
//			if ((device.getProductId() == 1027) && (device.getProductId() == 24577))
//			{
			kobuki_found = true;
//			}
		}
		else
		{
			Log.i(TAG, "Intent does not carry a valid USB device");
			HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
			Iterator<Map.Entry<String,UsbDevice>> device_it = deviceList.entrySet().iterator();
			Log.i(TAG, "Listing all available USB devices.");
			while (device_it.hasNext())
			{
				Map.Entry<String,UsbDevice> list_entry = (Map.Entry<String,UsbDevice>) device_it.next();
				Log.i(TAG, "Device name: " + list_entry.getKey());
				Log.i(TAG, "Product ID: " + Integer.toString(list_entry.getValue().getProductId()));
				Log.i(TAG, "Vendor ID: " + Integer.toString(list_entry.getValue().getVendorId()));
				Log.i(TAG, "Device ID: " + Integer.toString(list_entry.getValue().getDeviceId()));
				Log.i(TAG, "Device name: " + list_entry.getValue().getDeviceName());
				if ((list_entry.getValue().getProductId() == 1027) && (list_entry.getValue().getProductId() == 24577))
				{
					Log.i(TAG, "Kobuki found!");
					kobuki_found = true;
					device = list_entry.getValue();
					break;
				}
			}
		}
		
		// Looking through USB accessories
		if (!kobuki_found)
		{
			UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
			if (accessory != null)
			{
				Log.d(TAG, "Found accessory: " + accessory);
			}
			else
			{
				Log.i(TAG, "Intent does not carry a valid USB accessory!");
				UsbAccessory[] accessoryList = manager.getAccessoryList();
				if
//				Log.i(TAG, "Found a total of " + accessoryList.length + " accessories.");
//				for (int i = 0; i < accessoryList.length; ++i)
//				{
//					Log.d(TAG, "Found accessory: " + accessoryList[i]);
//			    }
			}
		}
		
		if (kobuki_found)
		{
			Log.i(TAG, "Kobuki found!");
			// default Android USB style
	//			UsbInterface intf = device.getInterface(0);
	//			UsbEndpoint endpoint = intf.getEndpoint(0);
	//			UsbDeviceConnection connection = manager.openDevice(device);
	//			boolean forceClaim = true;
	//			connection.claimInterface(intf, forceClaim);
	//			connection.controlTransfer(requestType, request, value, index, buffer, length, timeout)
			// USB serial for Android style
			UsbSerialDriver driver;
			driver = UsbSerialProber.acquire(manager, device);
			if (driver != null)
			{
			  try
			  {
				driver.open();
			    driver.setBaudRate(115200);
			    byte buffer[] = new byte[16];
			    int numBytesRead = driver.read(buffer, 1000);
			    Log.i(TAG, "Read " + numBytesRead + " bytes.");

			    byte message[] = new byte[10]; // 3 + payload + 1; payload = (1 + 1 + + 2 + 2) = 6
			    message[0] = (byte) 0xaa; // header 0
			    message[1] = (byte) 0x55; // header 1
			    message[2] = 6; // payload length
			    
			    // payloads
			    message[3] = 1; // base control header
			    message[4] = 4; // base control length
			    message[5] = 50; // base control speed 1. byte
			    message[6] = 0; // base control speed 2. byte
			    message[7] = 1; // base control radius 1. byte
			    message[8] = 0; // base control radius 2. byte
			    
			    int message_size = message.length;
			    Log.i(TAG, "message size: " + message_size);
			    byte cs = 0;
			    for (int i = 2; i < (message_size - 1); i++)
			    {
			      cs = (byte) (cs ^ message[i]);
				  Log.i(TAG, "checksum: " + cs);
			    }
			    message[9] = cs; // checksum
			    int numBytesWritten = driver.write(message, 1000);
			    try
			    {
			        Thread.sleep(20);
			    }
			    catch(InterruptedException ex)
			    {
			        Thread.currentThread().interrupt();
			    }
			    numBytesWritten = driver.write(message, 1000);
			    try
			    {
			        Thread.sleep(20);
			    }
			    catch(InterruptedException ex)
			    {
			        Thread.currentThread().interrupt();
			    }
			    numBytesWritten = driver.write(message, 1000);
			    try
			    {
			        Thread.sleep(20);
			    }
			    catch(InterruptedException ex)
			    {
			        Thread.currentThread().interrupt();
			    }
			    numBytesWritten = driver.write(message, 1000);
			    Log.i(TAG, "Wrote " + numBytesWritten + " bytes.");
			    try
			    {
			        Thread.sleep(3000);
			    }
			    catch(InterruptedException ex)
			    {
			        Thread.currentThread().interrupt();
			    }
				driver.close();
			  }
			  catch (IOException e)
			  {
				  // Deal with error.
				  Log.i(TAG, "Error occured when communicating with the device!");
			  }
			}
		}
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present. 
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
