package vn.com.sonhasg.lichsha;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.com.sonhasg.swipeback.SwipeBackActivity;
import vn.com.sonhasg.swipeback.SwipeBackLayout;

public class HistoryTodoActivity extends SwipeBackActivity {

    private SwipeBackLayout mSwipeBackLayout;

    private List<TodoListItem> historyListItem = new ArrayList<>();
    private ListView listHistoryTodo;
    private TodoListAdapter historyListAdapter;
    private String congviec;

    private String tag_string_req = "req_histories";

    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_todo);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Lịch sử thay đổi công việc");

        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        listHistoryTodo = (ListView) findViewById(R.id.histories_list);
        historyListAdapter = new TodoListAdapter(this, historyListItem);
        listHistoryTodo.setAdapter(historyListAdapter);

        congviec = getIntent().getStringExtra("congviec");

        if (!(connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected())) {
            showSnack();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchHistories();
            }
        }, 500);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchHistories() {
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {

            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_HISTORIES, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {
                            historyListItem.clear();

                            JSONArray data = jsonObject.getJSONArray("data");

                            int n = data.length();
                            for (int i = 0; i < n; i++) {
                                JSONObject item = data.getJSONObject(i);
                                TodoListItem todoDetail = new TodoListItem();
                                todoDetail.setId("");
                                todoDetail.setNhanvien("");
                                todoDetail.setTen_nhanvien("");
                                todoDetail.setName_vn(item.getString("name_vn"));
                                todoDetail.setNghiphep(item.getString("nghiphep"));
                                todoDetail.setDiachi(item.getString("diachi"));
                                todoDetail.setTime_begin(item.getString("time_begin"));
                                todoDetail.setTime_end(item.getString("time_end"));
                                todoDetail.setLatitude("");
                                todoDetail.setLongitude("");
                                todoDetail.setGhichu("");
                                todoDetail.setCheckgps("-1");
                                todoDetail.setComments("Sửa lúc: " + item.getString("date_added"));
                                todoDetail.setRealaddress("");

                                historyListItem.add(todoDetail);
                            }
                            historyListAdapter.notifyDataSetChanged();

                            if (n == 0) {
                                Toast.makeText(getApplicationContext(), "Công việc không thay đồi làn nào!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Không thể tải dữ liệu từ máy chủ!", Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Không thể tải dữ liệu từ máy chủ!", Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Kết nối máy chủ thất bại!", Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("api_key", AppConfig.API_KEY);
                    params.put("congviec", congviec);

                    return params;
                }
            };

            AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
        }
    }

    private void showSnack() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.container), "Vui lòng kết nối Internet!", Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }
}
