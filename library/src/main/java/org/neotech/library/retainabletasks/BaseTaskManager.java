package org.neotech.library.retainabletasks;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rolf on 29-2-2016.
 */
public class BaseTaskManager extends TaskManager {

    private static final String TAG = "SimpleTaskManager";

    protected final HashMap<String, Task<?, ?>> tasks = new HashMap<>();

    @Override
    public Task<?, ?> getTask(@NonNull String tag) {
        return tasks.get(tag);
    }

    @Override
    @MainThread
    public Task<?, ?> attach(@NonNull String tag, @NonNull Task.Callback callback){
        final Task<?, ?> task = tasks.get(tag);
        if(task == null){
            return null;
        }
        return attach(task, callback);
    }

    @Override
    @MainThread
    public Task<?, ?> attach(@NonNull String tag, @NonNull TaskAttachListener listener){
        final Task<?, ?> task = tasks.get(tag);
        if(task == null){
            return null;
        }
        return attach(task, listener.onPreAttach(task));
    }

    @Override
    public Task<?, ?> attach(@NonNull Task<?, ?> task, @NonNull Task.Callback callback) {
        logCallback(callback);
        task.setCallback(new CallbackShadow(callback));
        return task;
    }

    @Override
    public void attachAll(@NonNull Task.Callback callback, @NonNull String... tags) {
        for(String tag: tags){
            attach(tag, callback);
        }
    }

    @Override
    public void attachAll(@NonNull TaskAttachListener attachListener, @NonNull String... tags) {
        for(String tag: tags){
            attach(tag, attachListener);
        }
    }

    @Override
    public Task<?, ?> detach(@NonNull String tag) {
        final Task<?, ?> task = tasks.get(tag);
        if(task != null){
            task.removeCallback();
        }
        return task;
    }

    @Override
    public void detachAll(@NonNull String... tags) {
        for(String tag: tags){
            detach(tag);
        }
    }

    private void logCallback(final Task.Callback callback){
        if(callback instanceof Fragment || callback instanceof android.app.Fragment){
            Log.i(TAG, "Important: Found attached Callback which is an instance of Fragment. If your Fragment doesn't follow the activity lifecycle make sure to detach the task in Fragment.onStop()!");
        }
    }

    @Override
    @MainThread
    public <Progress, Result> void execute(@NonNull Task<Progress, Result> task, @NonNull Task.Callback callback){
        final Task currentTask = tasks.get(task.getTag());
        if(currentTask != null && currentTask.isRunning()){
            throw new IllegalStateException("Task with an equal tag: '" + task.getTag() + "' has already been added and is currently running or finishing.");
        }
        tasks.put(task.getTag(), task);
        task.setCallback(new CallbackShadow(callback));
        TaskExecutor.execute(task);
    }

    @Override
    @MainThread
    public boolean isResultDelivered(@NonNull String tag) {
        Task task = tasks.get(tag);
        return task != null && task.isResultDelivered();
    }

    @Override
    @MainThread
    public boolean isRunning(@NonNull String tag) {
        Task task = tasks.get(tag);
        return task != null && task.isRunning();
    }


    @Override
    @MainThread
    public Task<?, ?> cancel(@NonNull String tag){
        final Task<?, ?> task = tasks.remove(tag);
        if(task != null){
            task.cancel(false);
        }
        return task;
    }

    @MainThread
    public void cancelAll(){
        for(Map.Entry<String, Task<?, ?>> task: tasks.entrySet()){
            task.getValue().cancel(true);
        }
    }

    @MainThread
    void detachListeners(){
        for(Map.Entry<String, Task<?, ?>> task: tasks.entrySet()){
            task.getValue().removeCallback();
        }
    }

    @MainThread
    private void removeFinishedTask(Task expectedTask){
        Task task = tasks.get(expectedTask.getTag());
        if(task != expectedTask){
            Log.i(TAG, "Task '" + expectedTask.getTag() + "' has already been removed, because another task has been added while this task was finishing.");
        }
        tasks.remove(expectedTask.getTag());
    }

    private final class CallbackShadow implements Task.AdvancedCallback  {

        private final Task.Callback callback;

        public CallbackShadow(Task.Callback callback) {
            this.callback = callback;
        }

        @Override
        public void onPreExecute(Task<?, ?> task) {
            callback.onPreExecute(task);
        }

        @Override
        public void onPostExecute(Task<?, ?> task) {
            removeFinishedTask(task);
            callback.onPostExecute(task);
        }

        @Override
        public void onProgressUpdate(Task<?, ?> task, Object progress) {
            if(callback instanceof Task.AdvancedCallback) {
                ((Task.AdvancedCallback) callback).onProgressUpdate(task, progress);
            }
        }

        @Override
        public void onCanceled(Task<?, ?> task) {
            removeFinishedTask(task);
            if(callback instanceof Task.AdvancedCallback) {
                ((Task.AdvancedCallback) callback).onCanceled(task);
            }
        }
    }
}
