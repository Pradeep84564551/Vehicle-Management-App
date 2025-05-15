package vehiclemanagementapp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginDialog extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private boolean succeeded;

    // --- Simple hardcoded credentials (INSECURE - for demo purposes only!) ---
    private final String CORRECT_USERNAME = "root";
    private final String CORRECT_PASSWORD = "dbms"; // Use char[] for real passwords

    // --- Colors ---
    private Color bgColor = new Color(60, 63, 65);
    private Color fgColor = new Color(220, 220, 220);
    private Color buttonColor = new Color(75, 110, 175);
    private Color buttonTextColor = Color.BLACK;
    private Color inputBgColor = new Color(80, 83, 85);

    public LoginDialog(Frame parent) {
        super(parent, "Login", true); // true makes it modal

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bgColor);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Username Label and Field
        JLabel usernameLabel = new JLabel("Username: ");
        usernameLabel.setForeground(fgColor);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setBackground(inputBgColor);
        usernameField.setForeground(fgColor);
        usernameField.setCaretColor(fgColor);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(usernameField, gbc);

        // Password Label and Field
        JLabel passwordLabel = new JLabel("Password: ");
        passwordLabel.setForeground(fgColor);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setBackground(inputBgColor);
        passwordField.setForeground(fgColor);
        passwordField.setCaretColor(fgColor);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(passwordField, gbc);

        // Buttons Panel
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bp.setBackground(bgColor);

        loginButton = createStyledButton("Login");
        loginButton.addActionListener(e -> onLogin());

        cancelButton = createStyledButton("Cancel");
        cancelButton.addActionListener(e -> onCancel());

        bp.add(loginButton);
        bp.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(bp, gbc);

        // Add panel to dialog
        getContentPane().add(panel);

        pack(); // Adjusts dialog size to fit components
        setResizable(false);
        setLocationRelativeTo(parent); // Center relative to parent (or screen if parent is null)

        // Handle Enter key press in password field
        passwordField.addActionListener(e -> onLogin());

        // Handle window closing event
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // We handle closing ourselves
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
    }

     private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(buttonColor);
        button.setForeground(buttonTextColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(buttonColor.darker(), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        // Basic hover effect (optional)
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(buttonColor.brighter());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(buttonColor);
            }
        });
        return button;
    }

    private void onLogin() {
        String enteredUsername = usernameField.getText().trim();
        String enteredPassword = new String(passwordField.getPassword());

        // --- Simple Authentication Logic ---
        if (enteredUsername.equals(CORRECT_USERNAME) && enteredPassword.equals(CORRECT_PASSWORD)) {
            succeeded = true;
            dispose(); // Close the login dialog
        } else {
            JOptionPane.showMessageDialog(LoginDialog.this,
                    "Invalid username or password.",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            // Reset password field
            passwordField.setText("");
            succeeded = false;
        }
    }

    private void onCancel() {
        succeeded = false;
        dispose(); // Close the login dialog
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}