/*
 * Copyright (c) 2015 ELAN e.V.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package de.elanev.studip.android.app.frontend.messages;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;

import de.elanev.studip.android.app.R;

public class MessageDetailActivity extends AppCompatActivity {

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // First request toolbar progrss indicator
    supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

    // Then set the content with toolbar
    setContentView(R.layout.content_frame);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setHomeButtonEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    setTitle(R.string.compose_message);

    Intent extras = getIntent();
    // No arguments, nothing to display, finish activity
    if (extras == null) {
      finish();
      return;
    }

    if (savedInstanceState == null) {
      MessageDetailFragment messageDetailFragment = MessageDetailFragment.newInstance(extras.getExtras());
      getSupportFragmentManager().beginTransaction()
          .add(R.id.content_frame, messageDetailFragment, MessageComposeFragment.class.getName())
          .commit();
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

}
