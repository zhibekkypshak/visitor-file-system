package edu.visitor.fs;

import java.util.Optional;

public abstract class Node implements Element {

    private final String name;
    private DirectoryNode parent;

    Node(String name) {
        this.name = requireValidName(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<DirectoryNode> getParent() {
        return Optional.ofNullable(parent);
    }

    DirectoryNode parentOrNull() {
        return parent;
    }

    void setParent(DirectoryNode newParent) {
        this.parent = newParent;
    }

    private static String requireValidName(String name) {
        if (name == null || name.isBlank()) {
            throw new TreeIntegrityException("Name must not be null or blank");
        }
        if (name.contains("/")) {
            throw new TreeIntegrityException("Name must not contain '/': " + name);
        }
        if (name.equals(".") || name.equals("..")) {
            throw new TreeIntegrityException("Reserved name: " + name);
        }
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
