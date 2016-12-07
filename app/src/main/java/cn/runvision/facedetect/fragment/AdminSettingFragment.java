package cn.runvision.facedetect.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import cn.runvision.facedetect.R;

/**
 * Created by Administrator on 2016/11/29.
 */

public class AdminSettingFragment extends Fragment {
    private LinearLayout admin_password_title,admin_auth_title,admin_add_or_delete_title;
    private LinearLayout mAddAdmin_ly,mDeleteAdmin_ly,mAddAdmin_Auth_ly;
    private MyClick myClick=new MyClick();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mView = inflater.inflate(R.layout.admin_fragment, null);
        initView(mView);
        return mView;
    }

    private void initView( View mView) {
        //三个插入的布局
        admin_password_title=( LinearLayout) mView.findViewById(R.id.admin_password_title);
        admin_auth_title=( LinearLayout) mView.findViewById(R.id.admin_auth_title);
        admin_add_or_delete_title=( LinearLayout) mView.findViewById(R.id.admin_add_or_delete_title);
        admin_password_title.setVisibility(View.GONE);
        admin_auth_title.setVisibility(View.GONE);
        admin_add_or_delete_title.setVisibility(View.VISIBLE);
        //新增管理员布局点击事件
        mAddAdmin_ly=( LinearLayout) mView.findViewById(R.id.mAddAdmin_ly);
        mDeleteAdmin_ly=( LinearLayout) mView.findViewById(R.id.mDeleteAdmin_ly);
        mAddAdmin_ly.setOnClickListener(myClick);
        mDeleteAdmin_ly.setOnClickListener(myClick);
        mAddAdmin_Auth_ly=( LinearLayout) mView.findViewById(R.id.mAddAdmin_Auth_ly);
        mAddAdmin_Auth_ly.setOnClickListener(myClick);



    }






    class MyClick implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.mAddAdmin_ly://新增管理员
                    admin_add_or_delete_title.setVisibility(View.GONE);
                    admin_password_title.setVisibility(View.VISIBLE);
                    break;
                case R.id.mDeleteAdmin_ly://删除管理员
                    admin_add_or_delete_title.setVisibility(View.GONE);
                    admin_password_title.setVisibility(View.VISIBLE);
                    break;
                case R.id.mAddAdmin_Auth_ly:
                    admin_add_or_delete_title.setVisibility(View.GONE);
                    admin_password_title.setVisibility(View.GONE);
                    admin_auth_title.setVisibility(View.VISIBLE);
                    break;

            }

        }
    }




    }