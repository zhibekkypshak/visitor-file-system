package edu.visitor.fs;

final class TestTrees {

    private TestTrees() {
    }

    record Workspace(DirectoryNode root, DirectoryNode photos) {
    }
    static Workspace workspace() {
        DirectoryNode root = new DirectoryNode("workspace");
        root.addFile("notes.txt", 300);
        root.addFile("todo.txt", 50);

        DirectoryNode archive = root.addDirectory("archive");
        DirectoryNode year = archive.addDirectory("2024");
        year.addFile("report.pdf", 9_000);
        year.addFile("notes.txt", 700);

        root.addDirectory("music");

        DirectoryNode photos = root.addDirectory("photos");
        photos.addFile("portrait.jpg", 2_500);
        DirectoryNode holiday = photos.addDirectory("holiday");
        holiday.addFile("beach.jpg", 4_000);
        holiday.addFile("beach-edit.jpg", 6_000);
        holiday.addFile("notes.txt", 120);
        return new Workspace(root, photos);
    }
}
