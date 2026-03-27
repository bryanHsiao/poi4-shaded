import java.io.*;
import java.net.*;

/**
 * Diagnostic: check if XMLBeans type systems and resources are loadable
 */
public class DiagTest {
    public static void main(String[] args) throws Exception {
        System.out.println("=== XMLBeans Diagnostic ===\n");
        ClassLoader cl = DiagTest.class.getClassLoader();

        // 1. Check resource paths
        String[] resources = {
            "poi4shaded_schemaorg_apache_xmlbeans/system/sXMLLANG/index.xsb",
            "poi4shaded_schemaorg_apache_xmlbeans/system/sXMLLANG/spaceattribute.xsb",
            "poi4shaded_schemaorg_apache_xmlbeans/attribute/http_3A_2F_2Fwww_2Ew3_2Eorg_2FXML_2F1998_2Fnamespace/space.xsb",
            "poi4shaded_schemaorg_apache_xmlbeans/system/sD023D6490046BA0250A839A9AD24C443/index.xsb",
        };
        System.out.println("[1] Resource availability:");
        for (String r : resources) {
            URL url = cl.getResource(r);
            System.out.println("  " + (url != null ? "OK" : "MISSING") + " " + r);
        }

        // 2. Check TypeSystemHolder classes
        System.out.println("\n[2] TypeSystemHolder classes:");
        String[] holders = {
            "poi4shaded_schemaorg_apache_xmlbeans.system.sXMLLANG.TypeSystemHolder",
            "poi4shaded_schemaorg_apache_xmlbeans.system.sXMLSCHEMA.TypeSystemHolder",
            "poi4shaded_schemaorg_apache_xmlbeans.system.sXMLCONFIG.TypeSystemHolder",
            "poi4shaded_schemaorg_apache_xmlbeans.system.sXMLTOOLS.TypeSystemHolder",
            "poi4shaded_schemaorg_apache_xmlbeans.system.sD023D6490046BA0250A839A9AD24C443.TypeSystemHolder",
        };
        for (String h : holders) {
            try {
                Class<?> c = Class.forName(h);
                java.lang.reflect.Field f = c.getDeclaredField("typeSystem");
                f.setAccessible(true);
                Object ts = f.get(null);
                System.out.println("  " + (ts != null ? "OK" : "NULL") + " " + h);
                if (ts == null) {
                    // Try to load it
                    try {
                        java.lang.reflect.Method m = c.getMethod("loadTypeSystem");
                        Object ts2 = m.invoke(null);
                        System.out.println("    loadTypeSystem() returned: " + (ts2 != null ? ts2.getClass().getName() : "null"));
                    } catch (Exception e2) {
                        System.out.println("    loadTypeSystem() error: " + e2.getCause());
                    }
                }
            } catch (Exception e) {
                System.out.println("  FAIL " + h + " -> " + e);
            }
        }

        // 3. Check SchemaTypeSystemImpl.METADATA_PACKAGE_GEN
        System.out.println("\n[3] METADATA_PACKAGE_GEN:");
        try {
            Class<?> stsi = Class.forName("poi4shaded.org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl");
            java.lang.reflect.Field f = stsi.getDeclaredField("METADATA_PACKAGE_GEN");
            f.setAccessible(true);
            String val = (String) f.get(null);
            System.out.println("  Value: \"" + val + "\"");
            // Check if this matches JAR resource paths
            String testPath = val + "/system/sXMLLANG/index.xsb";
            URL url = cl.getResource(testPath);
            System.out.println("  Resource test (" + testPath + "): " + (url != null ? "FOUND" : "NOT FOUND"));
        } catch (Exception e) {
            System.out.println("  Error: " + e);
        }

        // 4. Check context type loader
        System.out.println("\n[4] Context type loader:");
        try {
            Class<?> xb = Class.forName("poi4shaded.org.apache.xmlbeans.XmlBeans");
            java.lang.reflect.Method m = xb.getMethod("typeLoaderForClassLoader", ClassLoader.class);
            Object loader = m.invoke(null, cl);
            System.out.println("  Type loader: " + loader.getClass().getName());

            // Try to find xml:space attribute
            javax.xml.namespace.QName spaceQName = new javax.xml.namespace.QName(
                "http://www.w3.org/XML/1998/namespace", "space");
            java.lang.reflect.Method findAttr = loader.getClass().getMethod("findAttribute", javax.xml.namespace.QName.class);
            Object attr = findAttr.invoke(loader, spaceQName);
            System.out.println("  findAttribute(xml:space): " + (attr != null ? "FOUND" : "NOT FOUND"));
        } catch (Exception e) {
            System.out.println("  Error: " + e);
            if (e.getCause() != null) {
                System.out.println("  Cause: " + e.getCause());
            }
        }

        System.out.println("\n=== Done ===");
    }
}
