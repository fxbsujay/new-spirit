package cn.spirit.go.model;

import java.util.ArrayList;
import java.util.List;

public class Page<T> {

    public List<T> list;

    public Integer page;

    public Integer total;


    public Page() {
        this.list = new ArrayList<>();
        this.page = 0;
        this.total = 0;
    }
}
