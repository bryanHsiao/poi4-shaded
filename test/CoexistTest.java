import java.io.FileOutputStream;

/**
 * POI 3 + Shaded POI 4 Coexistence Test
 *
 * 1. Use POI 3 (org.apache.poi) to create a .xls file
 * 2. Use Shaded POI 4 (poi4shaded.org.apache.poi) to create a .xlsx file
 * 3. Verify both can be loaded without class conflicts
 */
public class CoexistTest {

    public static void main(String[] args) {
        System.out.println("=== POI 3 + Shaded POI 4 Coexistence Test ===\n");

        boolean poi3ok = testPoi3();
        boolean poi4ok = testPoi4Shaded();

        System.out.println("\n=== Results ===");
        System.out.println("POI 3 (org.apache.poi)          : " + (poi3ok ? "PASS" : "FAIL"));
        System.out.println("Shaded POI 4 (poi4shaded.*)     : " + (poi4ok ? "PASS" : "FAIL"));
        System.out.println("Coexistence                     : " + (poi3ok && poi4ok ? "PASS" : "FAIL"));
    }

    private static boolean testPoi3() {
        System.out.println("[POI 3] Start...");
        try {
            org.apache.poi.hssf.usermodel.HSSFWorkbook wb = new org.apache.poi.hssf.usermodel.HSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("POI3-Test");
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Hello from POI 3");

            FileOutputStream out = new FileOutputStream("poi3_test.xls");
            wb.write(out);
            out.close();
            wb.close();

            System.out.println("[POI 3] Version: " + org.apache.poi.Version.getVersion());
            System.out.println("[POI 3] Created poi3_test.xls");
            return true;
        } catch (Exception e) {
            System.out.println("[POI 3] FAILED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean testPoi4Shaded() {
        System.out.println("\n[Shaded POI 4] Start...");
        try {
            poi4shaded.org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new poi4shaded.org.apache.poi.xssf.usermodel.XSSFWorkbook();
            poi4shaded.org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("POI4-Shaded-Test");
            poi4shaded.org.apache.poi.ss.usermodel.Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Hello from Shaded POI 4");

            FileOutputStream out = new FileOutputStream("poi4_shaded_test.xlsx");
            wb.write(out);
            out.close();
            wb.close();

            System.out.println("[Shaded POI 4] Version: " + poi4shaded.org.apache.poi.Version.getVersion());
            System.out.println("[Shaded POI 4] Created poi4_shaded_test.xlsx");
            return true;
        } catch (Exception e) {
            System.out.println("[Shaded POI 4] FAILED: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
