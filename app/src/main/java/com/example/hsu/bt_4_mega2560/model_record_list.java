package com.example.hsu.bt_4_mega2560;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;
@Entity
public class model_record_list {
    @Id(autoincrement = true)
    Long id;
    @Unique
    String name;
    @Generated(hash = 1369625965)
    public model_record_list(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    @Generated(hash = 139128131)
    public model_record_list() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

}



