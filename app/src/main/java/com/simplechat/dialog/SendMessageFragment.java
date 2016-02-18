package com.simplechat.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.simplechat.R;

/**
 * Created by xack1 on 18.02.2016.
 */
public class SendMessageFragment extends DialogFragment {

    private static final String HOST_EXTRA = "HOST_EXTRA";
    private static final String NAME_EXTRA = "NAME_EXTRA";

    private String mHost;
    private String mName;

    private EditText etMessage;
    private FloatingActionButton btnSend;


    private OnMessageSendListener mListener;

    // Container Activity implemented this interface
    public interface OnMessageSendListener {
        public void onMessageSend(String host, String message);
    }


    public static SendMessageFragment newInstance(String host, String name) {
        SendMessageFragment f = new SendMessageFragment();

        // putting arguments to bundle
        Bundle args = new Bundle();
        args.putString(HOST_EXTRA, host);
        args.putString(NAME_EXTRA, name);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //attaching listener to container activity
        try {
            mListener = (OnMessageSendListener) activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //extracting arguments
        mHost = getArguments().getString(HOST_EXTRA);
        mName = getArguments().getString(NAME_EXTRA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog, container, false);

        getDialog().setTitle(getString(R.string.message_for, mName));


        etMessage = (EditText) v.findViewById(R.id.etMessage);
        btnSend = (FloatingActionButton) v.findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(etMessage.getText().length() > 0) {
                    if(mListener != null) {
                        // pass data to container activity through interface
                        mListener.onMessageSend(mHost, etMessage.getText().toString());
                    }

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);

                    dismiss();
                } else {
                    Toast.makeText(getActivity(), R.string.enter_message, Toast.LENGTH_LONG).show();
                }
            }
        });

        return v;
    }
}
