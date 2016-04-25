package todomore.androidfirebase;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.darwinsys.todo.model.Priority;
import com.darwinsys.todo.model.Status;
import com.darwinsys.todo.model.Task;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import todomore.androidfirebase.R;

/**
 * The Main Activity of this application; displays a list of Tasks.
 * This activity has different presentations for handset and tablet-size devices.
 * On handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TaskDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class TaskListActivity extends AppCompatActivity {

    private final static String TAG = "TaskListActivity";
	/** Whether or not the activity is in two-pane mode, i.e. running on a wide device.  */
    private boolean mTwoPane;
	/** The ID of the current user */
    static String mCurrentUser = "idarwin";
	/** The View Adapter */
    private SimpleItemRecyclerViewAdapter mAdapter;

	// UI Controls
	/** A Name for a new task to add */
	private EditText mAddTF;
	/** The Priority chooser */
	private Spinner prioSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);


        ((ApplicationClass)getApplication()).getDatabase().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println("There are " + snapshot.getChildrenCount() + " Todo Tasks(s)");
                ApplicationClass.sTasks.clear();
                for (DataSnapshot dnlSnapshot: snapshot.getChildren()) {
                    Task task = dnlSnapshot.getValue(Task.class);
                    String jsonKey = dnlSnapshot.getKey();
                    ApplicationClass.sTasks.add(new KeyValueHolder<>(jsonKey, task));
                }
                Collections.sort(ApplicationClass.sTasks, tasksComparator);
                mAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(FirebaseError error) {
                Toast.makeText(getBaseContext(), "Task read cancelled!! " + error, Toast.LENGTH_LONG).show();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mAddTF = (EditText) findViewById(R.id.addTF);
		prioSpinner = (Spinner) findViewById(R.id.prioSpinner);
		prioSpinner.setSelection(Priority.High.ordinal());

        View recyclerView = findViewById(R.id.task_list);
        assert recyclerView != null;
        mAdapter = new SimpleItemRecyclerViewAdapter(ApplicationClass.sTasks);
        ((RecyclerView) recyclerView).setAdapter(mAdapter);

        if (findViewById(R.id.task_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
	 * Adds a new item to the list, from the main screen.
	 * Called from the View when the Add button is pressed;
	 * registered via onClick= so no Listener code
	 */
	public void addItem(View v) {
		String name = mAddTF.getText().toString();
		if (name.isEmpty()) {
			Toast.makeText(this, "Text required!", Toast.LENGTH_SHORT).show();
			return;
		}

        Log.d(TAG, "addItem: trying to add " + name);

		/* Do the work here! Create a Task... */
		Task t = new Task();
		t.setName(mAddTF.getText().toString());
		t.setPriority(Priority.values()[prioSpinner.getSelectedItemPosition()]);
		t.setModified(System.currentTimeMillis());
		t.setStatus(Status.NEW);

		/* NOW: send it to the cloud... */
        Firebase push = ((ApplicationClass)getApplication()).getDatabase().push();
        push.setValue(t);
        Log.d(TAG, "Local key for pushed Task is " + push.getKey());

        // We don't have to add it to the list: our list listener will get an Event soon...

        // ... but if we get here, it's been fed into Fire so we don't need to keep it.
        mAddTF.setText("");
	}


    /**
     * Comparator is needed because the the Firebase sorting is so flamin' basic!
     * "Queries can only order by one key at a time. Calling orderByChild()
     * multiple times on the same query throws an error."
     * So we give up and use a Comparator.
     * Sort first by priority DESC, then by name ASC.
     */
    public static final Comparator<KeyValueHolder<String,Task>> tasksComparator = new Comparator<KeyValueHolder<String,Task>>() {

        @Override
        public int compare(KeyValueHolder<String,Task> thing1, KeyValueHolder<String,Task> thing2) {
            Task t1 = thing1.value;
            Task t2 = thing2.value;
            // Compare by priority, in descending order
            if (t1.getPriority().ordinal() < t2.getPriority().ordinal())
                return -1;
            if (t1.getPriority().ordinal() > t2.getPriority().ordinal())
                return +1;
            // Priorities are the same, compare on name.
            return t1.getName().compareTo(t2.getName());
        }
    };

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<KeyValueHolder<String,Task>> mValues;

        public SimpleItemRecyclerViewAdapter(List<KeyValueHolder<String,Task>> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.task_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mItem = mValues.get(position).value;
            holder.mContentView.setText(holder.mItem.getName());
			holder.mPrioView.setText(holder.mItem.getPriority().toString());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putInt(TaskDetailFragment.ARG_ITEM_INDEX, position);
                        TaskDetailFragment fragment = new TaskDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.task_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, TaskDetailActivity.class);
                        intent.putExtra(TaskDetailFragment.ARG_ITEM_INDEX, position);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mPrioView;
            public final TextView mContentView;
            public Task mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mPrioView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
