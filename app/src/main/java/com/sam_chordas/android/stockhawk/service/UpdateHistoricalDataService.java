package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.HistoricalQuoteColumns;
import com.sam_chordas.android.stockhawk.data.HistoricalQuoteProvider;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UpdateHistoricalDataService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.sam_chordas.android.stockhawk.service.action.FOO";
    public static final String SYMBOL = "symbol";
    private static final String LOG_TAG = UpdateHistoricalDataService.class.getSimpleName();
    private Context mContext;
    private OkHttpClient client = new OkHttpClient();

    private String startDate;
    private String endDate = Utils.today();

    public UpdateHistoricalDataService() {
        super("UpdateHistoricalDataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String param1 = intent.getStringExtra(SYMBOL);
            handleActionFoo(param1);
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1) {
        Cursor initQueryCursor;
        if (mContext == null){
            mContext = this;
        }

        startDate = mContext.getResources().getString(R.string.default_start_date);
        endDate = Utils.today();

        String s = getLastStoredDate(param1);
        Log.d(LOG_TAG,"Last Stored Date:" + s);

        if(!Utils.isToday(s)){
            //Data is already updated.
            return;
        }

        StringBuilder urlStringBuilder = new StringBuilder();
        try{
            // Base URL for the Yahoo query
            urlStringBuilder.append("http://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol=" +
                    "\"" + param1 + "\" and startDate=" +
                    "\"" + startDate + "\" and endDate=" +
                    "\"" + endDate + "\"", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // finalize the URL for the API query.
        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

        String urlString;
        String getResponse;

        if (urlStringBuilder != null){
            urlString = urlStringBuilder.toString();
            Log.d(LOG_TAG,"URL:" + urlString);
            try{
                getResponse = fetchData(urlString);
                try {

                    Log.d(LOG_TAG,"Response:" + getResponse);
                    ContentValues contentValues = new ContentValues();

                    ArrayList<ContentProviderOperation> contentProviderOperations = Utils.quoteHistoricalJsonToContentVals(getResponse);
                    if(contentProviderOperations != null && !contentProviderOperations.isEmpty()) {
                        mContext.getContentResolver().applyBatch(HistoricalQuoteProvider.AUTHORITY, contentProviderOperations);
                    } else{
                        //Stock already updated to latest.
                    }
                }catch (RemoteException | OperationApplicationException e){
                    Log.e(LOG_TAG, "Error applying batch insert", e);
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    String fetchData(String url) throws IOException{
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private String getLastStoredDate(String symbol){
        Cursor c = mContext.getContentResolver().query(HistoricalQuoteProvider.Quotes.CONTENT_URI,
                new String[] { HistoricalQuoteColumns._ID,
                        HistoricalQuoteColumns.SYMBOL,
                        HistoricalQuoteColumns.DATE,
                        HistoricalQuoteColumns.OPEN,
                        HistoricalQuoteColumns.CLOSE,
                        HistoricalQuoteColumns.LOW,
                        HistoricalQuoteColumns.HIGH,
                        HistoricalQuoteColumns.VOLUME}, HistoricalQuoteColumns.SYMBOL + "= ?",
                new String[] { symbol }, HistoricalQuoteColumns.DATE  +" DESC LIMIT 1");
        if (c.getCount() != 0) {
            c.moveToLast();
            return c.getString(2);
        } else {
            return null;
        }
    }


}
