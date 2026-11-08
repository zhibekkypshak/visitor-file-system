package edu.visitor.fs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TreeIntegrityTest {

    @Test
    void negativeFileSizeIsRejectedButZeroIsAllowed() {
        assertThrows(TreeIntegrityException.class, () -> new FileNode("bad.bin", -1));
        assertEquals(0, new FileNode("ok.bin", 0).getSize());
    }

    @Test
    void duplicateSiblingNameIsRejectedAndTreeIsLeftUnchanged() {
        DirectoryNode dir = new DirectoryNode("d");
        FileNode original = dir.addFile("same", 1);

        assertThrows(TreeIntegrityException.class, () -> dir.addFile("same", 2));
        assertThrows(TreeIntegrityException.class, () -> dir.addDirectory("same"));

        assertEquals(1, dir.getChildren().size());
        assertSame(original, dir.getChild("same").orElseThrow());
    }

    @Test
    void sameNameIsAllowedInDifferentDirectories() {
        DirectoryNode root = new DirectoryNode("root");
        root.addDirectory("a").addFile("notes.txt", 1);
        root.addDirectory("b").addFile("notes.txt", 2);

        assertEquals(2, root.getChildren().size());
    }

    @Test
    void nodeThatAlreadyHasAParentCannotBeAddedToAnotherDirectory() {
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        FileNode file = first.addFile("shared.txt", 1);

        assertThrows(TreeIntegrityException.class, () -> second.add(file));

        assertTrue(second.getChildren().isEmpty());
        assertSame(first, file.getParent().orElseThrow());
    }

    @Test
    void directoryCannotBeAddedToItself() {
        DirectoryNode dir = new DirectoryNode("d");

        assertThrows(TreeIntegrityException.class, () -> dir.add(dir));
        assertTrue(dir.getChildren().isEmpty());
    }

    @Test
    void ancestorCannotBeAddedBelowItsDescendantBecauseThatWouldCreateACycle() {
        DirectoryNode a = new DirectoryNode("a");
        DirectoryNode b = a.addDirectory("b");
        DirectoryNode c = b.addDirectory("c");

        assertThrows(TreeIntegrityException.class, () -> c.add(a)); // a -> b -> c -> a
        assertThrows(TreeIntegrityException.class, () -> b.add(a));

        assertTrue(c.getChildren().isEmpty());
        assertTrue(a.getParent().isEmpty());
    }

    @Test
    void removedNodeCanBeMovedToAnotherDirectory() {
        DirectoryNode from = new DirectoryNode("from");
        DirectoryNode to = new DirectoryNode("to");
        from.addFile("moving.txt", 5);

        Node moved = from.remove("moving.txt").orElseThrow();
        to.add(moved);

        assertTrue(from.getChild("moving.txt").isEmpty());
        assertSame(moved, to.getChild("moving.txt").orElseThrow());
        assertSame(to, moved.getParent().orElseThrow());
    }

    @Test
    void anAncestorBecomesAddableAfterItIsDetachedFromTheChain() {
        DirectoryNode a = new DirectoryNode("a");
        DirectoryNode b = a.addDirectory("b");
        a.remove("b");

        b.add(a);

        assertSame(b, a.getParent().orElseThrow());
    }

    @Test
    void invalidNamesAreRejected() {
        assertThrows(TreeIntegrityException.class, () -> new FileNode(null, 1));
        assertThrows(TreeIntegrityException.class, () -> new FileNode("  ", 1));
        assertThrows(TreeIntegrityException.class, () -> new DirectoryNode("a/b"));
        assertThrows(TreeIntegrityException.class, () -> new DirectoryNode(".."));
    }

    @Test
    void nullChildIsRejected() {
        assertThrows(TreeIntegrityException.class, () -> new DirectoryNode("d").add(null));
    }

    @Test
    void deepNestingRemainsSupported() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode current = root;
        for (int level = 0; level < 200; level++) {
            current = current.addDirectory("level-" + level);
        }
        current.addFile("deep.txt", 1);

        TotalSizeVisitor visitor = new TotalSizeVisitor();
        root.accept(visitor);
        assertEquals(1, visitor.getTotalSize());
    }

    @Test
    void childrenCanNotBeChangedThroughTheReturnedList() {
        DirectoryNode dir = new DirectoryNode("d");
        dir.addFile("a", 1);
        List<Element> children = dir.getChildren();

        assertThrows(UnsupportedOperationException.class, children::clear);
        assertThrows(UnsupportedOperationException.class, () -> children.add(new FileNode("x", 1)));

        dir.addFile("b", 1);
        assertEquals(1, children.size());
        assertEquals(2, dir.getChildren().size());
    }
}