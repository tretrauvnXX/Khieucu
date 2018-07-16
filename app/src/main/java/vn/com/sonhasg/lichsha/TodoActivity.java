package vn.com.sonhasg.lichsha;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.com.sonhasg.swipeback.SwipeBackActivity;
import vn.com.sonhasg.swipeback.SwipeBackLayout;

/**
 * Created by nguyenphuoc on 04/19/2017.
 */

public class TodoActivity extends SwipeBackActivity {

    private String uid = "0";

    private List<TodoListItem> todoListItem = new ArrayList<>();
    private TodoListAdapter todoListAdapter;

    private SwipeRefreshLayout swipeRefreshLayoutTodo;

    private String date, phongban;

    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        PrefManager prefManager = new PrefManager(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        uid = prefManager.getUser();

        ListView listViewTodo = (ListView) findViewById(R.id.todos_list);
        todoListAdapter = new TodoListAdapter(this, todoListItem);
        listViewTodo.setAdapter(todoListAdapter);

        swipeRefreshLayoutTodo = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_todo);

        listViewTodo.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tid = ((TextView) view.findViewById(R.id.tvId)).getText().toString();
                String ten_nhanvien = ((TextView) view.findViewById(R.id.tvTen_nhanvien)).getText().toString();
                String nhanvien = ((TextView) view.findViewById(R.id.tvNhanvien)).getText().toString();
                String name_vn = ((TextView) view.findViewById(R.id.tvName_vn)).getText().toString();
                String nghiphep = ((TextView) view.findViewById(R.id.tvNghiphep)).getText().toString();
                String diachi = ((TextView) view.findViewById(R.id.tvDiachi)).getText().toString();
                String time_begin = ((TextView) view.findViewById(R.id.tvTime_begin)).getText().toString();
                String time_end = ((TextView) view.findViewById(R.id.tvTime_end)).getText().toString();
                String latitude = ((TextView) view.findViewById(R.id.tvLatitude)).getText().toString();
                String longitude = ((TextView) view.findViewById(R.id.tvLongitude)).getText().toString();
                String ghichu = ((TextView) view.findViewById(R.id.tvGhichu)).getText().toString();
                String checkgps = ((TextView) view.findViewById(R.id.tvCheckgps)).getText().toString();
                String realgps = ((TextView) view.findViewById(R.id.tvRealgps)).getText().toString();

                Intent intent = new Intent(TodoActivity.this, ViewTodoActivity.class);

                intent.putExtra("parent", tid);
                intent.putExtra("ten_nhanvien", ten_nhanvien);
                intent.putExtra("nhanvien", nhanvien);
                intent.putExtra("name_vn", name_vn);
                intent.putExtra("nghiphep", nghiphep);
                intent.putExtra("diachi", diachi);
                intent.putExtra("time_begin", time_begin);
                intent.putExtra("time_end", time_end);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("ghichu", ghichu);
                intent.putExtra("checkgps", checkgps);
                intent.putExtra("realgps", realgps);

                startActivity(intent);
            }
        });

        swipeRefreshLayoutTodo.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTodos();
            }
        });

        swipeRefreshLayoutTodo.post(new Runnable() {
                @Override
                public void run() {
                    fetchTodos();
                }
            }
        );

        date = getIntent().getStringExtra("date");
        phongban = getIntent().getStringExtra("phongban");

        if (date != null) {
            getSupportActionBar().setTitle("Ngày " + date.split("-")[2] + "/" + date.split("-")[1] + "/" + date.split("-")[0]);
        }

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
                fetchTodos();
            }
        }, 500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_todo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_add_todo) {
            Intent intent = new Intent(TodoActivity.this, AddTodoActivity.class);
            if (date != null) {
                intent.putExtra("time_begin", date + " " + timeFormatter.format(new Date()));
                intent.putExtra("time_end", date + " " + timeFormatter.format(new Date()));
            }
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchTodos() {
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {

            swipeRefreshLayoutTodo.setRefreshing(true);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_TODOS, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {
                            todoListItem.clear();

                            JSONArray data = jsonObject.getJSONArray("data");

                            int n = data.length();
                            for (int i = 0; i < n; i++) {
                                JSONObject item = data.getJSONObject(i);
                                TodoListItem todoDetail = new TodoListItem();
                                todoDetail.setId(item.getString("id"));
                                todoDetail.setNhanvien(item.getString("nhanvien"));
                                if (!phongban.equals("") && !phongban.equals("0") && phongban != null) {
                                    todoDetail.setTen_nhanvien(item.getString("ten_nhanvien"));
                                } else {
                                    todoDetail.setTen_nhanvien("");
                                }
                                todoDetail.setName_vn(item.getString("name_vn"));
                                todoDetail.setNghiphep(item.getString("nghiphep"));
                                todoDetail.setDiachi(item.getString("diachi"));
                                todoDetail.setTime_begin(item.getString("time_begin"));
                                todoDetail.setTime_end(item.getString("time_end"));
                                todoDetail.setLatitude(item.getString("latitude"));
                                todoDetail.setLongitude(item.getString("longitude"));
                                todoDetail.setGhichu(item.getString("ghichu"));
                                todoDetail.setCheckgps(item.getString("checkgps"));
                                todoDetail.setComments(item.getString("comments"));
                                todoDetail.setRealgps(item.getString("realgps"));

                                todoListItem.add(todoDetail);
                            }
                            todoListAdapter.notifyDataSetChanged();

                            if (n == 0) {
                                Toast.makeText(getApplicationContext(), "Danh sách công việc trống!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Không thể tải dữ liệu từ máy chủ!", Toast.LENGTH_LONG).show();
                        }
                        swipeRefreshLayoutTodo.setRefreshing(false);

                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Không thể tải dữ liệu từ máy chủ!", Toast.LENGTH_LONG).show();
                        swipeRefreshLayoutTodo.setRefreshing(false);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Kết nối máy chủ thất bại!", Toast.LENGTH_LONG).show();
                    swipeRefreshLayoutTodo.setRefreshing(false);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("api_key", AppConfig.API_KEY);
                    if (!phongban.equals("") && !phongban.equals("0") && phongban != null) {
                        params.put("phongban", phongban + "");
                    } else {
                        params.put("nhanvien", uid + "");
                    }
                    params.put("from", date != null ? date : "");
                    params.put("to", date != null ? date : "");

                    return params;
                }
            };

            String tag_string_req = "req_todos";
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
