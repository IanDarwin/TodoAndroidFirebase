package todomore.todoandroidfirebase;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.darwinsys.todo.model.Task;

public class TaskEditActivity extends AppCompatActivity {

    private final static String TAG = TaskEditActivity.class.getSimpleName();
    private Task mTask;
    private String mKey;
    private EditText nameTF, descrTF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If the user just deleted an item and that event hasn't yet made it to fhe main view,
        // they could crash the app by trying to edit it again. Block that, before setContentView().
        int index = getIntent().getIntExtra(TaskDetailFragment.ARG_ITEM_INDEX, 0);
        if (index < 0 || index >= ApplicationClass.sTasks.size()) {
            Toast.makeText(this, "Unable to edit that item", Toast.LENGTH_SHORT).show();
            finish();
        }
        KeyValueHolder<String, Task> taskWrapper = ApplicationClass.sTasks.get(index);
        mTask = taskWrapper.value;
        mKey = taskWrapper.key;

        setContentView(R.layout.activity_task_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        nameTF = (EditText) findViewById(R.id.nameEditText);
        descrTF = (EditText) findViewById(R.id.descrEditText);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewToModel();
                doSave();
            }
        });

        modelToView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.save_menuitem:
                doSave();
                return true;
            case R.id.delete_menuitem:
                doDelete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void doSave() {
        viewToModel();
        Log.d(TAG, "Saving " + mTask);

        ((ApplicationClass)getApplication()).getDatabase().child(mKey).setValue(mTask);

        finish();
    }

    void doDelete() {
        Log.d(TAG, "Removing " + mTask);
        ((ApplicationClass)getApplication()).getDatabase().child(mKey).removeValue();
        finish();
    }

    void modelToView() {
        nameTF.setText(mTask.getName());
        descrTF.setText(mTask.getDescription());
    }

    void viewToModel() {
        mTask.setName(nameTF.getText().toString());
        mTask.setDescription(descrTF.getText().toString());
    }
}
