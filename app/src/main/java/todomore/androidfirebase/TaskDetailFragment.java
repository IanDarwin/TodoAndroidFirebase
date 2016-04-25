package todomore.androidfirebase;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.darwinsys.todo.model.Context;
import com.darwinsys.todo.model.Project;
import com.darwinsys.todo.model.Task;

import todomore.androidfirebase.R;

/**
 * A fragment representing a single Task detail screen.
 * This fragment is either contained in a {@link TaskListActivity}
 * in two-pane mode (on tablets) or a {@link TaskDetailActivity}
 * on handsets.
 */
public class TaskDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_INDEX = "item_id";

    /**
     * The Task this fragment is presenting.
     */
    private Task mTask;

    /**
     * Mandatory empty constructor.
     */
    public TaskDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_INDEX)) {
            // Load the content specified by the fragment arguments.
            int id = getArguments().getInt(ARG_ITEM_INDEX, 0);
            mTask = ApplicationClass.sTasks.get(id).value;

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.task_detail, container, false);

        Activity activity = getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mTask.getName());
        }

        // Show the Task description as text in a TextView.
        if (mTask != null) {
            ((TextView) rootView.findViewById(R.id.task_detail_description)).setText(mTask.getDescription());
            ((TextView) rootView.findViewById(R.id.task_detail_priority)).setText(mTask.getPriority().toString());
            ((TextView) rootView.findViewById(R.id.task_detail_status)).setText(mTask.getStatus().toString());
            final Context context = mTask.getContext();
            if (context != null)
                ((TextView) rootView.findViewById(R.id.task_detail_context)).setText(context.toString());
            final Project project = mTask.getProject();
            if (project != null)
                ((TextView) rootView.findViewById(R.id.task_detail_project)).setText(project.toString());
            ((TextView) rootView.findViewById(R.id.task_detail_created)).setText(mTask.getCreationDate().toString());
        }

        return rootView;
    }
}
