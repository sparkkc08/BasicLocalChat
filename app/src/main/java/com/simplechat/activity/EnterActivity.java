package com.simplechat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.simplechat.R;

public class EnterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mChatName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        mChatName = (EditText) findViewById(R.id.etName);


        Intent intent = getIntent();

        if(intent != null) {
            if(intent.hasExtra(MainActivity.CHAT_NAME_EXTRA)) {
                String name = intent.getStringExtra(MainActivity.CHAT_NAME_EXTRA);
                mChatName.setText(name);
                mChatName.setSelection(mChatName.getText().length());
                mChatName.clearFocus();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCreateChat:
                if(checkName()) {
                    Intent intentNewChat = new Intent(EnterActivity.this, MainActivity.class);
                    intentNewChat.putExtra(MainActivity.CHAT_NAME_EXTRA, String.valueOf(mChatName.getText()));
                    intentNewChat.putExtra(MainActivity.MODE_NEW_CHAT_EXTRA, true);

                    startActivity(intentNewChat);
                    finish();
                }

                break;

            case R.id.btnEnterToChat:
                if(checkName()) {
                    Intent intentNewChat = new Intent(EnterActivity.this, MainActivity.class);
                    intentNewChat.putExtra(MainActivity.CHAT_NAME_EXTRA, String.valueOf(mChatName.getText()));

                    startActivity(intentNewChat);
                    finish();
                }

                break;
        }
    }

    private boolean checkName() {
        boolean isValid = mChatName.getText().length() != 0;

        if(!isValid) {
            Toast.makeText(this, R.string.enter_name, Toast.LENGTH_LONG).show();
            mChatName.requestFocus();
        }

        return isValid;
    }

}
