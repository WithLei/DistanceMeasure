package com.android.renly.distancemeasure.Bean;

public class MeasureData {
    /**
     * 车牌号
     */
    private String carId;
    /**
     * 车辆朝向
     */
    private String carDirection;
    /**
     * 初始距离
     */
    private int startDistance;
    /**
     * 当前距离
     */
    private int nowDistance;
    /**
     * 测量结果
     */
    private String result;
    /**
     * 时长
     */
    private String measureTime;
    /**
     * 测量时间
     */
    private String time;
    /**
     * 数据库中分配的ID
     */
     private int theID;

    public MeasureData(String carId, String carDirection, int startDistance, int nowDistance, String result, String measureTime, String time,int ID) {
        this.carId = carId;
        this.carDirection = carDirection;
        this.startDistance = startDistance;
        this.nowDistance = nowDistance;
        this.result = result;
        this.measureTime = measureTime;
        this.time = time;
        this.theID = ID;
    }

    public MeasureData(String carId, String carDirection, int startDistance, int nowDistance, String result, String measureTime, String time) {
        this.carId = carId;
        this.carDirection = carDirection;
        this.startDistance = startDistance;
        this.nowDistance = nowDistance;
        this.result = result;
        this.measureTime = measureTime;
        this.time = time;
    }

    public MeasureData() {
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public String getCarDirection() {
        return carDirection;
    }

    public void setCarDirection(String carDirection) {
        this.carDirection = carDirection;
    }

    public int getStartDistance() {
        return startDistance;
    }

    public void setStartDistance(int startDistance) {
        this.startDistance = startDistance;
    }

    public int getNowDistance() {
        return nowDistance;
    }

    public void setNowDistance(int nowDistance) {
        this.nowDistance = nowDistance;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getMeasureTime() {
        return measureTime;
    }

    public void setMeasureTime(String measureTime) {
        this.measureTime = measureTime;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getTheID() {
        return theID;
    }

    public void setTheID(int theID) {
        this.theID = theID;
    }
}
