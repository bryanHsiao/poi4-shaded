import java.io.*;

/**
 * Test: create a .docx with table, then read it back.
 * This triggers xml:space cross-type-system handle resolution.
 */
public class DocxReadTest {
    public static void main(String[] args) throws Exception {
        System.out.println("=== DOCX Read Test ===\n");

        File file = new File("test_table.docx");

        // Step 1: Create a .docx with table
        System.out.println("[1] Creating .docx with table...");
        poi4shaded.org.apache.poi.xwpf.usermodel.XWPFDocument doc =
            new poi4shaded.org.apache.poi.xwpf.usermodel.XWPFDocument();
        doc.createParagraph().createRun().setText("Test document with table");

        poi4shaded.org.apache.poi.xwpf.usermodel.XWPFTable table =
            doc.createTable(3, 3);
        table.getRow(0).getCell(0).setText("A1");
        table.getRow(0).getCell(1).setText("B1");
        table.getRow(0).getCell(2).setText("C1");
        table.getRow(1).getCell(0).setText("A2");
        table.getRow(1).getCell(1).setText("B2");
        table.getRow(1).getCell(2).setText("C2");

        FileOutputStream fos = new FileOutputStream(file);
        doc.write(fos);
        fos.close();
        doc.close();
        System.out.println("[1] Created: " + file.getName());

        // Step 2: Read the .docx back
        System.out.println("\n[2] Reading .docx back...");
        FileInputStream fis = new FileInputStream(file);
        poi4shaded.org.apache.poi.xwpf.usermodel.XWPFDocument doc2 =
            new poi4shaded.org.apache.poi.xwpf.usermodel.XWPFDocument(fis);

        System.out.println("  Paragraphs: " + doc2.getParagraphs().size());
        System.out.println("  Tables: " + doc2.getTables().size());

        if (!doc2.getTables().isEmpty()) {
            poi4shaded.org.apache.poi.xwpf.usermodel.XWPFTable t = doc2.getTables().get(0);
            System.out.println("  Table rows: " + t.getRows().size());
            for (int r = 0; r < t.getRows().size(); r++) {
                StringBuilder sb = new StringBuilder("    Row " + r + ": ");
                for (int c = 0; c < t.getRow(r).getTableCells().size(); c++) {
                    if (c > 0) sb.append(" | ");
                    sb.append(t.getRow(r).getCell(c).getText());
                }
                System.out.println(sb.toString());
            }
        }

        fis.close();
        doc2.close();
        file.delete();

        System.out.println("\n=== PASS ===");
    }
}
