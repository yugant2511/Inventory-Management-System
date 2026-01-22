package comparators;

import java.util.Comparator;
import model.Product;

public class NameComparator implements Comparator<Product> {

    public NameComparator() {
    }

    @Override
    public int compare(Product p1, Product p2) {
        return p1.getName().compareToIgnoreCase(p2.getName());
    }
}
