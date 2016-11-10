package com.sam_chordas.android.stockhawk.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.HistoricalQuoteColumns;
import com.sam_chordas.android.stockhawk.data.HistoricalQuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.UpdateHistoricalDataService;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.R.attr.data;
import static com.sam_chordas.android.stockhawk.R.id.change;
import static com.sam_chordas.android.stockhawk.R.id.fromDate;
import static com.sam_chordas.android.stockhawk.R.id.toDate;

public class DetailActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor>,DatePickerFragment.DateChanged{

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private String symbol;

    private static final int CURSOR_LOADER_ID = 0;
    private Context mContext;
    private LineChart chart;
    private Spinner spinner;
    private static TextView fromDate, toDate;
    private static String startDate,endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mContext = this;

        symbol = getIntent().getStringExtra(UpdateHistoricalDataService.SYMBOL);

        startDate = getResources().getString(R.string.default_start_date);
        endDate = Utils.today();

        chart = (LineChart) findViewById(R.id.linechart);

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        setSymbolTodayData();

        Intent intent = new Intent(this,UpdateHistoricalDataService.class);
        intent.putExtra(UpdateHistoricalDataService.SYMBOL,symbol);
        startService(intent);

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_options, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        fromDate = (TextView) findViewById(R.id.fromDate);
        toDate = (TextView) findViewById(R.id.toDate);

        fromDate.setText(startDate);
        toDate.setText(endDate);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, HistoricalQuoteProvider.Quotes.CONTENT_URI,
                new String[]{ HistoricalQuoteColumns._ID, HistoricalQuoteColumns.SYMBOL, HistoricalQuoteColumns.DATE,
                        HistoricalQuoteColumns.HIGH,HistoricalQuoteColumns.LOW,
                        HistoricalQuoteColumns.OPEN,HistoricalQuoteColumns.CLOSE,
                        HistoricalQuoteColumns.ADJ_CLOSE},
                HistoricalQuoteColumns.SYMBOL + " = ? AND " + HistoricalQuoteColumns.DATE + " >= ? AND " + HistoricalQuoteColumns.DATE + " <= ?",
                new String[]{symbol,startDate,endDate},
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
        List<Entry> entries = new ArrayList<Entry>();

        if(cursor != null && cursor.getCount() > 0){

            int i=0;
            while(cursor.moveToNext()){
                entries.add(new Entry(i++, cursor.getFloat(3)));
            }

        }

        LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
        dataSet.setCircleColor(R.color.material_red_700);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh
    }

    public void showDatePickerDialog(View v) {

        Bundle b = new Bundle();

        if(v.getId() == R.id.fromDate) {
            b.putBoolean("isStartDate",true);
        }else if(v.getId() == R.id.toDate){
            b.putBoolean("isStartDate",false);
        }
        DialogFragment newFragment = DatePickerFragment.newInstance(b);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void dateChanged(String date, boolean isStartDate) {
        if (isStartDate) {
            startDate = date;
            fromDate.setText(startDate);
        } else {
            endDate = date;
            toDate.setText(endDate);
        }

        DetailActivity.this.getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, DetailActivity.this);
    }


    public void setSymbolTodayData(){
        Typeface robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");

        TextView symbolText = (TextView) findViewById(R.id.stock_symbol);
        symbolText.setTypeface(robotoLight);
        TextView bidPrice = (TextView) findViewById(R.id.bid_price);
        TextView change = (TextView) findViewById(R.id.change);

        symbolText.setText(symbol);
        symbolText.setContentDescription(symbol);
        bidPrice.setText(getIntent().getStringExtra("bid_price"));
        bidPrice.setContentDescription(getIntent().getStringExtra("bid_price"));

        int sdk = Build.VERSION.SDK_INT;
        if (getIntent().getIntExtra("is_up",0) == 1){
            if (sdk < Build.VERSION_CODES.JELLY_BEAN){
                change.setBackgroundDrawable(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            }else {
                change.setBackground(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            }
        } else{
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                change.setBackgroundDrawable(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            } else{
                change.setBackground(
                        mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            }
        }
        if (Utils.showPercent){
            change.setText(getIntent().getStringExtra("percent_change"));
        } else{
            change.setText(getIntent().getStringExtra("change"));
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        fromDate.setText(startDate);
        toDate.setText(endDate);
    }
}

