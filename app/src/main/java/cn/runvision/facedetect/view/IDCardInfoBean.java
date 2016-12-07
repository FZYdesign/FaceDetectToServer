package cn.runvision.facedetect.view;



import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/9.
 */

public class IDCardInfoBean implements Serializable {
    private String mName;
    private String mSex;
    private String mNation;
    private String mBirthYear;
    private String mAddress;
    private String mIDNumber;
    private String Issue;
    private String mIDValidity;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getSex() {
        return mSex;
    }

    public void setSex(String sex) {
        this.mSex = sex;
    }

    public String getNation() {
        return mNation;
    }

    public void setNation(String nation) {
        this.mNation = nation;
    }

    public String getBirthYear() {
        return mBirthYear;
    }

    public void setBirthYear(String birthYear) {
        this.mBirthYear = birthYear;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public String getIDNumber() {
        return mIDNumber;
    }

    public void setIDNumber(String IDNumber) {
        this.mIDNumber = IDNumber;
    }

    public String getIssue() {
        return Issue;
    }

    public void setIssue(String issue) {
        Issue = issue;
    }

    public String getIDValidity() {
        return mIDValidity;
    }

    public void setIDValidity(String IDValidity) {
        this.mIDValidity = IDValidity;
    }
}
