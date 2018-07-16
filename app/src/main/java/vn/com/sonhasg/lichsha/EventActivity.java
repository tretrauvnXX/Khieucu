package vn.com.sonhasg.lichsha;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventActivity extends AppCompatActivity {

    private PrefManager prefManager;
    private SQLiteHandler sqLiteHandler;

    private NavigationView navigationView;
    private DrawerLayout drawer;

    HashMap<String, String> user;

    private String tag_string_req = "req_events";
    private String phongban = "", domain = "";

    private List<TodoListItem> eventListItem = new ArrayList<>();
    private TodoListAdapter eventListAdapter;

    private SwipeRefreshLayout swipeRefreshLayoutEvent;

    private int num_part = 0;

    private ConnectivityManager connectivityManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        prefManager = new PrefManager(this);
        phongban = prefManager.getPhongban2();
        domain = prefManager.getDomain();

        sqLiteHandler = new SQLiteHandler(this);
        user = sqLiteHandler.getUserDetails();

        setContentView(R.layout.activity_event);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sự kiện");
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        View navHeader = navigationView.getHeaderView(0);
        TextView tvFullname = (TextView) navHeader.findViewById(R.id.tvFullname);
        TextView tvDepartment = (TextView) navHeader.findViewById(R.id.tvDepartment);
        ImageView imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        ImageView imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        Glide.with(this).load(AppConfig.URL_NAVHEADERBG).crossFade().diskCacheStrategy(DiskCacheStrategy.ALL).into(imgNavHeaderBg);
        Glide.with(this).load(AppConfig.URL_PROFILEIMG).crossFade().thumbnail(0.5f).bitmapTransform(new CircleTransform(this)).diskCacheStrategy(DiskCacheStrategy.ALL).into(imgProfile);

        tvFullname.setText(user.get("fullname"));
        tvDepartment.setText(user.get("partname"));

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                drawer.closeDrawers();

                switch (menuItem.getItemId()) {

                    case R.id.nav_home:
                        startActivity(new Intent(EventActivity.this, MainActivity.class));
                        finish();

                        return false;

                    case R.id.nav_notifications:
                        break;

                    case R.id.nav_branchs:
                        startActivity(new Intent(EventActivity.this, BranchActivity.class));
                        finish();

                        return true;

                    case R.id.nav_customers:
                        startActivity(new Intent(EventActivity.this, CustomerActivity.class));
                        finish();

                        return true;

                    case R.id.nav_location:
                        startActivity(new Intent(EventActivity.this, LocationActivity.class));
                        finish();

                        return true;

                    default:
                }

                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawer.setDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();

        navigationView.getMenu().getItem(1).setChecked(true);

        ListView listViewEvent = (ListView) findViewById(R.id.events_list);
        eventListAdapter = new TodoListAdapter(this, eventListItem);
        listViewEvent.setAdapter(eventListAdapter);

        swipeRefreshLayoutEvent = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_event);

        listViewEvent.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String eid = ((TextView) view.findViewById(R.id.tvId)).getText().toString();
                String name_vn = ((TextView) view.findViewById(R.id.tvName_vn)).getText().toString();
                String diachi = ((TextView) view.findViewById(R.id.tvDiachi)).getText().toString();
                String time_begin = ((TextView) view.findViewById(R.id.tvTime_begin)).getText().toString();
                String time_end = ((TextView) view.findViewById(R.id.tvTime_end)).getText().toString();

                Intent intent = new Intent(EventActivity.this, ViewEventActivity.class);

                intent.putExtra("parent", eid);
                intent.putExtra("name_vn", name_vn);
                intent.putExtra("diachi", diachi);
                intent.putExtra("time_begin", time_begin);
                intent.putExtra("time_end", time_end);
                startActivity(intent);
            }
        });

        swipeRefreshLayoutEvent.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchParts();

                fetchEvents();
            }
        });

        swipeRefreshLayoutEvent.post(new Runnable() {
                @Override
                public void run() {
                    fetchParts();

                    fetchEvents();
                }
            }
        );

        navigationView.getMenu().getItem(5).setTitle("Phòng / chi nhánh");

        if (!(connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected())) {
            showSnack();
        }

        fetchParts();
    }

    private void fetchParts() {
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_PARTS, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {

                            navigationView.getMenu().getItem(5).getSubMenu().clear();

                            JSONArray data = jsonObject.getJSONArray("data");

                            num_part = data.length();

                            navigationView.getMenu().getItem(5).getSubMenu().add(3, 0, 0, "Công ty").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    phongban = item.getItemId() + "";
                                    prefManager.setPhongban2(phongban);

                                    getSupportActionBar().setTitle("Sự kiện Công ty");

                                    fetchEvents();

                                    for (int m = 0; m < num_part + 1; m++) {
                                        navigationView.getMenu().getItem(5).getSubMenu().getItem(m).setChecked(false);
                                    }
                                    return false;
                                }
                            });

                            for (int i = 1; i < num_part + 1; i++) {
                                JSONObject item = data.getJSONObject(i - 1);

                                navigationView.getMenu().getItem(5).getSubMenu().add(3, Integer.parseInt(item.getString("id")), i, item.getString("name_vn")).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        phongban = item.getItemId() + "";
                                        prefManager.setPhongban2(phongban);

                                        fetchEvents();

                                        navigationView.getMenu().getItem(5).getSubMenu().getItem(0).setChecked(false);
                                        for (int m = 0; m < num_part + 1; m++) {
                                            navigationView.getMenu().getItem(5).getSubMenu().getItem(m).setChecked(false);
                                        }
                                        return false;
                                    }
                                });
                            }

                            if (num_part > 0) {
                                if (phongban == null || phongban.equals("")) {
                                    phongban = navigationView.getMenu().getItem(5).getSubMenu().getItem(0).getItemId() + "";
                                    prefManager.setPhongban2(phongban);
                                }
                                navigationView.getMenu().findItem(Integer.parseInt(phongban)).setChecked(true);
                            }
                        }

                    } catch (JSONException ignored) {

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("api_key", AppConfig.API_KEY);
                    params.put("domain", domain);

                    return params;
                }
            };

            AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
        }
    }

    private void fetchEvents() {
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {

            swipeRefreshLayoutEvent.setRefreshing(true);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_EVENTS, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {
                            if (!phongban.equals("")) {
                                getSupportActionBar().setTitle("Sự kiện " + navigationView.getMenu().findItem(Integer.parseInt(phongban)).getTitle());
                            }

                            eventListItem.clear();

                            JSONArray data = jsonObject.getJSONArray("data");

                            int n = data.length();
                            for (int i = 0; i < n; i++) {
                                JSONObject item = data.getJSONObject(i);
                                TodoListItem eventDetail = new TodoListItem();
                                eventDetail.setId(item.getString("id"));
                                eventDetail.setName_vn(item.getString("name_vn"));
                                eventDetail.setDiachi(item.getString("diachi"));
                                eventDetail.setTime_begin(item.getString("time_begin"));
                                eventDetail.setTime_end(item.getString("time_end"));
                                eventDetail.setGhichu(item.getString("ghichu"));
                                eventDetail.setCheckgps("-1");
                                eventDetail.setNghiphep("0");
                                eventDetail.setRealgps("");
                                eventDetail.setRealaddress("");
                                eventDetail.setComments(item.getString("comments"));

                                eventListItem.add(eventDetail);
                            }
                            eventListAdapter.notifyDataSetChanged();

                            if (n == 0) {
                                Toast.makeText(getApplicationContext(), "Danh sách sự kiện trống!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Không thể tải dữ liệu từ máy chủ!", Toast.LENGTH_LONG).show();
                        }
                        swipeRefreshLayoutEvent.setRefreshing(false);

                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Không thể tải dữ liệu từ máy chủ!", Toast.LENGTH_LONG).show();
                        swipeRefreshLayoutEvent.setRefreshing(false);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Kết nối máy chủ thất bại!", Toast.LENGTH_LONG).show();
                    swipeRefreshLayoutEvent.setRefreshing(false);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("api_key", AppConfig.API_KEY);
                    params.put("phongban", phongban + "");
                    params.put("from", "");
                    params.put("to", "");
                    params.put("domain", domain);

                    return params;
                }
            };

            AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logoutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    private void logoutUser() {
        prefManager.setLogin(false);
        sqLiteHandler.deleteUsers();

        Intent service = new Intent(getApplicationContext(), GPSTrackerService.class);
        stopService(service);

        Intent intent = new Intent(EventActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showSnack() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.drawer_layout), "Vui lòng kết nối Internet!", Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }
}
