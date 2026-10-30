package view;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for View.
 */
public class ViewTest {

    @Test
    public void testGetReleaseDateBuildsExpectedDateString() throws Exception {
        View view = new View("test");

        setComboBoxSelection(view, "yearBox", "2026");
        setComboBoxSelection(view, "monthBox", "04");
        setComboBoxSelection(view, "dayBox", "18");

        assertEquals("2026-04-18", view.getReleaseDate());

        view.dispose();
    }

    private void setComboBoxSelection(View view, String fieldName, String value) throws Exception {
        Field field = View.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        javax.swing.JComboBox<?> comboBox = (javax.swing.JComboBox<?>) field.get(view);
        comboBox.setSelectedItem(value);
    }
}