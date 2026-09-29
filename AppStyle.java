import javax.swing.*;
// import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.plaf.basic.BasicButtonUI;

import java.awt.*;

public class AppStyle {

    private static final Color BACKGROUND = new Color(9, 14, 11);
    private static final Color SURFACE = new Color(16, 25, 19);
    private static final Color GREEN = new Color(74, 255, 145);
    private static final Color TEXT = new Color(195, 244, 212);
    private static final Color BORDER = new Color(43, 83, 57);
    private static final Color SELECTION = new Color(28, 83, 48);

    private static final Font NORMAL =
        new Font(Font.MONOSPACED, Font.PLAIN, 14);

    private static final Font BOLD =
        new Font(Font.MONOSPACED, Font.BOLD, 14);

    public static void stylePanel(JPanel panel) {
        panel.setBackground(BACKGROUND);

        for (Component component : panel.getComponents()) {
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                label.setForeground(TEXT);
                label.setFont(
                    new Font(
                        Font.MONOSPACED,
                        label.getFont().getStyle(),
                        label.getFont().getSize()
                    )
                );
            }
        }
    }

    public static void styleField(JTextField field) {
        field.setFont(NORMAL);
        field.setForeground(TEXT);
        field.setBackground(SURFACE);
        field.setCaretColor(GREEN);
        field.setSelectionColor(SELECTION);
        field.setSelectedTextColor(Color.WHITE);
        field.setPreferredSize(new Dimension(250, 40));

        field.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            )
        );
    }

    public static void stylePrimaryButton(JButton button) {
        styleButton(button, true);
    }

    public static void styleSecondaryButton(JButton button) {
        styleButton(button, false);
    }

    private static void styleButton(
        JButton button,
        boolean primary
    ) {
        button.setFont(BOLD);
        button.setForeground(primary ? BACKGROUND : GREEN);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setCursor(
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.setBorder(
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        );

        // Calculate width so longer labels are not cut off.
        int textWidth = button.getFontMetrics(BOLD)
            .stringWidth(button.getText());

        button.setPreferredSize(
            new Dimension(Math.max(120, textWidth + 44), 44)
        );

        // Paint a pill-shaped background.
        button.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics graphics, JComponent component) {
                AbstractButton current = (AbstractButton) component;
                Graphics2D g = (Graphics2D) graphics.create();

                g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color fill;

                if (!current.isEnabled()) {
                    fill = BORDER;
                } else if (current.getModel().isPressed()) {
                    fill = primary
                        ? new Color(35, 175, 92)
                        : new Color(29, 73, 44);
                } else if (current.getModel().isRollover()) {
                    fill = primary
                        ? new Color(125, 255, 177)
                        : new Color(23, 53, 33);
                } else {
                    fill = primary ? GREEN : SURFACE;
                }

                int width = component.getWidth();
                int height = component.getHeight();

                g.setColor(fill);
                g.fillRoundRect(
                    1, 1, width - 3, height - 3,
                    height - 3, height - 3
                );

                if (!primary) {
                    g.setColor(GREEN);
                    g.drawRoundRect(
                        1, 1, width - 3, height - 3,
                        height - 3, height - 3
                    );
                }

                // Keep keyboard focus visible.
                if (current.hasFocus()) {
                    g.setColor(primary ? BACKGROUND : GREEN);
                    g.setStroke(new BasicStroke(2f));
                    g.drawRoundRect(
                        5, 5, width - 11, height - 11,
                        height - 11, height - 11
                    );
                }

                g.dispose();
                super.paint(graphics, component);
            }
        });
    }

    public static void styleTable(JTable table) {
        table.setFont(NORMAL);
        table.setForeground(TEXT);
        table.setBackground(SURFACE);
        table.setRowHeight(36);
        table.setFillsViewportHeight(true);

        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(GREEN);

        // Give each cell padding and a visible focus outline.
        DefaultTableCellRenderer cells =
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                    JTable source,
                    Object value,
                    boolean selected,
                    boolean focused,
                    int row,
                    int column
                ) {
                    super.getTableCellRendererComponent(
                        source, value, selected, focused, row, column
                    );

                    if (source.convertColumnIndexToModel(column) == 0) {
                        setText(String.valueOf(row + 1));
                    }

                    setFont(NORMAL);
                    setBackground(selected ? SELECTION : SURFACE);
                    setForeground(selected ? GREEN : TEXT);

                    setBorder(
                        BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(
                                focused ? GREEN : getBackground()
                            ),
                            BorderFactory.createEmptyBorder(0, 19, 0, 10)
                        )
                    );

                    return this;
                }
            };

        table.setDefaultRenderer(Object.class, cells);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer headerRenderer =
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                    JTable source,
                    Object value,
                    boolean selected,
                    boolean focused,
                    int row,
                    int column
                ) {
                    super.getTableCellRendererComponent(
                        source, value, selected, focused, row, column
                    );

                    setOpaque(true);
                    setFont(BOLD);
                    setForeground(GREEN);
                    setBackground(BACKGROUND);
                    setHorizontalAlignment(SwingConstants.LEFT);

                    setBorder(
                        BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(
                                0, 0, 1, 0, BORDER
                            ),
                            BorderFactory.createEmptyBorder(
                                8, 20, 8, 10
                            )
                        )
                    );

                    return this;
                }
            };

        header.setDefaultRenderer(headerRenderer);

        header.setDefaultRenderer(headerRenderer);

        // Color the empty area around the table too.
        JScrollPane scrollPane = (JScrollPane)
            SwingUtilities.getAncestorOfClass(
                JScrollPane.class, table
            );

        if (scrollPane != null) {
            scrollPane.setBackground(BACKGROUND);
            scrollPane.getViewport().setBackground(SURFACE);
            scrollPane.setBorder(
                BorderFactory.createLineBorder(BORDER)
            );

            JPanel corner = new JPanel();
            corner.setBackground(BACKGROUND);

            scrollPane.setCorner(
                JScrollPane.UPPER_RIGHT_CORNER,
                corner
            );
        }
    }
}