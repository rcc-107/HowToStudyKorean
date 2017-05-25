package com.ridev.howtostudykorean.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ridev.howtostudykorean.adapters.TutorialAdapter;
import com.ridev.howtostudykorean.models.Lesson;

import static com.ridev.howtostudykorean.R.layout.tutorial;


/**
 * Created by Rica on 4/22/2017.
 */
public class TutorialFragment extends Fragment {

    private RecyclerView recyclerView;
    private TutorialAdapter adapter;
    private Lesson lesson;

    public TutorialFragment() {
    }

    public static TutorialFragment newInstance(Lesson les) {
        TutorialFragment fragment = new TutorialFragment();
        Bundle args = new Bundle();
        args.putParcelable("lesson",les);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(tutorial,container,false);
        lesson = getArguments().getParcelable("lesson");
        Log.d("tutorailFrag",lesson.getDesc());
        recyclerView = (RecyclerView) view;
        adapter = new TutorialAdapter(getContext(),lesson);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(adapter);
        return view;
    }


}
