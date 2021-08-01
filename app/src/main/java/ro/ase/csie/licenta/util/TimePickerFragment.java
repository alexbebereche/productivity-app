package ro.ase.csie.licenta.util;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.sql.Time;
import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Calendar c = Calendar.getInstance();
        return new TimePickerDialog(getActivity(), TimePickerDialog.THEME_HOLO_LIGHT,(TimePickerDialog.OnTimeSetListener) getActivity(), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
    }
}
