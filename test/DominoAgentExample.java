import lotus.domino.*;
import java.io.*;
import poi4shaded.org.apache.poi.poifs.filesystem.FileMagic;

/**
 * Domino Java Agent Example - POI 4 Shaded JAR
 *
 * Reads an attachment from a document (by UNID),
 * detects file format via FileMagic, then processes .xls/.xlsx/.doc/.docx accordingly.
 *
 * Usage:
 *   1. Change UNID to your target document's UNID
 *   2. Change FIELD_NAME to the rich text field that holds the attachment
 *   3. Deploy poi4-shaded-4.1.1.jar to Domino (jvm/lib/ext or ndext)
 */
public class DominoAgentExample extends AgentBase {

    // === CONFIG ===
    private static final String UNID = "PASTE_YOUR_UNID_HERE";
    private static final String FIELD_NAME = "att";

    public void NotesMain() {

        try {
            Session session = getSession();
            AgentContext agentContext = session.getAgentContext();
            Database db = agentContext.getCurrentDatabase();

            Document doc = db.getDocumentByUNID(UNID);
            if (doc == null) {
                System.out.println("Document not found: " + UNID);
                return;
            }

            RichTextItem rtItem = (RichTextItem) doc.getFirstItem(FIELD_NAME);
            if (rtItem == null) {
                System.out.println("Field '" + FIELD_NAME + "' not found.");
                return;
            }

            java.util.Vector<?> objs = rtItem.getEmbeddedObjects();
            if (objs == null || objs.isEmpty()) {
                System.out.println("No attachments in '" + FIELD_NAME + "' field.");
                return;
            }

            for (int i = 0; i < objs.size(); i++) {
                EmbeddedObject eo = (EmbeddedObject) objs.get(i);
                if (eo.getType() != EmbeddedObject.EMBED_ATTACHMENT) continue;

                String fileName = eo.getName();
                System.out.println("\n=== File: " + fileName + " ===");

                String tempPath = System.getProperty("java.io.tmpdir") + File.separator + fileName;
                eo.extractFile(tempPath);

                FileInputStream fis = new FileInputStream(tempPath);
                InputStream is = FileMagic.prepareToCheckMagic(fis);
                FileMagic magic = FileMagic.valueOf(is);
                is.close();

                System.out.println("FileMagic = " + magic);

                String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

                switch (magic.name()) {
                    case "OLE2":
                        System.out.println("Type = OLE2 (old format)");
                        handleOLE2(tempPath, ext);
                        break;
                    case "OOXML":
                        System.out.println("Type = OOXML (new format)");
                        handleOOXML(tempPath, ext);
                        break;
                    default:
                        System.out.println("Unsupported format: " + magic);
                        break;
                }

                new File(tempPath).delete();
                eo.recycle();
            }

            doc.recycle();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleOLE2(String path, String ext) throws Exception {
        FileInputStream fis = new FileInputStream(path);
        switch (ext) {
            case "xls":
                poi4shaded.org.apache.poi.hssf.usermodel.HSSFWorkbook wb =
                    new poi4shaded.org.apache.poi.hssf.usermodel.HSSFWorkbook(fis);
                System.out.println("Sheets: " + wb.getNumberOfSheets());
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    System.out.println("  [" + i + "] " + wb.getSheetName(i)
                        + " - rows: " + wb.getSheetAt(i).getPhysicalNumberOfRows());
                }
                wb.close();
                break;
            case "doc":
                poi4shaded.org.apache.poi.hwpf.HWPFDocument doc =
                    new poi4shaded.org.apache.poi.hwpf.HWPFDocument(fis);
                poi4shaded.org.apache.poi.hwpf.extractor.WordExtractor extractor =
                    new poi4shaded.org.apache.poi.hwpf.extractor.WordExtractor(doc);
                String text = extractor.getText();
                System.out.println("Text length: " + text.length());
                System.out.println("Preview: " + text.substring(0, Math.min(200, text.length())));
                extractor.close();
                doc.close();
                break;
            default:
                System.out.println("OLE2 but unhandled extension: " + ext);
                fis.close();
                break;
        }
    }

    private void handleOOXML(String path, String ext) throws Exception {
        FileInputStream fis = new FileInputStream(path);
        switch (ext) {
            case "xlsx":
                poi4shaded.org.apache.poi.xssf.usermodel.XSSFWorkbook wb =
                    new poi4shaded.org.apache.poi.xssf.usermodel.XSSFWorkbook(fis);
                System.out.println("Sheets: " + wb.getNumberOfSheets());
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    System.out.println("  [" + i + "] " + wb.getSheetName(i)
                        + " - rows: " + wb.getSheetAt(i).getPhysicalNumberOfRows());
                }
                wb.close();
                break;
            case "docx":
                poi4shaded.org.apache.poi.xwpf.usermodel.XWPFDocument doc =
                    new poi4shaded.org.apache.poi.xwpf.usermodel.XWPFDocument(fis);
                System.out.println("Paragraphs: " + doc.getParagraphs().size());
                System.out.println("Tables: " + doc.getTables().size());
                for (int i = 0; i < Math.min(5, doc.getParagraphs().size()); i++) {
                    System.out.println("  [" + i + "] " + doc.getParagraphs().get(i).getText());
                }
                doc.close();
                break;
            default:
                System.out.println("OOXML but unhandled extension: " + ext);
                fis.close();
                break;
        }
    }
}
