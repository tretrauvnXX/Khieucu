package vn.com.sonhasg.lichsha;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.com.sonhasg.swipeback.SwipeBackActivity;
import vn.com.sonhasg.swipeback.SwipeBackLayout;

/**
 * Created by nguyenphuoc on 04/22/2017.
 */

public class ViewTodoActivity extends SwipeBackActivity {

    private EditText etGhichu;

    private String parent;
    private String nhanvien;
    private String nghiphep;
    private String name_vn;
    private String diachi = "";
    private String time_begin;
    private String time_end;
    private String uid = "0";
    private String ghichu;
    private Double latitude, longitude;

    private ProgressDialog progressDialog;

    private List<TodoListItem> commentListItem = new ArrayList<>();
    private TodoListAdapter commentListAdapter;

    private SwipeRefreshLayout swipeRefreshLayoutComment;

    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_todo);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        PrefManager prefManager = new PrefManager(this);
        uid = prefManager.getUser();

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        TextView tvTen_nhanvien = (TextView) findViewById(R.id.tvTen_nhanvien);
        TextView tvName_vn = (TextView) findViewById(R.id.tvName_vn);
        TextView tvTime = (TextView) findViewById(R.id.tvTime);
        TextView tvDiachi = (TextView) findViewById(R.id.tvDiachi);
        TextView tvGhichu = (TextView) findViewById(R.id.tvGhichu);
        etGhichu = (EditText) findViewById(R.id.etGhichu);
        TextView tvRealAddress = (TextView) findViewById(R.id.tvRealAddress);
        ImageView ivCheckgps = (ImageView) findViewById(R.id.ivCheckgps);
        Button btnSave = (Button) findViewById(R.id.btnSave);
        LinearLayout rowAddress = (LinearLayout) findViewById(R.id.rowAddress);
        LinearLayout rowGhichu = (LinearLayout) findViewById(R.id.rowGhichu);
        LinearLayout rowRealAddress = (LinearLayout) findViewById(R.id.rowRealAddress);

        parent = getIntent().getStringExtra("parent");
        String ten_nhanvien = getIntent().getStringExtra("ten_nhanvien");
        nhanvien = getIntent().getStringExtra("nhanvien");
        name_vn = getIntent().getStringExtra("name_vn");
        nghiphep = getIntent().getStringExtra("nghiphep");
        diachi = getIntent().getStringExtra("diachi");
        time_begin = getIntent().getStringExtra("time_begin");
        time_end = getIntent().getStringExtra("time_end");
        ghichu = getIntent().getStringExtra("ghichu");
        String checkgps = getIntent().getStringExtra("checkgps");
        String realgps = getIntent().getStringExtra("realgps");

        String latitudeString = getIntent().getStringExtra("latitude");
        String longitudeString = getIntent().getStringExtra("longitude");
        if (latitudeString != null) {
            latitude = Double.parseDouble(latitudeString);
        }
        if (longitudeString != null) {
            longitude = Double.parseDouble(longitudeString);
        }

        getSupportActionBar().setTitle("Xem công việc");
        if (ten_nhanvien != null && !nhanvien.equals(uid)) {
            LinearLayout rowTen_nhanvien = (LinearLayout) findViewById(R.id.rowTen_nhanvien);
            tvTen_nhanvien.setText(ten_nhanvien);
            rowTen_nhanvien.setVisibility(View.VISIBLE);
        }
        tvName_vn.setText(name_vn);
        String date_begin = time_begin.split(" ")[0];
        String date_end = time_end.split(" ")[0];
        String time = "";
        if (nghiphep.equals("0")) {
            time += "Từ: " + (time_begin.split(" ")[1]).substring(0, 5);
            if (!date_end.equals(date_begin)) {
                time += ", " + date_begin.split("-")[2] + "/" + date_begin.split("-")[1];
            }
            time += " đến " + (time_end.split(" ")[1]).substring(0, 5) + ", " + date_end.split("-")[2] + "/" + date_end.split("-")[1];
        } else {
            if (!date_end.equals(date_begin)) {
                time += "Từ: " + date_begin.split("-")[2] + "/" + date_begin.split("-")[1];
                time += " đến " + date_end.split("-")[2] + "/" + date_end.split("-")[1];
            } else {
                time += date_begin.split("-")[2] + "/" + date_begin.split("-")[1];
            }
        }
        tvTime.setText(time);
        tvDiachi.setText(diachi);
        if (nghiphep != null && nghiphep.equals("1")) {
            rowAddress.setVisibility(View.GONE);
            rowGhichu.setVisibility(View.GONE);
        } else {
            rowAddress.setVisibility(View.VISIBLE);
            rowGhichu.setVisibility(View.VISIBLE);
        }
        if (ghichu != null && !ghichu.equals("") && !ghichu.equals("0")) {
            rowGhichu.setVisibility(View.VISIBLE);
            tvGhichu.setText(ghichu);
        } else {
            rowGhichu.setVisibility(View.GONE);
        }
        if (checkgps.equals("0")) {
            ivCheckgps.setImageResource(R.mipmap.todo_gpserror);
            if (realgps != null && !realgps.equals("")) {
                List<Address> addresses = null;

                try {
                    Double latitude = Double.parseDouble(realgps.split(",")[0]);
                    Double longitude = Double.parseDouble(realgps.split(",")[1]);

                    addresses = geocoder.getFromLocation(latitude, longitude, 1);

                    if (addresses == null || addresses.size()  == 0) {

                    } else {
                        Address address = addresses.get(0);
                        ArrayList<String> addressFragments = new ArrayList<String>();

                        for(int a = 1; a < address.getMaxAddressLineIndex(); a++) {
                            addressFragments.add(address.getAddressLine(a));
                        }

                        String myAddress = TextUtils.join(", ", addressFragments);

                        tvRealAddress.setText(myAddress);
                        rowRealAddress.setVisibility(View.VISIBLE);
                    }
                } catch (IOException ignored) {

                }
            } else {
                rowRealAddress.setVisibility(View.GONE);
            }
        } else if (!checkgps.equals("-1") || nghiphep.equals("1")) {
            ivCheckgps.setImageResource(R.mipmap.todo_gpsok);
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ghichu = etGhichu.getText().toString();

                if (ghichu.equals("")) {
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập nội dung bình luận!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {

                    String tag_string_req = "req_add_comment";

                    progressDialog.setMessage("Đang xử lý ...");
                    showDialog();

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_ADD_COMMENT, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            hideDialog();

                            etGhichu.setText("");

                            fetchComments();

                            Toast.makeText(getApplicationContext(), "Thêm bình luận thành công!", Toast.LENGTH_LONG).show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                            hideDialog();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("api_key", AppConfig.API_KEY);
                            params.put("type", "CongViec");
                            params.put("parent", parent);
                            params.put("nhanvien", uid);
                            params.put("name_vn", name_vn);
                            params.put("ghichu", ghichu);
                            return params;
                        }
                    };

                    AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
                } else {
                    showSnack();
                }
            }
        });

        ListView listViewTodo = (ListView) findViewById(R.id.comments_list);
        commentListAdapter = new TodoListAdapter(this, commentListItem);
        listViewTodo.setAdapter(commentListAdapter);

        swipeRefreshLayoutComment = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_comment);

        swipeRefreshLayoutComment.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchComments();
            }
        });

        swipeRefreshLayoutComment.post(new Runnable() {
                                         @Override
                                         public void run() {
                                             fetchComments();
                                         }
                                     }
        );

        if (!(connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected())) {
            showSnack();
        }
    }

    private void fetchComments() {
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {

            swipeRefreshLayoutComment.setRefreshing(true);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_COMMENTS, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {

                            commentListItem.clear();

                            JSONArray data = jsonObject.getJSONArray("data");

                            int n = data.length();
                            for (int i = 0; i < n; i++) {
                                JSONObject item = data.getJSONObject(i);
                                TodoListItem commentDetail = new TodoListItem();
                                commentDetail.setName_vn(item.getString("ten_nhanvien"));
                                commentDetail.setComments(item.getString("ghichu"));
                                commentDetail.setTime_begin(item.getString("date_added"));
                                commentDetail.setTime_end("");
                                commentDetail.setCheckgps("-1");
                                commentDetail.setNghiphep("0");
                                commentDetail.setRealgps("");

                                commentListItem.add(commentDetail);
                            }
                            commentListAdapter.notifyDataSetChanged();

                        }
                        swipeRefreshLayoutComment.setRefreshing(false);

                    } catch (JSONException e) {
                        swipeRefreshLayoutComment.setRefreshing(false);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    swipeRefreshLayoutComment.setRefreshing(false);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("api_key", AppConfig.API_KEY);
                    params.put("type", "CongViec");
                    params.put("parent", parent);

                    return params;
                }
            };

            String tag_string_req = "req_comments";
            AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
        }
    }

    private void showSnack() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.add_todo_info), "Vui lòng kết nối Internet!", Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_todo, menu);
        if (nhanvien != null && nhanvien.equals(uid)) {
            menu.getItem(0).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_edit_todo) {
            Intent intent = new Intent(ViewTodoActivity.this, AddTodoActivity.class);
            intent.putExtra("id", parent);
            intent.putExtra("name_vn", name_vn);
            intent.putExtra("nghiphep", nghiphep != null ? nghiphep : "");
            intent.putExtra("diachi", diachi);
            intent.putExtra("time_begin", time_begin);
            intent.putExtra("time_end", time_end);
            intent.putExtra("latitude", latitude + "");
            intent.putExtra("longitude", longitude + "");
            intent.putExtra("ghichu", ghichu);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_history_todo) {
            Intent intent = new Intent(ViewTodoActivity.this, HistoryTodoActivity.class);
            intent.putExtra("congviec", parent);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
