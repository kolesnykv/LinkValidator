package com.knubisoft;

import com.knubisoft.utils.LinkManager;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String site = "https://freemaxpictures.com/";
        Map<String, Integer> result = new LinkManager(site).collect();
        System.out.println(result.size());
        System.out.println(result);
    }
}