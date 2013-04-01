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
public class SemestersContract {

	public static final String TABLE = "semesters";

	public static final String CREATE_STRING = String.format(
			"create table if not exists %s (%s integer primary key, %s text unique,"
					+ " %s text, %s text, %s date, %s date, %s date, %s date)",
			TABLE, BaseColumns._ID, Columns.SEMESTER_ID,
			Columns.SEMESTER_TITLE, Columns.SEMESTER_DESCRIPTION,
			Columns.SEMESTER_BEGIN, Columns.SEMESTER_END,
			Columns.SEMESTER_SEMINARS_BEGIN, Columns.SEMESTER_SEMINARS_END);

	private SemestersContract() {
	}

	public static final class Columns implements BaseColumns {
		private Columns() {
		}

		public static final String SEMESTER_ID = "semester_id";
		public static final String SEMESTER_TITLE = "title";
		public static final String SEMESTER_DESCRIPTION = "description";
		public static final String SEMESTER_BEGIN = "begin";
		public static final String SEMESTER_END = "end";
		public static final String SEMESTER_SEMINARS_BEGIN = "seminars_begin";
		public static final String SEMESTER_SEMINARS_END = "seminars_end";
	}

}