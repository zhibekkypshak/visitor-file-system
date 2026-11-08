package edu.visitor.fs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TotalSizeVisitorTest {

    @Test
    void sumsEveryFileAcrossAllNestingLevels() {
        TestTrees.Workspace ws = TestTrees.workspace();
        TotalSizeVisitor visitor = new TotalSizeVisitor();

        ws.root().accept(visitor);

        assertEquals(22_670, visitor.getTotalSize());
    }

    @Test
    void measuresOnlyTheChosenSubtree() {
        TestTrees.Workspace ws = TestTrees.workspace();
        TotalSizeVisitor visitor = new TotalSizeVisitor();

        ws.photos().accept(visitor);

        assertEquals(2_500 + 4_000 + 6_000 + 120, visitor.getTotalSize());
    }

    @Test
    void directoriesContributeNothingAndEmptyDirectoryIsZero() {
        DirectoryNode root = new DirectoryNode("root");
        root.addDirectory("empty-1").addDirectory("empty-2");
        TotalSizeVisitor visitor = new TotalSizeVisitor();

        root.accept(visitor);

        assertEquals(0, visitor.getTotalSize());
    }

    @Test
    void zeroByteFilesAreAllowedAndDoNotChangeTheSum() {
        DirectoryNode root = new DirectoryNode("root");
        root.addFile("empty.bin", 0);
        root.addFile("data.bin", 42);
        TotalSizeVisitor visitor = new TotalSizeVisitor();

        root.accept(visitor);

        assertEquals(42, visitor.getTotalSize());
    }

    @Test
    void aSingleFileAsRootReportsItsOwnSize() {
        TotalSizeVisitor visitor = new TotalSizeVisitor();

        new FileNode("solo.dat", 777).accept(visitor);

        assertEquals(777, visitor.getTotalSize());
    }

    @Test
    void resultIsZeroBeforeAnyRun() {
        assertEquals(0, new TotalSizeVisitor().getTotalSize());
    }

    @Test
    void sizesBeyondIntRangeAreSummedWithoutOverflow() {
        DirectoryNode root = new DirectoryNode("root");
        root.addFile("a", 3_000_000_000L);
        root.addFile("b", 3_000_000_000L);
        TotalSizeVisitor visitor = new TotalSizeVisitor();

        root.accept(visitor);

        assertEquals(6_000_000_000L, visitor.getTotalSize());
    }
}