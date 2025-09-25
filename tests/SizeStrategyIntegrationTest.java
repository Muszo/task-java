import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class LeafFolder implements Folder {
    private final String name;
    private final String size;

    LeafFolder(String name, String size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSize() {
        return size;
    }
}

class MF implements MultiFolder {
    private final String name;
    private final String size;
    private final List<Folder> children;

    MF(String name, String size, List<Folder> children) {
        this.name = name;
        this.size = size;
        this.children = children;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSize() {
        return size;
    }

    @Override
    public List<Folder> getFolders() {
        return children;
    }
}

// Tests
public class SizeStrategyIntegrationTest {

    //  SomeSizeStrategy behavior
    @Test
    void someSizeStrategy_thresholds() {
        SomeSizeStrategy s = new SomeSizeStrategy();

        assertEquals(Size.SMALL, s.computeSize(List.of()), "0 children -> SMALL");

        // 1..5 children -> MEDIUM
        for (int k = 1; k <= 5; k++) {
            List<Folder> kids = new ArrayList<>();
            for (int i = 0; i < k; i++) kids.add(new LeafFolder("L" + i, "NA"));
            assertEquals(Size.MEDIUM, s.computeSize(kids), k + " children -> MEDIUM");
        }

        // >=6 children -> LARGE
        List<Folder> six = new ArrayList<>();
        for (int i = 0; i < 6; i++) six.add(new LeafFolder("L" + i, "NA"));
        assertEquals(Size.LARGE, s.computeSize(six), "6 children -> LARGE");
    }

    // FolderTree + strategy
    @Test
    void folderTree_findBySize_withStrategy_countsDirectChildrenOfEachNode() {
        MF root = new MF("Root", "NA", List.of(new LeafFolder("A", "NA"), new LeafFolder("B", "NA"), new MF("Sub", "NA", List.of(new LeafFolder("S1", "NA"), new LeafFolder("S2", "NA"), new LeafFolder("S3", "NA"), new LeafFolder("S4", "NA"), new LeafFolder("S5", "NA"), new LeafFolder("S6", "NA")))));

        FolderTree tree = new FolderTree(List.of(root));
        SomeSizeStrategy strategy = new SomeSizeStrategy();

        // Strategy size is based on number of direct children:
        //  Root has 3 children -> MEDIUM
        //  Sub has 6 children -> LARGE
        //  Leaves have 0 children -> SMALL

        List<Folder> small = tree.findFoldersBySize(Size.SMALL, strategy);
        List<Folder> med = tree.findFoldersBySize(Size.MEDIUM, strategy);
        List<Folder> large = tree.findFoldersBySize(Size.LARGE, strategy);

        // Verify names included in each bucket
        assertTrue(containsByName(small, "A"));
        assertTrue(containsByName(small, "B"));
        assertTrue(containsByName(small, "S1"));
        assertTrue(containsByName(small, "S6"));

        assertTrue(containsByName(med, "Root"));
        assertFalse(containsByName(med, "Sub"));

        assertTrue(containsByName(large, "Sub"));
        assertFalse(containsByName(large, "Root"));

        // Count invariants (every node appears exactly in one bucket)
        int totalNodes = 1 /*Root*/ + 1 /*Sub*/ + 2 /*A,B*/ + 6 /*S1..S6*/;
        assertEquals(totalNodes, small.size() + med.size() + large.size());
        assertEquals(totalNodes, tree.count());
    }

    @Test
    void fileCabinet_findBySize_stringVariant_parsesEnumAndUsesDefaultStrategy() {
        // A node with exactly 5 children -> MEDIUM with SomeSizeStrategy
        MF root = new MF("Top", "NA", List.of(new LeafFolder("c1", "NA"), new LeafFolder("c2", "NA"), new LeafFolder("c3", "NA"), new LeafFolder("c4", "NA"), new LeafFolder("c5", "NA")));

        FileCabinet cab = new FileCabinet();
        cab.addFolder("", root);

        assertTrue(containsByName(cab.findFoldersBySize("medium"), "Top"), "case-insensitive parse");
        assertFalse(cab.findFoldersBySize("MEDIUM").isEmpty());
        assertTrue(cab.findFoldersBySize("invalid").isEmpty(), "invalid enum returns empty");
        assertTrue(cab.findFoldersBySize(null).isEmpty(), "null returns empty");
    }


    @Test
    void fileCabinet_findBySize_usesStrategy_andHandlesCaseInsensitiveInput() {
        FileCabinet cab = new FileCabinet();

        MF root = new MF("R", "NA", List.of(new LeafFolder("a", "NA"), new LeafFolder("b", "NA"), new LeafFolder("c", "NA"), new LeafFolder("d", "NA")));

        cab.addFolder("", root);

        List<Folder> resLower = cab.findFoldersBySize("medium");
        List<Folder> resUpper = cab.findFoldersBySize("MEDIUM");

        assertTrue(containsByName(resLower, "R"));
        assertTrue(containsByName(resUpper, "R"));
        assertTrue(cab.findFoldersBySize("LARGE").isEmpty());
        assertTrue(cab.findFoldersBySize(null).isEmpty());
    }

    @Test
    void fileCabinet_addRemove_affectsStrategyClassification() {
        FileCabinet cab = new FileCabinet();

        // Start with a SMALL node (0 children)
        cab.addFolder("", new MF("Node", "NA", List.of()));
        assertTrue(containsByName(cab.findFoldersBySize("SMALL"), "Node"));
        assertEquals(1, cab.count());

        // Add 1 child -> MEDIUM (since 1..5 => MEDIUM)
        cab.addFolder("Node", new LeafFolder("child1", "NA"));
        assertFalse(containsByName(cab.findFoldersBySize("SMALL"), "Node"));
        assertTrue(containsByName(cab.findFoldersBySize("MEDIUM"), "Node"));
        assertEquals(2, cab.count());

        // Add more children to exceed 5 -> LARGE
        cab.addFolder("Node", new LeafFolder("child2", "NA"));
        cab.addFolder("Node", new LeafFolder("child3", "NA"));
        cab.addFolder("Node", new LeafFolder("child4", "NA"));
        cab.addFolder("Node", new LeafFolder("child5", "NA"));
        cab.addFolder("Node", new LeafFolder("child6", "NA"));
        assertTrue(containsByName(cab.findFoldersBySize("LARGE"), "Node"));
        assertEquals(7, cab.count());

        // Remove Node (root)
        cab.removeFolder("Node");
        assertEquals(0, cab.count());
        assertTrue(cab.findFoldersBySize("SMALL").isEmpty());
        assertTrue(cab.findFoldersBySize("MEDIUM").isEmpty());
        assertTrue(cab.findFoldersBySize("LARGE").isEmpty());
    }

    @Test
    void findByName_worksAlongsideStrategy() {
        MF root = new MF("Root", "NA", List.of(new MF("Inner", "NA", List.of(new LeafFolder("X", "NA")))));
        FolderTree tree = new FolderTree(List.of(root));

        assertTrue(tree.findFolderByName("Root").isPresent());
        assertTrue(tree.findFolderByName("Inner").isPresent());
        assertTrue(tree.findFolderByName("X").isPresent());
        assertTrue(tree.findFolderByName("Nope").isEmpty());
    }

    private static boolean containsByName(List<Folder> list, String name) {
        for (Folder f : list) {
            if (name.equals(f.getName())) return true;
        }
        return false;
    }
}
