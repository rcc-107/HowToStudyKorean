package com.ridev.howtostudykorean;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Messenger;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.google.android.vending.expansion.downloader.Constants;
import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.IDownloaderService;
import com.google.android.vending.expansion.downloader.IStub;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.CRC32;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ViewPager.PageTransformer, IDownloaderClient{

    private FirebaseDatabase database;
    private DatabaseReference ref;
    private ArrayList<Units> units = new ArrayList<Units>();
    private LinearLayout vpContainer;
    private ViewPager viewPager;
    private SectionPageAdapter sectionPageAdapter;

    private static final String LOG_TAG = "LVLDownloader";
    private ProgressBar mPB;

    private LinearLayout mDownloadLG;
    private TextView mStatusText;
    private TextView mProgressFraction;
    private TextView mProgressPercent;
    private TextView mAverageSpeed;
    private TextView mTimeRemaining;

    private View mDashboard;
    private View mCellMessage;

    private Button mPauseButton;
    private Button mWiFiSettingsButton;

    private boolean mStatePaused;
    private int mState;

    static private final float SMOOTHING_FACTOR = 0.005f;
    private boolean mCancelValidation;

    private IDownloaderService mRemoteService;

    private IStub mDownloaderClientStub;

    private static final XAPKFile[] xAPKS = {
            new XAPKFile(
                    true, // true signifies a main file
                    1, // the version of the APK that the file was uploaded
                    // against
                    362923 // the length of the file in bytes
            ),
    };

    private static class XAPKFile {
        public final boolean mIsMain;
        public final int mFileVersion;
        public final long mFileSize;

        XAPKFile(boolean isMain, int fileVersion, long fileSize) {
            mIsMain = isMain;
            mFileVersion = fileVersion;
            mFileSize = fileSize;
        }
    }

    boolean expansionFilesDelivered() {
        for (XAPKFile xf : xAPKS) {
            String fileName = Helpers.getExpansionAPKFileName(this, xf.mIsMain, xf.mFileVersion);
            if (!Helpers.doesFileExist(this, fileName, xf.mFileSize, false)) {
                return false;
            }
        }
        return true;
    }

    void validateXAPKZipFiles() {
        AsyncTask<Object, DownloadProgressInfo, Boolean> validationTask = new AsyncTask<Object, DownloadProgressInfo, Boolean>() {

            @Override
            protected void onPreExecute() {
                if(mDownloadLG != null) {
                    mDashboard.setVisibility(View.VISIBLE);
                    mCellMessage.setVisibility(View.GONE);
                    mStatusText.setText(R.string.text_verifying_download);
                    mPauseButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCancelValidation = true;
                        }
                    });
                    mPauseButton.setText(R.string.text_button_cancel_verify);
                }
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Object... params) {
                for (XAPKFile xf : xAPKS) {
                    String fileName = Helpers.getExpansionAPKFileName(
                            MainActivity.this,
                            xf.mIsMain, xf.mFileVersion);
                    if (!Helpers.doesFileExist(MainActivity.this, fileName,
                            xf.mFileSize, false))
                        return false;
                    fileName = Helpers
                            .generateSaveFileName(MainActivity.this, fileName);
                    ZipResourceFile zrf;
                    byte[] buf = new byte[1024 * 256];
                    try {
                        zrf = new ZipResourceFile(fileName);
                        ZipResourceFile.ZipEntryRO[] entries = zrf.getAllEntries();
                        /**
                         * First calculate the total compressed length
                         */
                        long totalCompressedLength = 0;
                        for (ZipResourceFile.ZipEntryRO entry : entries) {
                            totalCompressedLength += entry.mCompressedLength;
                        }
                        float averageVerifySpeed = 0;
                        long totalBytesRemaining = totalCompressedLength;
                        long timeRemaining;
                        /**
                         * Then calculate a CRC for every file in the Zip file,
                         * comparing it to what is stored in the Zip directory.
                         * Note that for compressed Zip files we must extract
                         * the contents to do this comparison.
                         */
                        for (ZipResourceFile.ZipEntryRO entry : entries) {
                            if (-1 != entry.mCRC32) {
                                long length = entry.mUncompressedLength;
                                CRC32 crc = new CRC32();
                                DataInputStream dis = null;
                                try {
                                    dis = new DataInputStream(
                                            zrf.getInputStream(entry.mFileName));

                                    long startTime = SystemClock.uptimeMillis();
                                    while (length > 0) {
                                        int seek = (int) (length > buf.length ? buf.length
                                                : length);
                                        dis.readFully(buf, 0, seek);
                                        crc.update(buf, 0, seek);
                                        length -= seek;
                                        long currentTime = SystemClock.uptimeMillis();
                                        long timePassed = currentTime - startTime;
                                        if (timePassed > 0) {
                                            float currentSpeedSample = (float) seek
                                                    / (float) timePassed;
                                            if (0 != averageVerifySpeed) {
                                                averageVerifySpeed = SMOOTHING_FACTOR
                                                        * currentSpeedSample
                                                        + (1 - SMOOTHING_FACTOR)
                                                        * averageVerifySpeed;
                                            } else {
                                                averageVerifySpeed = currentSpeedSample;
                                            }
                                            totalBytesRemaining -= seek;
                                            timeRemaining = (long) (totalBytesRemaining / averageVerifySpeed);
                                            this.publishProgress(
                                                    new DownloadProgressInfo(
                                                            totalCompressedLength,
                                                            totalCompressedLength
                                                                    - totalBytesRemaining,
                                                            timeRemaining,
                                                            averageVerifySpeed)
                                            );
                                        }
                                        startTime = currentTime;
                                        if (mCancelValidation)
                                            return true;
                                    }
                                    if (crc.getValue() != entry.mCRC32) {
                                        Log.e(Constants.TAG,
                                                "CRC does not match for entry: "
                                                        + entry.mFileName);
                                        Log.e(Constants.TAG,
                                                "In file: " + entry.getZipFileName());
                                        return false;
                                    }
                                } finally {
                                    if (null != dis) {
                                        dis.close();
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                return true;
            }

            @Override
            protected void onProgressUpdate(DownloadProgressInfo... values) {
                if(mDownloadLG != null) {
                    onDownloadProgress(values[0]);
                }
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if(mDownloadLG != null){
                    if (result) {
                        mDashboard.setVisibility(View.VISIBLE);
                        mCellMessage.setVisibility(View.GONE);
                        mStatusText.setText(R.string.text_validation_complete);
                        mPauseButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mDownloadLG.setVisibility(View.GONE);
                                vpContainer.setVisibility(View.VISIBLE);
                            }
                        });
                        mPauseButton.setText(android.R.string.ok);
                    } else {
                        mDashboard.setVisibility(View.VISIBLE);
                        mCellMessage.setVisibility(View.GONE);
                        mStatusText.setText(R.string.text_validation_failed);
                        mPauseButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        });
                        mPauseButton.setText(android.R.string.cancel);
                    }
                }
                super.onPostExecute(result);
            }

        };
        validationTask.execute(new Object());
    }

    private void initializeDownloadUI() {
        mDownloaderClientStub = DownloaderClientMarshaller.CreateStub
                (this, XAPKDownloader.class);

        mPB = (ProgressBar) findViewById(R.id.progressBar);
        mDownloadLG = (LinearLayout) findViewById(R.id.downloadProg);
        mStatusText = (TextView) findViewById(R.id.statusText);
        mProgressFraction = (TextView) findViewById(R.id.progressAsFraction);
        mProgressPercent = (TextView) findViewById(R.id.progressAsPercentage);
        mAverageSpeed = (TextView) findViewById(R.id.progressAverageSpeed);
        mTimeRemaining = (TextView) findViewById(R.id.progressTimeRemaining);
        mDashboard = findViewById(R.id.downloaderDashboard);
        mCellMessage = findViewById(R.id.approveCellular);
        mPauseButton = (Button) findViewById(R.id.pauseButton);
        mWiFiSettingsButton = (Button) findViewById(R.id.wifiSettingsButton);

        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStatePaused) {
                    mRemoteService.requestContinueDownload();
                } else {
                    mRemoteService.requestPauseDownload();
                }
                setButtonPausedState(!mStatePaused);
            }
        });

        mWiFiSettingsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });

        Button resumeOnCell = (Button) findViewById(R.id.resumeOverCellular);
        resumeOnCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRemoteService.setDownloadFlags(IDownloaderService.FLAGS_DOWNLOAD_OVER_CELLULAR);
                mRemoteService.requestContinueDownload();
                mCellMessage.setVisibility(View.GONE);
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setVisibility(View.GONE);

        ViewStub viewStub = (ViewStub) findViewById(R.id.viewStub);
        viewStub.setLayoutResource(R.layout.content_main);
        View view = viewStub.inflate();

        vpContainer = (LinearLayout) view.findViewById(R.id.vpContainer);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setClipToPadding(false);
        viewPager.setPadding(40,40,40,40);
        viewPager.setPageMargin(20);

        viewPager.setPageTransformer(false,this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);

        downloadDatabase();

        if (!expansionFilesDelivered()) {
            vpContainer.setVisibility(View.GONE);
            initializeDownloadUI();
            Log.d("XAPK"," after expansionFilesDelivered");
            viewStub.setVisibility(View.GONE);
            try {
                Intent launchIntent = MainActivity.this
                        .getIntent();
                Intent intentToLaunchThisActivityFromNotification = new Intent(
                        MainActivity
                                .this, MainActivity.this.getClass());
                intentToLaunchThisActivityFromNotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentToLaunchThisActivityFromNotification.setAction(launchIntent.getAction());

                if (launchIntent.getCategories() != null) {
                    for (String category : launchIntent.getCategories()) {
                        intentToLaunchThisActivityFromNotification.addCategory(category);
                    }
                }

                // Build PendingIntent used to open this activity from
                // Notification
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        MainActivity.this,
                        0, intentToLaunchThisActivityFromNotification,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                // Request to start the download
                int startResult = DownloaderClientMarshaller.startDownloadServiceIfRequired(this,
                        pendingIntent, XAPKDownloader.class);

                if (startResult != DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED) {
                    // The DownloaderService has started downloading the files,
                    // show progress
                    initializeDownloadUI();
                    return;
                } // otherwise, download not needed so we fall through to
                // starting the movie
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(LOG_TAG, "Cannot find own package! MAYDAY!");
                e.printStackTrace();
            }

        } else {
            validateXAPKZipFiles();
        }
    }

    @Override
    protected void onStart() {
        if (null != mDownloaderClientStub) {
            mDownloaderClientStub.connect(this);
        }
        super.onStart();
    }

    /**
     * Disconnect the stub from our service on stop
     */
    @Override
    protected void onStop() {
        if (null != mDownloaderClientStub) {
            mDownloaderClientStub.disconnect(this);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        this.mCancelValidation = true;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /* Downloads database from Firebase*/
    private void downloadDatabase(){
        database.getReference().child("lessons").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        database.getReference().child("words").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ref = database.getReference().child("units");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap:dataSnapshot.getChildren()) {
                    units.add(snap.getValue(Units.class));
                }
                sectionPageAdapter = new SectionPageAdapter(getBaseContext(),units,units.size());
                viewPager.setAdapter(sectionPageAdapter);
                for(int x=0;x<units.size();x++) {
                    View view = viewPager.findViewWithTag(x);
                    if(view != null) {
                        Units unit = units.get(x);
                        TextView title = (TextView) view.findViewById(R.id.unitTextView);
                        title.setText(unit.getTitle());
                        TextView desc = (TextView) view.findViewById(R.id.lessonsTextView);
                        desc.setText(Boolean.toString(expansionFilesDelivered()));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("firebaseERR",databaseError.toString());
            }
        });

    }

    private void setState(int newState) {
        if (mState != newState) {
            mState = newState;
            mStatusText.setText(Helpers.getDownloaderStringResourceIDFromState(newState));
        }
    }

    private void setButtonPausedState(boolean paused) {
        mStatePaused = paused;
        int stringResourceID = paused ? R.string.text_button_resume :
                R.string.text_button_pause;
        mPauseButton.setText(stringResourceID);
    }

    @Override
    public void onServiceConnected(Messenger m) {
        mRemoteService = DownloaderServiceMarshaller.CreateProxy(m);
        mRemoteService.onClientUpdated(mDownloaderClientStub.getMessenger());
    }

    @Override
    public void onDownloadStateChanged(int newState) {
        setState(newState);
        boolean showDashboard = true;
        boolean showCellMessage = false;
        boolean paused;
        boolean indeterminate;
        switch (newState) {
            case IDownloaderClient.STATE_IDLE:
                // STATE_IDLE means the service is listening, so it's
                // safe to start making calls via mRemoteService.
                paused = false;
                indeterminate = true;
                break;
            case IDownloaderClient.STATE_CONNECTING:
            case IDownloaderClient.STATE_FETCHING_URL:
                showDashboard = true;
                paused = false;
                indeterminate = true;
                break;
            case IDownloaderClient.STATE_DOWNLOADING:
                paused = false;
                showDashboard = true;
                indeterminate = false;
                break;

            case IDownloaderClient.STATE_FAILED_CANCELED:
            case IDownloaderClient.STATE_FAILED:
            case IDownloaderClient.STATE_FAILED_FETCHING_URL:
            case IDownloaderClient.STATE_FAILED_UNLICENSED:
                paused = true;
                showDashboard = false;
                indeterminate = false;
                break;
            case IDownloaderClient.STATE_PAUSED_NEED_CELLULAR_PERMISSION:
            case IDownloaderClient.STATE_PAUSED_WIFI_DISABLED_NEED_CELLULAR_PERMISSION:
                showDashboard = false;
                paused = true;
                indeterminate = false;
                showCellMessage = true;
                break;

            case IDownloaderClient.STATE_PAUSED_BY_REQUEST:
                paused = true;
                indeterminate = false;
                break;
            case IDownloaderClient.STATE_PAUSED_ROAMING:
            case IDownloaderClient.STATE_PAUSED_SDCARD_UNAVAILABLE:
                paused = true;
                indeterminate = false;
                break;
            case IDownloaderClient.STATE_COMPLETED:
                showDashboard = false;
                paused = false;
                indeterminate = false;
                validateXAPKZipFiles();
                return;
            default:
                paused = true;
                indeterminate = true;
                showDashboard = true;
        }
        int newDashboardVisibility = showDashboard ? View.VISIBLE : View.GONE;
        if (mDashboard.getVisibility() != newDashboardVisibility) {
            mDashboard.setVisibility(newDashboardVisibility);
        }
        int cellMessageVisibility = showCellMessage ? View.VISIBLE : View.GONE;
        if (mCellMessage.getVisibility() != cellMessageVisibility) {
            mCellMessage.setVisibility(cellMessageVisibility);
        }

        mPB.setIndeterminate(indeterminate);
        setButtonPausedState(paused);
    }

    @Override
    public void onDownloadProgress(DownloadProgressInfo progress) {
        mAverageSpeed.setText(getString(R.string.kilobytes_per_second,
                Helpers.getSpeedString(progress.mCurrentSpeed)));
        mTimeRemaining.setText(getString(R.string.time_remaining,
                Helpers.getTimeRemaining(progress.mTimeRemaining)));

        progress.mOverallTotal = progress.mOverallTotal;
        mPB.setMax((int) (progress.mOverallTotal >> 8));
        mPB.setProgress((int) (progress.mOverallProgress >> 8));
        mProgressPercent.setText(Long.toString(progress.mOverallProgress
                * 100 /
                progress.mOverallTotal) + "%");
        mProgressFraction.setText(Helpers.getDownloadProgressString
                (progress.mOverallProgress,
                        progress.mOverallTotal));
    }

    @Override
    public void transformPage(View view, float position) {
        final float MIN_SCALE = 0.85f;
        final float MIN_ALPHA = 0.5f;
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            float scaleFactor = Math.max(MIN_SCALE,1 - Math.abs(position));
            float scale = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            // Scale the page down (between MIN_SCALE and 1)
            view.setScaleY(scale);
            Log.d("vptransform","position:"+position+" scale:"+scaleFactor+" ID:"+view.getId());

        }else if(position > 1){
            view.setScaleY(0.85f);
        }
    }


    public class SectionPageAdapter extends PagerAdapter {

        private Context mContext;
        private ArrayList<Units> imageURI;
        private int unitsCount;

        public SectionPageAdapter(Context context,ArrayList<Units> imageURI, int unitsCount) {
            mContext = context;
            this.unitsCount = unitsCount;
            this.imageURI = imageURI;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.units_layout,container,false);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView title = (TextView) v.findViewById(R.id.unitTextView);
                    String selectedUnit = (String) title.getText().toString().toLowerCase();
                    Intent intent = new Intent(mContext,Lessons.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("lesson",selectedUnit);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
            container.addView(layout);
            layout.setTag(position);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return unitsCount;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }
}
