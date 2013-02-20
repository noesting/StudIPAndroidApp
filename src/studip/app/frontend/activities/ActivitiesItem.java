/*******************************************************************************
 * Copyright (c) 2013 ELAN e.V.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package studip.app.frontend.activities;


import studip.app.backend.datamodel.Activity;
import studip.app.backend.datamodel.User;
import studip.app.frontend.util.ArrayAdapterItem;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivitiesItem implements ArrayAdapterItem {

	public Activity activity; 

	public User author;
	
	public TextView authorTV, timeTV, titleTV, bodyTV;
	
	public ImageView authorIV;
	
	public ActivitiesItem(Activity activity, User author) {
		this.activity = activity;
		this.author = author;
	}
	
}
