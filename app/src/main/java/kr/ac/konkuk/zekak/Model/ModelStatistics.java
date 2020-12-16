package kr.ac.konkuk.zekak.Model;

import com.google.gson.annotations.SerializedName;

public class ModelStatistics {
    public static ModelStatistics userStatistics;

    public ModelStatistics() {
    }

    public ModelStatistics(int month, int calories, int carbohydrate, int protein, int fat, int natrium, int sugar, int cholesterol, int saturatedFat, int transFat) {
        this.month = month;
        this.calories = calories;
        this.carbohydrate = carbohydrate;
        this.protein = protein;
        this.fat = fat;
        this.natrium = natrium;
        this.sugar = sugar;
        this.cholesterol = cholesterol;
        this.saturatedFat = saturatedFat;
        this.transFat = transFat;
    }

    @SerializedName("month")         // 이 통계의 달(month), 매달 자동으로 갱신됨
    public int month = 0;

    @SerializedName("calories")
    public float calories;

    @SerializedName("carbohydrate")
    public float carbohydrate;

    @SerializedName("protein")
    public float protein;

    @SerializedName("fat")
    public float fat;

    @SerializedName("natrium")
    public float natrium;

    @SerializedName("sugar")
    public float sugar;

    @SerializedName("cholesterol")
    public float cholesterol;

    @SerializedName("saturatedFat")
    public float saturatedFat;

    @SerializedName("transFat")
    public float transFat;

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public float getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public float getCarbohydrate() {
        return carbohydrate;
    }

    public void setCarbohydrate(int carbohydrate) {
        this.carbohydrate = carbohydrate;
    }

    public float getProtein() {
        return protein;
    }

    public void setProtein(int protein) {
        this.protein = protein;
    }

    public float getFat() {
        return fat;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public float getNatrium() {
        return natrium;
    }

    public void setNatrium(int natrium) {
        this.natrium = natrium;
    }

    public float getSugar() {
        return sugar;
    }

    public void setSugar(int sugar) {
        this.sugar = sugar;
    }

    public float getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(int cholesterol) {
        this.cholesterol = cholesterol;
    }

    public float getSaturatedFat() {
        return saturatedFat;
    }

    public void setSaturatedFat(int saturatedFat) {
        this.saturatedFat = saturatedFat;
    }

    public float getTransFat() {
        return transFat;
    }

    public void setTransFat(int transFat) {
        this.transFat = transFat;
    }
}
