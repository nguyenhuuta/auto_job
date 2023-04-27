package com.webseleniumdriver.task;

import com.webseleniumdriver.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TimeoutUtils {

    public interface ITimeoutCall {
        void onTimeout(String key, long time);
    }

    private Map<String, TimeoutTask> timeoutTaskMap = new HashMap<>();

    private static TimeoutUtils instance = new TimeoutUtils();

    private TimeoutUtils() {
    }

    public static void startTimeoutTask(String key, long timeout, ITimeoutCall call) {
        TimeoutTask timeoutTask = new TimeoutTask(key, timeout, call);
        TimeoutTask oldTask = instance.timeoutTaskMap.get(key);
        if (oldTask != null) {
            oldTask.cancel();
        }
        instance.timeoutTaskMap.put(key, timeoutTask);
        new Timer().schedule(timeoutTask, timeout);
    }

    public static void stopTimeOutTask(String key) {
        TimeoutTask stopTask = instance.timeoutTaskMap.get(key);
        if (stopTask != null) {
            stopTask.cancel();
        }
    }

    private static class TimeoutTask extends TimerTask {
        String key;
        long timeout;
        ITimeoutCall call;

        public TimeoutTask(String key, long timeout, ITimeoutCall call) {
            this.key = key;
            this.timeout = timeout;
            this.call = call;
        }

        @Override
        public void run() {
            if (call != null) {
                try {
                    call.onTimeout(key, timeout);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Logger.d("TimeoutTask", "Exception when callback onTimeout ", ex);
                }
            }
        }
    }
}
