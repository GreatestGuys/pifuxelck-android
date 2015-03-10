package com.everythingissauce.pifuxelck.sync;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.Settings;
import com.everythingissauce.pifuxelck.ThreadUtil;
import com.everythingissauce.pifuxelck.api.Api;
import com.everythingissauce.pifuxelck.api.ApiProvider;
import com.everythingissauce.pifuxelck.auth.Identity;
import com.everythingissauce.pifuxelck.data.Game;
import com.everythingissauce.pifuxelck.data.InboxEntry;
import com.everythingissauce.pifuxelck.storage.HistoryStore;
import com.everythingissauce.pifuxelck.storage.IdentityProvider;
import com.everythingissauce.pifuxelck.storage.InboxStore;
import com.everythingissauce.pifuxelck.ui.HistoryActivity;
import com.everythingissauce.pifuxelck.ui.InboxActivity;
import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This SyncAdapter will pull in recently finished games and new inbox
 * entries from the Pifuxelck server.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

  private static final String TAG = "SyncAdapter";

  private static final int LIGHT_ON_MS = 1500;  // 1.5 seconds
  private static final int LIGHT_OFF_MS = 500;  // 0.5 seconds

  private static final int HISTORY_NOTIFICATION_ID = 0;
  private static final int INBOX_NOTIFICATION_ID = 1;

  private static final long[] VIBRATE_PATTERN = new long[] {
      0,     // Off
      2000,  // On

      1000,  // Off
      2000,  // On
  };

  private final Api mApi;
  private final HistoryStore mHistoryStore;
  private final IdentityProvider mIdentityProvider;
  private final InboxStore mInboxStore;
  private final Settings mSettings;

  public SyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);
    mApi = ApiProvider.getApi();
    mIdentityProvider = IdentityProvider.getInstance(context);
    mHistoryStore = new HistoryStore(context);
    mInboxStore = new InboxStore(context);
    mSettings = new Settings(context);
  }

  public SyncAdapter(
      Context context,
      boolean autoInitialize,
      boolean allowParallelSyncs) {
    super(context, autoInitialize, allowParallelSyncs);
    mApi = ApiProvider.getApi();
    mIdentityProvider = IdentityProvider.getInstance(context);
    mHistoryStore = new HistoryStore(context);
    mInboxStore = new InboxStore(context);
    mSettings = new Settings(context);
  }

  @Override
  public void onPerformSync(
      final Account account,
      final Bundle bundle,
      final String authority,
      final ContentProviderClient client,
      final SyncResult syncResult) {
     Futures.addCallback(
         syncHistory(mIdentityProvider, mApi, mHistoryStore),
         newNotificationCallback(
             R.string.new_history,
             HISTORY_NOTIFICATION_ID,
             HistoryActivity.class));

    Futures.addCallback(
        syncInbox(mIdentityProvider, mApi, mInboxStore),
        newNotificationCallback(
            R.string.new_inbox,
            INBOX_NOTIFICATION_ID,
            InboxActivity.class));
  }

  private FutureCallback<Integer> newNotificationCallback(
      final int titleString,
      final int id,
      final Class clazz) {
    final Resources res = getContext().getResources();
    final NotificationManager manager = (NotificationManager)
        getContext().getSystemService(Context.NOTIFICATION_SERVICE);

    return new FutureCallback<Integer>() {
      @Override
      public void onSuccess(Integer result) {
        if (result <= 0) {
          return;
        }

        // Create a pending intent with an artificially constructed back stack
        // for the given activity class.
        Intent resultIntent = new Intent(getContext(), clazz);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
        stackBuilder.addParentStack(clazz);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent pendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat
            .Builder(getContext())
            .setColor(res.getColor(R.color.accent))
            .setLights(res.getColor(R.color.accent), LIGHT_ON_MS, LIGHT_OFF_MS)
            .setSmallIcon(R.drawable.ic_create_white_18dp)
            .setContentTitle(res.getString(titleString))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        if (mSettings.shouldVibrate()) {
          notificationBuilder.setVibrate(VIBRATE_PATTERN);
        }

        manager.notify(id, notificationBuilder.build());
      }

      @Override
      public void onFailure(Throwable t) {
        // Do nothing.
      }
    };
  }

  public static ListenableFuture<Integer> syncInbox(
      final IdentityProvider identityProvider,
      final Api api,
      final InboxStore inboxStore) {
    if (!api.loggedIn()) {
      return loginAndTryInboxSyncAgain(identityProvider, api, inboxStore);
    }

    final Set<Long> initialIds = new HashSet<>(inboxStore.getEntryIds());
    return Futures.transform(
        api.inbox(),
        new Function<List<InboxEntry>, Integer>() {
          @Override
          public Integer apply(List<InboxEntry> entries) {
            int numNewEntries = 0;

            // Add new entries and determine which entries should be removed..
            for (InboxEntry entry : entries) {
              long id = entry.getGameId();
              if (!initialIds.contains(id)) {
                numNewEntries++;
                inboxStore.addEntry(entry);
              } else {
                // Remove IDs from the set that are still in the data store as we
                // go. That way when the entry list from the server has been
                // completely traversed, the only items that remain in initialIds
                // will be IDs that need to be removed.
                initialIds.remove(id);
              }
            }

            // Remove entries that are no longer in the inbox.
            for (Long id : initialIds) {
              inboxStore.remove(id);
            }

            return numNewEntries;
          }
        }, ThreadUtil.THREAD_POOL);
  }

  private static ListenableFuture<Integer> loginAndTryInboxSyncAgain(
      final IdentityProvider identityProvider,
      final Api api,
      final InboxStore inboxStore) {
    if(!identityProvider.hasIdentity()) {
      return Futures.immediateFailedCheckedFuture(
          new IllegalStateException("Not logged in"));
    }

    return Futures.transform(
        api.login(identityProvider.getIdentity()),
        new AsyncFunction<String, Integer>() {
          @Override
          public ListenableFuture<Integer> apply(String result) {
            return syncInbox(identityProvider, api, inboxStore);
          }
        }, ThreadUtil.THREAD_POOL);
  }

  public static ListenableFuture<Integer> syncHistory(
      final IdentityProvider identityProvider,
      final Api api,
      final HistoryStore historyStore) {
    if (!api.loggedIn()) {
      return loginAndTryHistorySyncAgain(identityProvider, api, historyStore);
    }

    final int initialSize = historyStore.getSize();
    return syncHistory(initialSize, api, historyStore);
  }

  private static ListenableFuture<Integer> syncHistory(
      final int initialSize,
      final Api api,
      final HistoryStore historyStore) {
    long lastGameCompletedTime = historyStore.getLastCompletedTime();
    final int previousSize = historyStore.getSize();

    return Futures.transform(
        api.history(lastGameCompletedTime),
        new AsyncFunction<List<Game>, Integer>() {
          @Override
          public ListenableFuture<Integer> apply(List<Game> games) {
            for (Game game : games) {
              historyStore.addGame(game);
            }

            int newSize = historyStore.getSize();

            // If there were no games in this query, then stop making network
            // requests, and update the UI. Otherwise, there might be more games,
            // so continue making network requests.
            if (previousSize == newSize) {
              return Futures.immediateFuture(newSize - initialSize);
            } else {
              return syncHistory(initialSize, api, historyStore);
            }
          }
        }, ThreadUtil.THREAD_POOL);
  }

  private static ListenableFuture<Integer> loginAndTryHistorySyncAgain(
      final IdentityProvider identityProvider,
      final Api api,
      final HistoryStore historyStore) {
    if(!identityProvider.hasIdentity()) {
      return Futures.immediateFailedFuture(
          new IllegalStateException("Not logged in."));
    }

    return Futures.transform(
        api.login(identityProvider.getIdentity()),
        new AsyncFunction<String, Integer>() {
          @Override
          public ListenableFuture<Integer> apply(String result) {
            return syncHistory(identityProvider, api, historyStore);
          }
        }, ThreadUtil.THREAD_POOL);
  }
}