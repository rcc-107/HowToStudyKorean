package com.ridev.howtostudykorean;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ViewPager.PageTransformer {

    private FirebaseDatabase database;
    private DatabaseReference ref;
    private ArrayList<Units> units = new ArrayList<Units>();
    private ViewPager viewPager;
    private SectionPageAdapter sectionPageAdapter;

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
        viewStub.inflate();

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setClipToPadding(false);
        viewPager.setPadding(40,40,40,40);
        viewPager.setPageMargin(20);

        database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
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
                        desc.setText(unit.getDesc());
                    }else{
                        sectionPageAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("firebaseERR",databaseError.toString());
            }
        });

        viewPager.setPageTransformer(false,this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
