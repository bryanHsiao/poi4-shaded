import java.io.*;

/**
 * Test FileMagic: auto-detect file format and open with correct class.
 *
 * Usage:
 *   java FileMagicTest <file>
 *   java FileMagicTest              (runs built-in demo)
 */
public class FileMagicTest {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            // test user-specified file
            detectAndRead(new File(args[0]));
        } else {
            // demo: create sample files then detect
            System.out.println("=== FileMagic Detection Test ===\n");
            runDemo();
        }
    }

    static void runDemo() throws Exception {
        // 1) Create .xls with POI 3
        File xlsFile = new File("demo_test.xls");
        org.apache.poi.hssf.usermodel.HSSFWorkbook wb3 =
            new org.apache.poi.hssf.usermodel.HSSFWorkbook();
        wb3.createSheet("POI3");
        wb3.getSheetAt(0).createRow(0).createCell(0).setCellValue("Hello from POI 3");
        FileOutputStream fos1 = new FileOutputStream(xlsFile);
        wb3.write(fos1);
        fos1.close();
        wb3.close();
        System.out.println("[Created] demo_test.xls (POI 3)\n");

        // 2) Create .xlsx with Shaded POI 4
        File xlsxFile = new File("demo_test.xlsx");
        poi4shaded.org.apache.poi.xssf.usermodel.XSSFWorkbook wb4 =
            new poi4shaded.org.apache.poi.xssf.usermodel.XSSFWorkbook();
        wb4.createSheet("POI4");
        wb4.getSheetAt(0).createRow(0).createCell(0).setCellValue("Hello from Shaded POI 4");
        FileOutputStream fos2 = new FileOutputStream(xlsxFile);
        wb4.write(fos2);
        fos2.close();
        wb4.close();
        System.out.println("[Created] demo_test.xlsx (Shaded POI 4)\n");

        // 3) Create .doc with POI 3
        File docFile = new File("demo_test.doc");
        org.apache.poi.hwpf.HWPFDocument doc3 = null;
        boolean docCreated = false;
        try {
            // HWPFDocument cannot create from scratch easily,
            // so we create a minimal OLE2 file using POI POIFS
            org.apache.poi.poifs.filesystem.POIFSFileSystem poifs =
                new org.apache.poi.poifs.filesystem.POIFSFileSystem();
            poifs.createDocument(
                new ByteArrayInputStream(new byte[]{0}), "WordDocument");
            FileOutputStream fos3 = new FileOutputStream(docFile);
            poifs.writeFilesystem(fos3);
            fos3.close();
            poifs.close();
            docCreated = true;
            System.out.println("[Created] demo_test.doc (OLE2 format)\n");
        } catch (Exception e) {
            System.out.println("[Skip] demo_test.doc creation: " + e.getMessage() + "\n");
        }

        // 4) Detect all files
        System.out.println("--- Detection Results ---\n");
        detectAndRead(xlsFile);
        System.out.println();
        detectAndRead(xlsxFile);
        if (docCreated) {
            System.out.println();
            detectAndRead(docFile);
        }

        // cleanup
        xlsFile.delete();
        xlsxFile.delete();
        if (docCreated) docFile.delete();

        System.out.println("\n=== Test Complete ===");
    }

    static void detectAndRead(File file) throws Exception {
        System.out.println("File: " + file.getName());

        FileInputStream fis = new FileInputStream(file);
        InputStream is = poi4shaded.org.apache.poi.poifs.filesystem.FileMagic
            .prepareToCheckMagic(fis);
        poi4shaded.org.apache.poi.poifs.filesystem.FileMagic magic =
            poi4shaded.org.apache.poi.poifs.filesystem.FileMagic.valueOf(is);

        System.out.println("  FileMagic = " + magic);

        switch (magic.name()) {
            case "OLE2":
                System.out.println("  Type      = OLE2 (old format: .xls / .doc / .ppt)");
                System.out.println("  Action    = Use HSSFWorkbook or HWPFDocument");
                break;
            case "OOXML":
                System.out.println("  Type      = OOXML (new format: .xlsx / .docx / .pptx)");
                System.out.println("  Action    = Use XSSFWorkbook or XWPFDocument");
                break;
            default:
                System.out.println("  Type      = " + magic + " (unknown or unsupported)");
                break;
        }

        is.close();
    }
}
