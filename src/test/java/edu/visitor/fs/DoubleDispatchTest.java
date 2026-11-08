package edu.visitor.fs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DoubleDispatchTest {

    private static final class RecordingVisitor implements Visitor {
        final List<String> calls = new ArrayList<>();

        @Override
        public void visit(FileNode file) {
            calls.add("visit(FileNode):" + file.getName());
        }

        @Override
        public void visit(DirectoryNode directory) {
            calls.add("visit(DirectoryNode):" + directory.getName());
        }
    }

    @Test
    void fileAcceptsCallsFileOverloadWithItself() {
        RecordingVisitor visitor = new RecordingVisitor();

        new FileNode("a.txt", 1).accept(visitor);

        assertEquals(List.of("visit(FileNode):a.txt"), visitor.calls);
    }

    @Test
    void directoryAcceptCallsDirectoryOverloadWithItself() {
        RecordingVisitor visitor = new RecordingVisitor();

        new DirectoryNode("docs").accept(visitor);

        assertEquals(List.of("visit(DirectoryNode):docs"), visitor.calls);
    }

    @Test
    void overloadIsChosenByRuntimeTypeWhenReferencedThroughTheElementInterface() {
        RecordingVisitor visitor = new RecordingVisitor();
        List<Element> elements = List.of(new DirectoryNode("d"), new FileNode("f", 0), new DirectoryNode("e"));

        for (Element element : elements) {
            element.accept(visitor);
        }

        assertEquals(
                List.of("visit(DirectoryNode):d", "visit(FileNode):f", "visit(DirectoryNode):e"),
                visitor.calls);
    }

    @Test
    void acceptOnDirectoryDoesNotTraverseByItself() {
        RecordingVisitor visitor = new RecordingVisitor();
        DirectoryNode dir = new DirectoryNode("d");
        dir.addFile("inner.txt", 5);

        dir.accept(visitor);

        assertEquals(List.of("visit(DirectoryNode):d"), visitor.calls);
    }
}
