/*******************************************************************************
 * Copyright (c) 2013 ELAN e.V.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
/**
 * 
 */
package de.elanev.studip.android.app.backend.db;

import android.provider.BaseColumns;

/**
 * @author joern
 * 
 */
public class ActivitiesContract {
	public static final String TABLE = "activities";
	public static final String CREATE_STRING = String
			.format("create table if not exists %s (%s integer primary key, %s text unique, "
					+ "%s text, %s text, %s text, %s text, %s text, %s text, %s text, %s date)",
					TABLE, BaseColumns._ID, Columns.ACTIVITY_ID,
					Columns.ACTIVITY_TITLE, Columns.ACTIVITY_AUTHOR,
					Columns.ACTIVITY_AUTHOR_ID, Columns.ACTIVITY_LINK,
					Columns.ACTIVITY_SUMMARY, Columns.ACTIVITY_CONTENT,
					Columns.ACTIVITY_CATEGORY, Columns.ACTIVITY_DATE);

	public ActivitiesContract() {
	}

	public static final class Columns implements BaseColumns {
		private Columns() {
		}

		public static final String ACTIVITY_ID = "id";
		public static final String ACTIVITY_TITLE = "title";
		public static final String ACTIVITY_AUTHOR = "author";
		public static final String ACTIVITY_AUTHOR_ID = "author_id";
		public static final String ACTIVITY_LINK = "link";
		public static final String ACTIVITY_SUMMARY = "summary";
		public static final String ACTIVITY_CONTENT = "content";
		public static final String ACTIVITY_CATEGORY = "category";
		public static final String ACTIVITY_DATE = "updated";
	}
}