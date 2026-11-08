package edu.visitor.fs;

import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

public final class DirectoryNode extends Node {

    private final TreeMap<String, Node> children = new TreeMap<>();

    public DirectoryNode(String name) {
        super(name);
    }

    public void add(Node child) {
        if (child == null) {
            throw new TreeIntegrityException("Child must not be null");
        }
        if (child.parentOrNull() != null) {
            throw new TreeIntegrityException(
                    "'" + child.getName() + "' already has a parent; remove it from there first");
        }
        if (isSelfOrAncestor(child)) {
            throw new TreeIntegrityException(
                    "Adding '" + child.getName() + "' to '" + getName() + "' would create a cycle");
        }
        if (children.containsKey(child.getName())) {
            throw new TreeIntegrityException(
                    "Duplicate sibling name '" + child.getName() + "' in '" + getName() + "'");
        }
        children.put(child.getName(), child);
        child.setParent(this);
    }

    public FileNode addFile(String name, long size) {
        FileNode file = new FileNode(name, size);
        add(file);
        return file;
    }
    public DirectoryNode addDirectory(String name) {
        DirectoryNode directory = new DirectoryNode(name);
        add(directory);
        return directory;
    }

    public Optional<Node> remove(String name) {
        Node removed = children.remove(name);
        if (removed != null) {
            removed.setParent(null);
        }
        return Optional.ofNullable(removed);
    }

    public Optional<Element> getChild(String name) {
        return Optional.ofNullable(children.get(name));
    }

    public List<Element> getChildren() {
        return List.copyOf(children.values());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    private boolean isSelfOrAncestor(Node candidate) {
        for (DirectoryNode current = this; current != null; current = current.parentOrNull()) {
            if (current == candidate) {
                return true;
            }
        }
        return false;
    }
}
