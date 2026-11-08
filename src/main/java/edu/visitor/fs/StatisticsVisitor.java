package edu.visitor.fs;

public final class StatisticsVisitor extends TraversingVisitor {

    private int fileCount;
    private int directoryCount;
    private int maxDepth;

    public int getFileCount() {
        return fileCount;
    }

    public int getDirectoryCount() {
        return directoryCount;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    @Override
    protected void resetState() {
        fileCount = 0;
        directoryCount = 0;
        maxDepth = 0;
    }

    @Override
    protected void onFile(FileNode file) {
        fileCount++;
        recordDepth();
    }

    @Override
    protected void onDirectory(DirectoryNode directory) {
        directoryCount++;
        recordDepth();
    }

    private void recordDepth() {
        maxDepth = Math.max(maxDepth, currentDepth());
    }
}