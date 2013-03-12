/*******************************************************************************
 * Copyright (c) 2013 ELAN e.V.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package studip.app.frontend.util;

import StudIPApp.app.R;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

/**
 * @author joern
 * 
 */
public class BaseSlidingFragmentActivity extends SlidingFragmentActivity {

    private int mTitleRes;
    protected ListFragment mFrag;

    public BaseSlidingFragmentActivity(int titleRes) {
	mTitleRes = titleRes;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setTitle(mTitleRes);

	/* Set menu */
	setBehindContentView(R.layout.menu_frame);
	FragmentTransaction t = this.getSupportFragmentManager()
		.beginTransaction();
	mFrag = new MenuFragment();
	t.replace(R.id.menu_frame, mFrag);
	t.commit();

	SlidingMenu sm = getSlidingMenu();
	sm.setShadowWidthRes(R.dimen.shadow_width);
	sm.setShadowDrawable(R.drawable.shadow);
	sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
	sm.setFadeDegree(0.35f);
	sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

	getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	setSlidingActionBarEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case android.R.id.home:
	    toggle();
	    return true;
	}
	return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	getSupportMenuInflater().inflate(R.menu.main, menu);
	return true;
    }

//    public class BasePagerAdapter extends FragmentPagerAdapter {
//	private List<Fragment> mFragments = new ArrayList<Fragment>();
//	private ViewPager mPager;
//
//	public BasePagerAdapter(FragmentManager fm, ViewPager vp) {
//	    super(fm);
//	    mPager = vp;
//	    mPager.setAdapter(this);
//	    for (int i = 0; i < 3; i++) {
//		addTab(new SampleListFragment());
//	    }
//	}
//
//	public void addTab(Fragment frag) {
//	    mFragments.add(frag);
//	}
//
//	@Override
//	public Fragment getItem(int position) {
//	    return mFragments.get(position);
//	}
//
//	@Override
//	public int getCount() {
//	    return mFragments.size();
//	}
//    }

}