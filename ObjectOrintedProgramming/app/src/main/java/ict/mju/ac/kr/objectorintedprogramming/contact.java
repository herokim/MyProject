package ict.mju.ac.kr.objectorintedprogramming;

/**
 * Created by mac on 2016. 12. 5..
 */

public class contact {

     private String name, tel, address, desc, image;

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public contact(String name, String tel, String address, String desc, String image) {
        this.name = name;

        this.tel = tel;
        this.address = address;
        this.desc = desc;
        this.image = image;
    }
}
