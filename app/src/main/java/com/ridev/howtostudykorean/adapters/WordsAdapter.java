package com.ridev.howtostudykorean.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ridev.howtostudykorean.R;
import com.ridev.howtostudykorean.models.Word;

import java.util.ArrayList;


/**
 * Created by Rica on 4/23/2017.
 */


public class WordsAdapter extends RecyclerView.Adapter<WordsAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Word> mWords;

    public WordsAdapter(Context context, ArrayList<Word> words) {
        mContext = context;
        mWords = words;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.tutorial_words,parent,false);
        WordsAdapter.ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Word w = mWords.get(position);
        holder.setWord(w.getWord()+" = "+w.getMeaning());
    }

    @Override
    public int getItemCount() {
            return mWords.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView word;
        public ViewHolder(View itemView) {
            super(itemView);
            word = (TextView) itemView.findViewById(R.id.wordTextView);
        }
        
        public void setWord(String word) {
            this.word.setText(word);
        }
    }
}
