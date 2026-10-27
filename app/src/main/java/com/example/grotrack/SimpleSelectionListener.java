package com.example.grotrack;

import android.view.View;
import android.widget.AdapterView;

final class SimpleSelectionListener implements AdapterView.OnItemSelectedListener {
    private final Runnable onSelection;
    SimpleSelectionListener(Runnable onSelection) { this.onSelection=onSelection; }
    @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { onSelection.run(); }
    @Override public void onNothingSelected(AdapterView<?> parent) { onSelection.run(); }
}
