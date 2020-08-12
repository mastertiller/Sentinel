package com.alibaba.csp.sentinel.demo.spring.webmvc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TestForSort {
    public static void main(String[] args) {
        int num = 50000;
        Random random = new Random();
        List<Integer> randomList = new ArrayList<>();
        for (int i = 0;i < num;i++){
            randomList.add(random.nextInt(num));
        }
        Collections.sort(randomList);
    }
}
