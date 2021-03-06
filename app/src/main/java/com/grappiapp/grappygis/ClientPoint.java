package com.grappiapp.grappygis;

public class ClientPoint{

    private float x;
    private float y;
    private String description;
    private String imageUrl;
    private int pointHash;
    private String category;
    private boolean isUpdateSystem;
    public static final String POINTS_DATA_KEY = "POINTS_DATA";
    public String user;

    public ClientPoint(float x, float y, String description, String imageUrl, String category, boolean isUpdateSystem) {
        this.x = x;
        this.y = y;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.isUpdateSystem = isUpdateSystem;
        this.pointHash = createPointHash(x, y, imageUrl, description, category, isUpdateSystem);
        this.user = "";
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public ClientPoint(float x, float y, String description, String imageUrl, String category, boolean isUpdateSystem, String user) {
        this.x = x;
        this.y = y;
        this.description = description;
        this.imageUrl = imageUrl;
        this.pointHash = createPointHash(x, y, imageUrl, description, category, isUpdateSystem);
        this.category = category;
        this.isUpdateSystem = isUpdateSystem;
        this.user = user;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getPointHash() {
        return pointHash;
    }


    public String getCategory() {
        return category;
    }

    public boolean isUpdateSystem() {
        return isUpdateSystem;
    }

    public static int createPointHash(float x, float y, String imgUrl, String description, String category, boolean isUpdateSystem){
        return (int)x + (int)y + (imgUrl == null ? 0 : imgUrl.hashCode()) + (description == null ? 0 : description.hashCode()) + (category == null ? 0 : category.hashCode()) + (isUpdateSystem ? 1 : 0);
    }
}
