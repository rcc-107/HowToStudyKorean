package com.ridev.howtostudykorean;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ridev.howtostudykorean.models.Lesson;

/**
 * Created by Rica on 4/19/2017.
 */

public class Lessons extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<Lesson,LessonViewHolder> mRecyclerAdapater;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setVisibility(View.GONE);

        ViewStub viewStub = (ViewStub) findViewById(R.id.viewStub);
        viewStub.setLayoutResource(R.layout.content_lessons);
        View view = viewStub.inflate();

        final String selectedUnit = getIntent().getExtras().getString("lesson");
        setTitle(selectedUnit.toUpperCase());
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference().child("lessons/"+selectedUnit);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.lessonsRV);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        mRecyclerAdapater = new FirebaseRecyclerAdapter<Lesson, LessonViewHolder>(Lesson.class,R.layout.lesson_item_layout,LessonViewHolder.class,mRef) {
            @Override
            protected void populateViewHolder(LessonViewHolder viewHolder, Lesson model, int position) {
                final Lesson lesson = model;
                viewHolder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getBaseContext(),TutorialActivity.class);
                        intent.putExtra(TutorialActivity.LESSON_OBJ,lesson);
                        startActivity(intent);
                    }
                });
                viewHolder.setDesc(model.getDesc());
                viewHolder.setTitle(model.getTitle());
            }
        };

        mRecyclerView.setAdapter(mRecyclerAdapater);

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

    public static class LessonViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView desc;
        public  View root;

        public LessonViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            title = (TextView) itemView.findViewById(R.id.lessonTitle);
            desc = (TextView) itemView.findViewById(R.id.lessonDesc);
        }

        public void setTitle(String title) {
            this.title.setText(title);
        }

        public void setDesc(String desc) {
            this.desc.setText(desc);
        }
    }
}
