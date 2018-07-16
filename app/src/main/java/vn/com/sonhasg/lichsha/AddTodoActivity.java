package vn.com.sonhasg.lichsha;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.com.sonhasg.slidedatetimepicker.SlideDateTimeListener;
import vn.com.sonhasg.slidedatetimepicker.SlideDateTimePicker;

/**
 * Created by nguyenphuoc on 04/22/2017.
 */

public class AddTodoActivity extends AppCompatActivity {
    private EditText etName_vn, etDiachi, etGhichu;
    private TextView tvTime_begin, tvTime_end;
    private CheckBox cbNghiphep;
    private String id;
    private String name_vn;
    private String diachi = "";
    private String time_begin;
    private String time_end;
    private String uid = "0";
    private String ghichu;
    private TextInputLayout etLayoutDiachi, etLayoutGhichu;

    private ProgressDialog progressDialog;

    private SimpleDateFormat datetimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        PrefManager prefManager = new PrefManager(this);
        uid = prefManager.getUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        etName_vn = (EditText) findViewById(R.id.etName_vn);
        cbNghiphep = (CheckBox) findViewById(R.id.cbNghiphep);
        etDiachi = (EditText) findViewById(R.id.etDiachi);
        tvTime_begin = (TextView) findViewById(R.id.tvTime_begin);
        tvTime_end = (TextView) findViewById(R.id.tvTime_end);
        etGhichu = (EditText) findViewById(R.id.etGhichu);
        Button btnSave = (Button) findViewById(R.id.btnSave);
        Button btnSetBegin = (Button) findViewById(R.id.btnSetBegin);
        Button btnSetEnd = (Button) findViewById(R.id.btnSetEnd);
        etLayoutDiachi = (TextInputLayout) findViewById(R.id.etLayoutDiachi);
        etLayoutGhichu = (TextInputLayout) findViewById(R.id.etLayoutGhichu);
        LinearLayout llTime_begin = (LinearLayout) findViewById(R.id.llTime_begin);
        LinearLayout llTime_end = (LinearLayout) findViewById(R.id.llTime_end);

        id = getIntent().getStringExtra("id");
        name_vn = getIntent().getStringExtra("name_vn");
        String nghiphep = getIntent().getStringExtra("nghiphep");
        diachi = getIntent().getStringExtra("diachi");
        time_begin = getIntent().getStringExtra("time_begin");
        time_end = getIntent().getStringExtra("time_end");
        ghichu = getIntent().getStringExtra("ghichu");

        getSupportActionBar().setTitle(id == null ? "Thêm công việc mới" : "Cập nhật công việc");
        btnSave.setText(id == null ? "Thêm công việc" : "Lưu công việc");
        etName_vn.setText(name_vn != null ? name_vn : "");
        if (nghiphep != null && nghiphep.equals("1")) {
            cbNghiphep.setChecked(true);
            etLayoutDiachi.setVisibility(View.GONE);
            etLayoutGhichu.setVisibility(View.GONE);
            llTime_begin.setVisibility(View.GONE);
            llTime_end.setVisibility(View.GONE);
        } else {
            etLayoutDiachi.setVisibility(View.VISIBLE);
            etLayoutGhichu.setVisibility(View.VISIBLE);
            llTime_begin.setVisibility(View.VISIBLE);
            llTime_end.setVisibility(View.VISIBLE);
        }
        etDiachi.setText(diachi != null ? diachi : "");
        tvTime_begin.setText(time_begin != null ? time_begin.substring(0, 16) : datetimeFormatter.format(new Date()));
        Date a = new Date();
        a.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
        tvTime_end.setText(time_end != null ? time_end.substring(0, 16) : datetimeFormatter.format(a));
        etGhichu.setText(ghichu != null ? ghichu : "");

        cbNghiphep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etName_vn.setText("Nghỉ phép");

                    etDiachi.setText("");
                    etLayoutDiachi.setVisibility(View.GONE);
                    etGhichu.setText("");
                    etLayoutGhichu.setVisibility(View.GONE);
                } else {
                    etName_vn.setText(name_vn != null ? name_vn : "");

                    etDiachi.setText(diachi != null ? diachi : "");
                    etLayoutDiachi.setVisibility(View.VISIBLE);
                    etGhichu.setText(ghichu != null ? ghichu : "");
                    etLayoutGhichu.setVisibility(View.VISIBLE);
                }
            }
        });

        btnSetBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = null;
                try {
                    date = datetimeFormatter.parse(tvTime_begin.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                new SlideDateTimePicker.Builder(getSupportFragmentManager()).setListener(new SlideDateTimeListener() {

                    @Override
                    public void onDateTimeSet(Date date) {
                        tvTime_begin.setText(datetimeFormatter.format(date));
                    }

                    @Override
                    public void onDateTimeCancel() {

                    }
                }).setInitialDate(date).setIndicatorColor(Color.parseColor("#d61920")).build().show();
            }
        });

        btnSetEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = null;
                try {
                    date = datetimeFormatter.parse(tvTime_end.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                new SlideDateTimePicker.Builder(getSupportFragmentManager()).setListener(new SlideDateTimeListener() {

                    @Override
                    public void onDateTimeSet(Date date) {
                        tvTime_end.setText(datetimeFormatter.format(date));
                    }

                    @Override
                    public void onDateTimeCancel() {

                    }
                }).setInitialDate(date).setIndicatorColor(Color.parseColor("#d61920")).build().show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name_vn = etName_vn.getText().toString().trim();
                diachi = etDiachi.getText().toString().trim();
                time_begin = tvTime_begin.getText().toString();
                time_end = tvTime_end.getText().toString();
                ghichu = etGhichu.getText().toString();

                if (name_vn.equals("")) {
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập tên công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (diachi.equals("") && !cbNghiphep.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập địa chỉ!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (time_begin.equals(" ")) {
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn thời gian bắt đầu!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (time_end.equals(" ")) {
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn thời gian kết thúc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (time_end.equals(time_begin) && !cbNghiphep.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Thời gian kết thúc phải lớn hơn thời gian bắt đầu!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {

                    String tag_string_req = "req_add_todo";

                    progressDialog.setMessage("Đang xử lý ...");
                    showDialog();

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_ADD_TODO, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            hideDialog();

                            if (response.equals("0")) {
                                Toast.makeText(getApplicationContext(), "Không tìm thấy vị trí cho địa chỉ này!", Toast.LENGTH_LONG).show();
                            } else {
                                if (id == null) {

                                    onBackPressed();

                                    Toast.makeText(getApplicationContext(), "Thêm công việc thành công!", Toast.LENGTH_LONG).show();
                                } else {

                                    Toast.makeText(getApplicationContext(), "Lưu thông tin công việc thành công!", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "Không thể lưu công việc!", Toast.LENGTH_LONG).show();
                            hideDialog();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("api_key", AppConfig.API_KEY);
                            params.put("id", id != null ? id : "");
                            params.put("nhanvien", uid);
                            params.put("name_vn", name_vn);
                            params.put("nghiphep", cbNghiphep.isChecked() ? "1" : "0");
                            params.put("time_begin", time_begin);
                            params.put("time_end", time_end);
                            params.put("diachi", diachi);
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

        if (!(connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected())) {
            showSnack();
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
