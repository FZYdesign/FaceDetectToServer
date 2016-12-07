package cn.runvision.facedetect.view;

/**
 * Created by Administrator on 2016/11/7.
 */

public class CheckBoxBean {
    private boolean isChecked;

    public CheckBoxBean() {
    }

    public CheckBoxBean(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
