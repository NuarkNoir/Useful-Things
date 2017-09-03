package com.nuark.trashbox.utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;

public class JSONArrayToListOfString
{
    public static ArrayList<String> convert(JSONArray jArr) throws JSONException
    {
        ArrayList<String> list = new ArrayList<>();
        for (int i=0, l=jArr.length(); i<l; i++){
            list.add(jArr.get(i).toString());
        }
        return list;
    }

    public static JSONArray convert(Collection<Object> list)
    {
        return new JSONArray(list);
    }
}