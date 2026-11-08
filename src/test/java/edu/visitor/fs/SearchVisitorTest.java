package edu.visitor.fs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class SearchVisitorTest {

    private static List<String> search(Element root, SearchVisitor visitor) {
        root.accept(visitor);
        return visitor.getMatchingPaths();
    }


    @Test
    void fileMustSatisfyBothTheNameAndTheSizeCondition() {
        DirectoryNode root = TestTrees.workspace().root();

        List<String> found = search(root, SearchVisitor.nameEndsWith(".txt", 300));

        assertEquals(List.of("archive/2024/notes.txt", "notes.txt"), found);
    }

    @Test
    void sizeThresholdIsInclusive() {
        DirectoryNode root = TestTrees.workspace().root();

        assertEquals(
                List.of("photos/holiday/beach-edit.jpg", "photos/holiday/beach.jpg"),
                search(root, SearchVisitor.nameEndsWith(".jpg", 4_000)));
        assertEquals(
                List.of("photos/holiday/beach-edit.jpg"),
                search(root, SearchVisitor.nameEndsWith(".jpg", 4_001)));
    }

    @Test
    void largeFileWithWrongNameIsNotReturned() {
        DirectoryNode root = TestTrees.workspace().root();

        List<String> found = search(root, SearchVisitor.nameContains("beach", 0));

        assertEquals(List.of("photos/holiday/beach-edit.jpg", "photos/holiday/beach.jpg"), found);
    }

    @Test
    void zeroThresholdLetsTheNameDecideAlone() {
        DirectoryNode root = TestTrees.workspace().root();

        assertEquals(
                List.of("archive/2024/notes.txt", "notes.txt", "photos/holiday/notes.txt"),
                search(root, SearchVisitor.nameEquals("notes.txt", 0)));
    }

    @Test
    void customPredicateCanBeUsedAsNameCriterion() {
        DirectoryNode root = TestTrees.workspace().root();

        List<String> found = search(root, new SearchVisitor(name -> name.startsWith("t"), 0));

        assertEquals(List.of("todo.txt"), found);
    }

    @Test
    void pathsAreRelativeToTheChosenRoot() {
        TestTrees.Workspace ws = TestTrees.workspace();

        List<String> found = search(ws.photos(), SearchVisitor.nameEquals("notes.txt", 0));

        assertEquals(List.of("holiday/notes.txt"), found);
    }

    @Test
    void pathsAreSortedLexicographicallyNotInTraversalOrder() {
        DirectoryNode root = new DirectoryNode("root");
        root.addDirectory("a").addFile("b.txt", 1);
        root.addFile("a-x.txt", 1);

        List<String> found = search(root, new SearchVisitor(name -> true, 0));

        assertEquals(List.of("a-x.txt", "a/b.txt"), found);
    }

    @Test
    void resultDoesNotDependOnInsertionOrder() {
        DirectoryNode first = new DirectoryNode("r");
        first.addFile("b.txt", 1);
        first.addFile("a.txt", 1);
        first.addDirectory("d").addFile("c.txt", 1);

        DirectoryNode second = new DirectoryNode("r");
        second.addDirectory("d").addFile("c.txt", 1);
        second.addFile("a.txt", 1);
        second.addFile("b.txt", 1);

        List<String> expected = List.of("a.txt", "b.txt", "d/c.txt");
        assertEquals(expected, search(first, SearchVisitor.nameEndsWith(".txt", 0)));
        assertEquals(expected, search(second, SearchVisitor.nameEndsWith(".txt", 0)));
    }

    @Test
    void whenTheRootIsAFileItsOwnNameIsReported() {
        List<String> found = search(new FileNode("solo.txt", 10), SearchVisitor.nameEndsWith(".txt", 10));

        assertEquals(List.of("solo.txt"), found);
    }

    @Test
    void noMatchGivesAnEmptyListNotNull() {
        DirectoryNode root = TestTrees.workspace().root();

        assertTrue(search(root, SearchVisitor.nameEquals("missing.txt", 0)).isEmpty());
        assertTrue(search(root, SearchVisitor.nameEndsWith(".txt", 1_000_000)).isEmpty());
    }

    @Test
    void emptyDirectoryGivesAnEmptyList() {
        assertTrue(search(new DirectoryNode("empty"), new SearchVisitor(name -> true, 0)).isEmpty());
    }

    @Test
    void resultBeforeAnyRunIsEmpty() {
        assertTrue(SearchVisitor.nameEquals("x", 0).getMatchingPaths().isEmpty());
    }

    @Test
    void returnedListCannotBeModifiedByTheCaller() {
        SearchVisitor visitor = SearchVisitor.nameEquals("notes.txt", 0);
        TestTrees.workspace().root().accept(visitor);

        assertThrows(UnsupportedOperationException.class, () -> visitor.getMatchingPaths().add("hacked"));
    }

    @Test
    void invalidConfigurationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SearchVisitor(null, 0));
        assertThrows(IllegalArgumentException.class, () -> SearchVisitor.nameEquals("x", -1));
    }
}
