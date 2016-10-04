package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by sam_chordas on 10/5/15.
 */
@Database(version = HistoricalQuoteDatabase.VERSION)
public class HistoricalQuoteDatabase {
  private HistoricalQuoteDatabase(){}

  public static final int VERSION = 7;

  @Table(HistoricalQuoteColumns.class) public static final String HISTORICAL_QUOTES = "historical_quotes";
}
