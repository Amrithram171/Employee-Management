package com.ems;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class EmployeeManagementGUI extends JFrame {

    private final EmployeeDAO dao = new EmployeeDAO();

    private JTextField idField;
    private JTextField nameField;
    private JTextField departmentField;
    private JTextField salaryField;
    private JTextField emailField;
    private JTextField searchField;
    private JTable table;
    private DefaultTableModel tableModel;

    public EmployeeManagementGUI() {
        setTitle("Employee Management System");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DBConnection.closeConnection();
                System.exit(0);
            }
        });

        setLayout(new BorderLayout(10, 10));
        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Employee Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        idField = new JTextField(6);
        idField.setEditable(false);
        nameField = new JTextField(15);
        departmentField = new JTextField(15);
        salaryField = new JTextField(10);
        emailField = new JTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        panel.add(idField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 3;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        panel.add(departmentField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Salary:"), gbc);
        gbc.gridx = 3;
        panel.add(salaryField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(emailField, gbc);

        return panel;
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {"ID", "Name", "Department", "Salary", "Email"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
            }
        });
        return new JScrollPane(table);
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh");

        searchField = new JTextField(15);
        JButton searchButton = new JButton("Search by Department");

        addButton.addActionListener(e -> addEmployee());
        updateButton.addActionListener(e -> updateEmployee());
        deleteButton.addActionListener(e -> deleteEmployee());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> refreshTable());
        searchButton.addActionListener(e -> searchByDepartment());

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);
        panel.add(refreshButton);
        panel.add(new JLabel("   Department:"));
        panel.add(searchField);
        panel.add(searchButton);

        return panel;
    }

    private void populateFormFromSelectedRow() {
        int row = table.getSelectedRow();
        idField.setText(tableModel.getValueAt(row, 0).toString());
        nameField.setText(tableModel.getValueAt(row, 1).toString());
        departmentField.setText(tableModel.getValueAt(row, 2).toString());
        salaryField.setText(tableModel.getValueAt(row, 3).toString());
        emailField.setText(tableModel.getValueAt(row, 4).toString());
    }

    private void addEmployee() {
        if (!validateForm()) {
            return;
        }
        Employee employee = new Employee(
                nameField.getText().trim(),
                departmentField.getText().trim(),
                Double.parseDouble(salaryField.getText().trim()),
                emailField.getText().trim()
        );
        if (dao.addEmployee(employee)) {
            JOptionPane.showMessageDialog(this, "Employee added with ID: " + employee.getId());
            clearForm();
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add employee.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEmployee() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select an employee from the table first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!validateForm()) {
            return;
        }
        Employee employee = new Employee(
                Integer.parseInt(idField.getText().trim()),
                nameField.getText().trim(),
                departmentField.getText().trim(),
                Double.parseDouble(salaryField.getText().trim()),
                emailField.getText().trim()
        );
        if (dao.updateEmployee(employee)) {
            JOptionPane.showMessageDialog(this, "Employee updated.");
            clearForm();
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update employee.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteEmployee() {
        if (idField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select an employee from the table first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this employee?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        int id = Integer.parseInt(idField.getText().trim());
        if (dao.deleteEmployee(id)) {
            JOptionPane.showMessageDialog(this, "Employee deleted.");
            clearForm();
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete employee.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchByDepartment() {
        String department = searchField.getText().trim();
        List<Employee> results = department.isEmpty() ? dao.getAllEmployees() : dao.searchByDepartment(department);
        populateTable(results);
    }

    private void refreshTable() {
        populateTable(dao.getAllEmployees());
    }

    private void populateTable(List<Employee> employees) {
        tableModel.setRowCount(0);
        for (Employee emp : employees) {
            tableModel.addRow(new Object[]{
                    emp.getId(), emp.getName(), emp.getDepartment(), emp.getSalary(), emp.getEmail()
            });
        }
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        departmentField.setText("");
        salaryField.setText("");
        emailField.setText("");
        table.clearSelection();
    }

    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()
                || departmentField.getText().trim().isEmpty()
                || salaryField.getText().trim().isEmpty()
                || emailField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            Double.parseDouble(salaryField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Salary must be a number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EmployeeManagementGUI().setVisible(true));
    }
}
