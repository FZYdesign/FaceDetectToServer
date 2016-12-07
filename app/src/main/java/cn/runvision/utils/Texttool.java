package cn.runvision.utils;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/12/2.
 */

public class Texttool {

    public static void setText(View view, int id,String text){

        TextView te=(TextView) view.findViewById(id);
        if(te!=null){
            if(text!=null && !text.equals("")){
                te.setText(text);
            }
        }
}
    public static void setText(Activity a, int id,String text){
        TextView te=(TextView) a.findViewById(id);
        if(te!=null){
            if(text!=null && !text.equals("")){
                te.setText(text);
            }
        }
    }
    public static void setText(TextView view,String text){
        if(view!=null){
            if(text!=null && !text.equals("")){
                view.setText(text);
            }
        }
    }

    public static Boolean Havecontent(TextView te){
        if(te !=null && !(te.getText().toString().equals(""))){
            return true;
        }else{
            return false;
        }

    }

    public static Boolean Havecontent(Activity a,int id){
        TextView text=(TextView) a.findViewById(id);
        if(text !=null && !(text.getText().toString().equals(""))){
            return true;
        }else{
            return false;
        }

    }
    public static Boolean Havecontent(View view,int id){
        TextView text=(TextView) view.findViewById(id);
        if(text !=null && !(text.getText().toString().trim().equals(""))){
            return true;
        }else{
            return false;
        }

    }
    public static String Gettext(TextView te){
        if(te!=null){
            if(!te.getText().equals("")){
                return te.getText().toString();
            }else{
                return "";
            }
        }else{
            return "";
        }

    }
    public static String Gettext(View view, int id){
        if(view!=null){
            TextView te=(TextView) view.findViewById(id);
            if(te!=null){
                return te.getText().toString();
            }else{
                return "";
            }

        }else{
            return "";
        }

    }
    public static String Gettext(Activity a, int id){
        if(a!=null){
            TextView te=(TextView) a.findViewById(id);
            if(te!=null){
                return te.getText().toString();
            }else{
                return "";
            }

        }else{
            return "";
        }

    }


}
