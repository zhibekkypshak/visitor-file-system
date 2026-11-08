package edu.visitor.fs;

import java.util.Optional;
public interface Element {

    String getName();

    Optional<DirectoryNode> getParent();

    void accept(Visitor visitor);
}
