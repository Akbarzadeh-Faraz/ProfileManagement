package model;

public class DiaryEntryRequest {
    public String title;
    public String content;

    public DiaryEntryRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}