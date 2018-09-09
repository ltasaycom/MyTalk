package com.example.luist.mytalk;

import java.util.Objects;

public class User {

    private String uid;

    private String displayName;

    private String email;

    private String photoUrl;

    private Double latitude;

    private Double longitude;

    private String direccion;

    private String address;

    public User() {
    }

    public User(String uid, String displayName, String email, String photoUrl, Double latitude, Double longitude, String direccion, String address) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.photoUrl = photoUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.direccion = direccion;
        this.address = address;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", direccion='" + direccion + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(uid, user.uid);
    }

}
