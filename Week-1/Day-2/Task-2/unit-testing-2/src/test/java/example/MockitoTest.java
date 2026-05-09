package example;

import org.junit.jupiter.api.Test;
// import org.mockito.ArgumentMatchers;
import static org.mockito.Mockito.*;

import java.util.Map;

public class MockitoTest {

    @Test
    void test() {
        Map<String, Object> map = mock(Map.class);

        // map.put("k", "v");
        //
        // when(map.get("k"))
        // .thenReturn("ABC")
        // .thenReturn("XYZ");
        //
        // System.out.println(map.get("k"));
        // System.out.println(map.get("k"));

        final int counter = 0;
        when(map.get(any()))
                .thenAnswer(iom -> {
                    String key = iom.getArgument(0, String.class);
                    return "VALUE_OF_" + (counter);
                });

        System.out.println(map.get("A"));
        System.out.println(map.get("B"));
        System.out.println(map.get("C"));

        // Mockito.verify(map).put(ArgumentMatchers.eq("k"), ArgumentMatchers.any());

        // verify(map, times(2)).put(eq("k"), any());
        // verifyNoInteractions(map);

    }

}
