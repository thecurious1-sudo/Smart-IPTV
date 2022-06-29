package vaibhav.tech.smartiptv.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import vaibhav.tech.smartiptv.adapters.IptvAdapter;
import vaibhav.tech.smartiptv.web.db.IptvContract;
import vaibhav.tech.smartiptv.web.db.WebBrowserDbHelper;

/**
 * Created by sergey on 15.04.17.
 */

public class IptvDataSource implements IptvAdapter.DataSource{

    WebBrowserDbHelper helper;

    public IptvDataSource(Context context)
    {
        helper = new WebBrowserDbHelper(context);
    }

    @Override
    public Cursor getRowIds() {
        SQLiteDatabase db =helper.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT _id FROM " + IptvContract.IptvEntry.TABLE_NAME + " order by _id desc", new String[]{});;
        // db.close();
        return cur;
    }

    @Override
    public Cursor getRowById(long rowId) {
        SQLiteDatabase db =helper.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + IptvContract.IptvEntry.TABLE_NAME + " WHERE _id = ?", new String[]{Long.toString(rowId)});
        return cur;
    }

    @Override
    public void deleteRow(long rowId) {
        helper.deleteHistory(rowId);
    }
}
