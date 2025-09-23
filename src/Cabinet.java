import java.util.List;
import java.util.Optional;

interface Cabinet {
    // zwraca dowolny element o podanej nazwie
    Optional<Folder> findFolderByName(String name);

    // zwraca wszystkie foldery podanego rozmiaru SMALL/MEDIUM/LARGE
    List<Folder> findFoldersBySize(String size);

    //zwraca liczbę wszystkich obiektów tworzących strukturę
    int count();
}
