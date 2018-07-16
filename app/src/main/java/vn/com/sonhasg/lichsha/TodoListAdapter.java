package vn.com.sonhasg.lichsha;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by nguyenphuoc on 04/23/2017.
 */

public class TodoListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<TodoListItem> todoListItems;

    public TodoListAdapter(Activity activity, List<TodoListItem> todoListItems) {
        this.activity = activity;
        this.todoListItems = todoListItems;
    }

    @Override
    public int getCount() {
        return todoListItems.size();
    }

    @Override
    public Object getItem(int location) {
        return todoListItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.todo_list_item, null);
        }

        TextView tvId = (TextView) convertView.findViewById(R.id.tvId);
        TextView tvNhanvien = (TextView) convertView.findViewById(R.id.tvNhanvien);
        TextView tvTen_nhanvien = (TextView) convertView.findViewById(R.id.tvTen_nhanvien);
        TextView tvName_vn = (TextView) convertView.findViewById(R.id.tvName_vn);
        TextView tvNghiphep = (TextView) convertView.findViewById(R.id.tvNghiphep);
        TextView tvRealgps = (TextView) convertView.findViewById(R.id.tvRealgps);
        TextView tvDiachi = (TextView) convertView.findViewById(R.id.tvDiachi);
        TextView tvTime_begin = (TextView) convertView.findViewById(R.id.tvTime_begin);
        TextView tvTime_end = (TextView) convertView.findViewById(R.id.tvTime_end);
        TextView tvLatitude = (TextView) convertView.findViewById(R.id.tvLatitude);
        TextView tvLongitude = (TextView) convertView.findViewById(R.id.tvLongitude);
        TextView tvGhichu = (TextView) convertView.findViewById(R.id.tvGhichu);
        TextView tvCheckgps = (TextView) convertView.findViewById(R.id.tvCheckgps);
        TextView tvComments = (TextView) convertView.findViewById(R.id.tvComments);
        ImageView ivCheckgps = (ImageView) convertView.findViewById(R.id.ivCheckgps);
        TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);
        LinearLayout rowTen_nhanvien = (LinearLayout) convertView.findViewById(R.id.rowTen_nhanvien);
        LinearLayout rowAddress = (LinearLayout) convertView.findViewById(R.id.rowAddress);

        TodoListItem todoListItem = todoListItems.get(position);
        tvId.setText(todoListItem.getId());
        tvNhanvien.setText(todoListItem.getNhanvien());
        tvTen_nhanvien.setText(todoListItem.getTen_nhanvien());
        if (tvTen_nhanvien.getText().equals("")) {
            rowTen_nhanvien.setVisibility(View.GONE);
        } else {
            rowTen_nhanvien.setVisibility(View.VISIBLE);
        }
        tvName_vn.setText(todoListItem.getName_vn());
        String nghiphep = todoListItem.getNghiphep();
        tvNghiphep.setText(nghiphep);
        tvDiachi.setText(todoListItem.getDiachi());
        tvTime_begin.setText(todoListItem.getTime_begin());
        tvTime_end.setText(todoListItem.getTime_end());
        tvLatitude.setText(todoListItem.getLatitude());
        tvLongitude.setText(todoListItem.getLongitude());
        String checkgps = todoListItem.getCheckgps();
        if (checkgps.equals("0")) {
            ivCheckgps.setImageResource(R.mipmap.todo_gpserror);
            ivCheckgps.setVisibility(View.VISIBLE);
            tvRealgps.setText(todoListItem.getRealgps());
        } else if (!checkgps.equals("-1") || nghiphep.equals("1")) {
            ivCheckgps.setImageResource(R.mipmap.todo_gpsok);
            ivCheckgps.setVisibility(View.VISIBLE);
        } else {
            ivCheckgps.setVisibility(View.GONE);
        }
        tvGhichu.setText(todoListItem.getGhichu());
        tvCheckgps.setText(checkgps);
        if (nghiphep.equals("1")) {
            rowAddress.setVisibility(View.GONE);
        } else {
            rowAddress.setVisibility(View.VISIBLE);
        }

        String comments = todoListItem.getComments();

        String time_begin = todoListItem.getTime_begin();
        String time_end = todoListItem.getTime_end();
        String date_begin = time_begin.split(" ")[0];
        String date_end = time_end.split(" ")[0];
        if (time_end.equals("")) {
            String time = (time_begin.split(" ")[1]).substring(0, 5);
            if (!date_end.equals(date_begin)) {
                time += ", " + date_begin.split("-")[2] + "/" + date_begin.split("-")[1];
            }
            tvTime.setText(time);
            rowAddress.setVisibility(View.GONE);
            tvComments.setText(comments);
        } else {
            String time = "";
            if (nghiphep.equals("0")) {
                time += "Từ: " + (time_begin.split(" ")[1]).substring(0, 5);
                if (!date_end.equals(date_begin)) {
                    time += ", ngày " + date_begin.split("-")[2] + "/" + date_begin.split("-")[1];
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

            if (todoListItem.getId().equals("")) {
                tvComments.setText(comments);
            } else {
                tvComments.setText(comments);
            }
        }
        return convertView;
    }
}
