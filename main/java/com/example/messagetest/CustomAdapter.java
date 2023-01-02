package com.example.messagetest;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
//import com.example.messagetest.R;

import java.util.List;

// ListView에서 데이터를 처리하기 위해서는 맞춤형 어댑터가 필요합니다.
// ListView에서 기존 항목을 삭제하려고 할때마다 해당 항목을 길게 누를 수 있습니다. 이를 통해 확인 요청하는 상자 표시.
// 사용자가 확인하는 대로 DB에서도 해당 항목 삭제 기능.
public class CustomAdapter extends ArrayAdapter<ContactModel> {

    Context context;
    List<ContactModel> contacts;

    public CustomAdapter(@NonNull Context context, List<ContactModel> contacts) {
        super(context, 0, contacts);
        this.context = context;
        this.contacts = contacts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // database helper Object 생성
        // database 핸들러
        DbHelper db = new DbHelper(context);

        // 위치에 대한 데이터 항목 가져오기
        ContactModel c = getItem(position);

        // 기존 보기가 재사용되고 있는지 확인하고 그렇지 않으면 보기를 확장
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
        }

        LinearLayout linearLayout = (LinearLayout) convertView.findViewById(R.id.linear);

        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
        TextView tvPhone = (TextView) convertView.findViewById(R.id.tvPhone);

        // Populate the data into the template
        // view using the data object
        tvName.setText(c.getName());
        tvPhone.setText(c.getPhoneNo());

        linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // MaterialAlertDialog Box 생성
                new MaterialAlertDialogBuilder(context)
                        .setTitle("연락처 삭제")
                        .setMessage("정말 이 연락처를 삭제하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 데이터베이스에서 지정된 연락처 삭제
                                db.deleteContact(c);
                                // 목록에서 항목 제거하기
                                contacts.remove(c);
                                // Data Set이 변경되었음을 Listview에  알려주기.
                                notifyDataSetChanged();
                                Toast.makeText(context, "연락처가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
                return false;
            }
        });
        // 완성된 뷰를 반환하여 화면에 렌더링
        return convertView;
    }

    // 이 메서드는 ListView를 업데이트
    public void refresh(List<ContactModel> list) {
        contacts.clear();
        contacts.addAll(list);
        notifyDataSetChanged();
    }
}