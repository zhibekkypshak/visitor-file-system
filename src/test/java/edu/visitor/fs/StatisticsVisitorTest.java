package edu.visitor.fs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StatisticsVisitorTest {

    private static StatisticsVisitor run(Element root) {
        StatisticsVisitor visitor = new StatisticsVisitor();
        root.accept(visitor);
        return visitor;
    }

    @Test
    void countsFilesDirectoriesAndDepthOfTheWholeTree() {
        StatisticsVisitor stats = run(TestTrees.workspace().root());

        assertEquals(8, stats.getFileCount());
        assertEquals(6, stats.getDirectoryCount());
        assertEquals(3, stats.getMaxDepth());
    }

    @Test
    void depthIsMeasuredFromTheChosenRoot() {
        StatisticsVisitor stats = run(TestTrees.workspace().photos());

        assertEquals(4, stats.getFileCount());
        assertEquals(2, stats.getDirectoryCount());
        assertEquals(2, stats.getMaxDepth());
    }

    @Test
    void emptyRootDirectoryCountsAsOneDirectoryAtDepthZero() {
        StatisticsVisitor stats = run(new DirectoryNode("empty"));

        assertEquals(0, stats.getFileCount());
        assertEquals(1, stats.getDirectoryCount());
        assertEquals(0, stats.getMaxDepth());
    }

    @Test
    void fileAsRootIsOneFileAtDepthZero() {
        StatisticsVisitor stats = run(new FileNode("solo", 3));

        assertEquals(1, stats.getFileCount());
        assertEquals(0, stats.getDirectoryCount());
        assertEquals(0, stats.getMaxDepth());
    }

    @Test
    void maximumDepthIsReachedByAChainOfEmptyDirectories() {
        DirectoryNode root = new DirectoryNode("r");
        root.addDirectory("a").addDirectory("b").addDirectory("c");
        root.addFile("shallow.txt", 1);

        StatisticsVisitor stats = run(root);

        assertEquals(1, stats.getFileCount());
        assertEquals(4, stats.getDirectoryCount());
        assertEquals(3, stats.getMaxDepth());
    }

    @Test
    void filesDirectlyUnderTheRootAreAtDepthOne() {
        DirectoryNode root = new DirectoryNode("r");
        root.addFile("a", 1);
        root.addFile("b", 1);

        assertEquals(1, run(root).getMaxDepth());
    }

    @Test
    void resultsAreZeroBeforeAnyRun() {
        StatisticsVisitor stats = new StatisticsVisitor();

        assertEquals(0, stats.getFileCount());
        assertEquals(0, stats.getDirectoryCount());
        assertEquals(0, stats.getMaxDepth());
    }
}
