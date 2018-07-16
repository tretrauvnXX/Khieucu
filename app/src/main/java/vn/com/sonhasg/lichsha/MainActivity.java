package vn.com.sonhasg.lichsha;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import vn.com.sonhasg.dynamiccalendar.DynamicCalendar;
import vn.com.sonhasg.dynamiccalendar.OnDateClickListener;

/**
 * Created by nguyenphuoc on 04/19/2017.
 */

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 99;
    boolean boolean_permission;

    private PrefManager prefManager;
    private SQLiteHandler sqLiteHandler;

    private NavigationView navigationView;
    private DrawerLayout drawer;

    private DynamicCalendar myCalendar;

    HashMap<String, String> user;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

    private String tag_string_req = "req_todos";
    private String uid = "0";
    private String phongban = "", domain = "";
    private String from = dateFormatter.format(new Date());

    private int num_part = 0;

    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        prefManager = new PrefManager(this);

        if (!prefManager.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        uid = prefManager.getUser();
        phongban = prefManager.getPhongban1();
        domain = prefManager.getDomain();

        checkUser();

        checkVersion();

        sqLiteHandler = new SQLiteHandler(this);
        user = sqLiteHandler.getUserDetails();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Lịch làm việc");

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        View navHeader = navigationView.getHeaderView(0);
        TextView tvFullname = (TextView) navHeader.findViewById(R.id.tvFullname);
        TextView tvDepartment = (TextView) navHeader.findViewById(R.id.tvDepartment);
        ImageView imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        ImageView imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        myCalendar = (DynamicCalendar) findViewById(R.id.myCalendar);

        myCalendar.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onClick(Date date) {
                Intent intent = new Intent(MainActivity.this, TodoActivity.class);
                intent.putExtra("date", dateFormatter.format(date));
                intent.putExtra("phongban", phongban);
                startActivity(intent);
            }

            @Override
            public void onLongClick(Date date) {
                Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
                intent.putExtra("time_begin", dateFormatter.format(date) + " " + timeFormatter.format(new Date()));
                startActivity(intent);
            }
        });

        myCalendar.setCurrentDateBackgroundColor("#FFF3A1");
        myCalendar.isSundayOff(true, "#ffffff", "#ff0000");
        myCalendar.setExtraDatesOfMonthTextColor("#bbbbbb");
        myCalendar.setExtraDatesOfMonthBackgroundColor("#F7F7F7");
        myCalendar.setMonthView("");

        myCalendar.iv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myCalendar.setMonthView("add");

                from = dateFormatter.format(myCalendar.getCurrentMonth());

                fetchTodos();
            }
        });

        myCalendar.iv_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myCalendar.setMonthView("sub");

                from = dateFormatter.format(myCalendar.getCurrentMonth());

                fetchTodos();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddTodoActivity.class));
            }
        });

        Glide.with(this).load(AppConfig.URL_NAVHEADERBG).crossFade().diskCacheStrategy(DiskCacheStrategy.ALL).into(imgNavHeaderBg);
        Glide.with(this).load(AppConfig.URL_PROFILEIMG).crossFade().thumbnail(0.5f).bitmapTransform(new CircleTransform(this)).diskCacheStrategy(DiskCacheStrategy.ALL).into(imgProfile);

        tvFullname.setText(user.get("fullname"));
        tvDepartment.setText(user.get("partname"));

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                drawer.closeDrawers();

                switch (menuItem.getItemId()) {

                    case R.id.nav_home:
                        break;

                    case R.id.nav_notifications:
                        startActivity(new Intent(MainActivity.this, EventActivity.class));
                        finish();

                        return false;

                    case R.id.nav_branchs:
                        startActivity(new Intent(MainActivity.this, BranchActivity.class));
                        finish();

                        return true;

                    case R.id.nav_customers:
                        startActivity(new Intent(MainActivity.this, CustomerActivity.class));
                        finish();

                        return true;

                    case R.id.nav_location:
                        startActivity(new Intent(MainActivity.this, LocationActivity.class));
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

        navigationView.getMenu().getItem(0).setChecked(true);

        navigationView.getMenu().getItem(5).setTitle("Phòng / chi nhánh");

        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS);
            }
        } else {
            boolean_permission = true;
        }

        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
            if (boolean_permission) {
                Intent intent = new Intent(getApplicationContext(), GPSTrackerService.class);
                startService(intent);
            }
        } else {
            showSnack();
        }
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

                            navigationView.getMenu().getItem(5).getSubMenu().add(3, 0, 0, "Công việc cá nhân").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    phongban = item.getItemId() + "";
                                    prefManager.setPhongban1(phongban);

                                    getSupportActionBar().setTitle(item + "");

                                    fetchTodos();

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
                                        prefManager.setPhongban1(phongban);

                                        getSupportActionBar().setTitle(item + "");

                                        fetchTodos();

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
                                    prefManager.setPhongban1(phongban);
                                }
                                navigationView.getMenu().findItem(Integer.parseInt(phongban)).setChecked(true);

                                getSupportActionBar().setTitle(navigationView.getMenu().findItem(Integer.parseInt(phongban)).getTitle());
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
                    params.put("nhanvien", uid);
                    params.put("domain", domain);

                    return params;
                }
            };

            AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
        }
    }

    private void fetchTodos() {
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {

            myCalendar.deleteAllEvent();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_CHECKGPS, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {

                            JSONArray data = jsonObject.getJSONArray("data");

                            int n = data.length();
                            for (int i = 0; i < n; i++) {
                                JSONObject item = data.getJSONObject(i);
                                String date_begin = item.getString("date_begin");
                                String date_todo = date_begin.split("-")[2] + "-" + date_begin.split("-")[1] + "-" + date_begin.split("-")[0];
                                myCalendar.addEvent(date_todo, item.getString("gps"), item.getString("gpsok"), item.getString("gpserror"));
                            }

                            myCalendar.refreshCalendar();
                        } else {
                            Toast.makeText(getApplicationContext(), "Không thể tải dữ liệu từ máy chủ!", Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
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
                    if (!phongban.equals("") && !phongban.equals("0") && phongban != null) {
                        params.put("phongban", phongban + "");
                    } else {
                        params.put("nhanvien", uid + "");
                    }
                    params.put("from", from);

                    return params;
                }
            };

            AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
        }
    }

    private void checkUser() {
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {

            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_CHECKUSER, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (!success) {
                            Toast.makeText(getApplicationContext(), "Tài khoản của bạn đã bị vô hiệu hoá!", Toast.LENGTH_LONG).show();

                            logoutUser();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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
                    params.put("nhanvien", uid + "");

                    return params;
                }
            };

            AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
        }
    }

    private void checkVersion() {
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {

            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_CHECKVERSION, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (!response.equals("0")) {
                        try {
                            String currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                            if (!response.equals(currentVersion)) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Ứng dụng đã có phiên bản mới, bạn có muốn cập nhật?")
                                        .setCancelable(false)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                                final String appPackageName = getPackageName();
                                                try {
                                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                                } catch (android.content.ActivityNotFoundException anfe) {
                                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                                }
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                                dialog.cancel();
                                            }
                                        });
                                final AlertDialog alert = builder.create();
                                alert.show();
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
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
                    params.put("platform", "Android");

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
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();

        checkUser();

        navigationView.getMenu().getItem(0).setChecked(true);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchTodos();
            }
        }, 200);

        fetchParts();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean_permission = true;
                } else {
                    Toast.makeText(getApplicationContext(), "Vui lòng cho phép truy cập vị trí của bạn!", Toast.LENGTH_LONG).show();
                    finish();
                    System.exit(0);
                }
            }
        }
    }

    private void logoutUser() {
        prefManager.setLogin(false);
        prefManager.setDomain("");
        sqLiteHandler.deleteUsers();

        Intent service = new Intent(getApplicationContext(), GPSTrackerService.class);
        stopService(service);

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showSnack() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.fab), "Vui lòng kết nối Internet!", Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }

}
