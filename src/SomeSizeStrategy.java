import java.util.List;

class SomeSizeStrategy implements SizeStrategy {
    @Override
    public Size computeSize(List<Folder> folders) {
        int n = (folders == null) ? 0 : folders.size();
        if (n == 0) return Size.SMALL;
        if (n <= 5) return Size.MEDIUM;
        return Size.LARGE;
    }
}
