package nati.financly.main_activity;

public class ItemView {
    private int image;
    private String categoryName, userComment, date, income_outcome, key, name, email;

    //Empty constructor
    public ItemView() {

    }

    //Constructor for RegisterActivity
    public ItemView(String name, String email) {
        this.name = name;
        this.email = email;
    }

    //Constructor for BalanceMainFragment
    public ItemView(String categoryName, String date, String income_outcome, String userComment) {
        this.categoryName = categoryName;
        this.date = date;
        this.income_outcome = income_outcome;
        this.userComment = userComment;
    }

    //Getters and setters//
    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIncome_outcome() {
        return income_outcome;
    }

    public void setIncome_outcome(String income_outcome) {
        this.income_outcome = income_outcome;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    ////
}
