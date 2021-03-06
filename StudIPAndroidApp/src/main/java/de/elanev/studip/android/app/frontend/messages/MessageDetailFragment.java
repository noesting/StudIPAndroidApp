/*
 * Copyright (c) 2014 ELAN e.V.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */
package de.elanev.studip.android.app.frontend.messages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.elanev.studip.android.app.R;
import de.elanev.studip.android.app.StudIPApplication;
import de.elanev.studip.android.app.backend.datamodel.Server;
import de.elanev.studip.android.app.backend.db.MessagesContract;
import de.elanev.studip.android.app.backend.db.UsersContract;
import de.elanev.studip.android.app.backend.net.oauth.OAuthConnector;
import de.elanev.studip.android.app.backend.net.util.StringRequest;
import de.elanev.studip.android.app.util.Prefs;
import de.elanev.studip.android.app.util.StuffUtil;
import de.elanev.studip.android.app.util.TextTools;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

/**
 * @author joern
 */
public class MessageDetailFragment extends Fragment implements LoaderCallbacks<Cursor> {
  public static final String TAG = MessageDetailFragment.class.getSimpleName();
  private static final int MESSAGE_REPLY = 1000;
  private static final int MESSAGE_FORWARD = 1001;
  protected final ContentObserver mObserver = new ContentObserver(new Handler()) {
    @Override
    public void onChange(boolean selfChange) {
      if (getActivity() == null) {
        return;
      }

      Loader<Cursor> loader = getLoaderManager().getLoader(0);
      if (loader != null) {
        loader.forceLoad();
      }
    }
  };
  private Context mContext;
  private Bundle mArgs;
  private String mMessageId, mSubject, mMessage, mSenderId, mSenderTitlePre, mSenderForename, mSenderLastname, mSenderTitlePost, mUserImageUrl;
  private long mDate;
  private String mApiUrl;
  private TextView mMessageSubjectTextView;
  private TextView mMessageDateTextView;
  private TextView mMessageBodyTextView;
  private TextView mMessageAuthorTextView;
  private ImageView mUserImageView;
  private boolean mDeleteButtonVisible = true;

  public MessageDetailFragment() {}

  /**
   * Returns a new instance of MessageDetailFragment and sets its arguments with the passed
   * bundle.
   *
   * @param arguments arguments to set to fragment
   * @return new instance of MessageDetailFragment
   */
  public static MessageDetailFragment newInstance(Bundle arguments) {
    MessageDetailFragment fragment = new MessageDetailFragment();

    fragment.setArguments(arguments);

    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mArgs = getArguments();
    mContext = getActivity();
    mApiUrl = Prefs.getInstance(getActivity()).getServer().getApiUrl();

  }

  @Override
  public View onCreateView(LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_message_detail, container, false);
    mMessageSubjectTextView = (TextView) v.findViewById(R.id.message_subject);
    mMessageDateTextView = (TextView) v.findViewById(R.id.text2);
    mMessageAuthorTextView = (TextView) v.findViewById(R.id.text1);
    mMessageBodyTextView = (TextView) v.findViewById(R.id.message_body);
    mUserImageView = (ImageView) v.findViewById(R.id.user_image);
    return v;

  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setHasOptionsMenu(true);

    // initialize CursorLoader
    getLoaderManager().initLoader(0, mArgs, this);

  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    activity.getContentResolver()
        .registerContentObserver(MessagesContract.CONTENT_URI_MESSAGES, true, mObserver);
  }

  @Override
  public void onDetach() {
    super.onDetach();
    getActivity().getContentResolver().unregisterContentObserver(mObserver);
  }

	/*
     * loader callbacks
	 */

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.message_detail_menu, menu);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    menu.findItem(R.id.delete_message).setVisible(mDeleteButtonVisible);

    if (!mDeleteButtonVisible) {
      ((AppCompatActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(true);
    } else {
      ((AppCompatActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(false);
    }

    super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case R.id.delete_message:
        if (getActivity() != null) {
          mDeleteButtonVisible = false;
          getActivity().supportInvalidateOptionsMenu();
        }

        String contactsUrl = String.format(getString(R.string.restip_messages_messageid),
            mApiUrl,
            mMessageId);
        StringRequest request = new StringRequest(Method.DELETE,
            contactsUrl,
            new Listener<String>() {
              public void onResponse(String response) {

                if (getActivity() != null && isAdded()) {
                  getActivity().finish();
                  Toast.makeText(mContext, getString(R.string.message_deleted), Toast.LENGTH_SHORT)
                      .show();
                }

                mContext.getContentResolver()
                    .delete(MessagesContract.CONTENT_URI_MESSAGES.buildUpon()
                            .appendPath(mMessageId)
                            .build(), null, null
                    );
              }
            },
            new ErrorListener() {
              /*
               * (non-Javadoc)
               *
               * @see com.android.volley.Response.ErrorListener
               * #onErrorResponse(com.android.volley. VolleyError)
               */
              public void onErrorResponse(VolleyError error) {
                if (getActivity() != null) {
                  mDeleteButtonVisible = true;
                  getActivity().supportInvalidateOptionsMenu();

                  if (error.getMessage() != null) Log.e(TAG, error.getMessage());

                  if (getActivity() != null && isAdded()) Toast.makeText(mContext,
                      getString(R.string.something_went_wrong) + error.getMessage(),
                      Toast.LENGTH_SHORT
                  ).show();
                }
              }
            }
        );

        try {
          Server server = Prefs.getInstance(mContext).getServer();
          OAuthConnector.with(server).sign(request);
        } catch (OAuthMessageSignerException e) {
          e.printStackTrace();
        } catch (OAuthExpectationFailedException e) {
          e.printStackTrace();
        } catch (OAuthCommunicationException e) {
          e.printStackTrace();
        } catch (OAuthNotAuthorizedException e) {
          StuffUtil.startSignInActivity(mContext);
        }
        StudIPApplication.getInstance().addToRequestQueue(request);

        return true;

      case R.id.forward_message:
        startMessageComposeActivityWithFlag(MESSAGE_FORWARD);
        return true;

      case R.id.reply_message:
        startMessageComposeActivityWithFlag(MESSAGE_REPLY);
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }

  }

  private void startMessageComposeActivityWithFlag(int flag) {

    Intent intent = new Intent(mContext, MessageComposeActivity.class);

    intent.putExtra(MessagesContract.Columns.Messages.MESSAGE, mMessage);
    intent.putExtra(MessagesContract.Columns.Messages.MESSAGE_SUBJECT, mSubject);
    intent.putExtra(MessagesContract.Columns.Messages.MESSAGE_MKDATE, mDate);
    intent.putExtra(UsersContract.Columns.USER_ID, mSenderId);
    intent.putExtra(UsersContract.Columns.USER_TITLE_PRE, mSenderTitlePre);
    intent.putExtra(UsersContract.Columns.USER_FORENAME, mSenderForename);
    intent.putExtra(UsersContract.Columns.USER_LASTNAME, mSenderLastname);
    intent.putExtra(UsersContract.Columns.USER_TITLE_POST, mSenderTitlePost);
    intent.putExtra("MessageFlag", flag);

    startActivity(intent);

  }

  public Loader<Cursor> onCreateLoader(int id, Bundle data) {

    return new CursorLoader(mContext,
        MessagesContract.CONTENT_URI_MESSAGES.buildUpon()
            .appendPath(data.getString(MessagesContract.Columns.Messages.MESSAGE_ID))
            .build(),
        MessageQuery.projection,
        null,
        null,
        MessagesContract.DEFAULT_SORT_ORDER_MESSAGES
    );
  }

  public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    if (getActivity() == null || cursor.getCount() < 1) {
      return;
    }

    cursor.moveToFirst();

    mMessageId = cursor.getString(cursor.getColumnIndex(MessagesContract.Columns.Messages.MESSAGE_ID));
    mSubject = cursor.getString(cursor.getColumnIndex(MessagesContract.Columns.Messages.MESSAGE_SUBJECT));
    getActivity().setTitle(mSubject);
    mDate = cursor.getLong(cursor.getColumnIndex(MessagesContract.Columns.Messages.MESSAGE_MKDATE));
    mMessage = cursor.getString(cursor.getColumnIndex(MessagesContract.Columns.Messages.MESSAGE));
    mSenderTitlePre = cursor.getString(cursor.getColumnIndex(UsersContract.Columns.USER_TITLE_PRE));
    mSenderForename = cursor.getString(cursor.getColumnIndex(UsersContract.Columns.USER_FORENAME));
    mSenderLastname = cursor.getString(cursor.getColumnIndex(UsersContract.Columns.USER_LASTNAME));
    mSenderTitlePost = cursor.getString(cursor.getColumnIndex(UsersContract.Columns.USER_TITLE_POST));
    mSenderId = cursor.getString(cursor.getColumnIndex(UsersContract.Columns.USER_ID));
    mUserImageUrl = cursor.getString(cursor.getColumnIndex(UsersContract.Columns.USER_AVATAR_NORMAL));

    mMessageBodyTextView.setMovementMethod(new ScrollingMovementMethod());

    mMessageSubjectTextView.setText(mSubject);

    if (mMessage != null) {
      mMessageBodyTextView.setText(Html.fromHtml(mMessage));
    }
    mMessageAuthorTextView.setText(TextTools.createNameSting(
        mSenderTitlePre,
        mSenderForename,
        mSenderLastname,
        mSenderTitlePost));
    mMessageDateTextView.setText(TextTools.getLocalizedTime(mDate, mContext));

    Picasso.with(mContext).load(mUserImageUrl)
        .resizeDimen(R.dimen.user_image_icon_size, R.dimen.user_image_icon_size)
        .centerCrop()
        .placeholder(R.drawable.nobody_normal)
        .into(mUserImageView);
  }

  public void onLoaderReset(Loader<Cursor> loader) {
  }

  /*
   * messages adapter
   */
  private interface MessageQuery {
    String[] projection = {
        MessagesContract.Qualified.Messages.MESSAGES_ID,
        MessagesContract.Qualified.Messages.MESSAGES_MESSAGE_ID,
        MessagesContract.Qualified.Messages.MESSAGES_MESSAGE_MKDATE,
        MessagesContract.Qualified.Messages.MESSAGES_MESSAGE_SUBJECT,
        MessagesContract.Qualified.Messages.MESSAGES_MESSAGE_PRIORITY,
        MessagesContract.Qualified.Messages.MESSAGES_MESSAGE,
        MessagesContract.Qualified.Messages.MESSAGES_MESSAGE_UNREAD,

        UsersContract.Qualified.USERS_USER_TITLE_PRE,
        UsersContract.Qualified.USERS_USER_FORENAME,
        UsersContract.Qualified.USERS_USER_LASTNAME,
        UsersContract.Qualified.USERS_USER_TITLE_POST,
        UsersContract.Qualified.USERS_USER_ID,
        UsersContract.Qualified.USERS_USER_AVATAR_NORMAL
    };
  }

}
