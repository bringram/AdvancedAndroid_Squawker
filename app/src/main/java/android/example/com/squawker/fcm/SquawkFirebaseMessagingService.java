package android.example.com.squawker.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class SquawkFirebaseMessagingService extends FirebaseMessagingService {

    private static final String LOG_TAG = SquawkFirebaseMessagingService.class.getSimpleName();

    private static final String KEY_AUTHOR = SquawkContract.COLUMN_AUTHOR;
    private static final String KEY_AUTHOR_KEY = SquawkContract.COLUMN_AUTHOR_KEY;
    private static final String KEY_MESSAGE = SquawkContract.COLUMN_MESSAGE;
    private static final String KEY_DATE = SquawkContract.COLUMN_DATE;

    private static final int MAX_NOTIFICATION_CHARACTERS = 30;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();

        if (data.size() > 0) {
            sendNotification(data);
            addSquawkToDatabase(data);
        }
    }

    private void addSquawkToDatabase(final Map<String, String> data) {
        AsyncTask<Void, Void, Void> insertSquawkTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                ContentValues squawkValues = new ContentValues();
                squawkValues.put(SquawkContract.COLUMN_AUTHOR, data.get(KEY_AUTHOR));
                squawkValues.put(SquawkContract.COLUMN_AUTHOR_KEY, data.get(KEY_AUTHOR_KEY));
                squawkValues.put(SquawkContract.COLUMN_MESSAGE, data.get(KEY_MESSAGE));
                squawkValues.put(SquawkContract.COLUMN_DATE, data.get(KEY_DATE));

                getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI, squawkValues);
                return null;
            }

        };

        insertSquawkTask.execute();
    }

    private void sendNotification(final Map<String, String> data) {
        String author = data.get(KEY_AUTHOR);
        String message = data.get(KEY_MESSAGE);

        if (message.length() > MAX_NOTIFICATION_CHARACTERS) {
            message = message.substring(0, MAX_NOTIFICATION_CHARACTERS) + "...";
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_duck)
                .setContentTitle(String.format(getString(R.string.notification_message), author))
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
