package edu.visitor.fs;

public final class TotalSizeVisitor extends TraversingVisitor {

    private long totalSize;

    public long getTotalSize() {
        return totalSize;
    }

    @Override
    protected void resetState() {
        totalSize = 0;
    }

    @Override
    protected void onFile(FileNode file) {
        totalSize = Math.addExact(totalSize, file.getSize());
    }

    @Override
    protected void onDirectory(DirectoryNode directory) {
    }
}