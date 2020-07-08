package xyz.ariefbayu.android.workmanagertutorial.worker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ForegroundInfo;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xyz.ariefbayu.android.workmanagertutorial.MainActivity;
import xyz.ariefbayu.android.workmanagertutorial.R;

public class ProcessingWorker extends Worker {

    Context currentContext;
    private NotificationManager notificationManager;

    public ProcessingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        currentContext = context;

        notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @NonNull
    @Override
    public Result doWork() {

        try {
            setForegroundAsync(createForegroundInfo("let me start the process"));
            Thread.sleep(60000 * 1);//1 minutes cycle
            setForegroundAsync(createForegroundInfo("completed"));
            String res = doTheActualProcessingWork();
            return Result.success(new Data.Builder().putString("RESULT", res).build());
        } catch (InterruptedException e) {
            Log.d("PWLOG", "Thread sleep failed...");
            e.printStackTrace();
            return Result.failure();
        }
    }

    private String doTheActualProcessingWork() {
        String res = "";
        Log.d("PWLOG", "Processing work...");

        setForegroundAsync(createForegroundInfo("receiving"));

        try{
            Thread.sleep(60000 * 1);//1 minutes cycle
        } catch (InterruptedException e){

        }

        OkHttpClient client = new OkHttpClient();

        String url = "https://webhook.site/816ea276-1111-4e87-ad2a-2ea7ec9f7ec8";
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            setForegroundAsync(createForegroundInfo("finished"));
            res = response.body().string();

        } catch (IOException e) {
            e.printStackTrace();
        }

        OneTimeWorkRequest refreshWork = new OneTimeWorkRequest.Builder(ProcessingWorker.class).build();
        WorkManager
                .getInstance(currentContext)
                .enqueueUniqueWork(MainActivity.PUSH_LOCATION_WORK_TAG, ExistingWorkPolicy.REPLACE, refreshWork);

        PeriodicWorkRequest periodicWork = new PeriodicWorkRequest.Builder(ProcessingWorker.class, 5, TimeUnit.MINUTES, 5, TimeUnit.MINUTES).build();

        return res;
    }

    @NonNull
    private ForegroundInfo createForegroundInfo(@NonNull String progress) {
        // Build a notification using bytesRead and contentLength

        Context context = getApplicationContext();
        String title = "PROGRESS";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        Notification notification = new NotificationCompat.Builder(context, "CHANID")
                .setContentTitle(title)
                .setTicker(progress)
                .setContentText(progress)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build();

        return new ForegroundInfo(1, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANID", "CHAN TEST", importance);
            channel.setDescription("This is test channel for notification");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = currentContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
