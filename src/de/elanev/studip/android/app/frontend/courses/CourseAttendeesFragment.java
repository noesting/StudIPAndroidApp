/*******************************************************************************
 * Copyright (c) 2013 ELAN e.V.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package de.elanev.studip.android.app.frontend.courses;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

import de.elanev.studip.android.app.R;
import de.elanev.studip.android.app.backend.datamodel.Course;
import de.elanev.studip.android.app.backend.datamodel.User;
import de.elanev.studip.android.app.backend.db.CoursesRepository;
import de.elanev.studip.android.app.backend.db.UsersRepository;
import de.elanev.studip.android.app.backend.net.services.syncservice.activitys.UsersResponderFragment;
import de.elanev.studip.android.app.frontend.util.AbstractBaseListFragment;

/**
 * @author joern
 * 
 */
public class CourseAttendeesFragment extends AbstractBaseListFragment {
	public static final String TAG = CourseAttendeesFragment.class
			.getSimpleName();
	private String mCid;
	public SherlockListFragment mFragment = this;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCid = getArguments().getString("cid");
		Course course = CoursesRepository.getInstance(getSherlockActivity())
				.getCourse(mCid);

		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		UsersResponderFragment responderFragment = (UsersResponderFragment) fm
				.findFragmentByTag("userResponder");
		if (responderFragment == null) {
			responderFragment = new UsersResponderFragment();
			responderFragment.setFragment(this);

			responderFragment.setArguments(getArguments());
			ft.add(responderFragment, "userResponder");
		}
		if (course != null) {
			responderFragment.mUsers = course.students;
			ft.commit();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		View detailView = inflater.inflate(R.layout.general_list_fragment,
				container, false);

		return detailView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(getNewListAdapter());
	}

	public UserAdapter getNewListAdapter() {
		UserAdapter adapter = new UserAdapter(getSherlockActivity());
		Course course = null;
		course = CoursesRepository.getInstance(getSherlockActivity())
				.getCourse(mCid);
		if (course != null) {
			UsersRepository userDb = UsersRepository
					.getInstance(getSherlockActivity());
			for (String uid : course.students) {
				User user = userDb.getUser(uid);
				if (user != null)
					adapter.add(new UserAdapterItem(user.getFullName()));
			}
		}

		return adapter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.elanev.studip.android.app.frontend.util.AbstractBaseListFragment#
	 * getNewCursor()
	 */
	@Override
	public Cursor getNewCursor() {
		return null;
	}

	private class UserAdapterItem {
		public String fullname;

		public UserAdapterItem(String fullname) {

			this.fullname = fullname;
		}
	}

	public class UserAdapter extends ArrayAdapter<UserAdapterItem> {

		public UserAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.user_item, null);
			// TODO load user image and show it
			// ImageView icon = (ImageView) convertView
			// .findViewById(R.id.user_image);
			// icon.setImageResource(USERICON);

			TextView title = (TextView) convertView.findViewById(R.id.fullname);
			title.setText(getItem(position).fullname);

			return convertView;
		}
	}

}