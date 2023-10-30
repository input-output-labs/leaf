package fr.iolabs.leaf.eligibilities;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class LeafEligibility {
    public final boolean eligible;
    public final List<String> reasons;

    public LeafEligibility() {
        this(false);
    }

    public LeafEligibility(boolean eligible) {
        this(eligible, new ArrayList<>());
    }

    public LeafEligibility(boolean eligible, List<String> reasons) {
        this.eligible = eligible;
        this.reasons = reasons;
    }

    public static LeafEligibility eligible() {
        return new LeafEligibility(true);
    }

    public static LeafEligibility notEligible(List<String> reasons) {
        return new LeafEligibility(false, reasons);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.eligible) {
            sb.append("Eligible");
        } else {
            sb.append("Non eligible");
            StringJoiner sj = new StringJoiner(",", "[", "]");
            this.reasons.forEach(sj::add);
        }
        return sb.toString();
    }
}
