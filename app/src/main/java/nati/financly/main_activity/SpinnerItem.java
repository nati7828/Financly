package nati.financly.main_activity;

public class SpinnerItem {
    private String category;
    private int image;

    public SpinnerItem(String category, int image){
        this.category = category;
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public int getImage() {
        return image;
    }


}
