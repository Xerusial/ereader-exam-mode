package edu.hm.eem_client.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.github.druk.dnssd.DNSSDService;

import edu.hm.eem_client.R;
import edu.hm.eem_client.net.ClientServiceManager;
import edu.hm.eem_library.model.HostItemViewModel;
import edu.hm.eem_library.net.NsdService;
import edu.hm.eem_library.view.AbstractMainActivity;
import edu.hm.eem_library.view.ItemListFragment;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * This activity is used to scan for teacher devices and connect to them.
 */
public class ScanActivity extends AppCompatActivity implements ItemListFragment.OnListFragmentPressListener, ClientServiceManager.ServiceReadyListener {
    public static final String PROF_FIELD = "Prof";
    public static final String ADDRESS_FIELD = "Address";
    public static final String PORT_FIELD = "Port";
    private static final String SHOWCASE_ID = "ScanActivity";
    private ConnectivityManager cm;
    private ClientServiceManager clientServiceManager;
    private HostItemViewModel model;
    private Switch sw;
    private ImageView progressBg;
    private ImageView progress;
    private AnimationDrawable progressAnim;
    private TextView uiLockView;
    private boolean uiLocked = false;
    private String examName;

    /**
     * Init views, managers, label the toolbar, get args
     *
     * @param savedInstanceState Android basics
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        model = ViewModelProviders.of(this).get(HostItemViewModel.class);
        clientServiceManager = new ClientServiceManager(getApplicationContext(), model.getLivedata(), this);
        cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        sw = findViewById(R.id.sw_scan_services);
        progressBg = findViewById(R.id.progress_background);
        progress = findViewById(R.id.progress);
        uiLockView = findViewById(R.id.ui_locker);
        Toolbar toolbar = findViewById(R.id.toolbar);
        sw.setOnClickListener(v -> sw.setChecked(progress(scanNetwork(sw.isChecked()))));
        progressAnim = (AnimationDrawable) progress.getDrawable();
        examName = getIntent().getStringExtra(AbstractMainActivity.EXAMNAME_FIELD);
        toolbar.setTitle(examName);
        tutorial();
    }

    /**
     * Enable DNSSD scanning for host devices in the local network.
     *
     * @param on / off
     * @return scanning has been enabled
     */
    private boolean scanNetwork(boolean on) {
        boolean ret = false;
        if (on && cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if ((activeNetwork != null) && (activeNetwork.isConnected())) {
                ret = true;
            } else {
                Toast.makeText(this, "Network is not up yet!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (on) model.getLivedata().clean(true);
        clientServiceManager.discover(on);
        return ret;
    }

    /**
     * Enable the progress bar
     *
     * @param on / off
     * @return boolean pass-through
     */
    private boolean progress(boolean on) {
        if (on) {
            progressAnim.start();
        } else {
            progressAnim.stop();
        }
        progressBg.setVisibility(on ? View.VISIBLE : View.GONE);
        progress.setVisibility(on ? View.VISIBLE : View.GONE);
        return on;
    }

    /**
     * Interface, which is called from the recyclerview fragment in this activity
     *
     * @param index which item has been pressed?
     */
    @Override
    public void onListFragmentPress(int index) {
        if (!uiLocked) {
            lock(true);
            NsdService item = model.get(index);
            clientServiceManager.resolve(item);
        }
    }

    /**
     * Display a screen lock, so no connection attempts can be done by the user, while still trying
     * to connect to a host.
     *
     * @param enable it
     */
    private void lock(boolean enable) {
        uiLocked = enable;
        uiLockView.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Disable scan switch, loading overlay and clean devices list
     */
    @Override
    protected void onPause() {
        sw.setChecked(progress(scanNetwork(false)));
        model.getLivedata().clean(false);
        lock(false);
        super.onPause();
    }

    /**
     * Disable scanning
     */
    @Override
    protected void onDestroy() {
        scanNetwork(false);
        super.onDestroy();
    }

    /**
     * Called if host IP and port have been found.
     *
     * @param nsdService holding IP and Port
     */
    @Override
    public void onServiceReady(NsdService nsdService) {
        Intent intent = new Intent(this, LockedActivity.class);
        intent.putExtra(PROF_FIELD, nsdService.serviceName);
        intent.putExtra(ADDRESS_FIELD, nsdService.address);
        intent.putExtra(PORT_FIELD, nsdService.port);
        intent.putExtra(AbstractMainActivity.EXAMNAME_FIELD, examName);
        startActivity(intent);
    }

    /**
     * Failed scanning
     *
     * @param service   current service
     * @param errorCode failure reason
     */
    @Override
    public void operationFailed(DNSSDService service, int errorCode) {
        lock(false);
    }

    /**
     * Show a little tutorial using a showcaseview
     */
    private void tutorial() {
        ShowcaseConfig config = new ShowcaseConfig();
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);
        sequence.setConfig(config);
        sequence.addSequenceItem(sw,
                getString(edu.hm.eem_library.R.string.tutorial_scan_switch), getString(android.R.string.ok));
        sequence.start();
    }
}
