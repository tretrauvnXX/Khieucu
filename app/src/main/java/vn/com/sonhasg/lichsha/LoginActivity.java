package vn.com.sonhasg.lichsha;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nguyenphuoc on 04/19/2017.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText txtUsername;
    private EditText txtPassword;
    private ProgressDialog progressDialog;
    private PrefManager prefManager;
    private SQLiteHandler sqLiteHandler;
    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        prefManager = new PrefManager(this);
        if (prefManager.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        sqLiteHandler = new SQLiteHandler(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = txtUsername.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();

                if (!username.isEmpty() && !password.isEmpty()) {

                    if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
                        checkLogin(username, password);
                    } else {
                        showSnack();
                        return;
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập thông tin đăng nhập!", Toast.LENGTH_LONG).show();
                }
            }
        });

        if (!(connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected())) {
            showSnack();
        }
    }

    private void checkLogin(final String username, final String password) {
        String tag_string_req = "req_login";

        progressDialog.setMessage("Đăng nhập tài khoản ...");
        showDialog();

        StringRequest stringRequest = new StringRequest(Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");

                    if (success) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        String id = data.getString("id");
                        String level = data.getString("level");
                        String fullname = data.getString("fullname");
                        String email = data.getString("email");
                        String part = data.getString("part");
                        String partname = data.getString("partname");
                        String domain = data.getString("domain");

                        prefManager.setLogin(true);
                        prefManager.setUser(id);
                        prefManager.setDomain(domain);
                        sqLiteHandler.addUser(id, level, fullname, email, part, partname);

                        Toast.makeText(getApplicationContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Thông tin đăng nhập sai. Vui lòng thử lại!", Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Không thể đăng nhập vào máy chủ!", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Kết nối máy chủ thất bại!", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("api_key", AppConfig.API_KEY);
                params.put("username", username);
                params.put("password", password);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void showSnack() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.loginWrapper), "Vui lòng kết nối Internet!", Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }
}
