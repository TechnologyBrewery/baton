package org.technologybrewery.baton.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Json repesentation of the files to proceess with Baton.
 */
public class FileSet {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String directory;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> includes;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> excludes;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean followSymlinks;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public List<String> getIncludes() {
        return includes;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    @JsonIgnore
    public void addInclude(String include) {
        if (this.includes == null) {
            this.includes = new ArrayList<>();
        }

        this.includes.add(include);
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    @JsonIgnore
    public void addExclude(String exclude) {
        if (this.excludes == null) {
            this.excludes = new ArrayList<>();
        }

        this.excludes.add(exclude);
    }

    public Boolean getFollowSymlinks() {
        return followSymlinks;
    }

    public void setFollowSymlinks(Boolean followSymlinks) {
        this.followSymlinks = followSymlinks;
    }
}
