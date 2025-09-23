import java.util.List;

interface Folder {
    String getName();
    String getSize();
}
interface MultiFolder extends Folder {
    List<Folder> getFolders();
}
