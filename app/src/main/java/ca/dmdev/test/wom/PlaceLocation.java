package ca.dmdev.test.wom;

import android.net.Uri;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by mathe_000 on 2016-02-05.
 */
public class PlaceLocation {

    private String id;
    private CharSequence address;
    private LatLng location;
    private CharSequence name;
    private String description;
    private Uri url;
    private CharSequence phone;
    private List<Category> categories;

    public PlaceLocation(Place place){
        id = place.getId();
        address = place.getAddress();
        location = place.getLatLng();
        name = place.getName();
        url = place.getWebsiteUri();
        phone = place.getPhoneNumber();
    }

    public CharSequence getAddress() {
        return address;
    }

    public void setAddress(CharSequence address) {
        this.address = address;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public CharSequence getName() {
        return name;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }

    public CharSequence getPhone() {
        return phone;
    }

    public void setPhone(CharSequence phone) {
        this.phone = phone;
    }

    public Uri getUrl() {
        return url;
    }

    public void setUrl(Uri url) {
        this.url = url;
    }
}
