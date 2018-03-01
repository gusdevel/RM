package mode.retail.polaroid.mx.retailmode;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Shows instruction about this device owner.
 */
public class InstructionFragment extends Fragment {

    /**
     * @return A newly instantiated {@link InstructionFragment}.
     */
    public static InstructionFragment newInstance() {
        return new InstructionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_instruction, container, false);
    }

}