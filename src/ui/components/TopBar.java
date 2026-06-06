package ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * TopBar
 * ------
 * A reusable header panel displaying titles or welcome messages.
 */
@SuppressWarnings({"serial", "this-escape"})
public class TopBar extends JPanel {

    private JLabel titleLabel;

    public TopBar(String titleText) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(titleLabel, BorderLayout.WEST);
    }

    public void setTitleText(String text) {
        titleLabel.setText(text);
    }

    public JLabel getTitleLabel() {
        return titleLabel;
    }
}
