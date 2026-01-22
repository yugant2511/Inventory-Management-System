package util;

import java.util.List;

public class InventoryUtils {

    public static <T> void displayList(List<T> list) {
        if (list.isEmpty()) {
            System.out.println("No records found!");
            return;
        }
        for (T item : list) {
            System.out.println(item);
        }
    }
}
