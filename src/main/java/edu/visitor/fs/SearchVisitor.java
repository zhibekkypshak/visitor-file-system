package edu.visitor.fs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public final class SearchVisitor extends TraversingVisitor {

    private final Predicate<String> nameCriterion;
    private final long minSize;
    private final List<String> matchingPaths = new ArrayList<>();

    public SearchVisitor(Predicate<String> nameCriterion, long minSize) {
        if (nameCriterion == null) {
            throw new IllegalArgumentException("Name criterion must not be null");
        }
        if (minSize < 0) {
            throw new IllegalArgumentException("Minimum size must not be negative: " + minSize);
        }
        this.nameCriterion = nameCriterion;
        this.minSize = minSize;
    }

    public static SearchVisitor nameEquals(String name, long minSize) {
        return new SearchVisitor(name::equals, minSize);
    }

    public static SearchVisitor nameContains(String fragment, long minSize) {
        return new SearchVisitor(name -> name.contains(fragment), minSize);
    }

    public static SearchVisitor nameEndsWith(String suffix, long minSize) {
        return new SearchVisitor(name -> name.endsWith(suffix), minSize);
    }

    public List<String> getMatchingPaths() {
        List<String> sorted = new ArrayList<>(matchingPaths);
        Collections.sort(sorted);
        return Collections.unmodifiableList(sorted);
    }

    @Override
    protected void resetState() {
        matchingPaths.clear();
    }

    @Override
    protected void onFile(FileNode file) {
        if (nameCriterion.test(file.getName()) && file.getSize() >= minSize) {
            matchingPaths.add(currentRelativePath());
        }
    }

    @Override
    protected void onDirectory(DirectoryNode directory) {
    }
}
