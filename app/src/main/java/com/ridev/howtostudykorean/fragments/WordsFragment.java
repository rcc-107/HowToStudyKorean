package com.ridev.howtostudykorean.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ridev.howtostudykorean.R;
import com.ridev.howtostudykorean.adapters.WordsAdapter;
import com.ridev.howtostudykorean.models.Lesson;
import com.ridev.howtostudykorean.models.Word;

import java.util.ArrayList;

/**
 * Created by Rica on 4/22/2017.
 */

public class WordsFragment extends Fragment {

    private RecyclerView recyclerView;
    private WordsAdapter adapter;
    private Lesson lesson;
    private ArrayList<String> words;
    private ArrayList<Word> wordsList;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private Query wordsQuery;
    private FirebaseRecyclerAdapter fAdapter;

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
        View view = inflater.inflate(R.layout.tutorial,container,false);

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference().child("words");

        lesson = getArguments().getParcelable("lesson");
        words = lesson.getNouns();
        wordsList = new ArrayList<>();

        for(String word:words)  {
            wordsQuery = reference.orderByChild("word").equalTo(word);
        }

        recyclerView = (RecyclerView) view;
        fAdapter = new FirebaseRecyclerAdapter<Word,WordViewHolder>(Word.class,R.layout.tutorial_words,WordViewHolder.class,reference) {

            @Override
            protected void populateViewHolder(WordViewHolder viewHolder, Word model, int position) {
                Word wModel = (Word) model;
                viewHolder.setWord(wModel.getWord()+" = "+wModel.getMeaning());
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(fAdapter);
        return view;
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        private TextView word;
        public WordViewHolder(View itemView) {
            super(itemView);
            word = (TextView) itemView.findViewById(R.id.wordTextView);
        }

        public void setWord(String word) {
            this.word.setText(word);
        }
    }

}
