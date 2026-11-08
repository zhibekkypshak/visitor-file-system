package edu.visitor.fs;

import java.util.ArrayList;
import java.util.List;

public abstract class TraversingVisitor implements Visitor {

    private final List<String> pathFromRoot = new ArrayList<>();

    @Override
    public final void visit(FileNode file) {
        enter(file);
        try {
            onFile(file);
        } finally {
            leave();
        }
    }

    @Override
    public final void visit(DirectoryNode directory) {
        enter(directory);
        try {
            onDirectory(directory);
            for (Element child : directory.getChildren()) {
                child.accept(this);
            }
        } finally {
            leave();
        }
    }

    protected abstract void resetState();

    protected abstract void onFile(FileNode file);

    protected abstract void onDirectory(DirectoryNode directory);

    protected final int currentDepth() {
        return pathFromRoot.size() - 1;
    }
    protected final String currentRelativePath() {
        if (pathFromRoot.size() == 1) {
            return pathFromRoot.get(0);
        }
        return String.join("/", pathFromRoot.subList(1, pathFromRoot.size()));
    }

    private void enter(Element element) {
        if (pathFromRoot.isEmpty()) {
            resetState();
        }
        pathFromRoot.add(element.getName());
    }

    private void leave() {
        pathFromRoot.remove(pathFromRoot.size() - 1);
    }
}
