package util;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

/**
 * IconUtil Utility Class
 * ---------------------
 * Centralizes setting the application icon for JFrames and taskbar compatibility.
 */
public class IconUtil {

    /**
     * Sets the application icon (rupee symbol) to the specified frame.
     */
    public static void setAppIcon(JFrame frame) {
        if (frame == null) return;
        try {
            URL resource = IconUtil.class.getResource("/resources/rupee.png");
            if (resource != null) {
                // Taskbar and frame icon support
                Image image = Toolkit.getDefaultToolkit().getImage(resource);
                frame.setIconImage(image);
            } else {
                System.out.println("Warning: Application icon not found at /resources/rupee.png");
            }
        } catch (Exception e) {
            System.out.println("Error setting app icon: " + e.getMessage());
        }
    }
}
