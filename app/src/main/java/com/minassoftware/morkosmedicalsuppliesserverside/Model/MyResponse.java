package com.minassoftware.morkosmedicalsuppliesserverside.Model;

import java.util.List;

/**
 * Created by Mina on 9/19/2018.
 */

public class MyResponse {

    public long multicast_id;
    public int success;
    public int failure;
    public int canonical_id;
    public List<Result> requestList;

}
