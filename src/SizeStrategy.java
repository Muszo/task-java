import java.util.List;

interface SizeStrategy {
    Size computeSize(List<Folder> folders);
}