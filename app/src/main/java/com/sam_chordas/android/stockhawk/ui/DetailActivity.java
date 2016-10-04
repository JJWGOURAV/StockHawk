package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.db.chart.model.ChartSet;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.HistoricalQuoteColumns;
import com.sam_chordas.android.stockhawk.data.HistoricalQuoteProvider;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.service.UpdateHistoricalDataService;

import java.util.ArrayList;

import static android.R.attr.data;

public class DetailActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private String symbol;

    private static final int CURSOR_LOADER_ID = 0;
    private Context mContext;
    private LineChartView lineChartView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mContext = this;

        symbol = getIntent().getStringExtra(UpdateHistoricalDataService.SYMBOL);

        lineChartView = (LineChartView) findViewById(R.id.linechart);

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);


        Intent intent = new Intent(this,UpdateHistoricalDataService.class);
        intent.putExtra(UpdateHistoricalDataService.SYMBOL,symbol);
        startService(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, HistoricalQuoteProvider.Quotes.CONTENT_URI,
                new String[]{ HistoricalQuoteColumns._ID, HistoricalQuoteColumns.SYMBOL, HistoricalQuoteColumns.DATE,
                        HistoricalQuoteColumns.HIGH,HistoricalQuoteColumns.LOW,
                        HistoricalQuoteColumns.OPEN,HistoricalQuoteColumns.CLOSE,
                        HistoricalQuoteColumns.ADJ_CLOSE},
                HistoricalQuoteColumns.SYMBOL + " = ?",
                new String[]{symbol},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(LOG_TAG,"Count:" + cursor.getCount());
//        mCursorAdapter.swapCursor(cursor) ;
        if(cursor.getCount() > 0){
            findViewById(R.id.empty_view).setVisibility(View.GONE);
        } else {
            findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        }

        createLineSet(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//        mCursorAdapter.swapCursor(null);
        findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        createLineSet(null);
    }

    private void createLineSet(Cursor cursor){
        LineSet lineSet = null;

        if(cursor != null && cursor.getCount() > 0){
           String[] labels = new String[cursor.getCount()];
           float[] values = new float[cursor.getCount()];

            int i=0;
           while(cursor.moveToNext()){
               labels[i] = cursor.getString(2);
               values[i++] = cursor.getFloat(3);
           }

           lineSet = new LineSet(labels,values);
        }

        ArrayList<ChartSet> chartSets = new ArrayList<>();
        if(lineSet != null) {
            chartSets.add(lineSet);
        }

        lineChartView.addData(chartSets);
    }
}
