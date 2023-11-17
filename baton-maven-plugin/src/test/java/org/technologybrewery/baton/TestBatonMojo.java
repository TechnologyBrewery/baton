package org.technologybrewery.baton;

import java.util.Set;

public class TestBatonMojo extends BatonMojo {

    public void setDeactivateMigrations(Set<String> deactivateMigrations) {
        this.deactivateMigrations = deactivateMigrations;
    }

}
