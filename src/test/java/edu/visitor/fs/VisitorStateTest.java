package edu.visitor.fs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/** T7: repeated executions neither leak nor multiply accumulated state; no dependence on run order. */
class VisitorStateTest {

    @Test
    void totalSizeVisitorGivesTheSameResultWhenRunTwiceOnTheSameRoot() {
        DirectoryNode root = TestTrees.workspace().root();
        TotalSizeVisitor visitor = new TotalSizeVisitor();

        root.accept(visitor);
        root.accept(visitor);

        assertEquals(22_670, visitor.getTotalSize()); // not 45 340
    }

    @Test
    void searchVisitorDoesNotKeepMatchesOfThePreviousRun() {
        TestTrees.Workspace ws = TestTrees.workspace();
        SearchVisitor visitor = SearchVisitor.nameEquals("notes.txt", 0);

        ws.root().accept(visitor);
        assertEquals(3, visitor.getMatchingPaths().size());

        ws.photos().accept(visitor);
        assertEquals(List.of("holiday/notes.txt"), visitor.getMatchingPaths());

        ws.root().accept(visitor);
        assertEquals(3, visitor.getMatchingPaths().size());
    }

    @Test
    void statisticsVisitorResetsAllThreeCountersBetweenRuns() {
        TestTrees.Workspace ws = TestTrees.workspace();
        StatisticsVisitor visitor = new StatisticsVisitor();

        ws.root().accept(visitor);
        new DirectoryNode("tiny").accept(visitor);

        assertEquals(0, visitor.getFileCount());
        assertEquals(1, visitor.getDirectoryCount());
        assertEquals(0, visitor.getMaxDepth());
    }

    @Test
    void resultsReflectChangesMadeToTheTreeBetweenRuns() {
        DirectoryNode root = new DirectoryNode("root");
        root.addFile("a", 10);
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        root.accept(visitor);
        assertEquals(10, visitor.getTotalSize());

        root.addFile("b", 5);
        root.accept(visitor);

        assertEquals(15, visitor.getTotalSize());
    }

    @Test
    void resultsDoNotDependOnTheOrderInWhichVisitorsAreRun() {
        DirectoryNode root = TestTrees.workspace().root();

        TotalSizeVisitor size1 = new TotalSizeVisitor();
        SearchVisitor search1 = SearchVisitor.nameEndsWith(".jpg", 0);
        StatisticsVisitor stats1 = new StatisticsVisitor();
        root.accept(size1);
        root.accept(search1);
        root.accept(stats1);

        TotalSizeVisitor size2 = new TotalSizeVisitor();
        SearchVisitor search2 = SearchVisitor.nameEndsWith(".jpg", 0);
        StatisticsVisitor stats2 = new StatisticsVisitor();
        root.accept(stats2);
        root.accept(search2);
        root.accept(size2);

        assertEquals(size1.getTotalSize(), size2.getTotalSize());
        assertEquals(search1.getMatchingPaths(), search2.getMatchingPaths());
        assertEquals(stats1.getFileCount(), stats2.getFileCount());
        assertEquals(stats1.getDirectoryCount(), stats2.getDirectoryCount());
        assertEquals(stats1.getMaxDepth(), stats2.getMaxDepth());
    }

    @Test
    void visitorIsUsableAgainAfterARunFailedHalfWay() {
        class FailingOnce extends TraversingVisitor {
            boolean failed;
            long sum;

            @Override
            protected void resetState() {
                sum = 0;
            }

            @Override
            protected void onFile(FileNode file) {
                if (!failed) {
                    failed = true;
                    throw new IllegalStateException("boom");
                }
                sum += file.getSize();
            }

            @Override
            protected void onDirectory(DirectoryNode directory) {
            }
        }

        DirectoryNode root = TestTrees.workspace().root();
        FailingOnce visitor = new FailingOnce();

        assertThrows(IllegalStateException.class, () -> root.accept(visitor));
        root.accept(visitor);

        assertEquals(22_670, visitor.sum);
    }
}