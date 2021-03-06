package com.webaltry.bugz;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class QueryActivity extends Activity {

	public static final String QUERY_ID = "QUERY_ID";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.query);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

		TextView titleCaption = (TextView) findViewById(R.id.title_caption);
		titleCaption.setText("New Query");
		
		Button titleButton = (Button) findViewById(R.id.title_button);
		titleButton.setText("Save");

		BugzillaApplication app = (BugzillaApplication) getApplication();
		final Bugzilla bugz = app.getBugzilla();

		final TextView queryName = (TextView) findViewById(R.id.queryName);
		final TextView queryDescription = (TextView) findViewById(R.id.queryDescription);
		
		
		/* assigned-to */
		final TextView queryAssignee = (TextView) findViewById(R.id.queryAssignee);
		
		/* product */
		final MultiAutoCompleteTextView queryProduct = (MultiAutoCompleteTextView) findViewById(R.id.queryProduct);
		queryProduct.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		QueryFieldButton buttonProduct = (QueryFieldButton)findViewById(R.id.queryProductButton);
		buttonProduct.configure(BugzillaDatabase.FIELD_NAME_PRODUCT, "Select Product", queryProduct);
		
		/* status */
		final MultiAutoCompleteTextView queryStatus = (MultiAutoCompleteTextView) findViewById(R.id.queryStatus);
		queryStatus.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		QueryFieldButton buttonStatus = (QueryFieldButton)findViewById(R.id.queryStatusButton);
		buttonStatus.configure(BugzillaDatabase.FIELD_NAME_STATUS, "Select Status", queryStatus);
		
		/* priority */
		final MultiAutoCompleteTextView queryPriority = (MultiAutoCompleteTextView) findViewById(R.id.queryPriority);
		queryPriority.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		QueryFieldButton buttonPriority = (QueryFieldButton)findViewById(R.id.queryPriorityButton);
		buttonPriority.configure(BugzillaDatabase.FIELD_NAME_PRIORITY, "Select Priority", queryPriority);
		
		/* severity */
		final MultiAutoCompleteTextView querySeverity = (MultiAutoCompleteTextView) findViewById(R.id.querySeverity);
		queryStatus.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		QueryFieldButton buttonSeverity = (QueryFieldButton)findViewById(R.id.querySeverityButton);
		buttonSeverity.configure(BugzillaDatabase.FIELD_NAME_SEVERITY, "Select Severity", querySeverity);
		
		/* resolution */
		final MultiAutoCompleteTextView queryResolution = (MultiAutoCompleteTextView) findViewById(R.id.queryResolution);
		queryResolution.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		QueryFieldButton buttonResolution = (QueryFieldButton)findViewById(R.id.queryResolutionButton);
		buttonResolution.configure(BugzillaDatabase.FIELD_NAME_RESOLUTION, "Select Resolution", queryResolution);
	
		
		updateAutoCompleteValues();

		/* look for query id; if present, editing existing query */
		final Intent intent = getIntent();
		final boolean editingQuery = intent.hasExtra(QUERY_ID);

		if (editingQuery) {
			
			long queryId = getIntent().getLongExtra(QUERY_ID, 0);

			ContentResolver resolver = this.getContentResolver();

			/* get the query definition from the database */
			Cursor queryCursor = resolver.query(ContentUris.withAppendedId(
					BugzillaProvider.URI_GET_QUERY, queryId), null, null, null,
					null);
			queryCursor.moveToFirst();
			
			//String name = queryCursor.getString(queryCursor.getColumnIndex(BugzillaDatabase.FIELD_NAME_NAME));
			
			titleCaption.setText("Edit Query");

			/* name */
			queryName.setText(queryCursor.getString(queryCursor.getColumnIndex(BugzillaDatabase.FIELD_NAME_NAME)));

			/* description */
			queryDescription.setText(queryCursor.getString(queryCursor
					.getColumnIndex(BugzillaDatabase.FIELD_NAME_DESCRIPTION)));

			
            /* assigned-to */
            queryAssignee.setText(queryCursor.getString(queryCursor
                    .getColumnIndex(BugzillaDatabase.FIELD_NAME_ASSIGNEE)));

            /* product */
            queryProduct.setText(queryCursor.getString(queryCursor
                    .getColumnIndex(BugzillaDatabase.FIELD_NAME_PRODUCT)));

            /* status */
            queryStatus.setText(queryCursor.getString(queryCursor
                    .getColumnIndex(BugzillaDatabase.FIELD_NAME_STATUS)));

            /* priority */
            queryPriority.setText(queryCursor.getString(queryCursor
                    .getColumnIndex(BugzillaDatabase.FIELD_NAME_PRIORITY)));

            /* severity */
            querySeverity.setText(queryCursor.getString(queryCursor
                    .getColumnIndex(BugzillaDatabase.FIELD_NAME_SEVERITY)));

            /* resolution */
            queryResolution.setText(queryCursor.getString(queryCursor
                    .getColumnIndex(BugzillaDatabase.FIELD_NAME_RESOLUTION)));

		} else {

			queryName.setText("New Query");
			queryDescription.setText("");
			queryAssignee.setText("");
			queryProduct.setText("");
			queryStatus.setText("");
			queryPriority.setText("");
			querySeverity.setText("");
			queryResolution.setText("");
		}

		/*
		 * make sure connected to server; need the legal values for certain
		 * fields for the UI
		 */

		if (!bugz.isConnected()) {
			/* run the login activity */
			startActivityForResult(new Intent(this, LoginActivity.class), 1);
		}

		titleButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {

				Query query = new Query();
				query.name = queryName.getText().toString();
				query.description = queryDescription.getText().toString();

				
				/* assigned-to */
				String value = queryAssignee.getText().toString();
				if (!value.isEmpty())
					query.constraints.add(new QueryConstraint(
							BugzillaDatabase.FIELD_NAME_ASSIGNEE, value));

				/* product */
				value = queryProduct.getText().toString();
				if (!value.isEmpty())
					query.constraints.add(new QueryConstraint(
							BugzillaDatabase.FIELD_NAME_PRODUCT, value));

				/* status */
				value = queryStatus.getText().toString();
				if (!value.isEmpty())
					query.constraints.add(new QueryConstraint(
							BugzillaDatabase.FIELD_NAME_STATUS, value));

				/* priority */
				value = queryPriority.getText().toString();
				if (!value.isEmpty())
					query.constraints.add(new QueryConstraint(
							BugzillaDatabase.FIELD_NAME_PRIORITY, value));

				/* severity */
				value = querySeverity.getText().toString();
				if (!value.isEmpty())
					query.constraints.add(new QueryConstraint(
							BugzillaDatabase.FIELD_NAME_SEVERITY, value));

				/* resolution */
				value = queryResolution.getText().toString();
				if (!value.isEmpty())
					query.constraints.add(new QueryConstraint(
							BugzillaDatabase.FIELD_NAME_RESOLUTION, value));
				
				
				BugzillaApplication app = (BugzillaApplication) getApplication();
				BugzillaServiceHelper helper = app.getBugzillaServiceHelper();

				if (editingQuery) {
					
					query.id = getIntent().getLongExtra(QUERY_ID, 0);
					query.idValid = true;
					helper.updateQuery(query);
					
				} else {
					
					helper.createQuery(query);
				}
				
				/* end the new/edit query activity */
				finish();

			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		BugzillaApplication app = (BugzillaApplication) getApplication();
		Bugzilla bugz = app.getBugzilla();

		if (!bugz.isConnected()) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setTitle("Bugzilla");

			builder.setMessage(
					"Must connect to Bugzilla server to add or edit queries")
					.setCancelable(false)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									finish();
								}
							});

			AlertDialog alert = builder.create();

			alert.show();
			
		} else {
			
			/* after connecting, update auto-complete fields */
			updateAutoCompleteValues();
		}
	}

	private void updateAutoCompleteValues() {

		BugzillaApplication app = (BugzillaApplication) getApplication();
		Bugzilla bugz = app.getBugzilla();

		/* product */
		final MultiAutoCompleteTextView queryProduct = (MultiAutoCompleteTextView) findViewById(R.id.queryProduct);
		ArrayList<String> productValues = bugz.getValues(BugzillaDatabase.FIELD_NAME_PRODUCT);
		if (productValues != null)
			queryProduct.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_dropdown_item_1line, productValues));

		/* status */
		final MultiAutoCompleteTextView queryStatus = (MultiAutoCompleteTextView) findViewById(R.id.queryStatus);
		ArrayList<String> statusValues = bugz.getValues(BugzillaDatabase.FIELD_NAME_STATUS);
		if (statusValues != null)
			queryStatus.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_dropdown_item_1line, statusValues));

		/* priority */
		final MultiAutoCompleteTextView queryPriority = (MultiAutoCompleteTextView) findViewById(R.id.queryPriority);
		ArrayList<String> priorityValues = bugz.getValues(BugzillaDatabase.FIELD_NAME_PRIORITY);
		if (priorityValues != null)
			queryPriority.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_dropdown_item_1line, priorityValues));

		/* severity */
		final MultiAutoCompleteTextView querySeverity = (MultiAutoCompleteTextView) findViewById(R.id.querySeverity);
		ArrayList<String> severityValues = bugz.getValues(BugzillaDatabase.FIELD_NAME_SEVERITY);
		if (statusValues != null)
			querySeverity.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_dropdown_item_1line, severityValues));

		/* resolution */
		final MultiAutoCompleteTextView queryResolution = (MultiAutoCompleteTextView) findViewById(R.id.queryResolution);
		ArrayList<String> resolutionValues = bugz.getValues(BugzillaDatabase.FIELD_NAME_RESOLUTION);
		if (resolutionValues != null)
			queryResolution.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_dropdown_item_1line, resolutionValues));

	}
}
