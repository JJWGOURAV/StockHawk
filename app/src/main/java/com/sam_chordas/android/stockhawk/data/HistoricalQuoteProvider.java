package com.sam_chordas.android.stockhawk.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by sam_chordas on 10/5/15.
 */
@ContentProvider(authority = HistoricalQuoteProvider.AUTHORITY, database = HistoricalQuoteDatabase.class)
public class HistoricalQuoteProvider {
  public static final String AUTHORITY = "com.sam_chordas.android.stockhawk.data.HistoricalQuoteProvider";

  static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

  interface Path{
    String HISTORICAL_QUOTES = "historical_quotes";
    String LAST_DATE = "last_date";
  }

  private static Uri buildUri(String... paths){
    Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
    for (String path:paths){
      builder.appendPath(path);
    }
    return builder.build();
  }

  @TableEndpoint(table = HistoricalQuoteDatabase.HISTORICAL_QUOTES)
  public static class Quotes{
    @ContentUri(
        path = Path.HISTORICAL_QUOTES,
        type = "vnd.android.cursor.dir/quote"
    )

    public static final Uri CONTENT_URI = buildUri(Path.HISTORICAL_QUOTES);
    public static final Uri LAST_DATE_CONTENT_URI = buildUri(Path.HISTORICAL_QUOTES,Path.LAST_DATE);


    @InexactContentUri(
        name = "QUOTE_ID",
        path = Path.HISTORICAL_QUOTES + "/*",
        type = "vnd.android.cursor.item/quote",
        whereColumn = HistoricalQuoteColumns.SYMBOL,
        pathSegment = 1
    )
    public static Uri withSymbol(String symbol){
      return buildUri(Path.HISTORICAL_QUOTES, symbol);
    }

    @InexactContentUri(
            name = "LAST_DATE",
            path = Path.LAST_DATE + "/*",
            type = "vnd.android.cursor.item/quote",
            whereColumn = HistoricalQuoteColumns.SYMBOL,
            pathSegment = 1
    )
    public static Uri withLastDate(String symbol){
      return buildUri(Path.LAST_DATE, symbol);
    }
  }
}
