package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * AboutPanel – a professional, modern "About Us" page for the Smart Finance Manager.
 *
 * <p>
 * Layout (BorderLayout):
 *   NORTH – Header with title, description, tagline, and optional vision.
 *   CENTER – Grid of team member cards (2x2).
 *   SOUTH – Footer with credits / links.
 * </p>
 *
 * The panel can be added to a {@link JFrame} like any other Swing component.
 */
@SuppressWarnings({"serial", "this-escape"})
public class AboutPanel extends JPanel {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AboutPanel.class.getName());

    /** Simple POJO representing a team member. */
    private static class Member {
        String name;
        String[] roles;
        String description;
        String github;
        String linkedIn;
        public Member(String name, String[] roles, String description, String github, String linkedIn) {
            this.name = name;
            this.roles = roles;
            this.description = description;
            this.github = github;
            this.linkedIn = linkedIn;
        }
    }

    private static final List<Member> TEAM = new ArrayList<>();
    static {
        TEAM.add(new Member("Suraj Sawant", new String[]{"Team Lead", "Backend", "Architecture", "UI Developer"},
                "Guides the architecture and server‑side logic.",
                "https://github.com/daystar-1nine",
                "https://www.linkedin.com/in/surajsawant19062005/"));
        TEAM.add(new Member("Shubhra Shinde", new String[]{"UI Developer", "Frontend"},
                "Crafts intuitive UI components.",
                "https://github.com/shubhrashinde",
                "https://www.linkedin.com/in/shubhra-shinde-aab746403/"));
        TEAM.add(new Member("Aditi Patil", new String[]{"Logic", "Feature Development"},
                "Implements core business rules.",
                "https://github.com/aditi22builds",
                "https://www.linkedin.com/in/aditi-patil-888560414/"));
        TEAM.add(new Member("Sharwani Kudu", new String[]{"Testing", "QA", "UI Support"},
                "Ensures quality and polish.",
                "https://github.com/shrawani3007",
                "https://www.linkedin.com/in/shrawani-kudu-212767393/"));
    }

    public AboutPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        add(createHeader(), BorderLayout.NORTH);
        add(createTeamSection(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
        
        util.ThemeUtil.applyTheme(this);
    }

    /**
     * Creates the top section containing the app title, description, tagline and vision.
     */
    private JComponent createHeader() {
        HeaderPanel header = new HeaderPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(15, 15, 15, 15));
        // Title
        JLabel title = new JLabel("Smart Finance Manager");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setForeground(Color.WHITE);
        title.setName("customColorLabel");
        // Description
        JLabel desc = new JLabel("<html><p style='text-align:center;'>A personal finance management application designed to help users track expenses, manage loans, and gain smart financial insights.</p></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 14));
        desc.setHorizontalAlignment(JLabel.CENTER);
        desc.setForeground(Color.WHITE);
        desc.setName("customColorLabel");
        // Tagline
        JLabel tagline = new JLabel("Built with simplicity, designed for smarter financial decisions.");
        tagline.setFont(new Font("SansSerif", Font.ITALIC, 13));
        tagline.setHorizontalAlignment(JLabel.CENTER);
        tagline.setForeground(Color.LIGHT_GRAY);
        tagline.setName("customColorLabel");
        // Project features list
        JLabel features = new JLabel("<html><ul style='margin-left:20px;'><li>Transaction tracking</li><li>Loan management</li><li>Financial reports</li><li>Insights & analytics</li></ul></html>");
        features.setFont(new Font("SansSerif", Font.PLAIN, 13));
        features.setHorizontalAlignment(JLabel.CENTER);
        features.setForeground(Color.WHITE);
        features.setName("customColorLabel");
        // Vision
        JLabel vision = new JLabel("<html><p style='text-align:center;'><b>Our Vision:</b> Build simple yet powerful tools that help users take control of their financial life.</p></html>");
        vision.setFont(new Font("SansSerif", Font.PLAIN, 12));
        vision.setHorizontalAlignment(JLabel.CENTER);
        vision.setForeground(Color.WHITE);
        vision.setName("customColorLabel");
        // Assemble vertically
        JPanel vbox = new JPanel(new GridLayout(0, 1, 8, 8));
        vbox.setOpaque(false);
        java.net.URL logoUrl = AboutPanel.class.getResource("/resources/rupee.png");
        if (logoUrl != null) {
            java.awt.Image img = new javax.swing.ImageIcon(logoUrl).getImage().getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new javax.swing.ImageIcon(img));
            logoLabel.setHorizontalAlignment(JLabel.CENTER);
            vbox.add(logoLabel);
        }
        vbox.add(title);
        vbox.add(desc);
        vbox.add(tagline);
        vbox.add(features);
        vbox.add(vision);
        header.add(vbox, BorderLayout.CENTER);
        return header;
    }

    /**
     * Creates the centre section containing a 2x2 grid of team member cards.
     */
    private JComponent createTeamSection() {
        JPanel teamPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        teamPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Our Team", TitledBorder.CENTER, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 16)));
        for (Member m : TEAM) {
            teamPanel.add(createMemberCard(m));
        }
        return teamPanel;
    }

    /**
     * Creates a single member card.
     */
    private JPanel createMemberCard(Member member) {
        JPanel card = new JPanel();
        card.setName("card");
        card.setLayout(new BorderLayout(5, 5));
        
        // Name & role (top)
        JLabel nameLabel = new JLabel(member.name);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JPanel rolesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        rolesPanel.setOpaque(false);
        rolesPanel.setBorder(BorderFactory.createEmptyBorder(0, -8, 0, 0)); // Offset the FlowLayout default margin
        
        for (String role : member.roles) {
            Color bg = getRoleColor(role);
            rolesPanel.add(createTag(role, bg, Color.WHITE));
        }

        // Description (center)
        JLabel descLabel = new JLabel("<html><i>" + member.description + "</i></html>");
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Buttons (bottom)
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        JButton gitBtn = createLinkButton("GitHub", member.github);
        JButton linkedInBtn = createLinkButton("LinkedIn", member.linkedIn);
        btnPanel.add(gitBtn);
        btnPanel.add(linkedInBtn);
        
        // Assemble vertically
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.add(nameLabel);
        top.add(Box.createVerticalStrut(4));
        top.add(rolesPanel);
        
        card.add(top, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }

    private JLabel createTag(String text, Color bg, Color fg) {
        JLabel label = new RoundedLabel(text, 10, bg, fg);
        label.setName("customColorLabel"); // Prevent theme override
        return label;
    }

    private Color getRoleColor(String role) {
        if (role == null) return new Color(66, 66, 66);
        String r = role.toLowerCase().trim();
        if (r.contains("lead")) {
            return new Color(123, 31, 162); // Purple (#7B1FA2)
        } else if (r.contains("backend") || r.contains("logic") || r.contains("feature")) {
            return new Color(25, 118, 210); // Blue (#1976D2)
        } else if (r.contains("ui") || r.contains("front") || r.contains("support")) {
            return new Color(46, 125, 50); // Green (#2E7D32)
        } else if (r.contains("test") || r.contains("qa")) {
            return new Color(239, 108, 0); // Orange (#EF6C00)
        } else if (r.contains("arch")) {
            return new Color(66, 66, 66); // Dark Gray (#424242)
        }
        return new Color(66, 66, 66);
    }

    // Custom Rounded JLabel subclass for tag component
    private static class RoundedLabel extends JLabel {
        private final int radius;
        private final Color bgColor;

        public RoundedLabel(String text, int radius, Color bgColor, Color fgColor) {
            super(text);
            this.radius = radius;
            this.bgColor = bgColor;
            setForeground(fgColor);
            setOpaque(false);
            setFont(new Font("SansSerif", Font.BOLD, 10));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Helper to create a button that opens a URL in the default browser. */
    private JButton createLinkButton(String label, String url) {
        JButton button = new JButton(label);
        button.setFont(new Font("SansSerif", Font.PLAIN, 11));
        button.setMargin(new Insets(2, 8, 2, 8));
        button.addActionListener(e -> openLink(url));
        return button;
    }

    /** Opens the given URL using {@link Desktop#browse}. */
    private void openLink(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to open link: " + url, ex);
        }
    }

    /** Footer with simple credits. */
    private JComponent createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel credit = new JLabel("© 2026 Smart Finance Manager – All rights reserved.");
        credit.setFont(new Font("SansSerif", Font.PLAIN, 11));
        credit.setHorizontalAlignment(JLabel.CENTER);
        footer.add(credit, BorderLayout.CENTER);
        return footer;
    }


    // ---------- Helper classes for UI ----------
    // Gradient header panel
    @SuppressWarnings("serial")
    private static class HeaderPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            Color top = new Color(70, 130, 180);
            Color bottom = new Color(25, 25, 112);
            GradientPaint gp = new GradientPaint(0, 0, top, 0, getHeight(), bottom);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // Rounded border for member cards
    private static class RoundedBorder implements Border {
        private final int radius;
        private final Color color;
        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius + 1, this.radius + 1, this.radius + 2, this.radius);
        }
        @Override
        public boolean isBorderOpaque() { return false; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }
}
