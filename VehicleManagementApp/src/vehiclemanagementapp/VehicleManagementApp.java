package vehiclemanagementapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VehicleManagementApp extends JFrame {

    private static final String URL = "jdbc:mysql://localhost:3306/VehicleDB";
    private static final String USER = "root";
    private static final String PASSWORD = "dbms";

    private JTextField makeField;
    private JTextField modelField;
    private JTextField yearField;
    private JTextField licensePlateField;
    private JTextField priceField;
    private JTextField searchField;
    private JComboBox<String> searchCriteriaCombo;
    private JTable vehicleTable;
    private DefaultTableModel tableModel;
    private JButton insertButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton searchButton;
    private JButton clearButton;

    private int selectedVehicleId = -1;

    private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

    private final Color primaryColor = new Color(60, 63, 65);
    private final Color secondaryColor = new Color(75, 110, 175);
    private final Color textColor = new Color(220, 220, 220);
    private final Color inputBgColor = new Color(80, 83, 85);
    private final Color tableHeaderBgColor = new Color(69, 73, 74);
    private final Color tableHeaderFgColor = new Color(210, 210, 210);
    private final Color tableGridColor = secondaryColor.darker();
    private final Color tableSelectionBgColor = secondaryColor;
    private final Color tableSelectionFgColor = Color.WHITE;

    public VehicleManagementApp() {
        super("Vehicle Management System");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(primaryColor);

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(primaryColor);
        topPanel.setBorder(new EmptyBorder(10, 10, 5, 10));
        topPanel.add(formPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        loadVehicles();
        setVisible(true);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(secondaryColor, 1), " Vehicle Details ",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 14), textColor));
        formPanel.setBackground(primaryColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        makeField = createStyledTextField();
        modelField = createStyledTextField();
        yearField = createStyledTextField();
        licensePlateField = createStyledTextField();
        priceField = createStyledTextField();

        addField(formPanel, gbc, "Make:", makeField, 0);
        addField(formPanel, gbc, "Model:", modelField, 1);
        addField(formPanel, gbc, "Year:", yearField, 2);
        addField(formPanel, gbc, "License Plate:", licensePlateField, 3);
        addField(formPanel, gbc, "Price:", priceField, 4);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(primaryColor);

        insertButton = createStyledButton("Add New");
        updateButton = createStyledButton("Update Selected");
        deleteButton = createStyledButton("Delete Selected");
        clearButton = createStyledButton("Clear Form");

        buttonPanel.add(insertButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(buttonPanel, gbc);

        insertButton.addActionListener(e -> insertVehicle());
        updateButton.addActionListener(e -> updateVehicle());
        deleteButton.addActionListener(e -> deleteVehicle());
        clearButton.addActionListener(e -> clearFormAndSelection());

        return formPanel;
    }

     private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setBackground(primaryColor);
        tablePanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        String[] columnNames = {"ID", "Make", "Model", "Year", "License Plate", "Price"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                 if (columnIndex == 5) return BigDecimal.class;
                 if (columnIndex == 3) return Integer.class;
                 if (columnIndex == 0) return Integer.class;
                 return String.class;
             }
        };
        vehicleTable = new JTable(tableModel);

        vehicleTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        vehicleTable.setRowHeight(25);
        vehicleTable.setGridColor(tableGridColor);
        vehicleTable.setShowGrid(true);
        vehicleTable.setBackground(inputBgColor);
        vehicleTable.setForeground(textColor);
        vehicleTable.setSelectionBackground(tableSelectionBgColor);
        vehicleTable.setSelectionForeground(tableSelectionFgColor);
        vehicleTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        vehicleTable.getTableHeader().setBackground(tableHeaderBgColor);
        vehicleTable.getTableHeader().setForeground(tableHeaderFgColor);
        vehicleTable.getTableHeader().setReorderingAllowed(false);
        vehicleTable.setAutoCreateRowSorter(true);

        vehicleTable.getColumnModel().getColumn(5).setCellRenderer(new CurrencyRenderer());
        vehicleTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        vehicleTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        vehicleTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        vehicleTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && vehicleTable.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(vehicleTable);
        scrollPane.getViewport().setBackground(inputBgColor);
        scrollPane.setBorder(BorderFactory.createLineBorder(secondaryColor, 1));

        JPanel bottomControlsPanel = new JPanel(new BorderLayout(10, 5));
        bottomControlsPanel.setBackground(primaryColor);
        bottomControlsPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        refreshButton = createStyledButton("Refresh Full List");
        refreshButton.addActionListener(e -> loadVehicles());
        bottomControlsPanel.add(refreshButton, BorderLayout.WEST);

        JPanel searchPanel = createSearchPanel();
        bottomControlsPanel.add(searchPanel, BorderLayout.CENTER);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(bottomControlsPanel, BorderLayout.SOUTH);

        return tablePanel;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(primaryColor);
        searchPanel.setBorder(new EmptyBorder(0, 5, 0, 0));

        JLabel searchLabel = new JLabel("Search By:");
        searchLabel.setForeground(textColor);
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        String[] criteria = {"License Plate", "Model", "Make", "Price (Max)"};
        searchCriteriaCombo = new JComboBox<>(criteria);
        styleComboBox(searchCriteriaCombo);

        searchField = new JTextField(15);
        styleTextField(searchField);

        searchButton = createStyledButton("Search");

        searchPanel.add(searchLabel);
        searchPanel.add(searchCriteriaCombo);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        searchButton.addActionListener(e -> searchVehicles());
        searchField.addActionListener(e -> searchVehicles());

        return searchPanel;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(15);
        styleTextField(textField);
        return textField;
    }

    private void styleTextField(JTextField textField) {
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textField.setBackground(inputBgColor);
        textField.setForeground(textColor);
        textField.setCaretColor(textColor);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(secondaryColor.darker(), 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setBackground(inputBgColor);
        comboBox.setForeground(textColor);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));

        Color btnBgColor = new Color(50, 85, 145);
        Color btnTextColor = Color.WHITE;
        Color btnBorderColor = btnBgColor.darker();
        Color btnHoverBgColor = btnBgColor.brighter();

        button.setBackground(btnBgColor);
        button.setForeground(btnTextColor);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(btnBorderColor, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(btnHoverBgColor);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                 if (button.isEnabled()) {
                    button.setBackground(btnBgColor);
                 }
            }
        });
        return button;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField, int gridy) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.1;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.9;
        panel.add(textField, gbc);
    }

    private Connection connect() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            String msg = "DB Connection Error: " + e.getMessage();
            if (e.getMessage().contains("Communications link failure")) {
                msg += "\n- Is the MySQL server running?\n- Is the hostname/port in URL correct?";
            } else if (e.getMessage().contains("Access denied")) {
                 msg += "\n- Are the USER and PASSWORD correct?";
            } else if (e.getMessage().contains("Unknown database")) {
                 msg += "\n- Does the database '" + getDatabaseNameFromUrl() + "' exist?";
            }
            System.err.println(msg);
            throw e;
        }
    }

    private void loadVehicles() {
        searchField.setText("");
        searchCriteriaCombo.setSelectedIndex(0);
        searchVehiclesInternal(null, null);
    }

    private void searchVehicles() {
        String searchTerm = searchField.getText().trim();
        String criteria = (String) searchCriteriaCombo.getSelectedItem();

        if (searchTerm.isEmpty() && !"Price (Max)".equals(criteria)) {
             showInfoDialog("Search term is empty. Displaying all vehicles.");
             loadVehicles();
             return;
        }
        searchVehiclesInternal(criteria, searchTerm);
    }

    private void searchVehiclesInternal(String criteria, String searchTerm) {
        String sql = "SELECT id, make, model, year, license_plate, price FROM vehicles";
        List<Object[]> vehicleData = new ArrayList<>();
        boolean searching = criteria != null && searchTerm != null && !searchTerm.isEmpty();
        boolean priceSearch = false;
        BigDecimal priceValue = null;

        if (searching) {
            String columnToSearch = "";
            switch (criteria) {
                case "License Plate": columnToSearch = "license_plate"; break;
                case "Model": columnToSearch = "model"; break;
                case "Make": columnToSearch = "make"; break;
                case "Price (Max)":
                    try {
                        if (searchTerm.isEmpty()) {
                             showErrorDialog("Please enter a maximum price value for the search.");
                             return;
                        }
                        priceValue = new BigDecimal(searchTerm);
                        if (priceValue.compareTo(BigDecimal.ZERO) < 0) {
                            showErrorDialog("Search price cannot be negative.");
                            return;
                        }
                        priceSearch = true;
                    } catch (NumberFormatException e) {
                        showErrorDialog("Invalid number format for Price search.\nPlease enter a valid decimal (e.g., 25000.00).");
                        return;
                    }
                    break;
                default:
                    showErrorDialog("Invalid search criteria selected.");
                    return;
            }

            if (priceSearch) {
                sql += " WHERE price <= ?";
            } else if (!columnToSearch.isEmpty()){
                 sql += " WHERE " + columnToSearch + " LIKE ?";
            } else {
                // This else branch is where 'searching' could potentially be reassigned
                // Although in the current logic flow, this might not be reached
                // if criteria validation prevents it. Keeping the fix is safer.
                searching = false;
            }
        }
        sql += " ORDER BY make, model, year";

        tableModel.setRowCount(0);
        clearFormAndSelection(false);

        // --- Database Interaction ---
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

             if (searching) { // Use the state of 'searching' determined *before* this block
                 if (priceSearch) {
                     pstmt.setBigDecimal(1, priceValue);
                 } else {
                     pstmt.setString(1, "%" + searchTerm + "%");
                 }
             }

             try (ResultSet rs = pstmt.executeQuery()) {
                 while (rs.next()) {
                    BigDecimal price = rs.getBigDecimal("price");
                    Object[] row = {
                        rs.getInt("id"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getString("license_plate"),
                        price
                    };
                    vehicleData.add(row);
                 }
             }
        } catch (SQLException e) {
            handleSqlException(e, null);
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> tableModel.setRowCount(0));
            return;
        } catch (Exception e) {
            showErrorDialog("An unexpected error occurred during search: " + e.getMessage());
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> tableModel.setRowCount(0));
            return;
        }

        // --- Update Table on EDT ---
        // FIX: Create an effectively final copy of 'searching' before the lambda
        final boolean wasSearching = searching;

        SwingUtilities.invokeLater(() -> {
            if (tableModel.getRowCount() > 0) tableModel.setRowCount(0);

            if (vehicleData.isEmpty()) {
                // Use the effectively final variable 'wasSearching' inside the lambda
                String message = wasSearching ? "No matching vehicles found." : "No vehicles in database.";
                 tableModel.addRow(new Object[]{null, message, "", "", "", null});
            } else {
                for (Object[] row : vehicleData) {
                    tableModel.addRow(row);
                }
            }
        });
    }

    private void insertVehicle() {
        String make = makeField.getText().trim();
        String model = modelField.getText().trim();
        String yearStr = yearField.getText().trim();
        String licensePlate = licensePlateField.getText().trim();
        String priceStr = priceField.getText().trim();

        if (!validateInput(make, model, yearStr, licensePlate, priceStr)) {
            return;
        }

        int year = Integer.parseInt(yearStr);
        BigDecimal price = new BigDecimal(priceStr);

        String sql = "INSERT INTO vehicles (make, model, year, license_plate, price) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, make);
            pstmt.setString(2, model);
            pstmt.setInt(3, year);
            pstmt.setString(4, licensePlate);
            pstmt.setBigDecimal(5, price);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                showInfoDialog("Vehicle inserted successfully!");
                clearFormAndSelection();
                loadVehicles();
            } else {
                showErrorDialog("Failed to insert vehicle (no rows affected).");
            }

        } catch (SQLException e) {
            handleSqlException(e, licensePlate);
        }
    }

    private void updateVehicle() {
        if (selectedVehicleId == -1) {
            showErrorDialog("Please select a vehicle from the table to update.");
            return;
        }

        String make = makeField.getText().trim();
        String model = modelField.getText().trim();
        String yearStr = yearField.getText().trim();
        String licensePlate = licensePlateField.getText().trim();
        String priceStr = priceField.getText().trim();

        if (!validateInput(make, model, yearStr, licensePlate, priceStr)) {
            return;
        }

        int year = Integer.parseInt(yearStr);
        BigDecimal price = new BigDecimal(priceStr);

        String sql = "UPDATE vehicles SET make = ?, model = ?, year = ?, license_plate = ?, price = ? WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, make);
            pstmt.setString(2, model);
            pstmt.setInt(3, year);
            pstmt.setString(4, licensePlate);
            pstmt.setBigDecimal(5, price);
            pstmt.setInt(6, selectedVehicleId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                showInfoDialog("Vehicle updated successfully!");
                clearFormAndSelection();
                loadVehicles();
            } else {
                showErrorDialog("Failed to update vehicle (vehicle not found or no changes detected).");
                selectedVehicleId = -1;
                loadVehicles();
            }

        } catch (SQLException e) {
             handleSqlException(e, licensePlate);
        }
    }

    private void deleteVehicle() {
        if (selectedVehicleId == -1) {
            showErrorDialog("Please select a vehicle from the table to delete.");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the selected vehicle?\n" +
                "Make: " + makeField.getText() + "\n" +
                "Model: " + modelField.getText() + "\n" +
                "License Plate: " + licensePlateField.getText() + "\n" +
                "(ID: " + selectedVehicleId + ")",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM vehicles WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selectedVehicleId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                showInfoDialog("Vehicle deleted successfully!");
                clearFormAndSelection();
                loadVehicles();
            } else {
                showErrorDialog("Failed to delete vehicle (vehicle not found).");
                selectedVehicleId = -1;
                loadVehicles();
            }

        } catch (SQLException e) {
            showErrorDialog("Database Delete Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateFormFromSelectedRow() {
        int selectedRow = vehicleTable.getSelectedRow();
        int modelRow = selectedRow >= 0 ? vehicleTable.convertRowIndexToModel(selectedRow) : -1;

        if (modelRow >= 0) {
            Object idValue = tableModel.getValueAt(modelRow, 0);

            if (idValue instanceof Integer) {
                selectedVehicleId = (Integer) idValue;

                makeField.setText(getStringValue(tableModel.getValueAt(modelRow, 1)));
                modelField.setText(getStringValue(tableModel.getValueAt(modelRow, 2)));
                yearField.setText(getStringValue(tableModel.getValueAt(modelRow, 3)));
                licensePlateField.setText(getStringValue(tableModel.getValueAt(modelRow, 4)));

                Object priceValue = tableModel.getValueAt(modelRow, 5);
                if (priceValue instanceof BigDecimal) {
                    priceField.setText(((BigDecimal) priceValue).toPlainString());
                } else {
                    priceField.setText("");
                }
            } else {
                 clearFormAndSelection(false);
            }
        } else {
             clearFormAndSelection(false);
        }
    }

    private String getStringValue(Object value) {
        return (value == null) ? "" : value.toString();
    }

    private void clearFormAndSelection() {
        clearFormAndSelection(true);
    }

    private void clearFormAndSelection(boolean clearTableSelection) {
        makeField.setText("");
        modelField.setText("");
        yearField.setText("");
        licensePlateField.setText("");
        priceField.setText("");
        selectedVehicleId = -1;

        if (clearTableSelection && vehicleTable != null) {
            vehicleTable.clearSelection();
        }
        makeField.requestFocusInWindow();
    }

    private boolean validateInput(String make, String model, String yearStr, String licensePlate, String priceStr) {
        if (make.isEmpty() || model.isEmpty() || yearStr.isEmpty() || licensePlate.isEmpty() || priceStr.isEmpty()) {
            showErrorDialog("All fields (Make, Model, Year, License Plate, Price) are required.");
            return false;
        }

        try {
            int year = Integer.parseInt(yearStr);
            int currentYear = java.time.Year.now().getValue();
            if (year < 1886 || year > currentYear + 1) {
                 showErrorDialog("Please enter a valid Year (e.g., between 1886 and " + (currentYear + 1) + ").");
                 yearField.requestFocusInWindow();
                 return false;
            }
        } catch (NumberFormatException e) {
            showErrorDialog("Year must be a valid whole number.");
            yearField.requestFocusInWindow();
            return false;
        }

        if (licensePlate.length() < 2 || licensePlate.length() > 20) {
             showErrorDialog("License Plate must be between 2 and 20 characters.");
             licensePlateField.requestFocusInWindow();
             return false;
        }

        try {
            BigDecimal price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                 showErrorDialog("Price cannot be negative.");
                 priceField.requestFocusInWindow();
                 return false;
            }
        } catch (NumberFormatException e) {
            showErrorDialog("Price must be a valid decimal number (e.g., 19999.95). Do not include currency symbols or commas.");
            priceField.requestFocusInWindow();
            return false;
        }

        return true;
    }

     private void handleSqlException(SQLException e, String licensePlateForError) {
         String errorMsg = e.getMessage().toLowerCase();
         int errorCode = e.getErrorCode();

         System.err.println("SQL Error Code: " + errorCode);
         System.err.println("SQL State: " + e.getSQLState());
         System.err.println("Error Message: " + e.getMessage());

         if (errorCode == 1062 || errorMsg.contains("duplicate entry")) {
             if (errorMsg.contains("license_plate")) {
                showErrorDialog("Error: License plate '" + licensePlateForError + "' already exists. Please use a unique license plate.");
             } else if (errorMsg.contains("primary")) {
                  showErrorDialog("Database Error: Trying to insert duplicate primary key (ID). This should not normally happen.");
             }
             else {
                showErrorDialog("Database Constraint Error: A unique value (e.g., license plate) already exists.");
             }
         } else if (errorCode == 1146 || errorMsg.contains("table") && errorMsg.contains("doesn't exist")) {
             showErrorDialog("Database Error: Table 'vehicles' not found in database '" + getDatabaseNameFromUrl() + "'.\nPlease ensure the table is created correctly.");
         } else if (errorCode == 1054 || errorMsg.contains("unknown column")) {
              if (errorMsg.contains("'price'")) {
                  showErrorDialog("Database Error: Column 'price' not found in the 'vehicles' table.\nPlease run the 'ALTER TABLE vehicles ADD COLUMN price DECIMAL(10, 2) NULL;' command on your database.");
              } else {
                   showErrorDialog("Database Error: An unknown column was referenced. Check table structure.\n" + e.getMessage());
              }
         } else if (errorMsg.contains("communications link failure") || errorMsg.contains("connect timed out") || errorCode == 0 && e.getSQLState().startsWith("08")) {
             showErrorDialog("Database Connection Error: Cannot connect to the database server.\n- Is the server running?\n- Are the connection details (URL, Port) correct?\n- Is the network connection stable?");
         } else if (errorCode == 1045 || errorMsg.contains("access denied")) {
             showErrorDialog("Database Access Error: Invalid username or password ('" + USER + "').");
         } else if (errorCode == 1049 || errorMsg.contains("unknown database")) {
              showErrorDialog("Database Error: Database '" + getDatabaseNameFromUrl() + "' not found on the server.");
         }
         else if (errorCode == 1406 || errorMsg.contains("data too long for column")) {
              showErrorDialog("Data Error: Input value is too long for a database column (e.g., Make, Model, License Plate).\n" + e.getMessage());
         }
         else {
             showErrorDialog("A database error occurred: " + e.getMessage() + "\n(Code: " + errorCode + ")");
             e.printStackTrace();
         }
     }

     private String getDatabaseNameFromUrl() {
         try {
             int lastSlash = URL.lastIndexOf('/');
             if (lastSlash != -1 && lastSlash < URL.length() - 1) {
                 String dbPart = URL.substring(lastSlash + 1);
                 int questionMark = dbPart.indexOf('?');
                 return (questionMark == -1) ? dbPart : dbPart.substring(0, questionMark);
             }
         } catch (Exception e) {
             /* ignore parsing errors, return default */
         }
         return "[Unknown DB]";
     }

    private void showErrorDialog(String message) {
        Runnable showDialog = () -> JOptionPane.showMessageDialog(
                VehicleManagementApp.this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);

        if (SwingUtilities.isEventDispatchThread()) {
            showDialog.run();
        } else {
            SwingUtilities.invokeLater(showDialog);
        }
    }

    private void showInfoDialog(String message) {
        Runnable showDialog = () -> JOptionPane.showMessageDialog(
                 VehicleManagementApp.this,
                 message,
                 "Information",
                 JOptionPane.INFORMATION_MESSAGE);

        if (SwingUtilities.isEventDispatchThread()) {
             showDialog.run();
        } else {
             SwingUtilities.invokeLater(showDialog);
        }
    }

    static class CurrencyRenderer extends DefaultTableCellRenderer {
        public CurrencyRenderer() {
            super();
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value instanceof BigDecimal) {
                setText(currencyFormatter.format(value));
            } else if (value == null) {
                 setText("");
            } else {
                 setText(value.toString());
            }
            return c;
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
           System.err.println("Could not set Nimbus look and feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            // Assumes LoginDialog class exists in its own LoginDialog.java file
            LoginDialog loginDlg = new LoginDialog(null);
            loginDlg.setVisible(true);

            if (loginDlg.isSucceeded()) {
                System.out.println("Login successful. Starting Vehicle Management App...");
                new VehicleManagementApp();
            } else {
                System.out.println("Login cancelled or failed. Exiting application.");
                System.exit(0);
            }
        });
    }
}