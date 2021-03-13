package cgeo.geocaching.filters;

import cgeo.geocaching.R;
import cgeo.geocaching.activity.AbstractActionBarActivity;
import cgeo.geocaching.databinding.AdvancedFilterActivityBinding;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;



/**
 * Show a filter selection using an {@code ExpandableListView}.
 */
public class AdvancedFilterActivity extends AbstractActionBarActivity {

    public static final int REQUEST_SELECT_FILTER = 456;
    public static final String EXTRA_FILTER_RESULT = "efr";

    private AdvancedFilterActivityBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeAndContentView(R.layout.advanced_filter_activity);
        binding = AdvancedFilterActivityBinding.bind(findViewById(R.id.activity_viewroot));
        binding.button.setOnClickListener(b -> setResult());
    }


    private void setResult() {
        final Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_FILTER_RESULT, binding.text.getText().toString());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public static void selectFilter(@NonNull final Activity context) {
        context.startActivityForResult(new Intent(context, AdvancedFilterActivity.class), REQUEST_SELECT_FILTER);
    }
}

