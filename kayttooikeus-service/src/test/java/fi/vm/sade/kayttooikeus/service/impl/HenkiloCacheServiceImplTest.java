package fi.vm.sade.kayttooikeus.service.impl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HenkiloCacheServiceImplTest {

    private String desc;
    private String input;
    private String expected;

    public void initHenkiloCacheServiceImplTest(String desc, String input, String expected) {
        this.desc = desc;
        this.input = input;
        this.expected = expected;
    }

    public static Collection<String[]> parameters() {
        return Arrays.asList(
                new String[]{"Handles null:s", null, null},
                new String[]{"Handles empty strings", "", ""},
                new String[]{"Trims prefix", " foo", "foo"},
                new String[]{"Trims suffix", "foo ", "foo"},
                new String[]{"Trims both", " foo ", "foo"}
        );
    }

    @MethodSource("parameters")
    @ParameterizedTest(name = "{0}")
    public void trim(String desc, String input, String expected) {
        initHenkiloCacheServiceImplTest(desc, input, expected);
        assertEquals(expected, HenkiloCacheServiceImpl.trim(input), desc);
    }
}
