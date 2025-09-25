import java.util.*;

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

    private final FolderNode root = new FolderNode(new Folder() {
        @Override public String getName() { return ""; }
        @Override public String getSize() { return "NA"; }
    });

    private int size = 0;

    public FolderTree(List<Folder> initialFolders) {
        if (initialFolders == null) throw new IllegalArgumentException("Initial folders cannot be null");
        for (Folder f : initialFolders) {
            if (f == null) continue;
            addAsChildren(root.children, f);
        }
    }

    public void addFolder(List<String> path, Folder folder) {
        if (folder == null) return;
        if (path == null || path.isEmpty()) {
            addAsChildren(root.children, folder);
            return;
        }
        FolderNode parent = findNodeByPath(path);
        if (parent == null) throw new IllegalArgumentException("Path not found: " + String.join("/", path));
        addAsChildren(parent.children, folder);
    }

    public void removeFolder(List<String> path) {
        if (path == null || path.isEmpty()) return;

        if (path.size() == 1) {
            String name = path.get(0);
            for (Iterator<FolderNode> it = root.children.iterator(); it.hasNext(); ) {
                FolderNode n = it.next();
                if (name.equals(n.getName())) {
                    size -= subtreeSize(n);
                    it.remove();
                    break;
                }
            }
            return;
        }

        FolderNode parent = findNodeByPath(path.subList(0, path.size() - 1));
        if (parent == null) return;
        String last = path.get(path.size() - 1);
        for (Iterator<FolderNode> it = parent.children.iterator(); it.hasNext(); ) {
            FolderNode child = it.next();
            if (last.equals(child.getName())) {
                size -= subtreeSize(child);
                it.remove();
                break;
            }
        }
    }

    public Optional<Folder> findFolderByName(String name) {
        if (name == null) return Optional.empty();
        FolderNode n = dfsFindByName(name);
        return n == null ? Optional.empty() : Optional.of(n.folder);
    }

    public List<Folder> findFoldersBySize(Size desired, SizeStrategy strategy) {
        if (desired == null || strategy == null) return List.of();
        List<Folder> out = new ArrayList<>();
        for (FolderNode r : root.children) collectBySizeStrategy(r, desired, strategy, out);
        return out;
    }

    public int count() {
        return size;
    }

    // helpers

    private void addAsChildren(List<FolderNode> into, Folder folder) {
        if (folder instanceof MultiFolder mf) {
            FolderNode node = new FolderNode(mf);
            into.add(node);
            size++;
            List<Folder> subs = mf.getFolders();
            if (subs != null) {
                for (Folder sub : subs) if (sub != null) addAsChildren(node.children, sub);
            }
        } else {
            into.add(new FolderNode(folder));
            size++;
        }
    }

    private int subtreeSize(FolderNode node) {
        int total = 1;
        for (FolderNode ch : node.children) total += subtreeSize(ch);
        return total;
    }

    private FolderNode findNodeByPath(List<String> path) {
        if (path == null || path.isEmpty()) return null;

        List<FolderNode> level = root.children;
        FolderNode current = null;
        for (String seg : path) {
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
        Deque<FolderNode> stack = new ArrayDeque<>(root.children);
        while (!stack.isEmpty()) {
            FolderNode n = stack.pop();
            if (name.equals(n.getName())) return n;
            if (!n.children.isEmpty()) stack.addAll(n.children);
        }
        return null;
    }

    private void collectBySizeStrategy(FolderNode node, Size desired, SizeStrategy strategy, List<Folder> out) {
        List<Folder> childFolders = new ArrayList<>(node.children.size());
        for (FolderNode ch : node.children) childFolders.add(ch.folder);

        if (strategy.computeSize(childFolders) == desired) out.add(node.folder);
        for (FolderNode ch : node.children) collectBySizeStrategy(ch, desired, strategy, out);
    }
}
