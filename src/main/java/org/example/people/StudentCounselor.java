package org.example.people;

import java.util.HashMap;
import java.util.Map;

public class StudentCounselor {

    public static void main(String[] args) {


        String lastName = "Starosta";
        Counselor counselor = findCounselor(lastName);
        if (counselor != null) {
            System.out.println("Counselor: " + counselor.getName());
            System.out.println("Email: " + counselor.getEmail());
        } else {
            System.out.println("Counselor not found for last name: " + lastName);
        }
    }

    public static Counselor findCounselor(String lastName) {
        // Initialize the map with counselors' information
        Map<String, Counselor> counselors = new HashMap<>(){{
//            put("A-Elgo", new Counselor("Mr. Bobby Howe", "robert.howe@ocdsb.ca"));
//            put("Elha-Lin", new Counselor("Mr. Scheepers", "greg.scheepers@ocdsb.ca"));
//            put("Ling-Shar", new Counselor("Ms. Walter", "michelle.walter@ocdsb.ca"));
//            put("Shaw-Z", new Counselor("Ms. Lisak", "dubravka.lisak@ocdsb.ca"));

            put("A-Elgo", new Counselor("Mr. Bobby Howe", "fenceryounger@gmail.com"));
            put("Elha-Lin", new Counselor("Mr. Scheepers", "fenceryounger@gmail.com"));
            put("Ling-Shar", new Counselor("Ms. Walter", "fenceryounger@gmail.com"));
            put("Shaw-Z", new Counselor("Ms. Lisak", "fenceryounger@gmail.com"));
        }};

        if (lastName.compareToIgnoreCase("A") >= 0 && lastName.compareToIgnoreCase("Elgo") <= 0) {
            return counselors.get("A-Elgo");
        } else if (lastName.compareToIgnoreCase("Elha") >= 0 && lastName.compareToIgnoreCase("Lin") <= 0) {
            return counselors.get("Elha-Lin");
        } else if (lastName.compareToIgnoreCase("Ling") >= 0 && lastName.compareToIgnoreCase("Shar") <= 0) {
            return counselors.get("Ling-Shar");
        } else if (lastName.compareToIgnoreCase("Shaw") >= 0 && lastName.compareToIgnoreCase("Z") <= 0) {
            return counselors.get("Shaw-Z");
        } else {
            return null;
        }
    }
}
