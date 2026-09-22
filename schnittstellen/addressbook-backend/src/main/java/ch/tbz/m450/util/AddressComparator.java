package ch.tbz.m450.util;

import ch.tbz.m450.repository.Address;

import java.util.Comparator;
import java.util.List;

public class AddressComparator implements Comparator<Address> {

    public enum SortField {
        LASTNAME,
        FIRSTNAME,
        PHONENUMBER,
        REGISTRATION_DATE
    }

    private final List<SortField> sortFields;

    /**
     * Standard-Sortierung: nach Nachname, bei Gleichstand nach Vorname.
     */
    public AddressComparator() {
        this(SortField.LASTNAME, SortField.FIRSTNAME);
    }

    /**
     * Sortiert nach den angegebenen Attributen, in der angegebenen Reihenfolge
     * (das erste Attribut entscheidet zuerst, bei Gleichstand das nächste usw.).
     */
    public AddressComparator(SortField... sortFields) {
        this.sortFields = List.of(sortFields);
    }

    @Override
    public int compare(Address a1, Address a2) {
        for (SortField field : sortFields) {
            int result = compareField(a1, a2, field);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private int compareField(Address a1, Address a2, SortField field) {
        return switch (field) {
            case LASTNAME -> a1.getLastname().compareTo(a2.getLastname());
            case FIRSTNAME -> a1.getFirstname().compareTo(a2.getFirstname());
            case PHONENUMBER -> a1.getPhonenumber().compareTo(a2.getPhonenumber());
            case REGISTRATION_DATE -> a1.getRegistrationDate().compareTo(a2.getRegistrationDate());
        };
    }
}
