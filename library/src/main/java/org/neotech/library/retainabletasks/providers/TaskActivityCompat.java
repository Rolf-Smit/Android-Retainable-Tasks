package org.neotech.library.retainabletasks.providers;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.TaskManager;
import org.neotech.library.retainabletasks.TaskManagerLifeCycleProxy;
import org.neotech.library.retainabletasks.TaskManagerProvider;

/**
 * <p>
 * A regular {@link AppCompatActivity} with support for {@link Task Tasks}. You can use the
 * {@link TaskActivityCompat#getTaskManager()} method to get the Activity's {@link TaskManager} and use it
 * to execute Tasks which will automatically be retained across configuration changes by the
 * {@link TaskActivityCompat}.</p>
 *
 * <p>
 * Each task that's started using the {@link TaskActivityCompat TaskActivty's} {@link TaskManager} will
 * automatically be retained, this happens during the {@link AppCompatActivity#onStart()} method.
 * You will receive calls to the {@link TaskActivityCompat#onPreAttach(Task)} method for each active
 * {@link Task} and you must return a {@link Task.Callback} listener for each of the Tasks.
 * </p>
 *
 * <p>
 * If you already use an extended version of the {@link AppCompatActivity} class you can implement
 * the {@link TaskActivityCompat TaskActivity's} behaviour yourself using the
 * {@link TaskManagerLifeCycleProxy}.</p>
 *
 * @see AppCompatActivity
 * @see TaskManagerLifeCycleProxy
 */
public class TaskActivityCompat extends AppCompatActivity implements TaskManagerProvider {

    private final TaskManagerLifeCycleProxy proxy = new TaskManagerLifeCycleProxy(this);

    @Override
    protected void onStart() {
        super.onStart();
        proxy.onStart();
    }

    @Override
    protected void onStop() {
        proxy.onStop();
        super.onStop();
    }

    @Override
    public final TaskManager getTaskManager() {
        return proxy.getTaskManager();
    }

    @Override
    public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
        return null;
    }
}