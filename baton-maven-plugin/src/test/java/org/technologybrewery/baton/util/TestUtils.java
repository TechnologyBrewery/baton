
package org.technologybrewery.baton.util;

import io.cucumber.java.ParameterType;

public class TestUtils {
    @ParameterType( value = "true|True|TRUE|false|False|FALSE")
    public Boolean booleanValue(String value){
        return Boolean.valueOf(value);
    }
}
