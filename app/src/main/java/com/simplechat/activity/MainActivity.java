package com.simplechat.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.simplechat.R;
import com.simplechat.adapter.ClientAdapter;
import com.simplechat.dialog.SendMessageFragment;
import com.simplechat.model.MessagePackageModel;
import com.simplechat.utils.GsonHelper;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, SendMessageFragment.OnMessageSendListener {


    public static final String CHAT_NAME_EXTRA = "CHAT_NAME_EXTRA";
    public static final String MODE_NEW_CHAT_EXTRA = "MODE_NEW_CHAT_EXTRA";

    private static final int DEFAULT_PORT = 8887;

    private WebSocketClient mWebSocketClient = null;
    private WebSocketServer mWebSocketServer = null;
    private String mName;
    private TextView tvAddress;
    private TextView tvMessages;
    private Button btnConnection;
    private EditText etMessage;
    private EditText etServerIp;
    private ListView lvClients;
    private FloatingActionButton btnSend;

    private static final HashMap<String, WebSocket> hostConnectionPair = new HashMap<>();
    private static final HashMap<String, String> hostNamePair = new HashMap<>();
    private static final LinkedHashMap<String, MessagePackageModel> hostStatusPair = new LinkedHashMap<>();

    private ClientAdapter mClientAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        lvClients = (ListView) findViewById(R.id.lvClients);
        mClientAdapter = new ClientAdapter(this, hostStatusPair);
        lvClients.setAdapter(mClientAdapter);
        lvClients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //if user sends personal message
                MessagePackageModel messagePackage = (new ArrayList<>(hostStatusPair.values())).get(position);

                DialogFragment newFragment = SendMessageFragment.newInstance(messagePackage.from, messagePackage.name);
                newFragment.show(getSupportFragmentManager(), SendMessageFragment.class.getSimpleName());

                onBackPressed();
            }
        });

        tvAddress = (TextView) findViewById(R.id.tvAddress);
        tvAddress.setText(getIpAddress());

        btnConnection = (Button) findViewById(R.id.btnConnection);

        btnSend = (FloatingActionButton) findViewById(R.id.btnSend);
        btnSend.setEnabled(false);

        etServerIp = (EditText) findViewById(R.id.etServerIp);

        tvMessages = (TextView) findViewById(R.id.tvMessages);

        etMessage = (EditText) findViewById(R.id.etMessage);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWebSocketClient != null && mWebSocketClient.getConnection() != null &&
                        etMessage.getText().length() > 0) {

                    MessagePackageModel messagePackage = new MessagePackageModel(
                            mName,
                            etMessage.getText().toString(),
                            getIpAddress());

                    String jsonPackage = GsonHelper.toJson(messagePackage);

                    if(jsonPackage != null) {
                        mWebSocketClient.send(jsonPackage);
                    }

                    etMessage.setText("");
                    hideKeyboard();
                }
            }
        });


        Intent intent = getIntent();

        if(intent != null) {
            if(intent.hasExtra(CHAT_NAME_EXTRA)) {
                mName = intent.getStringExtra(CHAT_NAME_EXTRA);
            }

            if(intent.hasExtra(MODE_NEW_CHAT_EXTRA)) {
                tvAddress.setVisibility(View.VISIBLE);
                etServerIp.setVisibility(View.GONE);
                btnConnection.setVisibility(View.GONE);

                startServer();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_leave_chat) {

            if(mWebSocketServer != null) {
                try {
                    mWebSocketServer.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(mWebSocketClient != null && mWebSocketClient.getConnection() != null) {
                mWebSocketClient.close();
            }

            Intent intentNewChat = new Intent(this, EnterActivity.class);
            intentNewChat.putExtra(MainActivity.CHAT_NAME_EXTRA, mName);

            startActivity(intentNewChat);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnection:
                if(etServerIp.isEnabled()) {
                    if(etServerIp.getText().length() == 0) {
                        Toast.makeText(MainActivity.this, R.string.enter_ip, Toast.LENGTH_LONG).show();
                        etServerIp.requestFocus();
                    } else {
                        connectToServer(etServerIp.getText().toString());
                    }
                } else {
                    if(mWebSocketClient != null) {
                        mWebSocketClient.close();
                    } else {
                        btnSend.setEnabled(false);
                        etServerIp.setEnabled(true);
                        btnConnection.setText(R.string.connect);
                    }
                }

                break;
        }
    }

    private void startServer() {
        //WebSocketImpl.DEBUG = true;
        mWebSocketServer = new WebSocketServer(new InetSocketAddress(DEFAULT_PORT)) {
            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {

                Iterator<String> itConnections = hostConnectionPair.keySet().iterator();
                while (itConnections.hasNext()) {
                    String host = itConnections.next();
                    //defining status offline

                    WebSocket connection = hostConnectionPair.get(host);
                    if(conn.equals(connection) && !connection.isOpen()) {
                        String name = hostNamePair.get(host);

                        MessagePackageModel messagePackage = new MessagePackageModel(host, name, false);

                        String jsonMessagePackage = GsonHelper.toJson(messagePackage);

                        if(jsonMessagePackage != null) {
                            sendMessageToAll(jsonMessagePackage);
                        }

                        itConnections.remove();
                        break;
                    }
                }
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                Log.d("MyApp", String.format("onMessage(%s)", message));
                MessagePackageModel messagePackage = GsonHelper.fromJson(message, MessagePackageModel.class);
                if(messagePackage != null) {
                    if(messagePackage.message == null || messagePackage.message.length() == 0) {
                        //defining new user or status online

                        hostNamePair.put(messagePackage.from, messagePackage.name);

                        MessagePackageModel messagePackageStatus = new MessagePackageModel(messagePackage.from, messagePackage.name, true);

                        String jsonMessagePackage = GsonHelper.toJson(messagePackageStatus);

                        if(jsonMessagePackage != null) {
                            sendMessageToAll(jsonMessagePackage);
                        }

                    } else {
                        if (messagePackage.to == null || messagePackage.to.length() == 0) {
                            sendMessageToAll(message);
                        } else {
                            if(!messagePackage.from.equals(messagePackage.to)) {
                                //if user sending message to himself
                                sendMessageDef(messagePackage.from, message);
                            }
                            sendMessageDef(messagePackage.to, message);
                        }
                    }
                }

            }

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                hostConnectionPair.put(conn.getRemoteSocketAddress().getHostName(), conn);

                //broadcast notification about new connection or status online
                Iterator<String> itConnections = hostNamePair.keySet().iterator();
                while (itConnections.hasNext()) {
                    String host = itConnections.next();

                    String name = hostNamePair.get(host);

                    MessagePackageModel messagePackage = new MessagePackageModel(host, name);

                    String jsonMessagePackage = GsonHelper.toJson(messagePackage);

                    if(jsonMessagePackage != null) {
                        sendMessageDef(conn.getRemoteSocketAddress().getHostName(), jsonMessagePackage);
                    }
                }

            }
        };

        mWebSocketServer.start();

        connectToServer(getIpAddress());
    }

    private void connectToServer(String host) {
        try {
            mWebSocketClient = new WebSocketClient(new URI("ws://"+host+":"+DEFAULT_PORT)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //switching views state
                            btnSend.setEnabled(true);
                            etServerIp.setEnabled(false);
                            btnConnection.setText(R.string.disconnect);
                        }
                    });

                    //sending user info and status online
                    MessagePackageModel messagePackage = new MessagePackageModel(getIpAddress(), mName);
                    String jsonMessagePackage = GsonHelper.toJson(messagePackage);

                    if(jsonMessagePackage != null) {
                        mWebSocketClient.send(jsonMessagePackage);
                    }
                }

                @Override
                public void onMessage(final String message) {
                    Log.e("MyApp", "onMessage message = " + message);

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //unpacking json message
                            MessagePackageModel messagePackage = GsonHelper.fromJson(message, MessagePackageModel.class);

                            if(messagePackage != null) {
                                if(messagePackage.message == null || messagePackage.message.length() == 0) {
                                    //changing users status
                                    hostStatusPair.put(messagePackage.from, messagePackage);
                                    mClientAdapter.notifyDataSetChanged();
                                } else {
                                    //adding message to all screen or to specific user if private
                                    String from = messagePackage.name + ": ";
                                    String fullText = from + (messagePackage.to == null ? "" : "[PRIVATE] ") + messagePackage.message;

                                    Spannable spannable = new SpannableString(fullText);

                                    spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 0, from.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, from.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    tvMessages.append(spannable);
                                    tvMessages.append("\n");
                                }
                            }
                        }
                    });
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //switching views state

                            btnSend.setEnabled(false);
                            etServerIp.setEnabled(true);
                            btnConnection.setText(R.string.connect);

                            hostStatusPair.clear();
                            mClientAdapter.notifyDataSetChanged();
                        }
                    });
                }

                @Override
                public void onError(Exception ex) {
                }
            };
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mWebSocketClient.connect();
    }

    //sending message to all users(connections)
    private void sendMessageToAll(String message) {
        Iterator<String>  it = hostConnectionPair.keySet().iterator();
        while (it.hasNext()) {
            WebSocket connection = hostConnectionPair.get(it.next());

            if (connection.isOpen()) {
                connection.send(message);
            }
        }
    }

    //sending message to specified user(connection)
    private void sendMessageDef(String to, String message) {
        WebSocket connection = hostConnectionPair.get(to);
        if(connection != null) {
            connection.send(message);
        }
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ip;
    }

    @Override
    public void onMessageSend(String host, String message) {
        if (mWebSocketClient != null && mWebSocketClient.getConnection() != null) {

            //pack and send personal message
            MessagePackageModel messagePackage = new MessagePackageModel(
                    mName,
                    message,
                    getIpAddress(),
                    host);

            String jsonPackage = GsonHelper.toJson(messagePackage);

            if(jsonPackage != null) {
                mWebSocketClient.send(jsonPackage);
            }

            hideKeyboard();
        }
    }
}
