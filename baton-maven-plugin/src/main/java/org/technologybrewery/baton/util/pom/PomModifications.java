package org.technologybrewery.baton.util.pom;

import org.technologybrewery.baton.util.FileUtils;
import org.apache.maven.model.InputLocation;
import org.codehaus.plexus.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Tracks and applies modifications to a Maven POM file "in-place" to preserve formatting.
 */
public final class PomModifications extends TreeSet<PomModifications.Modification> {

    /**
     * Returns an instance of the Final class configure with an iterable of all loaded POM file modifications.
     *
     * @see org.technologybrewery.baton.util.pom.PomModifications.Final
     */
    public Final finalizeMods() {
        return new Final(iterator());
    }

    /**
     * A class to iteratively apply a series of POM file modifications.
     *
     * @see org.technologybrewery.baton.util.pom.PomModifications.Modification
     */
    public static class Final {
        private Iterator<Modification> iterator;
        private Modification next;

        /**
         * Constructor that initializes a series of POM file modifications passed in as an Iterator of the
         * internally defined Modification class.
         *
         * @param iterator A given Iterator of the type Modification.
         * @see org.technologybrewery.baton.util.pom.PomModifications.Modification
         */
        private Final(Iterator<Modification> iterator) {
            this.iterator = iterator;
            next = iterator.next();
        }


        public boolean appliesTo(int lineNumber) {
            return next != null && next.getStart().getLineNumber() == lineNumber;
        }

        /**
         * Applies the next modification to the input reader, writing the result to the output writer.
         *
         * @param in the input reader.
         * @param out the output writer.
         * @param line the current line of the input reader.
         * @return the line number of `in` after the modification has been applied (changes iff the input reader was advanced).
         * @throws IOException if an I/O error occurs.
         */
        public int apply(BufferedReader in, Writer out, String line) throws IOException {
            int newInputLine = next.apply(in, out, line);
            if (iterator.hasNext()) {
                next = iterator.next();
            } else {
                next = null;
            }
            return newInputLine;
        }
    }

    /**
     * The abstract class by which all POM file modifications inherit from.
     */
    public abstract static class Modification implements Comparable<Modification> {
        private final InputLocation start;

        /**
         * The constructor accepts an InputLocation type representing the line and column of the file
         * where modifications will start being applied.
         *
         * @param start the location to begin modifying the content.
         * @see org.apache.maven.model.InputLocation
         */
        protected Modification(InputLocation start) {
            this.start = start;
        }

        public InputLocation getStart() {
            return start;
        }
        
        public abstract int apply(BufferedReader in, Writer out, String currentLine) throws IOException;

        @Override
        public int compareTo(Modification o) {
            return Comparator.comparingInt(InputLocation::getLineNumber)
                    .thenComparingInt(InputLocation::getColumnNumber)
                    .compare(this.getStart(), o.getStart());
        }

        @Override
        public boolean equals(Object obj) {
            boolean match = false;
            if (obj instanceof Modification) {
                match = this.compareTo((Modification) obj) == 0;
            }
            return match;
        }
    }

    /**
     * Advances the input reader to the line at the specified end location, writing only the content that is not between
     * the start location and the end location to the output writer. I.e., any content on the first line before the
     * start of the location, and any content on the last line after the end of the location, is written to the output
     * writer.
     */
    public static class Deletion extends Modification {
        private final InputLocation end;

        /**
         * Constructor to delete content between the given start and end parameters.
         *
         * @param start the location to begin deleting POM file content (inclusive).
         * @param end the location to stop deleting POM file content (exclusive).
         * @see org.apache.maven.model.InputLocation
         */
        public Deletion(InputLocation start, InputLocation end) {
            super(start);
            this.end = end;
        }

        public InputLocation getEnd() {
            return end;
        }

        @Override
        public int apply(BufferedReader in, Writer out, String line) throws IOException {
            int current = getStart().getLineNumber();
            // NB: clashes with other modifications on same line
            int startColumn = getStart().getColumnNumber();
            String substring = startColumn == 0 ? "" : line.substring(0, startColumn - 1);
            if (StringUtils.isNotBlank(substring)) {
                out.write(substring);
                out.write("\n");
            }
            while (current < getEnd().getLineNumber()) {
                line = in.readLine();
                current++;
            }
            if( getEnd().getColumnNumber() <= line.length() ) {
                out.write(line.substring(getEnd().getColumnNumber()-1));
                out.write("\n");
            }
            return current;
        }

        @Override
        public boolean equals(Object obj) {
            boolean match = false;
            if (obj instanceof Deletion) {
                match = this.compareTo((Deletion) obj) == 0;
            }
            return match;
        }
    }

    /**
     * Inserts the produced content at the specified start line and before the existing content on that line.  Does NOT
     * support adding content to the middle of a line.
     */
    public static class Insertion extends Modification {
        private final Function<String,String> contentProducer;
        private final int currentIndent;

        /**
         * Constructor to insert content before the given start existing content on the given start index.
         *
         * @param start the location to insert the content (before the existing content on that line).
         * @param currentIndent the indent level of the current content on the line.
         * @param contentProducer a function that produces the content to insert, given a one-level indent string.
         * @see org.apache.maven.model.InputLocation
         */
        public Insertion(InputLocation start, int currentIndent, UnaryOperator<String> contentProducer) {
            super(start);
            this.contentProducer = contentProducer;
            this.currentIndent = currentIndent;
        }

        @Override
        public int apply(BufferedReader in, Writer out, String line) throws IOException {
            String indent = FileUtils.getIndent(line, currentIndent);
            out.write(contentProducer.apply(indent));
            out.write(line);
            out.write("\n");
            return getStart().getLineNumber();
        }

        @Override
        public boolean equals(Object obj) {
            boolean match = false;
            if (obj instanceof Insertion) {
                match = this.compareTo((Insertion) obj) == 0;
            }
            return match;
        }
    }

    /**
     * Replaces the content between the start and end locations with the produced content.
     */
    public static class Replacement extends Modification {
        private final InputLocation end;
        private final Function<String,String> contentProducer;
        private final int indentLvl;

        /**
         * Constructor for replacing content within a single line.
         *
         * @param start the location to insert the new content.
         * @param end the location to skip to, existing content between start and end will be deleted.
         * @param content the new content.
         */
        public Replacement(InputLocation start, InputLocation end, String content) {
            this(start, end, 0, l -> content);
        }

        /**
         * Constructor for multi-line replacements.
         *
         * @param start the location to insert the new content.
         * @param end the location to skip to, existing content between start and end will be deleted.
         * @param indentLvl the indent level of the current content on the line.
         * @param contentProducer a function that produces the content to insert, given a one-level indent string.
         */
        public Replacement(InputLocation start, InputLocation end, int indentLvl, UnaryOperator<String> contentProducer) {
            super(start);
            this.end = end;
            this.contentProducer = contentProducer;
            this.indentLvl = indentLvl;
        }

        public InputLocation getEnd() {
            return end;
        }

        public Function<String, String> getContentProducer() {
            return contentProducer;
        }

        public int getIndentLvl() {
            return indentLvl;
        }

        @Override
        public int apply(BufferedReader in, Writer out, String line) throws IOException {
            int current = getStart().getLineNumber();
            // NB: clashes with other modifications on same line
            String substring = line.substring(0, getStart().getColumnNumber() - 1);
            if (StringUtils.isNotBlank(substring)) {
                out.write(substring);
            }
            String indent = FileUtils.getIndent(line, getIndentLvl());
            out.write(getContentProducer().apply(indent));
            while (current < getEnd().getLineNumber()) {
                line = in.readLine();
                current++;
            }
            if( getEnd().getColumnNumber() <= line.length() ) {
                out.write(line.substring(getEnd().getColumnNumber()-1));
                out.write("\n");
            }
            return current;
        }

        @Override
        public boolean equals(Object obj) {
            boolean match = false;
            if (obj instanceof Replacement) {
                match = this.compareTo((Replacement) obj) == 0;
            }
            return match;
        }

    }
}
