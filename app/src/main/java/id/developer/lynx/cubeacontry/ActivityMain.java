package id.developer.lynx.cubeacontry;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.developer.lynx.cubeacontry.bluetooth.container.BluetoothDeviceStore;
import id.developer.lynx.cubeacontry.bluetooth.util.BluetoothScanner;
import id.developer.lynx.cubeacontry.bluetooth.util.BluetoothUtils;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.easycursor.objectcursor.EasyObjectCursor;

public class ActivityMain extends AppCompatActivity {

    @BindView(R.id.text_act_main_bluetooth_le) TextView textBluetoothLE;
    @BindView(R.id.text_act_main_bluetooth_status) TextView textBluetoothStatus;
    @BindView(R.id.text_act_main_bluetooth_count) TextView textBluetoothCount;
    @BindView(R.id.list_act_main_bluetooth) ListView listBluetooth;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(android.R.id.empty) TextView textEmpty;

    BluetoothScanner mBluetoothScanner;
    BluetoothUtils mBluetoothUtils;
    BluetoothDeviceStore mBluetoothDeviceStore;
    AdapterListLeDevice mAdapterListLeDevice, tempListAdapter;

    List<ObjectBeacon> listBeacon;

    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device, rssi, scanRecord, System.currentTimeMillis());
            mBluetoothDeviceStore.addDevice(deviceLe);
            final EasyObjectCursor<BluetoothLeDevice> c = mBluetoothDeviceStore.getDeviceCursor();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapterListLeDevice.swapCursor(c);
                    updateItemCount(mAdapterListLeDevice.getCount());
                    getPos(mAdapterListLeDevice.getCount());
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        listBeacon = new ArrayList<>();

        mBluetoothDeviceStore = new BluetoothDeviceStore();
        mBluetoothUtils = new BluetoothUtils(this);
        mBluetoothScanner = new BluetoothScanner(mLeScanCallback, mBluetoothUtils);
        updateItemCount(0);
        getPos(0);

        initListView();
    }

    private void initListView(){
        listBluetooth.setEmptyView(textEmpty);
        listBluetooth.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        BluetoothLeDevice device = mAdapterListLeDevice.getItem(position);
                        if (device == null) return;

//                        Intent intent = new Intent(this, DeviceDetailsActivity.class);
//                        intent.putExtra(DeviceDetailsActivity.EXTRA_DEVICE, device);
//
//                        startActivity(intent);
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        if (!mBluetoothScanner.isScanning()) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.layout_actionbar_progress);
        }

        if (listBluetooth.getCount() > 0) {
            menu.findItem(R.id.menu_share).setVisible(true);
        } else {
            menu.findItem(R.id.menu_share).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                startScan();
                break;
            case R.id.menu_stop:
                mBluetoothScanner.scanLeDevice(-1, false);
                showLogBeacon();
                invalidateOptionsMenu();
                break;
            case R.id.menu_about:
//                displayAboutDialog();
                break;
            case R.id.menu_share:
                mBluetoothDeviceStore.shareDataAsEmail(this);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothScanner.scanLeDevice(-1, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        final boolean mIsBluetoothOn = mBluetoothUtils.isBluetoothOn();
        final boolean mIsBluetoothLePresent = mBluetoothUtils.isBluetoothLeSupported();

        if (mIsBluetoothOn) {
            textBluetoothStatus.setText(R.string.on);
        } else {
            textBluetoothStatus.setText(R.string.off);
        }

        if (mIsBluetoothLePresent) {
            textBluetoothLE.setText(R.string.supported);
        } else {
            textBluetoothLE.setText(R.string.not_supported);
        }

        invalidateOptionsMenu();
    }

    private void startScan() {
        final boolean mIsBluetoothOn = mBluetoothUtils.isBluetoothOn();
        final boolean mIsBluetoothLePresent = mBluetoothUtils.isBluetoothLeSupported();
        mBluetoothDeviceStore.clear();
        updateItemCount(0);
        getPos(0);
        mAdapterListLeDevice = new AdapterListLeDevice(this, mBluetoothDeviceStore.getDeviceCursor());
        listBluetooth.setAdapter(mAdapterListLeDevice);
//         List<IBeaconDevice> listBeacon = mLeDeviceListAdapter.getList();

        mBluetoothUtils.askUserToEnableBluetoothIfNeeded();
        if (mIsBluetoothOn && mIsBluetoothLePresent) {
            mBluetoothScanner.scanLeDevice(-1, true); //waktu scan
            invalidateOptionsMenu(); // digunakan untuk loading
        }
    }

    private void updateItemCount(final int count) {
//        sLeDeviceListAdapter = new LeDeviceListAdapter(this, mDeviceStore.getDeviceCursor());
        textBluetoothCount.setText(
                getString(
                        R.string.formatter_item_count,
                        String.valueOf(count)));
    }

    private void getPos(final int count){
        tempListAdapter =new AdapterListLeDevice(this, mBluetoothDeviceStore.getDeviceCursor());
        for(int i=0;i<count;i++){
            Log.d(Utils.TAG_LINE, "Beacon-" +i+ " " +tempListAdapter.getList().get(i).getAddress());
        }

        listBeacon.clear();

        String [] mac={"D4:E9:4A:11:06:FB","F3:23:B6:3C:78:F0","C1:59:5D:BE:23:BB"};
        double hasil_x,hasil_y;
        double [] jarak=new double[3];
        double [] beacon_position_x = {0.0,1.0,3.0,8.0,9.9,12.0};
        double [] beacon_position_y = {7.0,1.0,0.2,8.0,9.9,12.0};
        String [][] temp= new String[count][2];

        for(int i=0; i < count; i++){
//            temp[i][0]=tempListAdapter.getList().get(i).getAddress();
//            temp[i][1]=String.valueOf(tempListAdapter.getList().get(i).getAccuracy());
            Log.d(Utils.TAG_LINE, " Cube-" +i+ " " +temp[i][0]+ " - " +temp[i][1]);

            String macBeacon = tempListAdapter.getList().get(i).getAddress();
            String uuidBeacon = tempListAdapter.getList().get(i).getUUID();
            Integer majorBeacon = tempListAdapter.getList().get(i).getMajor();
            Double jarakBeacon = tempListAdapter.getList().get(i).getAccuracy();

            listBeacon.add(new ObjectBeacon(macBeacon, uuidBeacon, majorBeacon, jarakBeacon));
        }

        Collections.sort(listBeacon);

//        Arrays.sort(temp, new Comparator<String[]>() {
//            @Override
//            public int compare(final String[] entry1, final String[] entry2) {
//                final String time1 = entry1[1];
//                final String time2 = entry2[1];
//                return time1.compareTo(time2);
//            }
//        });

//        for(int i=0;i<count;i++){
//            Log.d(Utils.TAG_LINE, "Urut-" +i+ " " +temp[i][0]+ " - " +temp[i][1]);
//        }

//        for(int i=0;i<3;i++){
//            jarak[i]= Double.parseDouble(temp[i][1])/50;
//        }

//        for(int i=0;i<3;i++){

//            for(int j=0;j<mac.length;j++){
//                if(temp[i][0].equalsIgnoreCase(mac[j])){
//                    beacon_position_x[i]=beacon_position_x[j];
//                    beacon_position_y[i]=beacon_position_y[j];
//                }
//            }
//        }
//
//        hasil_x = ((Math.pow(jarak[0], 2) - Math.pow(jarak[1], 2)+Math.pow(beacon_position_x[1], 2)-Math.pow(beacon_position_x[0], 2)+Math.pow(beacon_position_y[1], 2)-Math.pow(beacon_position_y[0], 2))*(2*(beacon_position_y[2]-beacon_position_y[1])) - (Math.pow(jarak[1], 2) - Math.pow(jarak[2], 2)+Math.pow(beacon_position_x[2], 2)-Math.pow(beacon_position_x[1], 2)+Math.pow(beacon_position_y[2], 2)-Math.pow(beacon_position_y[1], 2))*(2*(beacon_position_y[1]-beacon_position_y[0])))/
//                    ((2*(beacon_position_x[1]-beacon_position_x[2]))*(2*(beacon_position_y[1]-beacon_position_y[0]))-(2*(beacon_position_x[0]-beacon_position_x[1]))*(2*(beacon_position_y[2]-beacon_position_y[1])));
//
//        hasil_y = ((Math.pow(jarak[0], 2)-Math.pow(jarak[1], 2)+Math.pow(beacon_position_x[1], 2)-Math.pow(beacon_position_x[0], 2)+Math.pow(beacon_position_y[1], 2)-Math.pow(beacon_position_y[0], 2)) + (hasil_x*2*(beacon_position_x[0]-beacon_position_x[1]))) / (2*(beacon_position_y[1]-beacon_position_y[0]));
//
//        Log.d("Posisi-",hasil_x+" "+hasil_y);

    }

    private void showLogBeacon(){

        for (int i = 0; i < listBeacon.size(); i++) {
            Log.d(Utils.TAG_LINE, "uuid = " +listBeacon.get(i).getUuid()+
                    ", major = " +listBeacon.get(i).getMajor()+
                    ", mac = " +listBeacon.get(i).getMac()+
                    ", dis = " +listBeacon.get(i).getJarak());
        }
    }
}
