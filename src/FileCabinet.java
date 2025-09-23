import java.util.*;

class FileCabinet implements Cabinet {
    private FolderTree folders;

    @Override
    public Optional<Folder> findFolderByName(String name) {
        return folders == null || name == null ? Optional.empty() : folders.findFolderByName(name);
    }

    @Override
    public List<Folder> findFoldersBySize(String size) {
        if (folders == null || size == null) return List.of();
        try {
            Size wanted = Size.valueOf(size.toUpperCase());
            return folders.findFoldersBySize(wanted, new SomeSizeStrategy());
        } catch (IllegalArgumentException ex) {
            return List.of();
        }
    }

    @Override
    public int count() {
        return folders == null ? 0 : folders.count();
    }

    public void addFolder(String path, Folder folder) {
        if (folders == null) folders = new FolderTree(List.of());
        folders.addFolder(path, folder);
    }

    public void removeFolder(String path) {
        if (folders != null) folders.removeFolder(path);
    }
}


class SomeSizeStrategy implements SizeStrategy {
    @Override
    public Size computeSize(List<Folder> folders) {
        int n = (folders == null) ? 0 : folders.size();
        if (n == 0) return Size.SMALL;
        if (n <= 5) return Size.MEDIUM;
        return Size.LARGE;
    }
}

class FolderTree {
    private static class FolderNode implements Folder {
        final Folder folder;
        List<FolderNode> children = new ArrayList<>();

        FolderNode(Folder folder) {
            this.folder = folder;
        }

        @Override
        public String getName() {
            return folder.getName();
        }

        @Override
        public String getSize() {
            return folder.getSize();
        }
    }

    private final List<FolderNode> roots = new ArrayList<>();

    public FolderTree(List<Folder> initialFolders) {
        if (initialFolders == null) throw new IllegalArgumentException("Initial folders cannot be null");
        for (Folder f : initialFolders) {
            if (f == null) continue;
            addAsChildren(roots, f);
        }
    }

    public void addFolder(String path, Folder folder) {
        if (folder == null) return;
        if (path == null || path.isBlank()) {
            addAsChildren(roots, folder);
            return;
        }
        FolderNode parent = findNodeByPath(path);
        if (parent == null) throw new IllegalArgumentException("Path not found: " + path);
        addAsChildren(parent.children, folder);
    }

    public void removeFolder(String path) {
        if (path == null || path.isBlank()) return;
        String[] segs = splitPath(path);
        if (segs.length == 0) return;

        if (segs.length == 1) {
            roots.removeIf(n -> segs[0].equals(n.getName()));
            return;
        }

        FolderNode parent = findNodeByPath(String.join("/", Arrays.copyOf(segs, segs.length - 1)));
        if (parent == null) return;
        String last = segs[segs.length - 1];
        parent.children.removeIf(n -> last.equals(n.getName()));
    }

    public Optional<Folder> findFolderByName(String name) {
        if (name == null) return Optional.empty();
        FolderNode n = dfsFindByName(name);
        return n == null ? Optional.empty() : Optional.of(n.folder);
    }

    public List<Folder> findFoldersBySize(String size) {
        if (size == null) return List.of();
        try {
            return findFoldersBySize(Size.valueOf(size.toUpperCase()), new SomeSizeStrategy());
        } catch (IllegalArgumentException ex) {
            return List.of();
        }
    }

    public List<Folder> findFoldersBySize(Size desired, SizeStrategy strategy) {
        if (desired == null || strategy == null) return List.of();
        List<Folder> out = new ArrayList<>();
        for (FolderNode r : roots) collectByStrategySize(r, desired, strategy, out);
        return out;
    }

    public int count() {
        int total = 0;
        Deque<FolderNode> stack = new ArrayDeque<>(roots);
        while (!stack.isEmpty()) {
            FolderNode n = stack.pop();
            total++;
            if (!n.children.isEmpty()) stack.addAll(n.children);
        }
        return total;
    }

    // helpers

    private void addAsChildren(List<FolderNode> into, Folder folder) {
        if (folder instanceof MultiFolder mf) {
            FolderNode node = new FolderNode(mf);
            into.add(node);
            List<Folder> subs = mf.getFolders();
            if (subs != null) {
                for (Folder sub : subs) if (sub != null) addAsChildren(node.children, sub);
            }
        } else {
            into.add(new FolderNode(folder));
        }
    }

    private FolderNode findNodeByPath(String path) {
        String[] segs = splitPath(path);
        if (segs.length == 0) return null;

        List<FolderNode> level = roots;
        FolderNode current = null;
        for (String seg : segs) {
            current = null;
            for (FolderNode n : level) {
                if (seg.equals(n.getName())) {
                    current = n;
                    break;
                }
            }
            if (current == null) return null;
            level = current.children;
        }
        return current;
    }

    private FolderNode dfsFindByName(String name) {
        Deque<FolderNode> stack = new ArrayDeque<>(roots);
        while (!stack.isEmpty()) {
            FolderNode n = stack.pop();
            if (name.equals(n.getName())) return n;
            if (!n.children.isEmpty()) stack.addAll(n.children);
        }
        return null;
    }

    private void collectByStrategySize(FolderNode node, Size desired, SizeStrategy strategy, List<Folder> out) {
        List<Folder> childFolders = new ArrayList<>(node.children.size());
        for (FolderNode ch : node.children) childFolders.add(ch.folder);

        if (strategy.computeSize(childFolders) == desired) out.add(node.folder);
        for (FolderNode ch : node.children) collectByStrategySize(ch, desired, strategy, out);
    }

    private static String[] splitPath(String path) {
        return Arrays.stream(path.trim().split("/"))
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
    }
}
