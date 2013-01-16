
package com.webaltry.bugz;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.j2bugzilla.base.Bug;

public class BugzillaProcessor {
    
    private static final String TAG = BugzillaProcessor.class.getSimpleName();

    public void createQuery(BugzillaApplication app, Query query, BugzillaProcessorCallback callback) {
        
        Log.d(TAG, "createQuery");
        
        if (query.idValid == true)
            return;

        /* insert record in "queries" table */
        ContentResolver resolver = app.getContentResolver();
        ContentValues values = new ContentValues();
        
        values.put(BugzillaDatabase.FIELD_NAME_NAME, query.name);
        values.put(BugzillaDatabase.FIELD_NAME_DESCRIPTION, query.description);

        for (QueryConstraint constraint : query.constraints) {
            values.put(constraint.field, constraint.value);
        }

        Uri uri = resolver.insert(BugzillaProvider.URI_ADD_QUERY, values);
        String id = uri.getLastPathSegment();
        query.id = Long.parseLong(id);
        query.idValid = true;

        callback.requestComplete(0);
    }

    public void runQuery(BugzillaApplication app, long queryId, BugzillaProcessorCallback callback) {

        //if (query.idValid == false)
        //    return;
        
        ContentResolver resolver = app.getContentResolver();

        // before running the query, check the database to see if the
        // results table contains any records for the input
        // query, if so, do nothing
        Cursor resultCursor = resolver.query(
                ContentUris.withAppendedId(BugzillaProvider.URI_GET_RESULTS_COUNT, queryId), null,
                null, null, null);
        resultCursor.moveToFirst();
        int count = resultCursor.getInt(0);
        if (count > 0) {
            callback.requestComplete(0);
            return;
        }
        
        /* get query definition from database */
        Cursor queryCursor = resolver.query(
                ContentUris.withAppendedId(BugzillaProvider.URI_GET_QUERY, queryId), null,
                null, null, null);
        queryCursor.moveToFirst();
        
        String assignedTo = queryCursor.getString(queryCursor.getColumnIndex(BugzillaDatabase.FIELD_NAME_ASSIGNEE));

        Bugzilla bugzilla = app.getBugzilla();
        ArrayList<Bug> bugs = bugzilla.searchBugs(assignedTo);

        /* delete any existing records in "results" table */
        resolver.delete(ContentUris.withAppendedId(BugzillaProvider.URI_DELETE_RESULTS, queryId), null,
                null);

        if (bugs != null) {

            for (Bug bug : bugs) {

                /* create entry in "results" table */
                // queryId = query.id
                // bugId = bug.getID
                ContentValues values = new ContentValues();
                values.put(BugzillaDatabase.FIELD_NAME_QUERY_ID, queryId);
                values.put(BugzillaDatabase.FIELD_NAME_BUG_ID, bug.getID());
                resolver.insert(BugzillaProvider.URI_ADD_RESULT, values);

                // some bugs may already exist, others may not
                // TODO figure out update/insert

                // create/update entry in "bugs" table
                // bugId = bug.getID
                // summary = bug.getSummary
                // etc.

                values.clear();
                values.put(BugzillaDatabase.FIELD_NAME_BUG_ID, bug.getID());
                values.put(BugzillaDatabase.FIELD_NAME_SUMMARY, bug.getSummary());
                resolver.insert(BugzillaProvider.URI_ADD_OR_REPLACE_BUG, values);

            }
        }

        callback.requestComplete(0);
    }

}