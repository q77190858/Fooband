/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foogeez.bluetooth;

import java.util.HashMap;

public class BluetoothLeGattAttributes {
    private static HashMap<String, String> attributes = new HashMap<String, String>();
    
    public static String HEART_RATE_MEASUREMENT = "EE87FB00-C576-6669-1A17-C8EC0314413";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "5F73FA00-C8E9-EE61-AC20-FA06EF42179";
    
    //设备序列号SN
    public static String DEVICE_INFO_SN = "00002a25-0000-1000-8000-00805f9b34fb";   
    //设备软件版本
    public static String DEVICE_INFO_FW = "00002a26-0000-1000-8000-00805f9b34fb";    
    //设备硬件版本
    public static String DEVICE_INFO_HW = "00002a27-0000-1000-8000-00805f9b34fb";   
    //配对设置   硬件信息
    public static String DEVICE_BOND = "00002a29-0000-1000-8000-00805f9b34fb";
    
    public static String DEVICE_DFU_SERVICE = "00001530-1212-efde-1523-785feabcd123";
	public static String DEVICE_DFU_PCKT_PNT = "00001532-1212-efde-1523-785feabcd123";
	public static String DEVICE_DFU_CTRL_PNT = "00001531-1212-efde-1523-785feabcd123";
    
    //电量显示
    public static String DEVICE_BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_BATTERY_VALUE = "00002a19-0000-1000-8000-00805f9b34fb";
    
    //腕带数据
    public static String DEVICE_DATA_SERVICE = "5f73fa00-c8e9-ee61-ac20-fa06ef421796";
    public static String DEVICE_DATA_VALUE = "5f73fa01-c8e9-ee61-ac20-fa06ef421796";
    
    //腕带配置
    public static String DEVICE_SET_SERVICE = "ee87fb00-c576-6669-1a17-c8ec0314413b";
    public static String DEVICE_CONFIG_ADDR = "ee87fb01-c576-6669-1a17-c8ec0314413b";
    public static String DEVICE_CONFIG_DATA = "ee87fb02-c576-6669-1a17-c8ec0314413b";
    

    static {
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put(DEVICE_BOND, "配对数值");
        attributes.put(DEVICE_BATTERY_SERVICE, "电量显示");
        attributes.put(DEVICE_BATTERY_VALUE, "电量数值");
        attributes.put(DEVICE_DATA_SERVICE, "腕带数据");
        attributes.put(DEVICE_DATA_VALUE, "数据数值");
        attributes.put(DEVICE_SET_SERVICE, "腕带配置");
        attributes.put(DEVICE_CONFIG_ADDR, "配置地址");
        attributes.put(DEVICE_CONFIG_DATA, "配置数据");
        attributes.put(DEVICE_INFO_SN, "设备序列号");
        attributes.put(DEVICE_INFO_FW, "设备软件版本");
        attributes.put(DEVICE_INFO_HW, "设备硬件版本");
    }
    
    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}