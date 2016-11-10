package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.data.HistoricalQuoteColumns;
import com.sam_chordas.android.stockhawk.data.HistoricalQuoteProvider;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
//      Log.d("GOURAV",JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject("results").getJSONObject("quote");

          Log.d("GOURAV",jsonObject.toString());
          if(!jsonObject.getString("Bid").equals("null")){
            batchOperations.add(buildBatchOperation(jsonObject));
          } else {
            //Invalid stock name
          }
        } else{
          resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

          if (resultsArray != null && resultsArray.length() != 0){

            List<String> currentStockNames = new ArrayList<String>();
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);

              if(!jsonObject.getString("Bid").equals("null") && !currentStockNames.contains(jsonObject.getString("Bid"))){
                batchOperations.add(buildBatchOperation(jsonObject));
                currentStockNames.add(jsonObject.getString("Bid"));
              }
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static ArrayList quoteHistoricalJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try {
      jsonObject = new JSONObject(JSON);
//      Log.d("GOURAV",JSON);
      if (jsonObject != null && jsonObject.length() != 0) {
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));

        resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

        if (resultsArray != null && resultsArray.length() != 0) {

          for (int i = 0; i < resultsArray.length(); i++) {
            jsonObject = resultsArray.getJSONObject(i);
            batchOperations.add(buildHistoricalDataBatchOperation(jsonObject));
          }
        }
      }
    }catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString("Change");
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
          jsonObject.getString("ChangeinPercent"), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-'){
        builder.withValue(QuoteColumns.ISUP, 0);
      }else{
        builder.withValue(QuoteColumns.ISUP, 1);
      }

    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }

  public static ContentProviderOperation buildHistoricalDataBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
            HistoricalQuoteProvider.Quotes.CONTENT_URI);
    try {
      builder.withValue(HistoricalQuoteColumns.SYMBOL, jsonObject.getString("Symbol"));
      builder.withValue(HistoricalQuoteColumns.DATE, jsonObject.getString("Date"));
      builder.withValue(HistoricalQuoteColumns.OPEN, jsonObject.getString("Open"));
      builder.withValue(HistoricalQuoteColumns.HIGH, jsonObject.getString("High"));
      builder.withValue(HistoricalQuoteColumns.LOW, jsonObject.getString("Low"));
      builder.withValue(HistoricalQuoteColumns.CLOSE, jsonObject.getString("Close"));
      builder.withValue(HistoricalQuoteColumns.VOLUME, jsonObject.getString("Volume"));
      builder.withValue(HistoricalQuoteColumns.ADJ_CLOSE, jsonObject.getString("Adj_Close"));

    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }

  public static boolean isOldStockName(Context context,String input){
    Cursor c = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
            new String[] { QuoteColumns.SYMBOL }, QuoteColumns.SYMBOL + "= ?",
            new String[] { input }, null);
    if (c.getCount() != 0) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean isToday(String lastDate){

    if(lastDate == null)
      return true;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    try {
      Date date = format.parse(lastDate);

      if(date == new Date()){
        return true;
      }

    } catch (ParseException e) {
      e.printStackTrace();
    }

    return false;
  }

  public static String today(){
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");

    Date date = new Date();
    String datetime = dateformat.format(date);
//            System.out.println("Current Date Time : " + datetime);

    return datetime;
  }
}
