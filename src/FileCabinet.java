import java.util.*;

class FileCabinet implements Cabinet {
    private final SizeStrategy strategy;
    private FolderTree folders;

    FileCabinet() {
        this(new SomeSizeStrategy());
    }

    FileCabinet(SizeStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("strategy cannot be null");
        this.strategy = strategy;
    }

    @Override
    public Optional<Folder> findFolderByName(String name) {
        return folders == null || name == null ? Optional.empty() : folders.findFolderByName(name);
    }

    @Override
    public List<Folder> findFoldersBySize(String size) {
        if (folders == null) return List.of();
        Optional<Size> parsed = SizeUtils.parse(size);
        return parsed.isEmpty() ? List.of() : folders.findFoldersBySize(parsed.get(), strategy);
    }

    @Override
    public int count() {
        return folders == null ? 0 : folders.count();
    }

    public void addFolder(String path, Folder folder) {
        if (folders == null) folders = new FolderTree(List.of());
        List<String> components = (path == null || path.isBlank()) ? List.of() : splitPath(path);
        folders.addFolder(components, folder);
    }

    public void removeFolder(String path) {
        if (folders == null) return;
        if (path == null || path.isBlank()) return;
        folders.removeFolder(splitPath(path));
    }

    // helpers
    private static List<String> splitPath(String path) {
        return Arrays.stream(path.trim().split("/"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}

