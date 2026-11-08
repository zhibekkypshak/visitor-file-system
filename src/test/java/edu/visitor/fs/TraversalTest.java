package edu.visitor.fs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TraversalTest {

    private static final class LoggingVisitor extends TraversingVisitor {
        final List<String> log = new ArrayList<>();

        @Override
        protected void resetState() {
            log.clear();
        }

        @Override
        protected void onFile(FileNode file) {
            log.add("F:" + currentRelativePath() + "@" + currentDepth());
        }

        @Override
        protected void onDirectory(DirectoryNode directory) {
            log.add("D:" + directory.getName() + "@" + currentDepth());
        }
    }

    @Test
    void everyNodeIsVisitedExactlyOnceInPreOrderWithChildrenSortedByName() {
        DirectoryNode root = TestTrees.workspace().root();
        LoggingVisitor visitor = new LoggingVisitor();

        root.accept(visitor);

        assertEquals(
                List.of(
                        "D:workspace@0",
                        "D:archive@1",
                        "D:2024@2",
                        "F:archive/2024/notes.txt@3",
                        "F:archive/2024/report.pdf@3",
                        "D:music@1",
                        "F:notes.txt@1",
                        "D:photos@1",
                        "D:holiday@2",
                        "F:photos/holiday/beach-edit.jpg@3",
                        "F:photos/holiday/beach.jpg@3",
                        "F:photos/holiday/notes.txt@3",
                        "F:photos/portrait.jpg@2",
                        "F:todo.txt@1"),
                visitor.log);
    }

    @Test
    void emptyDirectoryIsVisitedOnce() {
        LoggingVisitor visitor = new LoggingVisitor();

        new DirectoryNode("empty").accept(visitor);

        assertEquals(List.of("D:empty@0"), visitor.log);
    }

    @Test
    void nestedEmptyDirectoriesAreAllVisited() {
        DirectoryNode root = new DirectoryNode("r");
        root.addDirectory("a").addDirectory("b").addDirectory("c");
        LoggingVisitor visitor = new LoggingVisitor();

        root.accept(visitor);

        assertEquals(List.of("D:r@0", "D:a@1", "D:b@2", "D:c@3"), visitor.log);
    }

    @Test
    void runOnASubDirectoryStartsAtDepthZeroAndTreatsItAsTheRoot() {
        TestTrees.Workspace ws = TestTrees.workspace();
        LoggingVisitor visitor = new LoggingVisitor();

        ws.photos().accept(visitor);

        assertEquals(
                List.of(
                        "D:photos@0",
                        "D:holiday@1",
                        "F:holiday/beach-edit.jpg@2",
                        "F:holiday/beach.jpg@2",
                        "F:holiday/notes.txt@2",
                        "F:portrait.jpg@1"),
                visitor.log);
    }
}
