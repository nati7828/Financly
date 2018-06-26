package nati.financly.main_activity;

public class SpinnerItem {
    private boolean isHeader;
    private String category;
    private int image;

    public SpinnerItem(boolean isHeader, String category, int image){
        this.isHeader = isHeader;
        this.category = category;
        this.image = image;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public String getCategory() {
        return category;
    }

    public int getImage() {
        return image;
    }


}
