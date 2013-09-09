/*******************************************************************************
 * Copyright (c) 2013 ELAN e.V.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package de.elanev.studip.android.app.frontend.courses;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import de.elanev.studip.android.app.R;
import de.elanev.studip.android.app.frontend.util.BaseSlidingFragmentActivity;

/**
 * Activity which holds the courses list fragment
 * 
 * @author joern
 * 
 */
public class CoursesActivity extends BaseSlidingFragmentActivity {

	/**
	 * Public constructor without parameters.
	 */
	public CoursesActivity() {
		super(R.string.Courses);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.elanev.studip.android.app.frontend.util.BaseSlidingFragmentActivity
	 * #onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_frame);

		if (savedInstanceState != null)
			return;

		FragmentManager fm = getSupportFragmentManager();
		CoursesFragment frag = null;

		frag = (CoursesFragment) fm.findFragmentByTag(CoursesFragment.class
				.getName());

		if (frag == null)
			frag = (CoursesFragment) CoursesFragment.instantiate(this,
					CoursesFragment.class.getName());

		fm.beginTransaction()
				.add(R.id.content_frame, frag, CoursesFragment.class.getName())
				.commit();
	}
}