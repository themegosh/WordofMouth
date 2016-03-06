package ca.dmdev.test.wom;

import com.google.android.gms.location.places.Place;

import java.util.List;

import ca.dmdev.test.wom.acccount.User;

/**
 * Created by mathe_000 on 2016-02-05.
 */
public class Review {
    private String placeId;
    private String ownerId;
    private String title;
    private String description;
    private boolean like;

    public Review(String description, boolean like, String ownerId, String placeId, String title) {
        this.description = description;
        this.like = like;
        this.ownerId = ownerId;
        this.placeId = placeId;
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
