package vn.com.sonhasg.lichsha;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.com.sonhasg.swipeback.SwipeBackActivity;
import vn.com.sonhasg.swipeback.SwipeBackLayout;

/**
 * Created by nguyenphuoc on 04/22/2017.
 */

public class ViewEventActivity extends SwipeBackActivity {

    private SwipeBackLayout mSwipeBackLayout;

    private TextView tvName_vn, tvDiachi, tvTime, tvGhichu;
    private EditText etGhichu;
    private Button btnSave;
    private String parent, name_vn, diachi = "", time_begin, time_end, uid = "0", ghichu;

    private ProgressDialog progressDialog;

    private PrefManager prefManager;

    private String tag_string_req = "req_comments";

    private List<TodoListItem> commentListItem = new ArrayList<>();
    private ListView listViewEvent;
    private TodoListAdapter commentListAdapter;

    private SwipeRefreshLayout swipeRefreshLayoutComment;

    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        prefManager = new PrefManager(this);
        uid = prefManager.getUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);


        tvName_vn = (TextView) findViewById(R.id.tvName_vn);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvDiachi = (TextView) findViewById(R.id.tvDiachi);
        tvGhichu = (TextView) findViewById(R.id.tvGhichu);
        etGhichu = (EditText) findViewById(R.id.etGhichu);
        btnSave = (Button) findViewById(R.id.btnSave);

        parent = getIntent().getStringExtra("parent");
        name_vn = getIntent().getStringExtra("name_vn");
        diachi = getIntent().getStringExtra("diachi");
        time_begin = getIntent().getStringExtra("time_begin");
        time_end = getIntent().getStringExtra("time_end");
        ghichu = getIntent().getStringExtra("ghichu");

        getSupportActionBar().setTitle("Xem sự kiện");
        tvName_vn.setText(name_vn);
        String date_begin = time_begin.split(" ")[0];
        String date_end = time_end.split(" ")[0];
        String time = "Từ: " + (time_begin.split(" ")[1]).substring(0, 5);
        if (!date_end.equals(date_begin)) {
            time += ", " + date_begin.split("-")[2] + "/" + date_begin.split("-")[1] + "/" + date_begin.split("-")[0];
        }
        time += " đến " + (time_end.split(" ")[1]).substring(0, 5) + ", " + date_end.split("-")[2] + "/" + date_end.split("-")[1] + "/" + date_end.split("-")[0];
        tvTime.setText(time);
        tvDiachi.setText(diachi);
        LinearLayout rowGhichu = (LinearLayout) findViewById(R.id.rowGhichu);
        if (ghichu != null && !ghichu.equals("")) {
            rowGhichu.setVisibility(View.VISIBLE);
            tvGhichu.setText(ghichu);
        } else {
            rowGhichu.setVisibility(View.GONE);
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
                            params.put("type", "SuKien");
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
                    return;
                }
            }
        });

        listViewEvent = (ListView) findViewById(R.id.comments_list);
        commentListAdapter = new TodoListAdapter(this, commentListItem);
        listViewEvent.setAdapter(commentListAdapter);

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
                    params.put("type", "SuKien");
                    params.put("parent", parent);

                    return params;
                }
            };

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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
