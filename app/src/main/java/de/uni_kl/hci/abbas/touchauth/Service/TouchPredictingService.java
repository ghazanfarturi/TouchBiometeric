package de.uni_kl.hci.abbas.touchauth.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import de.uni_kl.hci.abbas.touchauth.Activity.MainActivity;
import de.uni_kl.hci.abbas.touchauth.Model.SVM;
import de.uni_kl.hci.abbas.touchauth.Model.TouchFeatureExtraction;
import de.uni_kl.hci.abbas.touchauth.R;
import de.uni_kl.hci.abbas.touchauth.Util.DataUtils;
import de.uni_kl.hci.abbas.touchauth.Util.FileUtils;

public class TouchPredictingService extends Service {
    public static double confidence;

    // Get External Storage Directory & the filename of raw data and features
    final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auth/Touch";
    final String clickFeatureFilename = dir + "/click_test_features.txt";
    final String slideFeatureFilename = dir + "/slide_test_features.txt";
    final String clickResultFilename = dir + "/click_result_features.txt";
    final String slideResultFilename = dir + "/slide_result_features.txt";

    final String clickCoefsFilename = dir + "/click_coefs.txt";
    final String slideCoefsFilename = dir + "/slide_coefs.txt";


    public TouchPredictingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        improvePriority();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                predict();
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    private void improvePriority() {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Touch Auth")
                .setContentText("Predicting Service Started.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        notification.contentIntent = contentIntent;
        startForeground(1, notification);
    }

    private void predict() {

        TouchDataCollectingService.collect(new TouchDataCollectingService.PostEventMethod(){

            private int slideNum = 0;
            private int clickNum = 0;
            private double[][] slideFeatures = new double[10][16];
            private double[][] clickFeatures = new double[10][2];

            @Override
            public Void call() throws Exception {

                double[] feature = TouchFeatureExtraction.extract(event);
                if (feature.length < 5) {
                    clickFeatures[clickNum] = feature;
                    FileUtils.writeFileFromNums(clickFeatureFilename, clickFeatures[clickNum++], true, false, 1);
                    if (clickNum >= 9) {
                        boolean ans = getPredictResult(DataUtils.cleanData(clickFeatures, true), true);
                        FileUtils.writeFile(clickResultFilename, ans + "\n", true);
                        clickFeatures = new double[10][2];
                        clickNum = 0;
                    }
                }
                else {
                    slideFeatures[slideNum] = feature;
                    FileUtils.writeFileFromNums(slideFeatureFilename, slideFeatures[slideNum++], true, false, 1);
                    if (slideNum >= 9) {
                        boolean ans = getPredictResult(DataUtils.cleanData(slideFeatures, true), false);
                        FileUtils.writeFile(slideResultFilename, ans + "\n", true);
                        slideFeatures = new double[10][16];
                        slideNum = 0;
                    }
                }
                return null;
            }
        });

    }

    private boolean getPredictResult(double[][] vectors, boolean isClick) {
        double positiveSum = 0, negativeSum;
        String filename = isClick ? clickCoefsFilename : slideCoefsFilename;
        SVM model = isClick ? MainActivity.clickModel : MainActivity.slideModel;

        vectors = DataUtils.scaleData(vectors, filename, true);
        for (double[] vector : vectors) {
            double ans = model.predict(vector);
            positiveSum += ans;
        }
        confidence = positiveSum / 10.0;
        return positiveSum > 2;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }
}
