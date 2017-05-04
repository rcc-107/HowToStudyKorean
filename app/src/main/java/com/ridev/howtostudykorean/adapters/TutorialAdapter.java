package com.ridev.howtostudykorean.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.ridev.howtostudykorean.R;
import com.ridev.howtostudykorean.models.Lesson;

/**
 * Created by Rica on 4/27/2017.
 */

public class TutorialAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private Lesson mLesson;
    private int mViewType;
    private static final int VIEWTYPE_TEXT = 0;
    private static final int VIEWTYPE_TABLE = 1;


    public TutorialAdapter(Context context, Lesson lesson) {
        mContext = context;
        mLesson = lesson;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder = null;
        switch(mViewType){
            case VIEWTYPE_TEXT:
                view = LayoutInflater.from(mContext).inflate(R.layout.tutorial_text,parent,false);
                viewHolder = new TutorialViewHolder(view);
                break;
            case VIEWTYPE_TABLE:
                view = LayoutInflater.from(mContext).inflate(R.layout.tutorial_webview,parent,false);
                 viewHolder = new TableViewHolder(view);
                break;
        }
        return viewHolder;
    }


    @Override
    public int getItemViewType(int position) {
        mViewType = Integer.parseInt(mLesson.getText().get(position).get("type"));
        switch(mViewType){
            case 0:
                return VIEWTYPE_TEXT;
            case 1:
                return VIEWTYPE_TABLE;
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (mViewType){
            case VIEWTYPE_TEXT:
                TutorialViewHolder tutvh = (TutorialViewHolder) holder;
                tutvh.setTitle(mLesson.getText().get(position).get("title"));
                tutvh.setText(mLesson.getText().get(position).get("body"));
                break;
            case VIEWTYPE_TABLE:
                TableViewHolder tabvh = (TableViewHolder) holder;
                tabvh.setTable(mLesson.getText().get(position).get("body"));
                break;
        }
//        ImageView imageView = new ImageView();

    }

    @Override
    public int getItemCount() {
        return mLesson.getText().size();
    }

    public static class TutorialViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView text;

        public TutorialViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tutTitle);
            text = (TextView) itemView.findViewById(R.id.tutText);
        }

        public void setTitle(String title) {
            this.title.setText(title);
        }

        public void setText(String text) {
            this.text.setText(Html.fromHtml(text));
        }
    }

    public static class TableViewHolder extends RecyclerView.ViewHolder {
        private WebView table;

        public TableViewHolder(View itemView) {
            super(itemView);
            table = (WebView) itemView.findViewById(R.id.tutWebView);
            table.getSettings().setUseWideViewPort(true);
        }

        public void setTable(String title) {
            this.table.loadData(title,"text/html; charset=utf-8","UTF-8");
        }

    }
}
