package xyz.ariefbayu.android.workmanagertutorial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.util.Log;

import xyz.ariefbayu.android.workmanagertutorial.worker.ProcessingWorker;

public class MainActivity extends AppCompatActivity {

    public static String PUSH_LOCATION_WORK_TAG = "PUSH_LOCATION_WORK_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OneTimeWorkRequest refreshWork = new OneTimeWorkRequest.Builder(ProcessingWorker.class).build();

        WorkManager
                .getInstance(getApplicationContext())
//                .getWorkInfoByIdLiveData(refreshWork.getId())
//                .observe(MainActivity.this, workInfo -> {
//                    String progress = "";
//                    if(workInfo != null){
//                        progress = workInfo.getProgress().getString("PROGRESS");
//                        Log.d("__LOG__", "Progress: progress");
//                    }
//                    Log.d("__LOG__", progress);
//                });
                .enqueueUniqueWork(PUSH_LOCATION_WORK_TAG, ExistingWorkPolicy.KEEP, refreshWork);
    }
}
