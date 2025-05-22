package managers;

import java.util.List;

import models.Storable;

public interface StorageManager {
    public void load(String filePath); // tha diavazei

    public void save(Storable s, String filePath, boolean append); // tha apothikeyei

    // append = true: create (kainoyrgio)
    // = false: allazeis kati
}