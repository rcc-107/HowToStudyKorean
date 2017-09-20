package com.ridev.howtostudykorean.fragments;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ridev.howtostudykorean.AudioPlayerActivity;
import com.ridev.howtostudykorean.R;
import com.ridev.howtostudykorean.adapters.WordsAdapter;
import com.ridev.howtostudykorean.models.Lesson;
import com.ridev.howtostudykorean.models.Word;

/**
 * Created by Rica on 4/22/2017.
 */

public class WordsFragment extends Fragment {

    private RecyclerView recyclerView;
    private WordsAdapter adapter;
    private Lesson lesson;
    private String title;
    private int mExpandedItem;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private Query wordsQuery;
    private FirebaseRecyclerAdapter fAdapter;

    private MediaPlayer mediaPlayer;

    public WordsFragment() {
    }

    public static WordsFragment newInstance(Lesson les) {
        WordsFragment fragment = new WordsFragment();
        Bundle args = new Bundle();
        args.putParcelable("lesson",les);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.tutorial,container,false);

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference().child("words");

        lesson = getArguments().getParcelable("lesson");
        title = lesson.getTitle();
        wordsQuery = reference.orderByChild("lesson").equalTo(title);


        recyclerView = (RecyclerView) view;
        fAdapter = new FirebaseRecyclerAdapter<Word,WordViewHolder>(Word.class,R.layout.tutorial_words,WordViewHolder.class,wordsQuery) {

            @Override
            protected void populateViewHolder(WordViewHolder viewHolder, Word model, final int position) {
                final boolean isExpanded = position == mExpandedItem;
                viewHolder.moreDetails.setVisibility(isExpanded? View.VISIBLE:View.GONE);
                viewHolder.word.setActivated(isExpanded);

                viewHolder.setWord(model.getWord()+" = "+model.getMeaning());
                viewHolder.setComus(model.getCommonUsages());
                viewHolder.setExample(model.getExamples());

                final TextView word = viewHolder.word;
                viewHolder.word.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        word.setCompoundDrawables(null,null, ContextCompat.getDrawable(getContext(),R.drawable.ic_arrow_drop_up_black),null);
                        notifyItemChanged(mExpandedItem);
                        mExpandedItem = isExpanded? -1:position;
                        notifyItemChanged(mExpandedItem);
                    }
                });

                final String audioPath = model.getAudio();
                viewHolder.playBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), AudioPlayerActivity.class);
                        intent.putExtra("audioPath",audioPath);
                        startActivity(intent);
                    }
                });
            }

        };
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(fAdapter);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fAdapter.cleanup();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {

        private TextView word;
        private TextView example;
        private TextView comus;
        private Button playBtn;
        private LinearLayout moreDetails;

        public WordViewHolder(View itemView) {
            super(itemView);
            word = (TextView) itemView.findViewById(R.id.wordTextView);
            example = (TextView) itemView.findViewById(R.id.exampleTextView);
            comus = (TextView) itemView.findViewById(R.id.comusTextView);
            playBtn = (Button) itemView.findViewById(R.id.audio_button);
            moreDetails = (LinearLayout) itemView.findViewById(R.id.moreDetails);
        }

        public void setComus(String commonUsages) {
            this.comus.setText(Html.fromHtml(commonUsages));
        }

        public void setWord(String word) {
            this.word.setText(word);
        }

        public void setExample(String examples) {
            this.example.setText(Html.fromHtml(examples));
        }
    }

}
