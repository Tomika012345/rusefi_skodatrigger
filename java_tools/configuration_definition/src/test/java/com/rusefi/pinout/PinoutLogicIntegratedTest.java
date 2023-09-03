package com.rusefi.pinout;

import com.rusefi.ReaderStateImpl;
import com.rusefi.enum_reader.Value;
import com.rusefi.newparse.DefinitionsState;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PinoutLogicIntegratedTest {

    private static final List<String> META_CONTENT = Arrays.asList("#define H144_LS_1 G7\n",
            "#define H144_DIGITAL\tE11\n",
            "\t\t\t\t#define\t\t\tH144_LS_2 G8\t\t\t\n",
            "// IN_O2S AIN13 A13 PA0\n",
            "#define H144_IN_O2S EFI_ADC_0\n",
            "// IN_O2S2 AIN12 PA1\n",
            "#define H144_IN_O2S2 EFI_ADC_1");

    @Test
    public void testMetaParsing() {
        Map</*meta name*/String, /*native name*/String> map = PinoutLogic.getStringStringMap(META_CONTENT);
        assertEquals(5, map.size());

        assertEquals("G8", map.get("H144_LS_2"));
        assertEquals("EFI_ADC_0", map.get("H144_IN_O2S"));
    }

    @Test
    public void testWholeThing() throws IOException {
        runPinoutTest("pins:\n" +
                "  - pin: 1\n" +
                "    id: [E11, E11]\n" +
                "    class: [event_inputs, switch_inputs]\n" +
                "    function: Digital trigger/switch input for instance Hall type CAM\n" +
                "    ts_name: Digital 2\n" +
                "    type: din",
                "//DO NOT EDIT MANUALLY, let automation work hard.\n" +
                        "\n" +
                        "// auto-generated by PinoutLogic.java based on key\n" +
                        "#include \"pch.h\"\n" +
                        "\n" +
                        "// see comments at declaration in pin_repository.h\n" +
                        "const char * getBoardSpecificPinName(brain_pin_e brainPin) {\n" +
                        "\tswitch(brainPin) {\n" +
                        "\t\tcase Gpio::E11: return \"Digital 2\";\n" +
                        "\t\tdefault: return nullptr;\n" +
                        "\t}\n" +
                        "\treturn nullptr;\n" +
                        "}\n");

    }

    @Test
    public void testTemplate() throws IOException {
        runPinoutTest("meta: meta.h\n" +
                        "pins:\n" +
                        "  - pin: 2\n" +
                        "    meta: H144_LS_2\n" +
                        "    class: outputs\n" +
                        "    function: Digital trigger/switch input for instance Hall type CAM\n" +
                        "    ts_name: ___ - Digital 1\n" +
                        "  - pin: 1\n" +
                        "    id: [H144_DIGITAL, H144_DIGITAL]\n" +
                        "    class: [event_inputs, switch_inputs]\n" +
                        "    function: Digital trigger/switch input for instance Hall type CAM\n" +
                        "    ts_name: ___ - Digital 2\n" +
                        "    type: din",
                "//DO NOT EDIT MANUALLY, let automation work hard.\n" +
                        "\n" +
                        "// auto-generated by PinoutLogic.java based on key\n" +
                        "#include \"pch.h\"\n" +
                        "\n" +
                        "// see comments at declaration in pin_repository.h\n" +
                        "const char * getBoardSpecificPinName(brain_pin_e brainPin) {\n" +
                        "\tswitch(brainPin) {\n" +
                        "\t\tcase Gpio::E11: return \"1 - Digital 2\";\n" +
                        "\t\tcase Gpio::G8: return \"2 - Digital 1\";\n" +
                        "\t\tdefault: return nullptr;\n" +
                        "\t}\n" +
                        "\treturn nullptr;\n" +
                        "}\n");

    }

    private static void runPinoutTest(String inputYaml, String expected) throws IOException {
        StringWriter testWriter = new StringWriter();

        Reader input = new StringReader(inputYaml);

        BoardInputs testBoard = new BoardInputs() {
            @Override
            public List<? extends File> getBoardYamlKeys() {
                return Collections.singletonList(new File("key"));
            }

            @Override
            public Reader getReader(File yamlKey) {
                return input;
            }

            @Override
            public String getName() {
                return "test";
            }

            @Override
            public List<String> getInputFiles() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Writer getWriter() {
                return testWriter;
            }

            @Override
            public List<String> getBoardMeta(String boardMetaFileName) {
                return META_CONTENT;
            }
        };

        ReaderStateImpl state = new ReaderStateImpl();

        state.getEnumsReader().read(new StringReader("enum class Gpio : uint16_t {\n" +
                                                        "Unassigned = 0,\n" +
                                                        "Invalid = 0x01,\n" +
                                                        "G8 = 5,\n" +
                                                        "E11 = 0x0B,\n" +
                                                        "};"));

        DefinitionsState definitionState = state.getEnumsReader().parseState;

        PinoutLogic logic = new PinoutLogic(testBoard);

        logic.registerBoardSpecificPinNames(state.getVariableRegistry(), definitionState, state.getEnumsReader());

        assertEquals(expected, testWriter.getBuffer().toString());
    }

    @Test
    public void parseInt() {
        assertEquals(1, Value.parseInt("1"));
        assertEquals(10, Value.parseInt("0x0a"));
        assertEquals(10, Value.parseInt("0xa"));
        assertEquals(10, Value.parseInt("0Xa"));
        assertEquals(11, Value.parseInt("0x0B"));
    }

}
