/*
 * Copyright (c) 2014 ELAN e.V.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package de.elanev.studip.android.app.frontend.courses;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

import de.elanev.studip.android.app.R;
import de.elanev.studip.android.app.StudIPApplication;
import de.elanev.studip.android.app.backend.datamodel.Document;
import de.elanev.studip.android.app.backend.datamodel.DocumentFolder;
import de.elanev.studip.android.app.backend.datamodel.DocumentFolders;
import de.elanev.studip.android.app.backend.datamodel.Server;
import de.elanev.studip.android.app.backend.db.CoursesContract;
import de.elanev.studip.android.app.backend.db.DocumentsContract;
import de.elanev.studip.android.app.backend.net.oauth.OAuthConnector;
import de.elanev.studip.android.app.backend.net.util.JacksonRequest;
import de.elanev.studip.android.app.util.ApiUtils;
import de.elanev.studip.android.app.util.Prefs;
import de.elanev.studip.android.app.util.StuffUtil;
import de.elanev.studip.android.app.util.TextTools;
import de.elanev.studip.android.app.widget.ProgressListFragment;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by joern on 03.02.14
 */
public class CourseDocumentsFragment extends Fragment {

  static final String TAG = CourseDocumentsFragment.class.getSimpleName();
  /**
   * Broadcast receiver listening for completion of a download
   */
  protected final BroadcastReceiver mDownloadManagerReceiver = new BroadcastReceiver() {

    @Override public void onReceive(Context context, Intent intent) {
      DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
      String action = intent.getAction();

      if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {

        long receivedID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(receivedID);

        Cursor cursor = mgr.query(query);
        int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);

        if (cursor.moveToFirst()) {
          queryStatus(cursor.getInt(statusIndex), cursor.getInt(reasonIndex));
        }

        cursor.close();
      }
    }

  };

  public CourseDocumentsFragment() {}

  public static CourseDocumentsFragment newInstance(Bundle arguments) {
    CourseDocumentsFragment fragment = new CourseDocumentsFragment();

    fragment.setArguments(arguments);

    return fragment;
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    // registering the broadcast receiver for completed downloads
    activity.registerReceiver(mDownloadManagerReceiver,
        new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
  }

  @Override public View onCreateView(LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {

    final FrameLayout wrapperLayout = new FrameLayout(getActivity());
    wrapperLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT));
    wrapperLayout.setId(R.id.document_list_wrapper);

    return wrapperLayout;
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    DocumentsListFragment frag = DocumentsListFragment.newInstance(getArguments());
    getChildFragmentManager().beginTransaction().replace(R.id.document_list_wrapper, frag).commit();

  }

  @Override public void onSaveInstanceState(Bundle outState) {
    outState.putAll(getArguments());
    super.onSaveInstanceState(outState);
  }

  @Override public void onDetach() {
    super.onDetach();
    // unregister the broadcast receiver to save resources
    getActivity().unregisterReceiver(mDownloadManagerReceiver);
  }

  private void queryStatus(int queryStatus, int queryReason) {

    switch (queryStatus) {
      case DownloadManager.STATUS_FAILED:
        showToastMessage(getDownloadFailedReason(queryReason));
        break;

      case DownloadManager.STATUS_SUCCESSFUL:
        showToastMessage(R.string.download_completed);
        try {
          // Show the download activity
          startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
        } catch (ActivityNotFoundException e) {
          // No download manager installed or active, let the user handle the downloads.
        }

        break;

      default:
        showToastMessage(R.string.unknown_error);
        break;
    }

  }

  private void showToastMessage(int stringRes) {
    if (isAdded()) Toast.makeText(getActivity(), stringRes, Toast.LENGTH_SHORT).show();
  }

  private static int getDownloadFailedReason(int queryReason) {
    switch (queryReason) {
      case DownloadManager.ERROR_CANNOT_RESUME:
        return R.string.error_cannot_resume;

      case DownloadManager.ERROR_DEVICE_NOT_FOUND:
        return R.string.error_device_not_found;

      case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
        return R.string.error_file_already_exists;

      case DownloadManager.ERROR_FILE_ERROR:
        return R.string.error_file_error;

      case DownloadManager.ERROR_HTTP_DATA_ERROR:
        return R.string.error_http_data_error;

      case DownloadManager.ERROR_INSUFFICIENT_SPACE:
        return R.string.error_insufficient_space;

      case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
        return R.string.error_to_many_redirects;

      case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
        return R.string.error_unhandled_error_code;

      default:
        return R.string.unknown_error;
    }

  }

  public static class DocumentsListFragment extends ProgressListFragment implements
      AdapterView.OnItemClickListener, StickyListHeadersListView.OnHeaderClickListener,
      SwipeRefreshLayout.OnRefreshListener {


    private static final String FOLDER_NAME = "folder_name";
    private Server mServer;
    private DocumentsAdapter mAdapter;
    private String mCourseId;
    private String mFolderId;
    private String mFolderName;

    public DocumentsListFragment() {}

    @Override public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      mCourseId = getArguments().getString(CoursesContract.Columns.Courses.COURSE_ID);
      mServer = Prefs.getInstance(getActivity()).getServer();
      mAdapter = new DocumentsAdapter(getActivity(), new ArrayList<Object>());
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      setEmptyMessage(R.string.no_documents);

      mSwipeRefreshLayoutListView.setOnRefreshListener(this);

      mListView.setOnItemClickListener(this);
      mListView.setOnHeaderClickListener(this);
      mListView.setAdapter(mAdapter);

    }

    @Override public void onStart() {
      super.onStart();
      mFolderId = getArguments().getString(DocumentsContract.Columns.DocumentFolders.FOLDER_ID);
      mFolderName = getArguments().getString(FOLDER_NAME);
      downloadDocumentsForFolder(mCourseId, mFolderId, mFolderName);
    }

    private void downloadDocumentsForFolder(String courseId,
        final String folderId,
        final String folderName) {
      setLoadingViewVisible(true);
      String foldersUrl;
      if (folderId != null) {
        foldersUrl =
            String.format(mContext.getString(R.string.restip_documents_rangeid_folder_folderid),
                mServer.getApiUrl(),
                courseId,
                folderId) + ".json";
      } else {
        foldersUrl = String.format(mContext.getString(R.string.restip_documents_rangeid_folder),
            mServer.getApiUrl(),
            courseId);
      }

      JacksonRequest<DocumentFolders> documentFoldersRequest = new JacksonRequest<DocumentFolders>(
          foldersUrl,
          DocumentFolders.class,
          null,
          new Response.Listener<DocumentFolders>() {
            public void onResponse(DocumentFolders response) {
              final List<Object> list = new ArrayList<Object>();
              if (folderId != null && folderName != null) {
                list.add(new BackButtonListEntry());
              }
              list.addAll(response.folders);
              list.addAll(response.documents);

              mAdapter.updateData(list, folderName);
              setLoadingViewVisible(false);
              mSwipeRefreshLayoutListView.setRefreshing(false);
            }

          },
          new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
              if (getActivity() != null && error != null && error.getMessage() != null) {
                Log.wtf(TAG, error.getMessage());
                Toast.makeText(mContext, R.string.sync_error_generic, Toast.LENGTH_LONG).show();
                mSwipeRefreshLayoutListView.setRefreshing(false);
                setLoadingViewVisible(false);
              }
            }
          }

          ,
          Request.Method.GET);

      DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(30000,
          DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
          DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

      documentFoldersRequest.setRetryPolicy(retryPolicy);
      documentFoldersRequest.setPriority(Request.Priority.IMMEDIATE);

      try {
        OAuthConnector.with(mServer).sign(documentFoldersRequest);
        StudIPApplication.getInstance().addToRequestQueue(documentFoldersRequest, TAG);
        mSwipeRefreshLayoutListView.setRefreshing(true);
      } catch (OAuthExpectationFailedException e) {
        e.printStackTrace();
      } catch (OAuthCommunicationException e) {
        e.printStackTrace();
      } catch (OAuthMessageSignerException e) {
        e.printStackTrace();
      } catch (OAuthNotAuthorizedException e) {
        StuffUtil.startSignInActivity(mContext);
      }
    }

    @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      Object obj = mAdapter.getItem(position);
      if (obj instanceof BackButtonListEntry) {
        getActivity().onBackPressed();
      } else if (obj instanceof DocumentFolder) {
        String folderName = ((DocumentFolder) obj).name;
        String folderId = ((DocumentFolder) obj).folder_id;
        Bundle args = new Bundle();
        args.putString(CoursesContract.Columns.Courses.COURSE_ID, mCourseId);
        args.putString(DocumentsContract.Columns.DocumentFolders.FOLDER_ID, folderId);
        args.putString(FOLDER_NAME, folderName);

        DocumentsListFragment frag = DocumentsListFragment.newInstance(args);
        getParentFragment().getChildFragmentManager()
            .beginTransaction()
            .replace(R.id.document_list_wrapper, frag)
            .addToBackStack(null)
            .commit();
      } else if (obj instanceof Document) {
        if (!ApiUtils.isOverApi14()) showWarningDialog();
        else downloadDocument((Document) obj);
      }
    }

    /**
     * Returns a new instance of DocumentsListFragment and sets its arguments with the passed
     * bundle.
     *
     * @param arguments arguments to set to fragment
     * @return new instance of DocumentsListFragment
     */
    public static DocumentsListFragment newInstance(Bundle arguments) {
      DocumentsListFragment fragment = new DocumentsListFragment();

      fragment.setArguments(arguments);

      return fragment;
    }

    private void showWarningDialog() {
      // Android Version to old, show an warning dialog instead
      FragmentManager fm = getFragmentManager();
      FragmentTransaction ft = fm.beginTransaction();
      Fragment prev = fm.findFragmentByTag("warning_dialog");
      if (prev != null) {
        ft.remove(prev);
      }
      ft.addToBackStack(null);

      Bundle args = new Bundle();
      args.putInt(WarningDialog.DIALOG_TITLE_RES, R.string.not_supported);
      args.putInt(WarningDialog.DIALOG_MESSAGE_RES, R.string.version_not_supported_message);

      WarningDialog.newInstance(args).show(ft, "warning_dialog");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB) private void downloadDocument(Document document) {
      String fileId = document.document_id;
      String fileName = document.filename;
      String fileDescription = document.description;
      DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
      String apiUrl = Prefs.getInstance(getActivity()).getServer().getApiUrl();

      boolean externalDownloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
          .mkdirs();

      // Query DownloadManager and check of file is already being downloaded
      boolean isDownloading = false;

      if (externalDownloadsDir) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_PAUSED |
            DownloadManager.STATUS_PENDING |
            DownloadManager.STATUS_RUNNING |
            DownloadManager.STATUS_SUCCESSFUL);
        Cursor cur = downloadManager.query(query);
        int col = cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
          isDownloading = (Environment.DIRECTORY_DOWNLOADS + fileName == cur.getString(col));
        }
        cur.close();
      }


      if (!isDownloading) {
        try {
          // Create the download URI
          String downloadUrl = String.format(getString(R.string.restip_documents_documentid_download),
              apiUrl,
              fileId);

          if (!ApiUtils.isOverApi14()) {
            downloadUrl = downloadUrl.replace("https://", "http://");
          }

          // Sign the download URL with the OAuth credentials and parse the URI
          Server server = Prefs.getInstance(mContext).getServer();
          String signedDownloadUrl = OAuthConnector.with(server).sign(downloadUrl);
          Uri downloadUri = Uri.parse(signedDownloadUrl);


          // Create the download request
          DownloadManager.Request request = new DownloadManager.Request(Uri.parse(signedDownloadUrl)).setAllowedNetworkTypes(
              DownloadManager.Request.NETWORK_WIFI
                  | DownloadManager.Request.NETWORK_MOBILE) // Only mobile and wifi allowed
              .setAllowedOverRoaming(false)                   // Disallow roaming downloading
              .setTitle(fileName)                             // Title of this download
              .setDescription(fileDescription);               // Description of this download
          if (externalDownloadsDir)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
          // download location and file name

          //Allowing the scanning by MediaScanner
          request.allowScanningByMediaScanner();
          request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

          try {
            downloadManager.enqueue(request);
          } catch (IllegalArgumentException e) {
            if (getActivity() != null) {
              Toast.makeText(getActivity(),
                  R.string.error_downloadmanager_disabled,
                  Toast.LENGTH_LONG).show();
            }
          }

        } catch (OAuthMessageSignerException e) {
          e.printStackTrace();
        } catch (OAuthExpectationFailedException e) {
          e.printStackTrace();
        } catch (OAuthCommunicationException e) {
          e.printStackTrace();
        } catch (OAuthNotAuthorizedException e) {
          StuffUtil.startSignInActivity(mContext);
        }
      }
    }

    @Override public void onHeaderClick(StickyListHeadersListView stickyListHeadersListView,
        View view,
        int i,
        long l,
        boolean b) {
      getActivity().onBackPressed();
    }

    @Override public void onRefresh() {
      downloadDocumentsForFolder(mCourseId, mFolderId, mFolderName);
    }

    public static class WarningDialog extends DialogFragment {


      public static final String DIALOG_TITLE_RES = "dialogTitleRes";
      public static final String DIALOG_MESSAGE_RES = "dialogMessageRes";
      private int mDialogTitleRes;
      private int mDialogMessageRes;

      public WarningDialog() {}

      /**
       * Returns an new instance of a WarningDialog fragment. The passed
       * arguments will be set for this instance.
       *
       * @param arguments Arguments to set for this particular instance
       * @return An WarningDialog fragment instance
       */
      public static WarningDialog newInstance(Bundle arguments) {
        WarningDialog fragment = new WarningDialog();

        fragment.setArguments(arguments);

        return fragment;
      }

      @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get url and title res from arguments
        mDialogTitleRes = getArguments().getInt(DIALOG_TITLE_RES);
        mDialogMessageRes = getArguments().getInt(DIALOG_MESSAGE_RES);

        return new AlertDialog.Builder(getActivity()).setTitle(mDialogTitleRes)
            .setMessage(mDialogMessageRes)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
              }
            })
            .create();
      }
    }

    private static class BackButtonListEntry {}

    private class DocumentsAdapter extends BaseAdapter implements StickyListHeadersAdapter {
      private LayoutInflater mInflater;
      private List<Object> mObjects;
      private String mSectionTitle;

      public DocumentsAdapter(Context context, List<Object> objects) {
        this.mInflater = LayoutInflater.from(context);
        this.mObjects = objects;
      }

      @Override public View getHeaderView(int position, View view, ViewGroup viewGroup) {

        if (view == null) {
          view = mInflater.inflate(R.layout.list_item_header, viewGroup, false);
          HeaderHolder holder = new HeaderHolder();
          holder.title = (TextView) view.findViewById(R.id.list_item_header_textview);
          view.setTag(holder);
        }

        HeaderHolder holder = (HeaderHolder) view.getTag();

        if (!TextUtils.isEmpty(mSectionTitle)) {
          holder.title.setText(mSectionTitle);
          holder.title.setVisibility(View.VISIBLE);
        } else {
          holder.title.setVisibility(View.GONE);
        }

        return view;
      }

      @Override public long getHeaderId(int position) {
        return 0;
      }

      @Override public int getCount() {
        return mObjects == null ? 0 : mObjects.size();

      }

      @Override public Object getItem(int position) {
        return mObjects.get(position);
      }

      @Override public long getItemId(int position) {
        return position;
      }

      @Override public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Object entry = mObjects.get(position);

        if (row == null) {
          row = mInflater.inflate(R.layout.list_item_two_text_icon, parent, false);
          DocumentHolder holder = new DocumentHolder();
          holder.title = (TextView) row.findViewById(R.id.text1);
          holder.subtitle = (TextView) row.findViewById(R.id.text2);
          holder.icon = (ImageView) row.findViewById(R.id.icon);
          row.setTag(holder);
        }

        DocumentHolder holder = (DocumentHolder) row.getTag();
        if (entry instanceof BackButtonListEntry) {
          holder.title.setText(R.string.back);
          holder.icon.setImageResource(R.drawable.ic_arrow_left_blue);
          //          holder.icon.setBackgroundColor(getResources().getColor(android.R.color.transparent));
          holder.subtitle.setVisibility(View.GONE);
        } else if (entry instanceof DocumentFolder) {
          DocumentFolder f = (DocumentFolder) entry;
          holder.icon.setImageResource(R.drawable.ic_folder);
          //          holder.icon.setBackgroundColor(getResources().getColor(R.color.studip_mobile_dark));
          holder.title.setText(f.name);
          String subText = String.format(getString(R.string.last_updated),
              TextTools.getTimeAgo(f.chdate, mContext));
          holder.subtitle.setText(subText);
          holder.subtitle.setVisibility(View.VISIBLE);
        } else {
          Document doc = (Document) entry;
          String docName = "Unnamed";
          docName = TextUtils.isEmpty(doc.name) ? doc.filename : doc.name;
          holder.icon.setImageResource(R.drawable.ic_file_generic);
          //          holder.icon.setBackgroundColor(getResources().getColor(R.color.studip_mobile_dark));
          holder.title.setText(docName);
          String subText = String.format(getString(R.string.downloads), doc.downloads);
          holder.subtitle.setText(subText + "\t\t\t" + TextTools.readableFileSize(doc.filesize));
          holder.subtitle.setVisibility(View.VISIBLE);
        }

        holder.icon.setColorFilter(getResources().getColor(R.color.studip_mobile_dark),
            PorterDuff.Mode.SRC_IN);
        return row;
      }

      @Override public boolean isEmpty() {
        return mObjects.isEmpty();
      }

      public void updateData(List<Object> data, String title) {
        mObjects.clear();
        mObjects.addAll(data);
        mSectionTitle = title;
        this.notifyDataSetChanged();
      }

      private class DocumentHolder {
        ImageView icon;
        TextView title, subtitle;
      }

      private class HeaderHolder {
        TextView title;
      }
    }
  }
}
