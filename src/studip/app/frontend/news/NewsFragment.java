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
package studip.app.frontend.news;

import studip.app.backend.db.NewsContract;
import studip.app.backend.db.NewsRepository;
import studip.app.backend.db.UsersContract;
import studip.app.backend.net.services.syncservice.activitys.NewsResponderFragment;
import StudIPApp.app.R;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SimpleCursorAdapter;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * @author joern
 * 
 */
public class NewsFragment extends SherlockListFragment {
    public static String TAG = "NewsFragment";
    private Context mContext = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	mContext = getSherlockActivity();
	getActivity().setTitle(getString(R.string.News));
	FragmentManager fm = getFragmentManager();
	FragmentTransaction ft = fm.beginTransaction();

	NewsResponderFragment responderFragment = (NewsResponderFragment) fm
		.findFragmentByTag(getString(R.string.News));
	if (responderFragment == null) {
	    responderFragment = new NewsResponderFragment();
	    responderFragment.setFragment(this);
	    ft.add(responderFragment, getString(R.string.News));
	}
	ft.commit();

	setListAdapter(getNewListAdapter());
    }

    public SimpleCursorAdapter getNewListAdapter() {
	return new SimpleCursorAdapter(mContext, R.layout.news_item,
		getNewCursor(), new String[] { NewsContract.Columns.NEWS_TOPIC,
			UsersContract.Columns.USER_FORENAME,
			NewsContract.Columns.NEWS_DATE,
			NewsContract.Columns.NEWS_BODY }, new int[] {
			R.id.title, R.id.author, R.id.time, R.id.body });
    }

    public Cursor getNewCursor() {
	return NewsRepository.getInstance(mContext).getCurrentNewsCursor();
    }

}