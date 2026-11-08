package edu.visitor.fs;

import java.util.List;

public final class Main {

    private record Studio(DirectoryNode root, DirectoryNode docs, DirectoryNode api, DirectoryNode src) {
    }

    private Main() {
    }

    public static void main(String[] args) {
        Studio studio = buildStudio();
        DirectoryNode root = studio.root();

        System.out.println("== Total size ==");
        printTotalSize("studio/     ", root);
        printTotalSize("studio/docs/", studio.docs());

        System.out.println();
        System.out.println("== Search (name criterion AND size >= threshold) ==");
        printSearch(root, "*.md  and size >= 3000", SearchVisitor.nameEndsWith(".md", 3_000));
        printSearch(root, "*.svg and size >= 0   ", SearchVisitor.nameEndsWith(".svg", 0));
        printSearch(root, "*.svg and size >= 800 ", SearchVisitor.nameEndsWith(".svg", 800));
        printSearch(root, "*.exe and size >= 0   ", SearchVisitor.nameEndsWith(".exe", 0));
        printSearch(studio.docs(), "(root = docs/) *.md >= 0", SearchVisitor.nameEndsWith(".md", 0));

        System.out.println();
        System.out.println("== Statistics ==");
        printStatistics("studio/     ", root);
        printStatistics("studio/docs/", studio.docs());

        System.out.println();
        System.out.println("== Reuse: the same visitor instance, run twice ==");
        TotalSizeVisitor reused = new TotalSizeVisitor();
        root.accept(reused);
        root.accept(reused);
        System.out.println("after 2 runs: " + reused.getTotalSize() + " bytes (not doubled)");

        System.out.println();
        System.out.println("== Rejected tree changes ==");
        FileNode ownedFile = new DirectoryNode("elsewhere").addFile("owned.txt", 1);
        tryAndReport("negative size ", () -> new FileNode("bad.bin", -5));
        tryAndReport("duplicate name", () -> root.addFile("readme.md", 1));
        tryAndReport("second parent ", () -> studio.src().add(ownedFile));
        tryAndReport("cycle         ", () -> studio.api().add(root));
    }

    private static Studio buildStudio() {
        DirectoryNode root = new DirectoryNode("studio");
        root.addFile("readme.md", 1_200);

        DirectoryNode assets = root.addDirectory("assets");
        assets.addFile("logo.png", 15_000);
        DirectoryNode icons = assets.addDirectory("icons");
        icons.addFile("play.svg", 800);
        icons.addFile("pause.svg", 750);
        assets.addDirectory("fonts");

        DirectoryNode docs = root.addDirectory("docs");
        docs.addFile("guide.md", 5_400);
        DirectoryNode api = docs.addDirectory("api");
        api.addFile("overview.md", 3_100);
        api.addFile("changelog.md", 9_900);

        DirectoryNode src = root.addDirectory("src");
        src.addFile("app.js", 12_300);
        src.addFile("util.js", 2_200);
        return new Studio(root, docs, api, src);
    }

    private static void printTotalSize(String label, DirectoryNode root) {
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        root.accept(visitor);
        System.out.println(label + " : " + visitor.getTotalSize() + " bytes");
    }

    private static void printSearch(DirectoryNode root, String label, SearchVisitor visitor) {
        root.accept(visitor);
        List<String> paths = visitor.getMatchingPaths();
        System.out.println(label + " -> " + (paths.isEmpty() ? "(no matches)" : paths));
    }

    private static void printStatistics(String label, DirectoryNode root) {
        StatisticsVisitor stats = new StatisticsVisitor();
        root.accept(stats);
        System.out.println(label + " files=" + stats.getFileCount()
                + ", directories=" + stats.getDirectoryCount()
                + ", maxDepth=" + stats.getMaxDepth());
    }

    private static void tryAndReport(String what, Runnable change) {
        try {
            change.run();
            System.out.println(what + ": accepted (unexpected)");
        } catch (TreeIntegrityException e) {
            System.out.println(what + ": REJECTED - " + e.getMessage());
        }
    }
}
