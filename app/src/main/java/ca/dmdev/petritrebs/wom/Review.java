package ca.dmdev.petritrebs.wom;

import java.util.List;

/**
 * Created by mathe_000 on 2016-02-05.
 */
public class Review {
    private int id;
    private Place place;
    private User user;
    private String title;
    private String description;
    private boolean endorce;
    private List<Tag> tags;
    private List<Endorcement> endorcements;

    public Review(){

    }
}
