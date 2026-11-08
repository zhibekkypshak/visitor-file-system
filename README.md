# Visitor Pattern — Advanced File System

In-memory hierarchical file system (files and directories) with three independent operations
implemented with the **Visitor** pattern. Java 17, Maven, JUnit 5.
Course ShP-2216 *Software Design Patterns*, Assignment 06.

## Commands

Run from the project root (the folder containing `pom.xml`):

```bash
mvn test                          # run the automated test suite
mvn -q compile exec:java          # run the deterministic demonstration
```

No IDE and no manual input is needed. Requires JDK 17+ and Maven 3.8+.

## Structure

| Role in the pattern | Type |
|---|---|
| Element interface | `Element` (`getName`, `getParent`, `accept(Visitor)`) |
| Concrete elements | `FileNode` (name + non-negative size), `DirectoryNode` (named children) |
| Shared element state | `Node` (name validation, owning directory) |
| Visitor interface | `Visitor` — `visit(FileNode)` and `visit(DirectoryNode)` |
| Traversal base class | `TraversingVisitor` |
| Concrete visitors | `TotalSizeVisitor`, `SearchVisitor`, `StatisticsVisitor` |
| Integrity errors | `TreeIntegrityException` |

## Design decisions

### Double dispatch (R2)
`FileNode.accept(v)` is `v.visit(this)` and `DirectoryNode.accept(v)` is `v.visit(this)`.
Step 1: the *runtime* class of the element selects which `accept` runs.
Step 2: inside it, `this` has a *static* type (`FileNode` / `DirectoryNode`), so the compiler picks the
matching overload of `visit`. No `instanceof`, `getClass()`, reflection or casts are used anywhere.

### Traversal owner (R7): the visitors
Recursion into children is written **once**, in `TraversingVisitor.visit(DirectoryNode)`; concrete
visitors only say what to do with a file or a directory (`onFile`, `onDirectory`).
Rules: pre-order (directory before its children), children in lexicographic order of name, every
reachable node exactly once per run. Why: `accept` stays a one-liner (so the elements never change
when an operation is added), and no walker has to find out whether a node has children without type checks.

### Paths and depth
* Root of a run has depth 0, its direct children depth 1, etc. `TraversingVisitor` keeps the list of
  names from the root to the current node; depth = list size − 1.
* Search path = those names *without* the root's own name, joined with `/`. If the root itself is a
  file, its own name is returned.
* Results are sorted with `String.compareTo` at the end (pre-order is not the same as lexicographic
  order of full paths: `a-x.txt` < `a/b.txt`).
* Names are case-sensitive; a name cannot be blank, contain `/`, or be `.` / `..`.

### State and reuse
Visitor instances are **reusable**. A *run* begins when `accept` is called while no run of that visitor
is in progress; `TraversingVisitor` then calls `resetState()`, so a getter always describes the latest
run only (zero/empty before the first run). Order of running different visitors is irrelevant.
If a run throws, the internal path is cleaned in `finally`. Visitors are not thread-safe.

### Tree integrity (R6)
`DirectoryNode.add` checks *before* modifying anything: non-null child, child has no parent yet,
child is neither this directory nor one of its ancestors (no cycles), name not used by a sibling.
`FileNode` rejects negative sizes. All violations throw `TreeIntegrityException`; a rejected change
leaves the tree untouched. A node is moved with `remove(name)` followed by `add(...)` elsewhere.
`getChildren()` returns an immutable snapshot; the internal map is never exposed.

### Extension boundary (R8)
New **operation** = one new class extending `TraversingVisitor`. `StatisticsVisitor` was added in a
separate commit without touching `FileNode`, `DirectoryNode`, `Element`, `Visitor` or any `accept`.
New **element type** is the opposite: `Visitor` gets a new `visit` overload and *every* existing visitor
must implement it, and the new element needs its own `accept`. The pattern is cheap while the element
hierarchy is stable and operations change often, and expensive in the reverse case.

## Tests

`mvn test` runs seven JUnit 5 classes: `DoubleDispatchTest` (T1), `TotalSizeVisitorTest` (T2, T6),
`SearchVisitorTest` (T3, T4, T6), `StatisticsVisitorTest` (T5, T6), `VisitorStateTest` (T7),
`TreeIntegrityTest` (T8), `TraversalTest` (R7).