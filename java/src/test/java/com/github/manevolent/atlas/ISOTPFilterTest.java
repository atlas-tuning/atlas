package com.github.manevolent.atlas;

import com.github.manevolent.atlas.j2534.J2534Device;
import com.github.manevolent.atlas.subaru.SubaruDITComponent;
import org.junit.jupiter.api.Test;

import static com.github.manevolent.atlas.subaru.SubaruProtocols.DIT_FILTER;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ISOTPFilterTest {

    @Test
    public void testSubaru_Patterns() {
        for (SubaruDITComponent component : SubaruDITComponent.values()) {
            if (component.getReplyAddress() == null) continue;
            assertTrue(DIT_FILTER.testPattern(component.getReplyAddress()),
                    component.getReplyAddress() + " filters correctly");
        }
    }

    @Test
    public void testSubaru_Masks() {
        for (SubaruDITComponent component : SubaruDITComponent.values()) {
            if (component.getSendAddress() == null) continue;
            if (component == SubaruDITComponent.BROADCAST) continue;

            assertTrue(DIT_FILTER.testFlow(component.getSendAddress()),
                    component.getSendAddress() + " filters correctly");
        }
    }

}
