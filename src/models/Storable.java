package models;

public interface Storable {
    String marshal(); // Returns object data as String content

    void unmarshal(String data); // Restores an object from a String content
}